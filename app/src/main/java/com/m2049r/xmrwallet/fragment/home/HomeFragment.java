package com.m2049r.xmrwallet.fragment.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.m2049r.xmrwallet.MainActivity;
import com.m2049r.xmrwallet.R;
import com.m2049r.xmrwallet.adapter.TransactionInfoAdapter;
import com.m2049r.xmrwallet.fragment.dialog.ReceiveBottomSheetDialog;
import com.m2049r.xmrwallet.fragment.dialog.SendBottomSheetDialog;
import com.m2049r.xmrwallet.model.TransactionInfo;
import com.m2049r.xmrwallet.model.Wallet;
import com.m2049r.xmrwallet.service.AddressService;
import com.m2049r.xmrwallet.service.BalanceService;
import com.m2049r.xmrwallet.service.HistoryService;
import com.m2049r.xmrwallet.service.TxService;

import java.util.Collections;

public class HomeFragment extends Fragment implements TransactionInfoAdapter.TxInfoAdapterListener {

    private HomeViewModel mViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        bindObservers(view);
        bindListeners(view);
    }

    private void bindListeners(View view) {
        ImageView settingsImageView = view.findViewById(R.id.settings_imageview);
        Button sendButton = view.findViewById(R.id.send_button);
        Button receiveButton = view.findViewById(R.id.receive_button);

        settingsImageView.setOnClickListener(view12 -> {
            navigate(R.id.settings_fragment);
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

        BalanceService.getInstance().balance.observe(getViewLifecycleOwner(), balance -> {
            unlockedBalanceTextView.setText(getString(R.string.wallet_balance_text, Wallet.getDisplayAmount(balance)));
        });

        BalanceService.getInstance().lockedBalance.observe(getViewLifecycleOwner(), lockedBalance -> {
            if(lockedBalance == 0) {
                lockedBalanceTextView.setVisibility(View.INVISIBLE);
            } else {
                lockedBalanceTextView.setText(getString(R.string.wallet_locked_balance_text, Wallet.getDisplayAmount(lockedBalance)));
                lockedBalanceTextView.setVisibility(View.VISIBLE);
            }
        });

        TransactionInfoAdapter adapter = new TransactionInfoAdapter(this);
        txHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        txHistoryRecyclerView.setAdapter(adapter);
        HistoryService.getInstance().history.observe(getViewLifecycleOwner(), history -> {
            if(history.isEmpty()) {
                txHistoryRecyclerView.setVisibility(View.GONE);
            } else {
                Collections.sort(history);
                adapter.submitList(history);
                txHistoryRecyclerView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onClickTransaction(TransactionInfo txInfo) {
        System.out.println(txInfo.hash);
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