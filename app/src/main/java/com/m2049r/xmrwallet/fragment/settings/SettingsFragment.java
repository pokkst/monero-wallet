package com.m2049r.xmrwallet.fragment.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.m2049r.xmrwallet.R;
import com.m2049r.xmrwallet.fragment.dialog.InformationBottomSheetDialog;
import com.m2049r.xmrwallet.fragment.dialog.PasswordBottomSheetDialog;
import com.m2049r.xmrwallet.model.WalletManager;
import com.m2049r.xmrwallet.service.PrefService;
import com.m2049r.xmrwallet.util.Constants;
import com.m2049r.xmrwallet.util.DayNightMode;
import com.m2049r.xmrwallet.util.NightmodeHelper;

public class SettingsFragment extends Fragment implements PasswordBottomSheetDialog.PasswordListener {

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
        Button displaySeedButton = view.findViewById(R.id.display_seed_button);
        SwitchCompat nightModeSwitch = view.findViewById(R.id.day_night_switch);
        SwitchCompat torSwitch = view.findViewById(R.id.tor_switch);

        nightModeSwitch.setChecked(NightmodeHelper.getPreferredNightmode() == DayNightMode.NIGHT);
        nightModeSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                NightmodeHelper.setAndSavePreferredNightmode(DayNightMode.NIGHT);
            } else {
                NightmodeHelper.setAndSavePreferredNightmode(DayNightMode.DAY);
            }
        });

        torSwitch.setChecked(PrefService.getInstance().getBoolean(Constants.PREF_USES_TOR, false));
        torSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            PrefService.getInstance().edit().putBoolean(Constants.PREF_USES_TOR, b).apply();

            // TODO display "Advanced" settings mode when enabled to configure specific proxy address and port
            String proxy = b ? "127.0.0.1:9050" : "";
            WalletManager.getInstance().setProxy(proxy);
            WalletManager.getInstance().getWallet().setProxy(proxy);
        });

        displaySeedButton.setOnClickListener(view1 -> {
            boolean usesPassword = PrefService.getInstance().getBoolean(Constants.PREF_USES_PASSWORD, false);
            if (usesPassword) {
                PasswordBottomSheetDialog passwordDialog = new PasswordBottomSheetDialog();
                passwordDialog.listener = this;
                passwordDialog.show(getActivity().getSupportFragmentManager(), "password_dialog");
            } else {
                displaySeedDialog();
            }
        });
    }

    private void displaySeedDialog() {
        InformationBottomSheetDialog informationDialog = new InformationBottomSheetDialog();
        informationDialog.showCopyButton = true;
        informationDialog.information = WalletManager.getInstance().getWallet().getSeed("");
        informationDialog.show(getActivity().getSupportFragmentManager(), "information_seed_dialog");
    }

    @Override
    public void onPasswordSuccess(String password) {
        displaySeedDialog();
    }

    @Override
    public void onPasswordFail() {
        Toast.makeText(getContext(), R.string.bad_password, Toast.LENGTH_SHORT).show();
    }
}