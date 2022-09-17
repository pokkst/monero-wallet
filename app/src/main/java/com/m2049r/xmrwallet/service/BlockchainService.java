package com.m2049r.xmrwallet.service;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.m2049r.xmrwallet.model.WalletManager;

public class BlockchainService extends ServiceBase {
    public static BlockchainService instance = null;
    private final MutableLiveData<Long> _currentHeight = new MutableLiveData<>(0L);
    public LiveData<Long> height = _currentHeight;
    private long daemonHeight = 0;
    private long lastDaemonHeightUpdateTimeMs = 0;
    public BlockchainService(MoneroHandlerThread thread) {
        super(thread);
        instance = this;
    }

    public static BlockchainService getInstance() {
        return instance;
    }

    public void refreshBlockchain() {
        _currentHeight.postValue(getCurrentHeight());
    }

    public long getCurrentHeight() {
        return WalletManager.getInstance().getWallet().getBlockChainHeight();
    }

    public long getDaemonHeight() {
        return this.daemonHeight;
    }

    public void setDaemonHeight(long height) {
        long t = System.currentTimeMillis();
        if (height > 0) {
            daemonHeight = height;
            lastDaemonHeightUpdateTimeMs = t;
        } else {
            if (t - lastDaemonHeightUpdateTimeMs > 120000) {
                daemonHeight = WalletManager.getInstance().getWallet().getDaemonBlockChainHeight();
                lastDaemonHeightUpdateTimeMs = t;
            }
        }
    }
}
