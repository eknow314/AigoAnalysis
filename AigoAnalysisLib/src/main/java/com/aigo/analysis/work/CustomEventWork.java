package com.aigo.analysis.work;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.aigo.analysis.BaseParams;
import com.aigo.analysis.TrackerHelper;
import com.aigo.analysis.dispatcher.DefaultPacketSender;
import com.aigo.analysis.dispatcher.Packet;
import com.aigo.analysis.event.GetInitDataEvent;
import com.aigo.analysis.tools.MapUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 自定义事件上报
 * @author: Eknow
 * @date: 2021/7/8 19:13
 */
public class CustomEventWork extends Worker {

    public CustomEventWork(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        JSONObject postData = new JSONObject(getInputData().getKeyValueMap());
        try {
            String extensionStr = postData.getString("extension");
            Map<String, Object> map = MapUtil.getStringToMap(extensionStr);
            JSONArray jsonArray = new JSONArray();
            for (String key : map.keySet()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("event_name", key);
                jsonObject.put("event_value", map.get(key));
                jsonArray.put(jsonObject);
            }
            postData.remove("extension");
            postData.put("extension", jsonArray);
        } catch (JSONException e) {
            postData.remove("extension");
            e.printStackTrace();
        }
        Packet packet = new Packet(getInputData().getString(BaseParams.TARGET_API_URL.toString())
                + "v1.0/nodes/event", postData, 1);
        DefaultPacketSender.post(packet, new DefaultPacketSender.OnRequestCallBack() {
            @Override
            public void onSuccess(String json) {

            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                if (errorCode == 400 && !TextUtils.isEmpty(errorMsg)) {
                    try {
                        JSONObject data = new JSONObject(errorMsg);
                        String err = data.getString("code");
                        String msg = data.getString("message");
                        if (err.equals("CUSTOM_FAIL") && msg.equals("dataVersion changed")) {
                            TrackerHelper.getInstance().with(new GetInitDataEvent());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        return Result.success();
    }
}
