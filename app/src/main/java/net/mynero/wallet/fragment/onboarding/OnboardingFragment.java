package net.mynero.wallet.fragment.onboarding;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import net.mynero.wallet.MainActivity;
import net.mynero.wallet.R;
import net.mynero.wallet.model.Wallet;
import net.mynero.wallet.model.WalletManager;
import net.mynero.wallet.service.PrefService;
import net.mynero.wallet.util.Constants;

import java.io.File;

public class OnboardingFragment extends Fragment {

    private OnboardingViewModel mViewModel;

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

        moreOptionsDropdownTextView.setOnClickListener(view12 -> mViewModel.onMoreOptionsClicked());
        moreOptionsChevronImageView.setOnClickListener(view12 -> mViewModel.onMoreOptionsClicked());

        createWalletButton.setOnClickListener(view1 -> {
            AsyncTask.execute(() -> {
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

    private void createOrImportWallet(String walletPassword, String walletSeed, String restoreHeightText) {
        MainActivity mainActivity = (MainActivity) getActivity();
        if(mainActivity != null) {
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
            boolean ok = walletStatus.isOk();
            walletFile.delete(); // cache is broken for some reason when recovering wallets. delete the file here. this happens in monerujo too.

            if (ok) {
                mainActivity.init(walletFile, walletPassword);
                mainActivity.runOnUiThread(mainActivity::onBackPressed);
            } else {
                Toast.makeText(mainActivity, getString(R.string.create_wallet_failed, walletStatus.getErrorString()), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean checkMnemonic(String seed) {
        return (seed.split("\\s").length == 25);
    }

    private void navigate(int destination) {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            FragmentManager fm = activity.getSupportFragmentManager();
            NavHostFragment navHostFragment =
                    (NavHostFragment) fm.findFragmentById(R.id.nav_host_fragment);
            if (navHostFragment != null) {
                navHostFragment.getNavController().navigate(destination);
            }
        }
    }
}