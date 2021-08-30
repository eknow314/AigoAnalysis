package com.aigo.analysis.dispatcher;

import androidx.annotation.NonNull;

import com.aigo.analysis.AigoAnalysis;
import com.aigo.analysis.Tracker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import timber.log.Timber;

/**
 * @Description: 硬盘离线缓存统计事件数据
 * @author: Eknow
 * @date: 2021/8/18 18:06
 */
public class EventDiskCache {
    private static final String TAG = AigoAnalysis.tag(EventDiskCache.class);
    private static final String SPLIT = ">>>";
    private static final String CACHE_DIR_NAME = "aigo_analysis_cache";
    private static final String VERSION = "1";
    private final LinkedBlockingQueue<File> mEventContainer = new LinkedBlockingQueue<>();
    private final File mCacheDir;
    private final long mMaxAge;
    private final long mMaxSize;
    private long mCurrentSize = 0;
    private boolean mDelayedClear = false;

    public EventDiskCache(Tracker tracker) {
        mMaxAge = tracker.getOfflineCacheAge();
        mMaxSize = tracker.getOfflineCacheSize();
        File baseDir = new File(tracker.getAigoAnalysis().getContext().getCacheDir(), CACHE_DIR_NAME);
        try {
            mCacheDir = new File(baseDir, new URL(tracker.getApiUrl()).getHost());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        //取出硬盘缓存的文件
        File[] storedContainers = mCacheDir.listFiles();
        if (storedContainers != null) {
            Arrays.sort(storedContainers);
            for (File container : storedContainers) {
                mCurrentSize += container.length();
                mEventContainer.add(container);
            }
        } else {
            if (!mCacheDir.mkdirs()) {
                Timber.tag(TAG).e("Failed to make disk cache dir %s", mCacheDir);
            }
        }
    }

    /**
     * 校验缓存限制，并且删除过期文件，调用时候加同步锁
     */
    private void checkCacheLimits() {
        long startTime = System.currentTimeMillis();
        if (mMaxAge < 0) {
            //缓存不可用，将之前缓存的文件全部删除
            Timber.tag(TAG).d("Caching is disabled.");
            while (!mEventContainer.isEmpty()) {
                File head = mEventContainer.poll();
                if (head != null && head.delete()) {
                    Timber.tag(TAG).e("Deleted cache container %s", head.getPath());
                }
            }
        } else if (mMaxAge > 0) {
            final Iterator<File> iterator = mEventContainer.iterator();
            while (iterator.hasNext()) {
                File head = iterator.next();
                long timestamp;
                //获取文件的时间
                try {
                    final String[] split = head.getName().split("_");
                    timestamp = Long.parseLong(split[1]);
                } catch (Exception e) {
                    Timber.tag(TAG).e(e);
                    timestamp = 0;
                }
                //将过期的文件删除
                if (timestamp < (System.currentTimeMillis() - mMaxAge)) {
                    if (head.delete()) {
                        Timber.tag(TAG).e("Deleted cache container %s", head.getPath());
                    } else {
                        Timber.tag(TAG).e("Failed to delete cache container %s", head.getPath());
                    }
                    iterator.remove();
                } else {
                    break;
                }
            }
        }
        if (mMaxSize != 0) {
            //再删除超出缓存限制的文件
            final Iterator<File> iterator = mEventContainer.iterator();
            while (iterator.hasNext() && mCurrentSize > mMaxSize) {
                File head = iterator.next();
                mCurrentSize -= head.length();
                iterator.remove();
                if (head.delete()) {
                    Timber.tag(TAG).e("Deleted cache container %s", head.getPath());
                } else {
                    Timber.tag(TAG).e("Failed to delete cache container %s", head.getPath());
                }
            }
        }
        long stopTime = System.currentTimeMillis();
        Timber.tag(TAG).d("Cache check took %dms", (stopTime - startTime));
    }

    private boolean isCachingEnabled() {
        return mMaxAge >= 0;
    }

    /**
     * 写入缓存
     *
     * @param toCache 需要缓存的事件数据
     */
    public synchronized void cache(@NonNull List<Event> toCache) {
        if (!isCachingEnabled() || toCache.isEmpty()) {
            return;
        }

        checkCacheLimits();

        long startTime = System.currentTimeMillis();
        File container = writeEventFile(toCache);
        if (container != null) {
            mEventContainer.add(container);
            mCurrentSize += container.length();
        }
        long stopTime = System.currentTimeMillis();
        Timber.tag(TAG).d("Caching of %d events took %dms (%s)", toCache.size(), (stopTime - startTime), container);
    }

    /**
     * 取出缓存，然后删除本地缓存数据
     *
     * @return
     */
    public synchronized List<Event> uncache() {
        List<Event> events = new ArrayList<>();
        if (!isCachingEnabled()) {
            return events;
        }

        long startTime = System.currentTimeMillis();
        while (!mEventContainer.isEmpty()) {
            File head = mEventContainer.poll();
            if (head != null) {
                events.addAll(readEventFile(head));
                //读取后直接删除文件
                if (!head.delete()) {
                    Timber.tag(TAG).e("Failed to delete cache container %s", head.getPath());
                }
            }
        }

        checkCacheLimits();

        long stopTime = System.currentTimeMillis();
        Timber.tag(TAG).d("Uncaching of %d events took %dms", events.size(), (stopTime - startTime));
        return events;
    }

    public synchronized boolean isEmpty() {
        if (!mDelayedClear) {
            checkCacheLimits();
            mDelayedClear = true;
        }
        return mEventContainer.isEmpty();
    }

    /**
     * 从缓存文件读取事件
     *
     * @param file
     * @return
     */
    private List<Event> readEventFile(@NonNull File file) {
        List<Event> events = new ArrayList<>();
        if (!file.exists()) {
            return events;
        }

        InputStream in = null;
        try {
            in = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(in);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String versionLine = bufferedReader.readLine();
            if (!VERSION.equals(versionLine)) {
                return events;
            }

            final long cutoff = System.currentTimeMillis() - mMaxAge;
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                final int split = line.indexOf(SPLIT);
                if (split == -1) {
                    continue;
                }

                try {
                    long timestamp = Long.parseLong(line.substring(0, split));
                    if (mMaxAge > 0 && timestamp < cutoff) {
                        continue;
                    }

                    String query = line.substring(split + SPLIT.length());
                    events.add(new Event(timestamp, query));
                } catch (Exception e) {
                    Timber.tag(TAG).e(e);
                }
            }
        } catch (IOException e) {
            Timber.tag(TAG).e(e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Timber.tag(TAG).e(e);
                }
            }
        }

