package com.m2049r.xmrwallet.fragment.dialog;

import android.os.Bundle;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.m2049r.xmrwallet.R;
import com.m2049r.xmrwallet.model.Wallet;
import com.m2049r.xmrwallet.service.TxService;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SendBottomSheetDialog extends BottomSheetDialogFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.send_bottom_sheet_dialog, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EditText addressEditText = view.findViewById(R.id.address_edittext);
        EditText amountEditText = view.findViewById(R.id.amount_edittext);
        Button sendButton = view.findViewById(R.id.send_button);

        TxService.getInstance().clearSendEvent.observe(getViewLifecycleOwner(), o -> {
            dismiss();
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
}