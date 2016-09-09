package com.wolffincdevelopment.hiit_it.handler;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by kylewolff on 7/10/2016.
 */
public class MessageHandler implements Parcelable
{
    private Handler handler;

    private int mData;

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(mData);
    }

    public static final Parcelable.Creator<MessageHandler> CREATOR = new Parcelable.Creator<MessageHandler>()
    {
        public MessageHandler createFromParcel(Parcel in) {
            return new MessageHandler(in);
        }

        public MessageHandler[] newArray(int size) {
            return new MessageHandler[size];
        }
    };

    private MessageHandler(Parcel in) {
        mData = in.readInt();
    }

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
