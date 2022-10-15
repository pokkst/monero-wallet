package net.mynero.wallet.fragment.settings;

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
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import net.mynero.wallet.MoneroApplication;
import net.mynero.wallet.R;
import net.mynero.wallet.data.DefaultNodes;
import net.mynero.wallet.data.Node;
import net.mynero.wallet.fragment.dialog.AddNodeBottomSheetDialog;
import net.mynero.wallet.fragment.dialog.EditNodeBottomSheetDialog;
import net.mynero.wallet.fragment.dialog.WalletKeysBottomSheetDialog;
import net.mynero.wallet.fragment.dialog.NodeSelectionBottomSheetDialog;
import net.mynero.wallet.fragment.dialog.PasswordBottomSheetDialog;
import net.mynero.wallet.model.Wallet;
import net.mynero.wallet.model.WalletManager;
import net.mynero.wallet.service.BlockchainService;
import net.mynero.wallet.service.PrefService;
import net.mynero.wallet.util.Constants;
import net.mynero.wallet.util.DayNightMode;
import net.mynero.wallet.util.NightmodeHelper;

import org.json.JSONArray;

public class SettingsFragment extends Fragment implements PasswordBottomSheetDialog.PasswordListener, NodeSelectionBottomSheetDialog.NodeSelectionDialogListener, AddNodeBottomSheetDialog.AddNodeListener, EditNodeBottomSheetDialog.EditNodeListener {

    private SettingsViewModel mViewModel;
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
        Button displayUtxosButton = view.findViewById(R.id.display_utxos_button);

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
        if (proxy.contains(":")) {
            String proxyAddress = proxy.split(":")[0];
            String proxyPort = proxy.split(":")[1];
            initProxyStuff(proxyAddress, proxyPort);
        }
        torSwitch.setChecked(usesProxy);
        if (usesProxy) {
            proxySettingsLayout.setVisibility(View.VISIBLE);
        } else {
            proxySettingsLayout.setVisibility(View.GONE);
        }

        addProxyTextListeners();

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

        displaySeedButton.setOnClickListener(view1 -> {
            boolean usesPassword = PrefService.getInstance().getBoolean(Constants.PREF_USES_PASSWORD, false);
            if (usesPassword) {
                PasswordBottomSheetDialog passwordDialog = new PasswordBottomSheetDialog();
                passwordDialog.cancelable = true;
                passwordDialog.listener = this;
                passwordDialog.show(getActivity().getSupportFragmentManager(), "password_dialog");
            } else {
                displaySeedDialog();
            }
        });

        displayUtxosButton.setOnClickListener(view1 -> {
            navigate(R.id.nav_to_utxos);
        });

        TextView statusTextView = view.findViewById(R.id.status_textview);
        BlockchainService.getInstance().connectionStatus.observe(getViewLifecycleOwner(), connectionStatus -> {
            if (connectionStatus == Wallet.ConnectionStatus.ConnectionStatus_Connected) {
                statusTextView.setText(getResources().getText(R.string.connected));
            } else if (connectionStatus == Wallet.ConnectionStatus.ConnectionStatus_Disconnected) {
                statusTextView.setText(getResources().getText(R.string.disconnected));
            } else if (connectionStatus == Wallet.ConnectionStatus.ConnectionStatus_WrongVersion) {
                statusTextView.setText(getResources().getText(R.string.version_mismatch));
            }
        });
        DefaultNodes defaultNode = usesProxy ? DefaultNodes.SAMOURAI_ONION : DefaultNodes.SAMOURAI;
        Node node = Node.fromString(PrefService.getInstance().getString(Constants.PREF_NODE_2, defaultNode.getUri())); // shouldn't use default value here
        selectNodeButton.setText(getString(R.string.node_button_text, node.getAddress()));
        selectNodeButton.setOnClickListener(view1 -> {
            NodeSelectionBottomSheetDialog dialog = new NodeSelectionBottomSheetDialog();
            dialog.listener = this;
            dialog.show(getActivity().getSupportFragmentManager(), "node_selection_dialog");
        });
    }

    private void displaySeedDialog() {
        WalletKeysBottomSheetDialog informationDialog = new WalletKeysBottomSheetDialog();
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
        if (validIpAddress) {
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
        Node node = Node.fromString(PrefService.getInstance().getString(Constants.PREF_NODE_2, ""));
        selectNodeButton.setText(getString(R.string.node_button_text, node.getAddress()));
        mViewModel.updateProxy(((MoneroApplication)getActivity().getApplication()));
        ((MoneroApplication)getActivity().getApplication()).getExecutor().execute(() -> {
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
    public void onClickedEditNode(String nodeString) {
        EditNodeBottomSheetDialog editNodeDialog = new EditNodeBottomSheetDialog();
        editNodeDialog.listener = this;
        editNodeDialog.nodeString = nodeString;
        editNodeDialog.show(getActivity().getSupportFragmentManager(), "edit_node_dialog");
    }

    @Override
    public void onNodeAdded() {
        NodeSelectionBottomSheetDialog dialog = new NodeSelectionBottomSheetDialog();
        dialog.listener = this;
        dialog.show(getActivity().getSupportFragmentManager(), "node_selection_dialog");
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

    @Override
    public void onNodeDeleted(Node node) {
        try {
            String nodesArray = PrefService.getInstance().getString(Constants.PREF_CUSTOM_NODES, "[]");
            JSONArray jsonArray = new JSONArray(nodesArray);
            for (int i = 0; i < jsonArray.length(); i++) {
                String jsonNodeString = jsonArray.getString(i);
                Node savedNode = Node.fromString(jsonNodeString);
                if (savedNode.toNodeString().equals(node.toNodeString()))
                    jsonArray.remove(i);
            }
            saveNodesAndReopen(jsonArray);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNodeEdited(Node oldNode, Node newNode) {
        try {
            String nodesArray = PrefService.getInstance().getString(Constants.PREF_CUSTOM_NODES, "[]");
            JSONArray jsonArray = new JSONArray(nodesArray);
            for (int i = 0; i < jsonArray.length(); i++) {
                String jsonNodeString = jsonArray.getString(i);
                Node savedNode = Node.fromString(jsonNodeString);
                if (savedNode.toNodeString().equals(oldNode.toNodeString()))
                    jsonArray.put(i, newNode.toNodeString());
            }
            saveNodesAndReopen(jsonArray);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveNodesAndReopen(JSONArray jsonArray) {
        PrefService.getInstance().edit().putString(Constants.PREF_CUSTOM_NODES, jsonArray.toString()).apply();
        onNodeAdded();
    }
}