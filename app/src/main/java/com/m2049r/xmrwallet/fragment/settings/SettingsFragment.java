package com.m2049r.xmrwallet.fragment.settings;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.m2049r.xmrwallet.R;
import com.m2049r.xmrwallet.data.DefaultNodes;
import com.m2049r.xmrwallet.data.Node;
import com.m2049r.xmrwallet.fragment.dialog.AddNodeBottomSheetDialog;
import com.m2049r.xmrwallet.fragment.dialog.InformationBottomSheetDialog;
import com.m2049r.xmrwallet.fragment.dialog.NodeSelectionBottomSheetDialog;
import com.m2049r.xmrwallet.fragment.dialog.PasswordBottomSheetDialog;
import com.m2049r.xmrwallet.model.Wallet;
import com.m2049r.xmrwallet.model.WalletManager;
import com.m2049r.xmrwallet.service.BlockchainService;
import com.m2049r.xmrwallet.service.PrefService;
import com.m2049r.xmrwallet.util.Constants;
import com.m2049r.xmrwallet.util.DayNightMode;
import com.m2049r.xmrwallet.util.NightmodeHelper;

public class SettingsFragment extends Fragment implements PasswordBottomSheetDialog.PasswordListener, NodeSelectionBottomSheetDialog.NodeSelectionDialogListener, AddNodeBottomSheetDialog.AddNodeListener {

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
    private Button selectNodeButton;

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
        selectNodeButton = view.findViewById(R.id.select_node_button);
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

        TextView statusTextView = view.findViewById(R.id.status_textview);
        BlockchainService.getInstance().connectionStatus.observe(getViewLifecycleOwner(), connectionStatus -> {
            if(connectionStatus == Wallet.ConnectionStatus.ConnectionStatus_Connected) {
                statusTextView.setText(getResources().getText(R.string.connected));
            } else if(connectionStatus == Wallet.ConnectionStatus.ConnectionStatus_Disconnected) {
                statusTextView.setText(getResources().getText(R.string.disconnected));
            } else if(connectionStatus == Wallet.ConnectionStatus.ConnectionStatus_WrongVersion) {
                statusTextView.setText(getResources().getText(R.string.version_mismatch));
            }
        });
        Node node = Node.fromString(PrefService.getInstance().getString(Constants.PREF_NODE, DefaultNodes.XMRTW.getAddress()));
        selectNodeButton.setText(getString(R.string.node_button_text, node.getAddress()));
        selectNodeButton.setOnClickListener(view1 -> {
            NodeSelectionBottomSheetDialog dialog = new NodeSelectionBottomSheetDialog();
            dialog.listener = this;
            dialog.show(getActivity().getSupportFragmentManager(), "node_selection_dialog");
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

    @Override
    public void onNodeSelected() {
        Node node = Node.fromString(PrefService.getInstance().getString(Constants.PREF_NODE, DefaultNodes.XMRTW.getAddress()));
        selectNodeButton.setText(getString(R.string.node_button_text, node.getAddress()));
        mViewModel.updateProxy();
        AsyncTask.execute(() -> {
            WalletManager.getInstance().getWallet().init(0);
            WalletManager.getInstance().getWallet().startRefresh();
        });
    }

    @Override
    public void onClickedAddNode() {
        AddNodeBottomSheetDialog addNodeDialog = new AddNodeBottomSheetDialog();
        addNodeDialog.listener = this;
        addNodeDialog.show(getActivity().getSupportFragmentManager(), "add_node_dialog");
    }

    @Override
    public void onNodeAdded() {
        NodeSelectionBottomSheetDialog dialog = new NodeSelectionBottomSheetDialog();
        dialog.listener = this;
        dialog.show(getActivity().getSupportFragmentManager(), "node_selection_dialog");
    }
}