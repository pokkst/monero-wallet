package com.m2049r.xmrwallet.service;

import com.m2049r.xmrwallet.data.Subaddress;
import com.m2049r.xmrwallet.model.TransactionInfo;
import com.m2049r.xmrwallet.model.Wallet;
import com.m2049r.xmrwallet.model.WalletManager;

public class AddressService extends ServiceBase {
    public static AddressService instance = null;
    private int latestAddressIndex = 1;

    public AddressService(MoneroHandlerThread thread) {
        super(thread);
        instance = this;
    }

    public static AddressService getInstance() {
        return instance;
    }

    public void refreshAddresses() {
        for (TransactionInfo info : HistoryService.getInstance().getHistory()) {
            if (info.addressIndex >= latestAddressIndex) {
                latestAddressIndex = info.addressIndex + 1;
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
