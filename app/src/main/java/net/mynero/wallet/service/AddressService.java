package net.mynero.wallet.service;

import net.mynero.wallet.data.Subaddress;
import net.mynero.wallet.model.TransactionInfo;
import net.mynero.wallet.model.Wallet;
import net.mynero.wallet.model.WalletManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        List<TransactionInfo> localTransactionList = new ArrayList<>(HistoryService.getInstance().getHistory());
        for (TransactionInfo info : localTransactionList) {
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
