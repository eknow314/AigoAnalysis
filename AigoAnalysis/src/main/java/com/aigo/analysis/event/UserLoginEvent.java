package com.aigo.analysis.event;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;

import com.aigo.analysis.AigoAnalysis;
import com.aigo.analysis.BaseParams;
import com.aigo.analysis.Tracker;
import com.aigo.analysis.work.UserLoginWork;

/**
 * @Description: 用户账号活动事件上报，
 * 成功之后，需要将用户的 userId 存储本地，随后的上报就会存在 userId
 * @author: Eknow
 * @date: 2021/5/18 10:27
 */
public class UserLoginEvent extends BaseEvent implements IWorkRequestEvent {

    private String userId;

    private String photo;
    private String nickName;
    private String account;
    private String phone;

    /**
     * @param userId
     * @param photo
     * @param nickName
     * @param account
     * @param phone
     */
    public UserLoginEvent(String userId, String photo, String nickName, String account, String phone) {
        this.userId = userId;
        this.photo = photo;
        this.nickName = nickName;
        this.account = account;
        this.phone = phone;
    }

    @Override
    public OneTimeWorkRequest send(Tracker tracker) {
        Data data = commonData(tracker)
                .putString("photo", photo)
                .putString("nick_name", nickName)
                .putString("account", account)
                .putString("phone", phone)
                .putString(BaseParams.USER_ID.toString(), userId)
                .build();

        return new OneTimeWorkRequest.Builder(UserLoginWork.class)
                .setInputData(data)
                .addTag(AigoAnalysis.tag(UserLoginEvent.class))
                .build();
    }
}
