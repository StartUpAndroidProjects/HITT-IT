package com.wolffincdevelopment.hiit_it;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/**
 * Created by kylewolff on 7/10/2016.
 */
public class MessageHandler
{
    private Handler handler;

    public MessageHandler(Handler handler)
    {
        this.handler = handler;
    }

    public Message createMessage(Message message, int what)
    {
        if (message == null) {
            message = handler.obtainMessage(what);
        } else {
            message = null;
            message = handler.obtainMessage(what);
        }

        return message;
    }

    public Message createMessage(Message message, int what, Bundle data)
    {
        if (message == null) {
            message = handler.obtainMessage(what);
            message.setData(data);
        } else {
            message = null;
            message = handler.obtainMessage(what);
            message.setData(data);
        }

        return message;
    }

    public void sendMessage(Message message)
    {
        handler.sendMessage(message);
    }
}
