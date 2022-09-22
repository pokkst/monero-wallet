package net.mynero.wallet.service;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import net.mynero.wallet.model.CoinsInfo;
import net.mynero.wallet.model.TransactionInfo;
import net.mynero.wallet.model.Wallet;
import net.mynero.wallet.model.WalletManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UTXOService extends ServiceBase {
    public static UTXOService instance = null;
    private final MutableLiveData<List<CoinsInfo>> _utxos = new MutableLiveData<>();
    public LiveData<List<CoinsInfo>> utxos = _utxos;
    public UTXOService(MoneroHandlerThread thread) {
        super(thread);
        instance = this;
    }

    public static UTXOService getInstance() {
        return instance;
    }

    public void refreshUtxos() {
        _utxos.postValue(getUtxos());
    }

    public List<CoinsInfo> getUtxos() {
        return WalletManager.getInstance().getWallet().getCoins().getAll();
    }

    public ArrayList<String> selectUtxos(long amount, boolean sendAll) {
        ArrayList<String> selectedUtxos = new ArrayList<>();
        List<CoinsInfo> utxos = getUtxos();
        if(sendAll) {
            for(CoinsInfo coinsInfo : utxos) {
                selectedUtxos.add(coinsInfo.getKeyImage());
            }
        } else {
            long amountSelected = 0;
            Collections.shuffle(utxos);
            for (CoinsInfo coinsInfo : utxos) {
                if (amount == Wallet.SWEEP_ALL) {
                    selectedUtxos.add(coinsInfo.getKeyImage());
                } else {
                    if (amountSelected <= amount) {
                        selectedUtxos.add(coinsInfo.getKeyImage());
                        amountSelected += coinsInfo.getAmount();
                    }
                }
            }
        }

        return selectedUtxos;
    }
}
