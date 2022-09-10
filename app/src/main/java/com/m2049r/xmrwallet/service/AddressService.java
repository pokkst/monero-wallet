package com.m2049r.xmrwallet.service;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.m2049r.xmrwallet.MainActivity;
import com.m2049r.xmrwallet.model.WalletManager;

public class AddressService extends ServiceBase {
    public static AddressService instance = null;

    public static AddressService getInstance() {
        return instance;
    }

    public AddressService(MoneroHandlerThread thread) {
        super(thread);
        instance = this;
    }

    public String getAddress() {
        return WalletManager.getInstance().getWallet().getAddress();
    }
}
