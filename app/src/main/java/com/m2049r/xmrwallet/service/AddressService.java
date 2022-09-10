package com.m2049r.xmrwallet.service;

import com.m2049r.xmrwallet.data.Subaddress;
import com.m2049r.xmrwallet.model.TransactionInfo;
import com.m2049r.xmrwallet.model.Wallet;
import com.m2049r.xmrwallet.model.WalletManager;
import java.util.HashMap;

public class AddressService extends ServiceBase {
    public static AddressService instance = null;

    public static AddressService getInstance() {
        return instance;
    }

    private final HashMap<String, Integer> subAddresses = new HashMap<>();
    private int latestAddressIndex = 1;

    public AddressService(MoneroHandlerThread thread) {
        super(thread);
        instance = this;
    }

    public void refreshAddresses() {
        Wallet wallet = WalletManager.getInstance().getWallet();
        int issuedAddressesSize = WalletManager.getInstance().getWallet().getNumSubaddresses();
        if(subAddresses.size() != issuedAddressesSize) {
            for (int i = 0; i < issuedAddressesSize; i++) {
                if(!subAddresses.containsValue(i)) {
                    subAddresses.put(wallet.getSubaddress(i), i);
                }
            }
        }

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

    public HashMap<String, Integer> getIssuedSubaddresses() {
        return subAddresses;
    }
}
