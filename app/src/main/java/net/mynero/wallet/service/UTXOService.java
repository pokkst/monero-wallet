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

    public ArrayList<String> selectUtxos(long amount, boolean sendAll) throws Exception {
        ArrayList<String> selectedUtxos = new ArrayList<>();
        ArrayList<String> seenTxs = new ArrayList<>();
        List<CoinsInfo> utxos = getUtxos();
        long amountSelected = 0;
        Collections.shuffle(utxos);
        //loop through each utxo
        for (CoinsInfo coinsInfo : utxos) {
            if(!coinsInfo.isSpent()) { //filter out spent outputs
                if (sendAll) {
                    // if send all, add all utxos and set amount to send all
                    selectedUtxos.add(coinsInfo.getKeyImage());
                    amountSelected = Wallet.SWEEP_ALL;
                } else {
                    //if amount selected is still less than amount needed, and the utxos tx hash hasn't already been seen, add utxo
                    if (amountSelected <= amount && !seenTxs.contains(coinsInfo.getHash())) {
                        selectedUtxos.add(coinsInfo.getKeyImage());
                        // we don't want to spend multiple utxos from the same transaction, so we prevent that from happening here.
                        seenTxs.add(coinsInfo.getHash());
                        amountSelected += coinsInfo.getAmount();
                    }
                }
            }
        }

        if (amountSelected <= amount && !sendAll) {
            throw new Exception("insufficient wallet balance");
        }

        return selectedUtxos;
    }
}
