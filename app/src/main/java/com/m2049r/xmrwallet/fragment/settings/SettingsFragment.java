package com.m2049r.xmrwallet.fragment.settings;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
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
    TextWatcher proxyAddressListener = new TextWatcher() {
        @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        @Override public void afterTextChanged(Editable editable) {
            if(mViewModel != null) {
                mViewModel.setProxyAddress(editable.toString());
                mViewModel.updateProxy();
            }
        }
    };
    TextWatcher proxyPortListener = new TextWatcher() {
        @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        @Override public void afterTextChanged(Editable editable) {
            if(mViewModel != null) {
                mViewModel.setProxyPort(editable.toString());
                mViewModel.updateProxy();
            }
        }
    };
    private EditText walletProxyAddressEditText;
    private EditText walletProxyPortEditText;

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
        ConstraintLayout proxySettingsLayout = view.findViewById(R.id.wallet_proxy_settings_layout);
        walletProxyAddressEditText = view.findViewById(R.id.wallet_proxy_address_edittext);
        walletProxyPortEditText = view.findViewById(R.id.wallet_proxy_port_edittext);

        nightModeSwitch.setChecked(NightmodeHelper.getPreferredNightmode() == DayNightMode.NIGHT);
        nightModeSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                NightmodeHelper.setAndSavePreferredNightmode(DayNightMode.NIGHT);
            } else {
                NightmodeHelper.setAndSavePreferredNightmode(DayNightMode.DAY);
            }
        });

        boolean usesProxy = PrefService.getInstance().getBoolean(Constants.PREF_USES_TOR, false);
        String proxy = PrefService.getInstance().getString(Constants.PREF_PROXY, "");
        if(proxy.contains(":")) {
            String proxyAddress = proxy.split(":")[0];
            String proxyPort = proxy.split(":")[1];
            initProxyStuff(proxyAddress, proxyPort);
        }
        torSwitch.setChecked(usesProxy);
        if(usesProxy) {
            proxySettingsLayout.setVisibility(View.VISIBLE);
        } else {
            proxySettingsLayout.setVisibility(View.GONE);
        }

        addProxyTextListeners();

        torSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            PrefService.getInstance().edit().putBoolean(Constants.PREF_USES_TOR, b).apply();
            if(b) {
                String proxyString = PrefService.getInstance().getString(Constants.PREF_PROXY, "");
                if(proxyString.contains(":")) {
                    removeProxyTextListeners();

                    String proxyAddress = proxyString.split(":")[0];
                    String proxyPort = proxyString.split(":")[1];
                    initProxyStuff(proxyAddress, proxyPort);

                    addProxyTextListeners();
                }
                proxySettingsLayout.setVisibility(View.VISIBLE);
            } else {
                proxySettingsLayout.setVisibility(View.GONE);
            }

            mViewModel.updateProxy();
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

    private void initProxyStuff(String proxyAddress, String proxyPort) {
        boolean validIpAddress = Patterns.IP_ADDRESS.matcher(proxyAddress).matches();
        if(validIpAddress) {
            mViewModel.setProxyAddress(proxyAddress);
            mViewModel.setProxyPort(proxyPort);
            walletProxyAddressEditText.setText(proxyAddress);
            walletProxyPortEditText.setText(proxyPort);
        }
    }

    private void removeProxyTextListeners() {
        walletProxyAddressEditText.removeTextChangedListener(proxyAddressListener);
        walletProxyPortEditText.removeTextChangedListener(proxyPortListener);
    }

    private void addProxyTextListeners() {
        walletProxyAddressEditText.addTextChangedListener(proxyAddressListener);
        walletProxyPortEditText.addTextChangedListener(proxyPortListener);
    }
}