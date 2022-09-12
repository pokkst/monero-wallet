package com.m2049r.xmrwallet.fragment.dialog;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.m2049r.xmrwallet.R;
import com.m2049r.xmrwallet.model.PendingTransaction;
import com.m2049r.xmrwallet.model.Wallet;
import com.m2049r.xmrwallet.service.BalanceService;
import com.m2049r.xmrwallet.service.TxService;
import com.m2049r.xmrwallet.util.Helper;

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

public class SendBottomSheetDialog extends BottomSheetDialogFragment {
    private MutableLiveData<Boolean> _sendingMax = new MutableLiveData<>(false);
    public LiveData<Boolean> sendingMax = _sendingMax;
    private MutableLiveData<PendingTransaction> _pendingTransaction = new MutableLiveData<>(null);
    public LiveData<PendingTransaction> pendingTransaction = _pendingTransaction;

    private EditText addressEditText;
    private EditText amountEditText;
    private TextView sendAllTextView;
    private TextView feeTextView;
    private TextView addressTextView;
    private TextView amountTextView;
    private Button createButton;
    private Button sendButton;
    private Button sendMaxButton;
    private ImageButton pasteAddressImageButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.send_bottom_sheet_dialog, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pasteAddressImageButton = view.findViewById(R.id.paste_address_imagebutton);
        sendMaxButton = view.findViewById(R.id.send_max_button);
        addressEditText = view.findViewById(R.id.address_edittext);
        amountEditText = view.findViewById(R.id.amount_edittext);
        sendButton = view.findViewById(R.id.send_tx_button);
        createButton = view.findViewById(R.id.create_tx_button);
        sendAllTextView = view.findViewById(R.id.sending_all_textview);
        feeTextView = view.findViewById(R.id.fee_textview);
        addressTextView = view.findViewById(R.id.address_pending_textview);
        amountTextView = view.findViewById(R.id.amount_pending_textview);

        pasteAddressImageButton.setOnClickListener(view1 -> {
            addressEditText.setText(Helper.getClipBoardText(view.getContext()));
        });

        sendMaxButton.setOnClickListener(view1 -> {
            boolean currentValue = sendingMax.getValue() != null ? sendingMax.getValue() : false;
            _sendingMax.postValue(!currentValue);
        });

        createButton.setOnClickListener(view1 -> {
            boolean sendAll = sendingMax.getValue() != null ? sendingMax.getValue() : false;
            String address = addressEditText.getText().toString().trim();
            String amount = amountEditText.getText().toString().trim();
            boolean validAddress = Wallet.isAddressValid(address);
            if (validAddress && (!amount.isEmpty() || sendAll)) {
                long amountRaw = Wallet.getAmountFromString(amount);
                long balance = BalanceService.getInstance().getUnlockedBalanceRaw();
                if((amountRaw >= balance || amountRaw <= 0) && !sendAll) {
                    Toast.makeText(getActivity(), getString(R.string.send_amount_invalid), Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(getActivity(), getString(R.string.creating_tx), Toast.LENGTH_SHORT).show();
                createButton.setEnabled(false);
                createTx(address, amount, sendAll);
            } else if (!validAddress) {
                Toast.makeText(getActivity(), getString(R.string.send_address_invalid), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), getString(R.string.send_amount_empty), Toast.LENGTH_SHORT).show();
            }
        });

        sendButton.setOnClickListener(view1 -> {
            PendingTransaction pendingTx = pendingTransaction.getValue();
            if(pendingTx != null) {
                Toast.makeText(getActivity(), getString(R.string.sending_tx), Toast.LENGTH_SHORT).show();
                sendButton.setEnabled(false);
                sendTx(pendingTx);
            }
        });

        sendingMax.observe(getViewLifecycleOwner(), sendingMax -> {
            if(pendingTransaction.getValue() == null) {
                if (sendingMax) {
                    amountEditText.setVisibility(View.INVISIBLE);
                    sendAllTextView.setVisibility(View.VISIBLE);
                    sendMaxButton.setText(getText(R.string.undo));
                } else {
                    amountEditText.setVisibility(View.VISIBLE);
                    sendAllTextView.setVisibility(View.GONE);
                    sendMaxButton.setText(getText(R.string.send_max));
                }
            }
        });

        pendingTransaction.observe(getViewLifecycleOwner(), pendingTx -> {
            showConfirmationLayout(pendingTx != null);

            if(pendingTx != null) {
                String address = addressEditText.getText().toString();
                addressTextView.setText(getString(R.string.tx_address_text, address));
                amountTextView.setText(getString(R.string.tx_amount_text, Helper.getDisplayAmount(pendingTx.getAmount())));
                feeTextView.setText(getString(R.string.tx_fee_text, Helper.getDisplayAmount(pendingTx.getFee())));
            }
        });
    }

    private void sendTx(PendingTransaction pendingTx) {
        AsyncTask.execute(() -> {
            boolean success = TxService.getInstance().sendTx(pendingTx);
            Activity activity = getActivity();
            if(activity != null) {
                activity.runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(getActivity(), getString(R.string.sent_tx), Toast.LENGTH_SHORT).show();
                        dismiss();
                    } else {
                        sendButton.setEnabled(true);
                        Toast.makeText(getActivity(), getString(R.string.error_sending_tx), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void createTx(String address, String amount, boolean sendAll) {
        AsyncTask.execute(() -> {
            PendingTransaction pendingTx = TxService.getInstance().createTx(address, amount, sendAll);
            if(pendingTx != null) {
                _pendingTransaction.postValue(pendingTx);
            } else {
                Activity activity = getActivity();
                if(activity != null) {
                    activity.runOnUiThread(() -> {
                        createButton.setEnabled(true);
                        Toast.makeText(getActivity(), getString(R.string.error_creating_tx), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void showConfirmationLayout(boolean show) {
        if(show) {
            sendButton.setVisibility(View.VISIBLE);
            addressEditText.setVisibility(View.GONE);
            amountEditText.setVisibility(View.GONE);
            sendAllTextView.setVisibility(View.GONE);
            createButton.setVisibility(View.GONE);
            sendMaxButton.setVisibility(View.GONE);
            pasteAddressImageButton.setVisibility(View.GONE);
            feeTextView.setVisibility(View.VISIBLE);
            addressTextView.setVisibility(View.VISIBLE);
            amountTextView.setVisibility(View.VISIBLE);
        } else {
            sendButton.setVisibility(View.GONE);
            addressEditText.setVisibility(View.VISIBLE);
            amountEditText.setVisibility(Boolean.TRUE.equals(sendingMax.getValue()) ? View.GONE : View.VISIBLE);
            sendAllTextView.setVisibility(Boolean.TRUE.equals(sendingMax.getValue()) ? View.VISIBLE : View.GONE);
            createButton.setVisibility(View.VISIBLE);
            sendMaxButton.setVisibility(View.VISIBLE);
            pasteAddressImageButton.setVisibility(View.VISIBLE);
            feeTextView.setVisibility(View.GONE);
            addressTextView.setVisibility(View.GONE);
            amountTextView.setVisibility(View.GONE);
        }
    }
}