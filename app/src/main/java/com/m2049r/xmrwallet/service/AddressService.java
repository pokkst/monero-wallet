package com.m2049r.xmrwallet.service;

import com.m2049r.xmrwallet.data.Subaddress;
import com.m2049r.xmrwallet.model.TransactionInfo;
import com.m2049r.xmrwallet.model.Wallet;
import com.m2049r.xmrwallet.model.WalletManager;

import java.util.ArrayList;
import java.util.HashMap;

public class AddressService extends ServiceBase {
    public static AddressService instance = null;

    public static AddressService getInstance() {
        return instance;
    }

    private int latestAddressIndex = 1;

    public AddressService(MoneroHandlerThread thread) {
        super(thread);
        instance = this;
    }

    public void refreshAddresses() {
        for (TransactionInfo info : HistoryService.getInstance().getHistory()) {
            if(info.addressIndex > latestAddressIndex) {
                latestAddressIndex = info.addressIndex;
            }
        }
    }

    public String getPrimaryAddress() {
        return WalletManager.getInstance().getWallet().getAddress();
    }

    public Subaddress getLatestSubaddress() {
        Wallet wallet = WalletManager.getInstance().getWallet();
        return wallet.getSubaddressObject(latestAddressIndex);
    }
}
