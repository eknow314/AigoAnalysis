package com.aigo.analysis.extra;

import android.content.Context;
import android.text.TextUtils;

import com.aigo.analysis.QueryParams;
import com.aigo.analysis.TrackMe;
import com.aigo.analysis.Tracker;
import com.aigo.analysis.dispatcher.DefaultPacketSender;
import com.aigo.analysis.dispatcher.Packet;
import com.aigo.analysis.dispatcher.PacketSender;
import com.aigo.analysis.dispatcher.PacketSenderCallback;
import com.aigo.analysis.tools.DateUtil;
import com.aigo.analysis.tools.DeviceIdUtil;

import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

/**
 * @Description: AigoSmart Analysis 获取设备唯一标识帮助类
 * @author: Eknow
 * @date: 2021/8/27 9:38
 */
public class InitAigoClient {

    private Context mContext;
    private Tracker tracker;
    private TrackMe mDefaultTrackMe;
    private PacketSender mPacketSender;

    private String mDeviceId;
    private int mClientAutoId;
    private int mDataVersion;
    private int mUserId;

    public InitAigoClient(Context mContext, Tracker tracker) {
        this.mContext = mContext;
        this.tracker = tracker;
        this.mDefaultTrackMe = tracker.getDefaultTrackMe();
        this.mPacketSender = new DefaultPacketSender();
    }

    public void launch() {
        Thread thread = new Thread(() -> {
            mDeviceId = mDefaultTrackMe.getString(QueryParams.DEVICE_ID, "");
            mClientAutoId = mDefaultTrackMe.getInt(QueryParams.CLIENT_AUTO_ID, 0);
            mDataVersion = mDefaultTrackMe.getInt(QueryParams.DATA_VERSION, 0);
            mUserId = mDefaultTrackMe.getInt(QueryParams.USER_ID, 0);

            if (TextUtils.isEmpty(mDeviceId)) {
                mDeviceId = DeviceIdUtil.getDeviceId(mContext);
                //获取最新的设备ID后，调用初始化接口，获取初始化数据
                getInitData();
            } else if (mClientAutoId == 0 || mDataVersion == 0) {
                //调用初始化接口，获取初始化数据
                getInitData();
            } else {
                //启动上报
                postStartReport();
            }
        });
        thread.setPriority(Thread.NORM_PRIORITY);
        thread.setName("AigoAnalysis-init");
        thread.start();
    }

    /**
     * 初始化接口调用
     */
    private void getInitData() {
        StringBuilder url = new StringBuilder()
                .append(tracker.getApiUrl())
                .append("v1.0/clients")
                .append("?ClientId=").append(mDeviceId)
                .append("&Ver=").append(tracker.getAigoAnalysis().getDeviceHelper().getUserVersionName())
                .append("&Country=").append(tracker.getAigoAnalysis().getDeviceHelper().getUserCountry())
                .append("&Device=").append(tracker.getAigoAnalysis().getDeviceHelper().getAndroidDeviceModel())
                .append("&OS=").append(tracker.getAigoAnalysis().getDeviceHelper().getSystemVersion())
                .append("&Time=").append(System.currentTimeMillis())
                .append("&LocalTime=").append(DateUtil.getLocalUnixTimestamp())
                .append("&Platform=").append(tracker.getSiteId());
        if (mUserId != 0) {
            url.append("&UserId=").append(mUserId);
        }
        mPacketSender.send(new Packet(url.toString(), tracker.getHeaders()), new PacketSenderCallback() {
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
                tracker.saveInitialParams(mDeviceId, mUserId, mClientAutoId, mDataVersion);
                tracker.getAigoAnalysis().setInit(true);
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                //初始化失败，停止上报
                tracker.getAigoAnalysis().setInit(false);
            }
        });
    }

    /**
     * 启动上报接口调用
     */
    private void postStartReport() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(QueryParams.CLIENT_AUTO_ID.toString(), mClientAutoId);
            jsonObject.put(QueryParams.USER_ID.toString(), mUserId);
            jsonObject.put(QueryParams.APP_VERSION.toString(), tracker.getAigoAnalysis().getDeviceHelper().getUserVersionName());
            jsonObject.put(QueryParams.COUNTRY.toString(), tracker.getAigoAnalysis().getDeviceHelper().getUserCountry());
            jsonObject.put(QueryParams.DEVICE.toString(), tracker.getAigoAnalysis().getDeviceHelper().getAndroidDeviceModel());
            jsonObject.put(QueryParams.DEVICE_OS.toString(), tracker.getAigoAnalysis().getDeviceHelper().getSystemVersion());
            jsonObject.put(QueryParams.TIME.toString(), System.currentTimeMillis());
            jsonObject.put(QueryParams.LOCAL_TIME.toString(), DateUtil.getLocalUnixTimestamp());
            jsonObject.put(QueryParams.PLATFORM.toString(), tracker.getSiteId());
            jsonObject.put(QueryParams.DATA_VERSION.toString(), mDataVersion);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Packet packet = new Packet(tracker.getApiUrl() + "v1.0/clients", jsonObject, tracker.getHeaders(), 1);
        mPacketSender.send(packet, new PacketSenderCallback() {
            @Override
            public void onSuccess(String json) {
                tracker.getAigoAnalysis().setInit(true);
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                if (errorCode == 400 && !TextUtils.isEmpty(errorMsg)) {
                    try {
                        JSONObject data = new JSONObject(errorMsg);
                        String err = data.getString("code");
                        String msg = data.getString("message");
                        if (err.equals("CUSTOM_FAIL") && msg.equals("dataVersion changed")) {
                            //重新获取初始化数据
                            getInitData();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


}
