package com.aigo.analysis.dispatcher;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.aigo.analysis.AigoAnalysis;
import com.aigo.analysis.QueryParams;
import com.aigo.analysis.TrackMe;
import com.aigo.analysis.Tracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import timber.log.Timber;

/**
 * @Description: 构造节点上报数据工厂，单事件/多事件批量上报
 * @author: Eknow
 * @date: 2021/8/18 17:19
 */
public class PacketFactory {

    private static final String TAG = AigoAnalysis.tag(PacketFactory.class);
    @VisibleForTesting
    public static final int PAGE_SIZE = 20;
    private final String mApiUrl;
    private TrackMe mTrackMe;

    public PacketFactory(Tracker tracker) {
        this.mApiUrl = tracker.getApiUrl();
        this.mTrackMe = tracker.getDefaultTrackMe();
    }

    /**
     * 构造发送数据包
     *
     * @param events
     * @return
     */
    public List<Packet> buildPackets(final List<Event> events) {
        if (events.isEmpty()) {
            return Collections.emptyList();
        }

        //单事件上报
        if (events.size() == 1) {
            Packet p = buildPacketForSingle(events.get(0));
            if (p == null) {
                return Collections.emptyList();
            } else {
                return Collections.singletonList(p);
            }
        }

        //根据事件类型排序（用请求地址判断）事件
        HashMap<String, List<Event>> sortMap = sortEvents(events);

        List<Packet> freshPackets = new ArrayList<>();
        for (List<Event> eventList : sortMap.values()) {
            //多事件，分页构造新的批量事件数据上报
            for (int i = 0; i < eventList.size(); i += PAGE_SIZE) {
                List<Event> batch = eventList.subList(i, Math.min(i + PAGE_SIZE, eventList.size()));
                Packet packet;
                if (batch.size() == 1) {
                    packet = buildPacketForSingle(batch.get(0));
                } else {
                    packet = buildPacketForBatch(batch);
                }
                if (packet != null) {
                    freshPackets.add(packet);
                }
            }
        }
        return freshPackets;
    }

    /**
     * 不同事件类型，根据目标地址分包
     *
     * @param events
     * @return
     */
    private HashMap<String, List<Event>> sortEvents(final List<Event> events) {
        HashMap<String, List<Event>> resultMap = new HashMap<>();
        HashSet<String> keySet = new HashSet<>();
        for (Event event : events) {
            String key = null;
            try {
                key = event.getJsonQuery().getString(QueryParams.TARGET_API_URL.toString());
            } catch (JSONException e) {
                e.printStackTrace();
                break;
            }
            if (!keySet.contains(key)) {
                keySet.add(key);
                List<Event> eventList = new ArrayList<>();
                eventList.add(event);
                resultMap.put(key, eventList);
            } else {
                resultMap.get(key).add(event);
            }
        }
        return resultMap;
    }

    /**
     * 构造批量上报的数据包
     * 规则：原 API 地址后面加 /batch
     *
     * @param events
     * @return
     */
    private Packet buildPacketForBatch(List<Event> events) {
        if (events == null || events.isEmpty()) {
            return null;
        }
        try {
            JSONObject params = buildCommonParams();
            //赋值批量参数
            JSONArray jsonArray = new JSONArray();
            for (Event event : events) {
                jsonArray.put(event.getJsonQuery());
            }
            params.put(QueryParams.BATCH_DETAIL.toString(), jsonArray);
            return new Packet(mApiUrl + events.get(0).getFuncApi() + "/batch", params, events.size());
        } catch (JSONException e) {
            Timber.tag(TAG).w(e, "Cannot create json object:\n%s", TextUtils.join(", ", events));
        }
        return null;
    }

    /**
     * 构造单条上报数据包
     *
     * @param event
     * @return
     */
    private Packet buildPacketForSingle(@NonNull Event event) {
        if (event.getJsonQuery() == null) {
            return null;
        }
        try {
            JSONObject params = buildCommonParams();
            for (Iterator<String> it = event.getJsonQuery().keys(); it.hasNext(); ) {
                String key = it.next();
                params.put(key, event.getJsonQuery().get(key));
            }
            return new Packet(mApiUrl + event.getFuncApi(), params);
        } catch (JSONException e) {
            Timber.tag(TAG).w(e, "Cannot create json object:\n%s", event);
        }
        return null;
    }

    /**
     * 构造共同的必传参数
     *
     * @return
     * @throws JSONException
     */
    private JSONObject buildCommonParams() throws JSONException {
        JSONObject params = new JSONObject();
        params.put(QueryParams.CLIENT_AUTO_ID.toString(), mTrackMe.get(QueryParams.CLIENT_AUTO_ID));
        params.put(QueryParams.APP_VERSION.toString(), mTrackMe.get(QueryParams.APP_VERSION));
        params.put(QueryParams.COUNTRY.toString(), mTrackMe.get(QueryParams.COUNTRY));
        params.put(QueryParams.PLATFORM.toString(), mTrackMe.get(QueryParams.PLATFORM));
        params.put(QueryParams.DATA_VERSION.toString(), mTrackMe.get(QueryParams.DATA_VERSION));
        return params;
    }
}
