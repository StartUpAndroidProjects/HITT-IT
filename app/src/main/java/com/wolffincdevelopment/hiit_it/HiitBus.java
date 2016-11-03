package com.wolffincdevelopment.hiit_it;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * Created by Kyle Wolff on 11/2/16.
 */

public class HiitBus extends Bus{

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    public HiitBus() {
        super();
    }

    public HiitBus(String identifier) {
        super(identifier);
    }

    public HiitBus(ThreadEnforcer enforcer) {
        super(enforcer);
    }

    public HiitBus(ThreadEnforcer enforcer, String identifier) {
        super(enforcer, identifier);
    }

    @Override
    public void post(final Object event) {

        if (Looper.myLooper() == Looper.getMainLooper()) {
            super.post(event);

        } else {

            mHandler.post(new Runnable() {
                @Override
                public void run() {

                    HiitBus.super.post(event);
                }
            });
        }
    }

}
