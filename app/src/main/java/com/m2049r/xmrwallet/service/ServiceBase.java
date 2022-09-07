package com.m2049r.xmrwallet.service;

import com.m2049r.xmrwallet.MainActivity;

public class ServiceBase {
    private final MainActivity mainActivity;
    private final MoneroHandlerThread thread;

    public ServiceBase(MainActivity mainActivity, MoneroHandlerThread thread) {
        this.mainActivity = mainActivity;
        this.thread = thread;
    }

    public MainActivity getMainActivity() {
        return mainActivity;
    }

    public MoneroHandlerThread getThread() {
        return thread;
    }
}
