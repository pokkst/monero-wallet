package com.m2049r.xmrwallet.fragment.home;

import android.os.Bundle;
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

import com.m2049r.xmrwallet.MainActivity;
import com.m2049r.xmrwallet.R;
import com.m2049r.xmrwallet.model.Wallet;
import com.m2049r.xmrwallet.service.AddressService;
import com.m2049r.xmrwallet.service.BalanceService;
import com.m2049r.xmrwallet.service.TxService;

public class HomeFragment extends Fragment {

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
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity == null) return;

        ImageView settingsImageView = view.findViewById(R.id.settings_imageview);
        ImageView addressImageView = view.findViewById(R.id.monero_qr_imageview);
        TextView addressTextView = view.findViewById(R.id.address_textview);
        TextView balanceTextView = view.findViewById(R.id.balance_textview);
        EditText addressEditText = view.findViewById(R.id.address_edittext);
        EditText amountEditText = view.findViewById(R.id.amount_edittext);
        Button sendButton = view.findViewById(R.id.send_button);

        AddressService.getInstance().address.observe(getViewLifecycleOwner(), addr -> {
            if (!addr.isEmpty()) {
                addressTextView.setText(addr);
                addressImageView.setImageBitmap(mViewModel.generate(addr, 256, 256));
            }
        });

        BalanceService.getInstance().balance.observe(getViewLifecycleOwner(), balance -> {
            balanceTextView.setText(getString(R.string.wallet_balance_text, Wallet.getDisplayAmount(balance)));
        });

        TxService.getInstance().clearSendEvent.observe(getViewLifecycleOwner(), o -> {
            addressEditText.setText(null);
            amountEditText.setText(null);
            sendButton.setEnabled(true);
        });

        settingsImageView.setOnClickListener(view12 -> {
            navigate(R.id.settings_fragment);
        });

        sendButton.setOnClickListener(view1 -> {
            String address = addressEditText.getText().toString().trim();
            String amount = amountEditText.getText().toString().trim();
            boolean validAddress = Wallet.isAddressValid(address);
            if (validAddress && !amount.isEmpty()) {
                sendButton.setEnabled(false);
                TxService.getInstance().sendTx(address, amount);
            } else if (!validAddress) {
                Toast.makeText(getActivity(), getString(R.string.send_address_invalid), Toast.LENGTH_SHORT).show();
            } else if (amount.isEmpty()) {
                Toast.makeText(getActivity(), getString(R.string.send_amount_empty), Toast.LENGTH_SHORT).show();
            }
        });
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