package net.mynero.wallet.service;

public class ServiceBase {
    private final MoneroHandlerThread thread;

    public ServiceBase(MoneroHandlerThread thread) {
        this.thread = thread;
    }

    public MoneroHandlerThread getThread() {
        return thread;
    }
}
