package com.m2049r.xmrwallet.fragment.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.m2049r.xmrwallet.R;
import com.m2049r.xmrwallet.model.Wallet;
import com.m2049r.xmrwallet.model.WalletManager;
import com.m2049r.xmrwallet.service.BlockchainService;
import com.m2049r.xmrwallet.util.DayNightMode;
import com.m2049r.xmrwallet.util.NightmodeHelper;

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
        SwitchCompat nightModeSwitch = view.findViewById(R.id.day_night_switch);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Seed: " + wallet.getSeed("")+"\n\n");
        stringBuilder.append("Private view-key: " + wallet.getSecretViewKey()+"\n\n");
        stringBuilder.append("Restore height: " + wallet.getRestoreHeight() + "\n\n");
        stringBuilder.append("Wallet height: " + wallet.getBlockChainHeight() + "\n\n");
        stringBuilder.append("Daemon height: " + BlockchainService.getInstance().getDaemonHeight() + "\n\n");
        walletInfoTextView.setText(stringBuilder.toString());

        nightModeSwitch.setChecked(NightmodeHelper.getPreferredNightmode(getContext()) == DayNightMode.NIGHT);
        nightModeSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            if(b) {
                NightmodeHelper.setAndSavePreferredNightmode(getContext(), DayNightMode.NIGHT);
            } else {
                NightmodeHelper.setAndSavePreferredNightmode(getContext(), DayNightMode.DAY);
            }
        });
    }
}