package com.m2049r.xmrwallet.fragment.onboarding;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.m2049r.xmrwallet.MainActivity;
import com.m2049r.xmrwallet.R;
import com.m2049r.xmrwallet.model.Wallet;
import com.m2049r.xmrwallet.model.WalletManager;
import com.m2049r.xmrwallet.service.PrefService;
import com.m2049r.xmrwallet.util.Constants;

import java.io.File;

public class OnboardingFragment extends Fragment {

    private OnboardingViewModel mViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(OnboardingViewModel.class);
        EditText walletPasswordEditText = view.findViewById(R.id.wallet_password_edittext);
        EditText walletSeedEditText = view.findViewById(R.id.wallet_seed_edittext);
        Button createWalletButton = view.findViewById(R.id.create_wallet_button);
        createWalletButton.setOnClickListener(view1 -> {
            String walletPassword = walletPasswordEditText.getText().toString();
            if(!walletPassword.isEmpty()) {
                PrefService.getInstance().edit().putBoolean(Constants.PREF_USES_PASSWORD, true).apply();
            }
            String walletSeed = walletSeedEditText.getText().toString().trim();
            File walletFile = new File(getActivity().getApplicationInfo().dataDir, Constants.WALLET_NAME);
            Wallet wallet = null;
            if(walletSeed.isEmpty()) {
                wallet = WalletManager.getInstance().createWallet(walletFile, walletPassword, Constants.MNEMONIC_LANGUAGE, 0);
            } else {
                wallet = WalletManager.getInstance().recoveryWallet(walletFile, walletPassword, walletSeed, "", 0);
            }
            wallet.close();
            ((MainActivity)getActivity()).init(walletFile, walletPassword);
            getActivity().onBackPressed();
        });
        walletSeedEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                String text = editable.toString();
                if(text.isEmpty()) {
                    createWalletButton.setText(R.string.create_wallet);
                } else {
                    createWalletButton.setText(R.string.menu_restore);
                }
            }
        });
    }
}