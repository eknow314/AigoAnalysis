package com.aigo.analysis.dispatcher;

import com.aigo.analysis.Tracker;

/**
 * @Description:
 * @author: Eknow
 * @date: 2021/8/18 14:55
 */
public interface DispatcherFactory {

    /**
     *
     * @param tracker
     * @return
     */
    Dispatcher build(Tracker tracker);
}
