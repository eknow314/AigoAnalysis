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
import com.aigo.analysis.tools.DeviceIdUtil;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import timber.log.Timber;

/**
 * @Description: 获取设备唯一标识
 * @author: Eknow
 * @date: 2021/5/18 9:38
 */
public class GetInitDataWork extends Worker {

    private String mDeviceId = "";
    private int mUserId;
    private int mClientAutoId;
    private Date dLocal;
    private Date dUTC;

    public GetInitDataWork(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        DateTime nowUTC = new DateTime(DateTimeZone.UTC);
        DateTime nowLocal = nowUTC.withZone(DateTimeZone.getDefault());
        dLocal = nowLocal.toLocalDateTime().toDate();
        dUTC = nowUTC.toLocalDateTime().toDate();
    }

    @NonNull
    @Override
    public Result doWork() {
        mDeviceId = getInputData().getString(BaseParams.DEVICE_ID.toString());
        mUserId = getInputData().getInt(BaseParams.USER_ID.toString(), 0);
        mClientAutoId = getInputData().getInt(BaseParams.CLIENT_AUTO_ID.toString(), 0);

        if (TextUtils.isEmpty(mDeviceId)) {
            //如果本地存储不存在，先去取Android手机设备唯一标识
            mDeviceId = DeviceIdUtil.getDeviceId(getApplicationContext());
            getClientAutoId();
//            DeviceID.getOAID(getApplicationContext(), new IGetter() {
//                @Override
//                public void onOAIDGetComplete(@NonNull String result) {
//                    mDeviceId = result;
//                    getClientAutoId();
//                }
//
//                @Override
//                public void onOAIDGetError(@NonNull Throwable error) {
//                    mDeviceId = DeviceID.getWidevineID();
//                    if (!TextUtils.isEmpty(mDeviceId)) {
//                        getClientAutoId();
//                        return;
//                    }
//                    mDeviceId = DeviceID.getAndroidID(getApplicationContext());
//                    if (!TextUtils.isEmpty(mDeviceId)) {
//                        getClientAutoId();
//                        return;
//                    }
//                    mDeviceId = DeviceID.getGUID(getApplicationContext());
//                    getClientAutoId();
//                }
//            });
        } else if (mClientAutoId == 0) {
            //如果本地存储的数据分析系统的客户端id为0，再次获取
            getClientAutoId();
        } else {
            //调用启动上报接口
            postStartReport();
        }
        return Result.success();
    }

    /**
     * 去后台获取唯一标识，相当于注册了当前设备到后台
     */
    private void getClientAutoId() {
        StringBuilder url = new StringBuilder().append(getInputData().getString(BaseParams.TARGET_API_URL.toString()))
                .append("v1.0/clients")
                .append("?ClientId=").append(mDeviceId)
                .append("&Ver=").append(getInputData().getString(BaseParams.APP_VERSION.toString()))
                .append("&Country=").append(getInputData().getString(BaseParams.COUNTRY.toString()))
                .append("&Device=").append(getInputData().getString(BaseParams.DEVICE_MODEL.toString()))
                .append("&OS=").append(getInputData().getString(BaseParams.SYSTEM_VERSION.toString()))
                .append("&Time=").append(dUTC.getTime())
                .append("&LocalTime=").append(dLocal.getTime())
                .append("&Platform=").append(TrackerHelper.PLATFORM);
        if (mUserId != 0) {
            url.append("&UserId=").append(mUserId);
        }
        DefaultPacketSender.get(new Packet(url.toString()), new DefaultPacketSender.OnRequestCallBack() {
            @Override
            public void onSuccess(String json) {
                //将注册成功获取到的设备 clientAutoId 存储起来
                try {
                    Timber.d(json);
                    JSONObject data = new JSONObject(json);
                    mClientAutoId = data.getInt("client_auto_id");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                TrackerHelper.getInstance().getTracker().saveInitialParams(mDeviceId, mUserId, mClientAutoId);
            }

            @Override
            public void onError(String errorMsg) {

            }
        });
    }

    /**
     * 启动上报
     */
    private void postStartReport() {
        JSONObject postData = new JSONObject(getInputData().getKeyValueMap());
        try {
            postData.put("device", getInputData().getString(BaseParams.DEVICE_MODEL.toString()));
            postData.put("os", getInputData().getString(BaseParams.SYSTEM_VERSION.toString()));
            postData.put("time", dUTC.getTime());
            postData.put("local_time", dLocal.getTime());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Packet packet = new Packet(getInputData().getString(BaseParams.TARGET_API_URL.toString())
                + "v1.0/clients", postData, 1);
        DefaultPacketSender.post(packet, null);
    }

}
