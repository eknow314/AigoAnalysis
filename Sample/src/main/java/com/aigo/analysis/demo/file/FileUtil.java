package com.aigo.analysis.demo.file;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import com.aigo.analysis.demo.ThisApp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class FileUtil {
    public static String MainDir = ""; //存储主目录
    public static final String VideoDir = "Video"; //录像保存文件
    public static final String VideoThumbsDir = "VideoThumb"; //录像缩略图文件
    public static final String SnapshotDir = "Snapshot"; //抓拍文件
    public static final String DeviceCaptureDir = "DeviceThumb"; //设备缩略图文件
    public static final String DevicePspCaptureDir = "DevicePspCapture"; //设备预置位抓图路径
    public static final String CacheDir = "Cache";
    public static final String PersonPortrait = "Portrait"; //个人头像文件
    public static final String customSound = "sound"; //声音保存文件

    public static final String TEMP_LOCATION_HOT = "hot/";

    static {

        MainDir = ThisApp.getInstance().getPackageName();
    }

    /**
     * 获取指定用户名下的录像目录
     *
     * @param account
     * @return
     */
    public static File getRecordDir(String account) {
        String videoDir = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
                || !Environment.isExternalStorageRemovable()) {
            videoDir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + MainDir
                    + File.separator + account + File.separator + VideoDir;
        } else {
            videoDir = ThisApp.getInstance().getFilesDir().getAbsolutePath() + File.separator + MainDir
                    + File.separator + account + File.separator + VideoDir;
        }

        File file = null;
        file = new File(videoDir);
        if (!file.exists() || !file.isDirectory()) {
            file.mkdirs();
        }
        return file;
    }

    /**
     * 获得录像缩略图的目录
     *
     * @param account
     * @return
     */
    public static File getRecordThumbsDir(String account) {
        String videoThumbsDir = null;
        if (Environment.getExternalStorageState().equals((Environment.MEDIA_MOUNTED))
                || !Environment.isExternalStorageRemovable()) {
            videoThumbsDir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + MainDir
                    + File.separator + account + File.separator + VideoThumbsDir;
        } else {
            videoThumbsDir = ThisApp.getInstance().getFilesDir().getAbsolutePath() + File.separator + MainDir
                    + File.separator + account + File.separator + VideoThumbsDir;
        }

        File file = null;
        file = new File(videoThumbsDir);
        if (!file.exists() || !file.isDirectory()) {
            file.mkdirs();
        }
        return file;
    }

    /**
     * 获取指定用户名下的抓拍目录
     *
     * @param account
     * @return
     */
    public static File getSnapshotLocalDir(String account) {
        String snapshotDir = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            snapshotDir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + MainDir
                    + File.separator + account + File.separator + TEMP_LOCATION_HOT + SnapshotDir;
        } else {
            snapshotDir = ThisApp.getInstance().getFilesDir().getAbsolutePath() + File.separator + MainDir
                    + File.separator + account + File.separator + TEMP_LOCATION_HOT + SnapshotDir;
        }
        File file = new File(snapshotDir);
        if (!file.exists() || !file.isDirectory()) {
            file.mkdirs();
        }
        return file;
    }


    /**
     * 获取指定用户名下的抓拍目录
     *
     * @param account
     * @return
     */
    public static File getSnapshotDir(String account) {
        String snapshotDir = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            snapshotDir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + MainDir
                    + File.separator + account + File.separator + SnapshotDir;
        } else {
            snapshotDir = ThisApp.getInstance().getFilesDir().getAbsolutePath()
                    + File.separator + account + File.separator + SnapshotDir;
        }
        File file = new File(snapshotDir);
        if (!file.exists() || !file.isDirectory()) {
            file.mkdirs();
        }
        return file;
    }

    /**
     * 获得头像的存储路径
     *
     * @param account
     * @return
     */
    public static File getPersonPortrait(String account) {
        String portrait = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            portrait = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + MainDir
                    + File.separator + account + File.separator + PersonPortrait;
        } else {
            portrait = ThisApp.getInstance().getFilesDir().getAbsolutePath() + File.separator + MainDir
                    + File.separator + account + File.separator + PersonPortrait;
        }
        File file = new File(portrait);
        if (!file.exists() || !file.isDirectory()) {
            file.mkdirs();
        }
        return file;

    }

    /**
     * 获得以用户名命名的头像的存储路径
     *
     * @param userName
     * @return
     */
    public static String getAccountNamePortrait(String userName) {
        String name = TextUtils.isEmpty(userName) ? "" : userName;
        File portraitDir = getPersonPortrait(name);
        String namePP = name + ".jpg";
        StringBuffer buffer = new StringBuffer();
        buffer.append(portraitDir.getAbsolutePath()).append('/').append(namePP);
        return buffer.toString();
    }

    /**
     * 保存图片到指定路径(bitmap)
     *
     * @param bitmap
     * @param path
     */
    public static void savePic(Bitmap bitmap, String path) {
        try {
            FileOutputStream out = null;
            out = new FileOutputStream(path);
            bitmap.compress(CompressFormat.JPEG, 100, out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 删除文件
     *
     * @param file
     * @return
     */
    public static boolean deleteFile(File file) {
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    public static String getSavedScreenShotPath(File file, String deviceId, int channelNum) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String channel = String.format("ch%1$02d", channelNum);
        String time = format.format(new Date());
        String fileName = deviceId + '_' + channel + '_' + time + ".png";
        final String path = file.getAbsolutePath() + File.separatorChar + fileName;
        try {
            new File(path).createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return path;
    }

    public static String getSavedScreenPspShotPath(File file, String deviceId, int channelNum, int presetId) {
        String channel = String.format("ch%1$02d", channelNum);
        String fileName = deviceId + '_' + channel + '_' + presetId + ".png";
        final String path = file.getAbsolutePath() + File.separatorChar + fileName;
        try {
            new File(path).createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return path;
    }

    /**
     * 获得录像文件存储路径
     *
     * @param account
     * @param deviceId
     * @param channelNum
     * @return
     */
    public static String getRecordFilePath(String account, String deviceId, int channelNum, String suffix) {
        File file = FileUtil.getRecordDir(account);
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String channel = String.format("ch%1$02d", channelNum);
        String time = format.format(new Date());

        String fileName = deviceId + '_' + channel + '_' + time + suffix;
        return (file.getAbsolutePath() + File.separatorChar + fileName);
    }

    /**
     * 获得对应录像文件缩略图的存储路径
     *
     * @param account
     * @param recordFilePath
     * @return
     */
    public static String getRecordFileThumbPath(String account, String recordFilePath, String suffix) {
        File recordFile = FileUtil.getRecordDir(account);
        File recordThumbFile = FileUtil.getRecordThumbsDir(account);

        String recordThumbPath = recordFilePath.replace(recordFile.getAbsolutePath(), recordThumbFile.getAbsolutePath())
                .replace(suffix, ".png");

        return recordThumbPath;
    }


    public static void toPictureInfoPage(Context context, String thumbPath) {

    }
}
