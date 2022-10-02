package net.mynero.wallet.service;

import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import net.mynero.wallet.model.CoinsInfo;
import net.mynero.wallet.model.PendingTransaction;
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
        final long basicFeeEstimate = calculateBasicFee(amount);
        final long amountWithBasicFee = amount + basicFeeEstimate;
        ArrayList<String> selectedUtxos = new ArrayList<>();
        ArrayList<String> seenTxs = new ArrayList<>();
        List<CoinsInfo> utxos = getUtxos();
        long amountSelected = 0;
        Collections.sort(utxos);
        //loop through each utxo
        for (CoinsInfo coinsInfo : utxos) {
            if (!coinsInfo.isSpent() && coinsInfo.isUnlocked()) { //filter out spent and locked outputs
                if (sendAll) {
                    // if send all, add all utxos and set amount to send all
                    selectedUtxos.add(coinsInfo.getKeyImage());
                    amountSelected = Wallet.SWEEP_ALL;
                } else {
                    //if amount selected is still less than amount needed, and the utxos tx hash hasn't already been seen, add utxo
                    if (amountSelected <= amountWithBasicFee && !seenTxs.contains(coinsInfo.getHash())) {
                        selectedUtxos.add(coinsInfo.getKeyImage());
                        // we don't want to spend multiple utxos from the same transaction, so we prevent that from happening here.
                        seenTxs.add(coinsInfo.getHash());
                        amountSelected += coinsInfo.getAmount();
                    }
                }
            }
        }

        if (amountSelected < amountWithBasicFee && !sendAll) {
            throw new Exception("insufficient wallet balance");
        }

        return selectedUtxos;
    }

    private long calculateBasicFee(long amount) {
        ArrayList<Pair<String, Long>> destinations = new ArrayList<>();
        destinations.add(new Pair<>("87MRtZPrWUCVUgcFHdsVb5MoZUcLtqfD3FvQVGwftFb8eSdMnE39JhAJcbuSW8X2vRaRsB9RQfuCpFciybJFHaz3QYPhCLw", amount));
        // destination string doesn't actually matter here, so i'm using the donation address. amount also technically doesn't matter
        // priority also isn't accounted for in the Monero C++ code. maybe this is a bug by the core Monero team, or i'm using an outdated method.
        return WalletManager.getInstance().getWallet().estimateTransactionFee(destinations, PendingTransaction.Priority.Priority_Low);
    }
}
