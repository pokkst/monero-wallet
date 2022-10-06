package net.mynero.wallet.service;

import net.mynero.wallet.MoneroApplication;
import net.mynero.wallet.data.Subaddress;
import net.mynero.wallet.model.TransactionInfo;
import net.mynero.wallet.model.Wallet;
import net.mynero.wallet.model.WalletManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddressService extends ServiceBase {
    public static AddressService instance = null;
    private int latestAddressIndex = 1;
    private int lastUsedSubaddress = 0;

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
            if (info.addressIndex > lastUsedSubaddress)
                lastUsedSubaddress = info.addressIndex;
        }
        latestAddressIndex = WalletManager.getInstance().getWallet().getNumSubaddresses();
    }

    public int getLastUsedSubaddress() {
        return lastUsedSubaddress;
    }

    public String getPrimaryAddress() {
        return WalletManager.getInstance().getWallet().getAddress();
    }

    public Subaddress freshSubaddress() {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss", Locale.US).format(new Date());
        Wallet wallet = WalletManager.getInstance().getWallet();
        wallet.addSubaddress(wallet.getAccountIndex(), timeStamp);
        refreshAddresses();
        wallet.store();
        return wallet.getSubaddressObject(latestAddressIndex);
    }

    public Subaddress currentSubaddress() {
        Wallet wallet = WalletManager.getInstance().getWallet();
        return wallet.getSubaddressObject(latestAddressIndex);
    }
}
