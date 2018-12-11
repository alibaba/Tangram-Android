package com.tmall.wireless.tangram.eventbus;

public interface IEventHandlerReceiver {
    void handleEvent(Event event);

    String eventTypeToHandler();
}
