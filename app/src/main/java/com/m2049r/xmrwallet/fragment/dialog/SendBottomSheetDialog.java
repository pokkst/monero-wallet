package com.m2049r.xmrwallet.fragment.dialog;

import android.content.ClipboardManager;
import android.os.Bundle;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.m2049r.xmrwallet.R;
import com.m2049r.xmrwallet.model.Wallet;
import com.m2049r.xmrwallet.service.BalanceService;
import com.m2049r.xmrwallet.service.TxService;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

public class SendBottomSheetDialog extends BottomSheetDialogFragment {
    private MutableLiveData<Boolean> _sendingMax = new MutableLiveData<>(false);
    public LiveData<Boolean> sendingMax = _sendingMax;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.send_bottom_sheet_dialog, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageButton pasteAddressImageButton = view.findViewById(R.id.paste_address_imagebutton);
        Button sendMaxButton = view.findViewById(R.id.send_max_button);
        EditText addressEditText = view.findViewById(R.id.address_edittext);
        EditText amountEditText = view.findViewById(R.id.amount_edittext);
        Button sendButton = view.findViewById(R.id.send_button);
        TextView sendAllTextView = view.findViewById(R.id.sending_all_textview);

        TxService.getInstance().clearSendEvent.observe(getViewLifecycleOwner(), o -> {
            dismiss();
        });

        pasteAddressImageButton.setOnClickListener(view1 -> {

        });

        sendMaxButton.setOnClickListener(view1 -> {
            boolean currentValue = sendingMax.getValue() != null ? sendingMax.getValue() : false;
            _sendingMax.postValue(!currentValue);
        });

        sendButton.setOnClickListener(view1 -> {
            String address = addressEditText.getText().toString().trim();
            String amount = amountEditText.getText().toString().trim();
            boolean validAddress = Wallet.isAddressValid(address);
            if (validAddress && !amount.isEmpty()) {
                long amountRaw = Wallet.getAmountFromString(amount);
                long balance = BalanceService.getInstance().getUnlockedBalanceRaw();
                if(amountRaw >= balance || amountRaw <= 0) {
                    Toast.makeText(getActivity(), getString(R.string.send_amount_invalid), Toast.LENGTH_SHORT).show();
                }
                sendButton.setEnabled(false);
                boolean sendAll = sendingMax.getValue() != null ? sendingMax.getValue() : false;
                TxService.getInstance().sendTx(address, amount, sendAll);
            } else if (!validAddress) {
                Toast.makeText(getActivity(), getString(R.string.send_address_invalid), Toast.LENGTH_SHORT).show();
            } else if (amount.isEmpty()) {
                Toast.makeText(getActivity(), getString(R.string.send_amount_empty), Toast.LENGTH_SHORT).show();
            }
        });

        sendingMax.observe(getViewLifecycleOwner(), sendingMax -> {
            if(sendingMax) {
                amountEditText.setVisibility(View.INVISIBLE);
                sendAllTextView.setVisibility(View.VISIBLE);
                sendMaxButton.setText(getText(R.string.undo));
            } else {
                amountEditText.setVisibility(View.VISIBLE);
                sendAllTextView.setVisibility(View.GONE);
                sendMaxButton.setText(getText(R.string.send_max));
            }
        });
    }
}