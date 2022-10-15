package net.mynero.wallet;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.fragment.NavHostFragment;

import net.mynero.wallet.data.DefaultNodes;
import net.mynero.wallet.data.Node;
import net.mynero.wallet.fragment.dialog.PasswordBottomSheetDialog;
import net.mynero.wallet.fragment.dialog.SendBottomSheetDialog;
import net.mynero.wallet.livedata.SingleLiveEvent;
import net.mynero.wallet.model.Wallet;
import net.mynero.wallet.model.WalletManager;
import net.mynero.wallet.service.AddressService;
import net.mynero.wallet.service.BalanceService;
import net.mynero.wallet.service.BlockchainService;
import net.mynero.wallet.service.HistoryService;
import net.mynero.wallet.service.MoneroHandlerThread;
import net.mynero.wallet.service.PrefService;
import net.mynero.wallet.service.TxService;
import net.mynero.wallet.service.UTXOService;
import net.mynero.wallet.util.Constants;
import net.mynero.wallet.util.UriData;

import org.json.JSONArray;

import java.io.File;

public class MainActivity extends AppCompatActivity implements MoneroHandlerThread.Listener, PasswordBottomSheetDialog.PasswordListener {
    public final SingleLiveEvent restartEvents = new SingleLiveEvent();
    private MoneroHandlerThread thread = null;
    private BalanceService balanceService = null;
    private AddressService addressService = null;
    private HistoryService historyService = null;
    private BlockchainService blockchainService = null;
    private UTXOService utxoService = null;

    private boolean proceedToSend = false;
    private UriData uriData = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        File walletFile = new File(getApplicationInfo().dataDir, Constants.WALLET_NAME);
        File walletKeysFile = new File(getApplicationInfo().dataDir, Constants.WALLET_NAME + ".keys");
        if (walletKeysFile.exists()) {
            boolean promptPassword = PrefService.getInstance().getBoolean(Constants.PREF_USES_PASSWORD, false);
            if (!promptPassword) {
                init(walletFile, "");
            } else {
                PasswordBottomSheetDialog passwordDialog = new PasswordBottomSheetDialog();
                passwordDialog.listener = this;
                passwordDialog.show(getSupportFragmentManager(), "password_dialog");
            }

            Intent intent = getIntent();
            Uri uri = intent.getData();
            if (uri != null) {
                uriData = UriData.parse(uri.toString());
                if (uriData != null) {
                    proceedToSend = true;
                }
            }
        } else {
            navigate(R.id.onboarding_fragment);
        }
    }

    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
    }

    private void navigate(int destination) {
        FragmentActivity activity = this;
        FragmentManager fm = activity.getSupportFragmentManager();
        NavHostFragment navHostFragment =
                (NavHostFragment) fm.findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navHostFragment.getNavController().navigate(destination);
        }
    }

    public MoneroHandlerThread getThread() {
        return thread;
    }

    public void init(File walletFile, String password) {
        upgradeOldNodePrefs();
        Wallet wallet = WalletManager.getInstance().openWallet(walletFile.getAbsolutePath(), password);
        thread = new MoneroHandlerThread("WalletService", this, wallet);
        new TxService(thread);
        this.balanceService = new BalanceService(thread);
        this.addressService = new AddressService(thread);
        this.historyService = new HistoryService(thread);
        this.blockchainService = new BlockchainService(thread);
        this.utxoService = new UTXOService(thread);
        thread.start();
    }

    private void upgradeOldNodePrefs() {
        try {
            String oldNodeString = PrefService.getInstance().getString("pref_node", "");
            String nodeString = "";
            if (!oldNodeString.isEmpty()) {
                String nodesArray = PrefService.getInstance().getString(Constants.PREF_CUSTOM_NODES, "[]");
                JSONArray jsonArray = new JSONArray(nodesArray);
                for (int i = 0; i < jsonArray.length(); i++) {
                    String jsonNodeString = jsonArray.getString(i);
                    Node savedNode = Node.fromString(jsonNodeString);
                    if(savedNode != null) {
                        if (savedNode.getAddress().equals(oldNodeString)) {
                            nodeString = savedNode.toNodeString();
                            break;
                        }
                    }
                }
                if(nodeString.isEmpty()) {
                    for (DefaultNodes defaultNode : DefaultNodes.values()) {
                        Node node = Node.fromString(defaultNode.getUri());
                        if(node != null) {
                            if(node.getAddress().equals(oldNodeString)) {
                                nodeString = node.toNodeString();
                                break;
                            }
                        }
                    }
                }
                if(!nodeString.isEmpty()) {
                    Node oldNode = Node.fromString(nodeString);
                    if (oldNode != null) {
                        PrefService.getInstance().edit().putString(Constants.PREF_NODE_2, oldNode.toNodeString()).apply();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRefresh() {
        this.historyService.refreshHistory();
        this.balanceService.refreshBalance();
        this.blockchainService.refreshBlockchain();
        this.addressService.refreshAddresses();
        this.utxoService.refreshUtxos();
    }

    @Override
    public void onConnectionFail() {
        runOnUiThread(() -> Toast.makeText(getApplication(), R.string.connection_failed, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onPasswordSuccess(String password) {
        File walletFile = new File(getApplicationInfo().dataDir, Constants.WALLET_NAME);
        init(walletFile, password);
        restartEvents.call();

        if (proceedToSend) {
            SendBottomSheetDialog sendDialog = new SendBottomSheetDialog();
            sendDialog.uriData = uriData;
            sendDialog.show(getSupportFragmentManager(), null);
        }
    }

    @Override
    public void onPasswordFail() {
        runOnUiThread(() -> Toast.makeText(getApplication(), R.string.bad_password, Toast.LENGTH_SHORT).show());
    }
}