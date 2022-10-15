package net.mynero.wallet.fragment.onboarding;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import net.mynero.wallet.MainActivity;
import net.mynero.wallet.MoneroApplication;
import net.mynero.wallet.R;
import net.mynero.wallet.data.DefaultNodes;
import net.mynero.wallet.model.Wallet;
import net.mynero.wallet.model.WalletManager;
import net.mynero.wallet.service.PrefService;
import net.mynero.wallet.util.Constants;

import java.io.File;

public class OnboardingFragment extends Fragment {

    private OnboardingViewModel mViewModel;
    TextWatcher proxyAddressListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (mViewModel != null) {
                mViewModel.setProxyAddress(editable.toString());
                mViewModel.updateProxy(((MoneroApplication)getActivity().getApplication()));
            }
        }
    };
    TextWatcher proxyPortListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (mViewModel != null) {
                mViewModel.setProxyPort(editable.toString());
                mViewModel.updateProxy(((MoneroApplication)getActivity().getApplication()));
            }
        }
    };
    private EditText walletProxyAddressEditText;
    private EditText walletProxyPortEditText;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_onboarding, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(OnboardingViewModel.class);
        EditText walletPasswordEditText = view.findViewById(R.id.wallet_password_edittext);
        EditText walletSeedEditText = view.findViewById(R.id.wallet_seed_edittext);
        EditText walletRestoreHeightEditText = view.findViewById(R.id.wallet_restore_height_edittext);
        Button createWalletButton = view.findViewById(R.id.create_wallet_button);
        TextView moreOptionsDropdownTextView = view.findViewById(R.id.advanced_settings_dropdown_textview);
        ImageView moreOptionsChevronImageView = view.findViewById(R.id.advanced_settings_chevron_imageview);
        SwitchCompat torSwitch = view.findViewById(R.id.tor_onboarding_switch);
        ConstraintLayout proxySettingsLayout = view.findViewById(R.id.wallet_proxy_settings_layout);
        walletProxyAddressEditText = view.findViewById(R.id.wallet_proxy_address_edittext);
        walletProxyPortEditText = view.findViewById(R.id.wallet_proxy_port_edittext);

        moreOptionsDropdownTextView.setOnClickListener(view12 -> mViewModel.onMoreOptionsClicked());
        moreOptionsChevronImageView.setOnClickListener(view12 -> mViewModel.onMoreOptionsClicked());

        createWalletButton.setOnClickListener(view1 -> {
            prepareDefaultNode();
            ((MoneroApplication)getActivity().getApplication()).getExecutor().execute(() -> {
                createOrImportWallet(
                        walletPasswordEditText.getText().toString(),
                        walletSeedEditText.getText().toString().trim(),
                        walletRestoreHeightEditText.getText().toString().trim()
                );
            });
        });
        walletSeedEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String text = editable.toString();
                if (text.isEmpty()) {
                    createWalletButton.setText(R.string.create_wallet);
                } else {
                    createWalletButton.setText(R.string.menu_restore);
                }
            }
        });
        torSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            PrefService.getInstance().edit().putBoolean(Constants.PREF_USES_TOR, b).apply();
            if (b) {
                String proxyString = PrefService.getInstance().getString(Constants.PREF_PROXY, "");
                if (proxyString.contains(":")) {
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

            mViewModel.updateProxy(((MoneroApplication)getActivity().getApplication()));
        });

        mViewModel.showMoreOptions.observe(getViewLifecycleOwner(), show -> {
            if (show) {
                moreOptionsChevronImageView.setImageResource(R.drawable.ic_keyboard_arrow_up);
                walletSeedEditText.setVisibility(View.VISIBLE);
                walletRestoreHeightEditText.setVisibility(View.VISIBLE);
            } else {
                moreOptionsChevronImageView.setImageResource(R.drawable.ic_keyboard_arrow_down);
                walletSeedEditText.setVisibility(View.GONE);
                walletRestoreHeightEditText.setVisibility(View.GONE);
            }
        });
    }

    private void prepareDefaultNode() {
        boolean usesTor = PrefService.getInstance().getBoolean(Constants.PREF_USES_TOR, false);
        DefaultNodes defaultNode = usesTor ? DefaultNodes.SAMOURAI_ONION : DefaultNodes.SAMOURAI;
        PrefService.getInstance().edit().putString(Constants.PREF_NODE_2, defaultNode.getUri()).apply();
    }

    private void createOrImportWallet(String walletPassword, String walletSeed, String restoreHeightText) {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            if (!walletPassword.isEmpty()) {
                PrefService.getInstance().edit().putBoolean(Constants.PREF_USES_PASSWORD, true).apply();
            }
            long restoreHeight = -1;
            File walletFile = new File(mainActivity.getApplicationInfo().dataDir, Constants.WALLET_NAME);
            Wallet wallet = null;
            if (walletSeed.isEmpty()) {
                wallet = WalletManager.getInstance().createWallet(walletFile, walletPassword, Constants.MNEMONIC_LANGUAGE, restoreHeight);
            } else {
                if (!checkMnemonic(walletSeed)) {
                    Toast.makeText(mainActivity, getString(R.string.invalid_mnemonic_code), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!restoreHeightText.isEmpty()) {
                    restoreHeight = Long.parseLong(restoreHeightText);
                }
                wallet = WalletManager.getInstance().recoveryWallet(walletFile, walletPassword, walletSeed, "", restoreHeight);
            }
            Wallet.Status walletStatus = wallet.getStatus();
            wallet.close();
            boolean ok = walletStatus.isOk();
            walletFile.delete(); // cache is broken for some reason when recovering wallets. delete the file here. this happens in monerujo too.

            if (ok) {
                mainActivity.init(walletFile, walletPassword);
                mainActivity.runOnUiThread(mainActivity::onBackPressed);
            } else {
                mainActivity.runOnUiThread(() -> Toast.makeText(mainActivity, getString(R.string.create_wallet_failed, walletStatus.getErrorString()), Toast.LENGTH_SHORT).show());
            }
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

    private void initProxyStuff(String proxyAddress, String proxyPort) {
        boolean validIpAddress = Patterns.IP_ADDRESS.matcher(proxyAddress).matches();
        if (validIpAddress) {
            mViewModel.setProxyAddress(proxyAddress);
            mViewModel.setProxyPort(proxyPort);
            walletProxyAddressEditText.setText(proxyAddress);
            walletProxyPortEditText.setText(proxyPort);
        }
    }

    private boolean checkMnemonic(String seed) {
        return (seed.split("\\s").length == 25);
    }
}