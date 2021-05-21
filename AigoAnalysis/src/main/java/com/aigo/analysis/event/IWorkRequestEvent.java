package com.aigo.analysis.event;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkRequest;

import com.aigo.analysis.Tracker;

/**
 * @Description:
 * @author: Eknow
 * @date: 2021/5/18 11:51
 */
public interface IWorkRequestEvent {

    OneTimeWorkRequest send(Tracker tracker);
}
