package com.wolffincdevelopment.hiit_it;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;

/**
 * Created by kylewolff on 8/18/2016.
 */
public class AudioPlayerBroadCastReceiver extends BroadcastReceiver {

    private MessageHandler messageHandler;
    private Message notificationMessage;
    private What what = new What();
    private Bundle bundle = new Bundle();

    @Override
    public void onReceive(Context context, Intent intent) {

        if(Constant.ACTION_PLAY_PAUSE.compareTo(intent.getAction()) == 0){

            notificationMessage = messageHandler.createMessage(notificationMessage, what.pauseResumeCurrentSong());

        }else if(Constant.ACTION_NEXT.compareTo(intent.getAction()) == 0) {

            bundle.clear();
            bundle.putString("next", "next");
            notificationMessage = messageHandler.createMessage(notificationMessage, what.getNextOrPrev());

        }else if(Constant.ACTION_PREVIOUS.compareTo(intent.getAction()) == 0) {

            bundle.clear();
            bundle.putString("prev", "prev");
            notificationMessage = messageHandler.createMessage(notificationMessage, what.getNextOrPrev());
        }
    }
}
