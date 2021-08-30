package com.aigo.analysis.dispatcher;

import com.aigo.analysis.Tracker;
import com.aigo.analysis.tools.Connectivity;

/**
 * @Description:
 * @author: Eknow
 * @date: 2021/8/18 14:56
 */
public class DefaultDispatcherFactory implements DispatcherFactory {

    @Override
    public Dispatcher build(Tracker tracker) {
        return new DefaultDispatcher(
                new EventCache(new EventDiskCache(tracker)),
                new Connectivity(tracker.getAigoAnalysis().getContext()),
                new PacketFactory(tracker),
                new DefaultPacketSender()
        );
    }
}
