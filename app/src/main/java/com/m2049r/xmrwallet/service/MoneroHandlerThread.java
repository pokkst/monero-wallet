/*
 * Copyright (C) 2006 The Android Open Source Project
 * Copyright (c) 2017 m2049r
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.m2049r.xmrwallet.service;

import static com.m2049r.xmrwallet.model.Wallet.SWEEP_ALL;

import com.m2049r.xmrwallet.data.DefaultNodes;
import com.m2049r.xmrwallet.data.Node;
import com.m2049r.xmrwallet.data.TxData;
import com.m2049r.xmrwallet.model.PendingTransaction;
import com.m2049r.xmrwallet.model.Wallet;
import com.m2049r.xmrwallet.model.WalletListener;
import com.m2049r.xmrwallet.model.WalletManager;
import com.m2049r.xmrwallet.util.Constants;

import java.io.File;


/**
 * Handy class for starting a new thread that has a looper. The looper can then be
 * used to create handler classes. Note that start() must still be called.
 * The started Thread has a stck size of STACK_SIZE (=5MB)
 */
public class MoneroHandlerThread extends Thread implements WalletListener {
    private Listener listener = null;
    // from src/cryptonote_config.h
    static public final long THREAD_STACK_SIZE = 5 * 1024 * 1024;
    private Wallet wallet;

    public MoneroHandlerThread(String name, Listener listener, Wallet wallet) {
        super(null, null, name, THREAD_STACK_SIZE);
        this.listener = listener;
        this.wallet = wallet;
    }

    @Override
    public synchronized void start() {
        super.start();
        this.listener.onRefresh();
    }

    @Override
    public void run() {
        boolean usesTor = PrefService.getInstance().getBoolean(Constants.PREF_USES_TOR, false);
        if(usesTor) {
            String proxy = "127.0.0.1:9050";
            WalletManager.getInstance().setProxy(proxy);
            WalletManager.getInstance().setDaemon(Node.fromString(DefaultNodes.boldsuck.getUri()));
            wallet.setProxy(proxy);
        } else {
            WalletManager.getInstance().setDaemon(Node.fromString(DefaultNodes.XMRTW.getUri()));
        }
        wallet.init(0);
        wallet.setListener(this);
        wallet.startRefresh();
    }

    @Override
    public void moneySpent(String txId, long amount) {
    }

    @Override
    public void moneyReceived(String txId, long amount) {
    }

    @Override
    public void unconfirmedMoneyReceived(String txId, long amount) {
    }

    @Override
    public void newBlock(long height) {
        refresh();
        BlockchainService.getInstance().setDaemonHeight(wallet.isSynchronized() ? height : 0);
    }

    @Override
    public void updated() {
        refresh();
    }

    int triesLeft = 5;

    @Override
    public void refreshed() {
        Wallet.ConnectionStatus status = wallet.getFullStatus().getConnectionStatus();
        if(status == Wallet.ConnectionStatus.ConnectionStatus_Disconnected || status == null) {
            if(triesLeft > 0) {
                wallet.startRefresh();
                triesLeft--;
            } else {
                listener.onConnectionFail();
            }
        } else {
            BlockchainService.getInstance().setDaemonHeight(wallet.getDaemonBlockChainHeight());
            wallet.setSynchronized();
            wallet.store();
            refresh();
        }
    }

    private void refresh() {
        wallet.refreshHistory();
        listener.onRefresh();
    }

    public PendingTransaction createTx(String address, String amountStr, boolean sendAll) {
        long amount = sendAll ? SWEEP_ALL : Wallet.getAmountFromString(amountStr);
        return wallet.createTransaction(new TxData(address, amount, 0, PendingTransaction.Priority.Priority_Default));
    }

    public boolean sendTx(PendingTransaction pendingTx) {
        return pendingTx.commit("", true);
    }

    public interface Listener {
        void onRefresh();
        void onConnectionFail();
    }
}
