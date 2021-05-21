package com.aigo.analysis.work;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.aigo.analysis.dispatcher.DefaultPacketSender;
import com.aigo.analysis.dispatcher.Packet;
import com.aigo.analysis.BaseParams;

import org.json.JSONObject;

/**
 * @Description: 用户登录事件上报
 * 成功之后，需要将用户的 userId 存储本地，随后的上报就会存在 userId
 * @author: Eknow
 * @date: 2021/5/18 16:11
 */
public class UserLoginWork extends Worker {

    public UserLoginWork(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        JSONObject postData = new JSONObject(getInputData().getKeyValueMap());
        Packet packet = new Packet(getInputData().getString(BaseParams.TARGET_API_URL.toString()) + "v1.0/users/login", postData, 1);
        DefaultPacketSender.post(packet, new DefaultPacketSender.OnRequestCallBack() {
            @Override
            public void onSuccess(String json) {
                // TODO: 2021/5/20 存储 userId 到本地SP 和 TrackMe
            }

            @Override
            public void onError(String errorMsg) {

            }
        });
        return Result.success();
    }
}
