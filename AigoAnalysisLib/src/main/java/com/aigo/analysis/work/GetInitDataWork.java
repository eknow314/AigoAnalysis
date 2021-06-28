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
import com.aigo.analysis.tools.DateUtil;
import com.aigo.analysis.tools.DeviceIdUtil;

import org.json.JSONException;
import org.json.JSONObject;

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
    private int mDataVersion;

    public GetInitDataWork(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
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
                .append("&Time=").append(System.currentTimeMillis())
                .append("&LocalTime=").append(DateUtil.getLocalUnixTimestamp())
                .append("&Platform=").append(getInputData().getInt(BaseParams.PLATFORM.toString(), TrackerHelper.PLATFORM));
        if (mUserId != 0) {
            url.append("&UserId=").append(mUserId);
        }
        DefaultPacketSender.get(new Packet(url.toString()), new DefaultPacketSender.OnRequestCallBack() {
            @Override
            public void onSuccess(String json) {
                //将注册成功获取到的设备 clientAutoId, dataVersion 存储起来
                try {
                    Timber.d(json);
                    JSONObject data = new JSONObject(json);
                    mClientAutoId = data.getInt("client_auto_id");
                    mDataVersion = data.getInt("data_version");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                TrackerHelper.getInstance().getTracker().saveInitialParams(mDeviceId, mUserId, mClientAutoId, mDataVersion);
            }

            @Override
            public void onError(int errorCode, String errorMsg) {

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
            postData.put("time", System.currentTimeMillis());
            postData.put("local_time", DateUtil.getLocalUnixTimestamp());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Packet packet = new Packet(getInputData().getString(BaseParams.TARGET_API_URL.toString())
                + "v1.0/clients", postData, 1);
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
                            getClientAutoId();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

}
