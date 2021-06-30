package com.aigo.analysis.work;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.aigo.analysis.TrackerHelper;
import com.aigo.analysis.dispatcher.DefaultPacketSender;
import com.aigo.analysis.dispatcher.Packet;
import com.aigo.analysis.BaseParams;
import com.aigo.analysis.event.GetInitDataEvent;

import org.json.JSONObject;

/**
 * @Description:  页面打点上报，页面名称和停留时间
 * @author: Eknow
 * @date: 2021/5/18 19:33
 */
public class PageWork extends Worker {

    public PageWork(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        JSONObject postData = new JSONObject(getInputData().getKeyValueMap());
        Packet packet = new Packet(getInputData().getString(BaseParams.TARGET_API_URL.toString())
                + "v1.0/nodes", postData, 1);
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
