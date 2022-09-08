package com.m2049r.xmrwallet.service;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.m2049r.xmrwallet.MainActivity;
import com.m2049r.xmrwallet.model.WalletManager;

public class BlockchainService extends ServiceBase {
    public static BlockchainService instance = null;

    public static BlockchainService getInstance() {
        return instance;
    }

    private final MutableLiveData<Long> _currentHeight = new MutableLiveData<>(0L);
    public LiveData<Long> height = _currentHeight;

    public BlockchainService(MainActivity mainActivity, MoneroHandlerThread thread) {
        super(mainActivity, thread);
        instance = this;
    }

    public void refreshBlockchain() {
        _currentHeight.postValue(getCurrentHeight());
    }

    public long getCurrentHeight() {
        return WalletManager.getInstance().getWallet().getBlockChainHeight();
    }

    public long getDaemonHeight() {
        return WalletManager.getInstance().getWallet().getDaemonBlockChainHeight();
    }
}
