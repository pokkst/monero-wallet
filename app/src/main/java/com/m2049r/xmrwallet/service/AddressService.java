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

    private final MutableLiveData<String> _address = new MutableLiveData<>("");
    public LiveData<String> address = _address;

    public AddressService(MainActivity mainActivity, MoneroHandlerThread thread) {
        super(mainActivity, thread);
        instance = this;
    }

    public void refreshAddress() {
        _address.postValue(WalletManager.getInstance().getWallet().getAddress());
    }
}
