package com.shejiao.web.websocket.factory;

import com.shejiao.web.websocket.im.Message;

/**
 * @Author shejiao
 */
public interface MessageFactory {

    void sendMessage(Message message);
}
