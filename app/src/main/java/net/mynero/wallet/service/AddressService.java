package net.mynero.wallet.service;

import net.mynero.wallet.data.Subaddress;
import net.mynero.wallet.model.TransactionInfo;
import net.mynero.wallet.model.Wallet;
import net.mynero.wallet.model.WalletManager;

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