        Timber.tag(TAG).d("Restored %d events from %s", events.size(), file.getPath());
        return events;
    }

    /**
     * 将事件写入缓存文件
     *
     * @param events
     * @return
     */
    private File writeEventFile(@NonNull List<Event> events) {
        if (events.isEmpty()) {
            return null;
        }

        File newFile = new File(mCacheDir, "events_" + events.get(events.size() - 1).getTimeStamp());
        FileWriter out = null;
        boolean dataWritten = false;
        try {
            out = new FileWriter(newFile);
            out.append(VERSION).append("\n");

            final long cutoff = System.currentTimeMillis() - mMaxAge;
            for (Event event : events) {
                if (mMaxAge > 0 && event.getTimeStamp() < cutoff) {
                    continue;
                }
                out.append(String.valueOf(event.getTimeStamp()))
                        .append(SPLIT)
                        .append(event.getEncodedQuery())
                        .append("\n");
                dataWritten = true;
            }
        } catch (IOException e) {
            Timber.tag(TAG).e(e);
            newFile.deleteOnExit();
            return null;
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    Timber.tag(TAG).e(e);
                }
            }
        }

        Timber.tag(TAG).d("Saved %d events to %s", events.size(), newFile.getPath());

        //如果没有写入事件数据，删除文件，返回 null
        if (dataWritten) {
            return newFile;
        } else {
            newFile.delete();
            return null;
        }
    }
}
