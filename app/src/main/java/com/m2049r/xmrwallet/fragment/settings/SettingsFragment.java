package com.m2049r.xmrwallet.fragment.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.m2049r.xmrwallet.R;
import com.m2049r.xmrwallet.model.Wallet;
import com.m2049r.xmrwallet.model.WalletManager;

public class SettingsFragment extends Fragment {

    private SettingsViewModel mViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        Wallet wallet = WalletManager.getInstance().getWallet();
        TextView walletInfoTextView = view.findViewById(R.id.wallet_info_textview);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Seed: " + wallet.getSeed("")+"\n\n");
        stringBuilder.append("Private view-key: " + wallet.getSecretViewKey()+"\n\n");
        stringBuilder.append("Restore height: " + wallet.getRestoreHeight() + "\n\n");
        walletInfoTextView.setText(stringBuilder.toString());
    }
}