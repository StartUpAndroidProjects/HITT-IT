package com.wolffincdevelopment.hiit_it;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by Kyle Wolff on 1/29/17.
 */

public class RxJavaBus {

    private static RxJavaBus rxJavaBus;

    public static RxJavaBus getInstance() {

        if (rxJavaBus == null) {
            rxJavaBus = new RxJavaBus();
        }

        return rxJavaBus;
    }

    private final PublishSubject<Object> bus = PublishSubject.create();

    public void send(Object o) {
        bus.onNext(o);
    }

    public <T> Disposable subscribe(Class<T> clazz, Consumer<T> action) {
        return bus.ofType(clazz)
                .subscribe(action);
    }
}
