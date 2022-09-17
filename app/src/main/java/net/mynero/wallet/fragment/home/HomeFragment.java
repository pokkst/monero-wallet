package net.mynero.wallet.fragment.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.mynero.wallet.MainActivity;
import net.mynero.wallet.R;
import net.mynero.wallet.adapter.TransactionInfoAdapter;
import net.mynero.wallet.fragment.dialog.ReceiveBottomSheetDialog;
import net.mynero.wallet.fragment.dialog.SendBottomSheetDialog;
import net.mynero.wallet.model.TransactionInfo;
import net.mynero.wallet.model.Wallet;
import net.mynero.wallet.model.WalletManager;
import net.mynero.wallet.service.BalanceService;
import net.mynero.wallet.service.BlockchainService;
import net.mynero.wallet.service.HistoryService;

import java.util.Collections;

public class HomeFragment extends Fragment implements TransactionInfoAdapter.TxInfoAdapterListener {

    long startHeight = 0;
    private HomeViewModel mViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity mainActivity = (MainActivity) getActivity();
        mViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        bindObservers(view);
        bindListeners(view);

        mainActivity.restartEvents.observe(getViewLifecycleOwner(), o -> {
            bindObservers(view);
            bindListeners(view);
        });
    }

    private void bindListeners(View view) {
        ImageView settingsImageView = view.findViewById(R.id.settings_imageview);
        Button sendButton = view.findViewById(R.id.send_button);
        Button receiveButton = view.findViewById(R.id.receive_button);

        settingsImageView.setOnClickListener(view12 -> {
            navigate(HomeFragmentDirections.navToSettings());
        });

        sendButton.setOnClickListener(view1 -> {
            SendBottomSheetDialog sendDialog = new SendBottomSheetDialog();
            sendDialog.show(getActivity().getSupportFragmentManager(), null);
        });

        receiveButton.setOnClickListener(view1 -> {
            ReceiveBottomSheetDialog receiveDialog = new ReceiveBottomSheetDialog();
            receiveDialog.show(getActivity().getSupportFragmentManager(), null);
        });
    }

    private void bindObservers(View view) {
        RecyclerView txHistoryRecyclerView = view.findViewById(R.id.transaction_history_recyclerview);
        TextView unlockedBalanceTextView = view.findViewById(R.id.balance_unlocked_textview);
        TextView lockedBalanceTextView = view.findViewById(R.id.balance_locked_textview);
        ConstraintLayout noHistoryLayout = view.findViewById(R.id.no_history_layout);

        BalanceService balanceService = BalanceService.getInstance();
        HistoryService historyService = HistoryService.getInstance();
        BlockchainService blockchainService = BlockchainService.getInstance();

        if (balanceService != null) {
            balanceService.balance.observe(getViewLifecycleOwner(), balance -> {
                unlockedBalanceTextView.setText(getString(R.string.wallet_balance_text, Wallet.getDisplayAmount(balance)));
            });

            balanceService.lockedBalance.observe(getViewLifecycleOwner(), lockedBalance -> {
                if (lockedBalance == 0) {
                    lockedBalanceTextView.setVisibility(View.INVISIBLE);
                } else {
                    lockedBalanceTextView.setText(getString(R.string.wallet_locked_balance_text, Wallet.getDisplayAmount(lockedBalance)));
                    lockedBalanceTextView.setVisibility(View.VISIBLE);
                }
            });
        }

        ProgressBar progressBar = view.findViewById(R.id.sync_progress_bar);
        if (blockchainService != null) {
            blockchainService.height.observe(getViewLifecycleOwner(), height -> {
                Wallet wallet = WalletManager.getInstance().getWallet();
                if (!wallet.isSynchronized()) {
                    if (startHeight == 0 && height != 1) {
                        startHeight = height;
                    }
                    long daemonHeight = blockchainService.getDaemonHeight();
                    long n = daemonHeight - height;
                    int x = 100 - Math.round(100f * n / (1f * daemonHeight - startHeight));
                    progressBar.setIndeterminate(height <= 1 || daemonHeight <= 0);
                    if (height > 1 && daemonHeight > 1) {
                        progressBar.setProgress(x);
                    }
                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                }
            });
        }

        TransactionInfoAdapter adapter = new TransactionInfoAdapter(this);
        txHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        txHistoryRecyclerView.setAdapter(adapter);
        if (historyService != null) {
            historyService.history.observe(getViewLifecycleOwner(), history -> {
                if (history.isEmpty()) {
                    txHistoryRecyclerView.setVisibility(View.GONE);
                    noHistoryLayout.setVisibility(View.VISIBLE);
                } else {
                    Collections.sort(history);
                    if (history.size() > 100) {
                        adapter.submitList(history.subList(0, 99));
                    } else {
                        adapter.submitList(history);
                    }
                    txHistoryRecyclerView.setVisibility(View.VISIBLE);
                    noHistoryLayout.setVisibility(View.GONE);
                }
            });
        }
    }

    @Override
    public void onClickTransaction(TransactionInfo txInfo) {
        NavDirections directions = HomeFragmentDirections.navToTransaction(txInfo);
        navigate(directions);
    }

    private void navigate(NavDirections destination) {
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