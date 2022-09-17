package com.m2049r.xmrwallet.fragment.dialog;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.zxing.client.android.Intents;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.m2049r.xmrwallet.R;
import com.m2049r.xmrwallet.model.PendingTransaction;
import com.m2049r.xmrwallet.model.Wallet;
import com.m2049r.xmrwallet.service.BalanceService;
import com.m2049r.xmrwallet.service.TxService;
import com.m2049r.xmrwallet.util.Helper;

import java.util.List;

public class SendBottomSheetDialog extends BottomSheetDialogFragment {
    private final MutableLiveData<Boolean> _sendingMax = new MutableLiveData<>(false);
    public LiveData<Boolean> sendingMax = _sendingMax;    private final ActivityResultLauncher<String> cameraPermissionsLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
            granted -> {
                if (granted) {
                    onScan();
                } else {
                    Toast.makeText(getActivity(), getString(R.string.no_camera_permission), Toast.LENGTH_SHORT).show();
                }
            });
    private final MutableLiveData<PendingTransaction> _pendingTransaction = new MutableLiveData<>(null);
    public LiveData<PendingTransaction> pendingTransaction = _pendingTransaction;
    private EditText addressEditText;
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if (result.getContents() != null) {
                    pasteAddress(result.getContents());
                }
            });
    private EditText amountEditText;
    private TextView sendAllTextView;
    private TextView feeTextView;
    private TextView addressTextView;
    private TextView amountTextView;
    private Button createButton;
    private Button sendButton;
    private Button sendMaxButton;
    private ImageButton pasteAddressImageButton;
    private ImageButton scanAddressImageButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.send_bottom_sheet_dialog, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pasteAddressImageButton = view.findViewById(R.id.paste_address_imagebutton);
        scanAddressImageButton = view.findViewById(R.id.scan_address_imagebutton);
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
            Context ctx = getContext();
            if (ctx != null) {
                String clipboard = Helper.getClipBoardText(ctx);
                if (clipboard != null) {
                    pasteAddress(clipboard);
                }
            }
        });

        scanAddressImageButton.setOnClickListener(view1 -> {
            onScan();
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
                if ((amountRaw >= balance || amountRaw <= 0) && !sendAll) {
                    Toast.makeText(getActivity(), getString(R.string.send_amount_invalid), Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(getActivity(), getString(R.string.creating_tx), Toast.LENGTH_SHORT).show();
                createButton.setEnabled(false);
                createTx(address, amount, sendAll, PendingTransaction.Priority.Priority_Default);
            } else if (!validAddress) {
                Toast.makeText(getActivity(), getString(R.string.send_address_invalid), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), getString(R.string.send_amount_empty), Toast.LENGTH_SHORT).show();
            }
        });

        sendButton.setOnClickListener(view1 -> {
            PendingTransaction pendingTx = pendingTransaction.getValue();
            if (pendingTx != null) {
                Toast.makeText(getActivity(), getString(R.string.sending_tx), Toast.LENGTH_SHORT).show();
                sendButton.setEnabled(false);
                sendTx(pendingTx);
            }
        });

        sendingMax.observe(getViewLifecycleOwner(), sendingMax -> {
            if (pendingTransaction.getValue() == null) {
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

            if (pendingTx != null) {
                String address = addressEditText.getText().toString();
                addressTextView.setText(getString(R.string.tx_address_text, address));
                amountTextView.setText(getString(R.string.tx_amount_text, Helper.getDisplayAmount(pendingTx.getAmount())));
                feeTextView.setText(getString(R.string.tx_fee_text, Helper.getDisplayAmount(pendingTx.getFee())));
            }
        });
    }

    private void onScan() {
        if (Helper.getCameraPermission(getActivity(), cameraPermissionsLauncher)) {
            ScanOptions options = new ScanOptions();
            options.setBeepEnabled(false);
            options.setOrientationLocked(true);
            options.setDesiredBarcodeFormats(List.of(Intents.Scan.QR_CODE_MODE));
            options.addExtra(Intents.Scan.SCAN_TYPE, Intents.Scan.MIXED_SCAN);
            barcodeLauncher.launch(options);
        }
    }

    private void sendTx(PendingTransaction pendingTx) {
        AsyncTask.execute(() -> {
            boolean success = TxService.getInstance().sendTx(pendingTx);
            Activity activity = getActivity();
            if (activity != null) {
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

    private void createTx(String address, String amount, boolean sendAll, PendingTransaction.Priority feePriority) {
        AsyncTask.execute(() -> {
            PendingTransaction pendingTx = TxService.getInstance().createTx(address, amount, sendAll, feePriority);
            if (pendingTx != null) {
                _pendingTransaction.postValue(pendingTx);
            } else {
                Activity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(() -> {
                        createButton.setEnabled(true);
                        Toast.makeText(getActivity(), getString(R.string.error_creating_tx), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void showConfirmationLayout(boolean show) {
        if (show) {
            sendButton.setVisibility(View.VISIBLE);
            addressEditText.setVisibility(View.GONE);
            amountEditText.setVisibility(View.GONE);
            sendAllTextView.setVisibility(View.GONE);
            createButton.setVisibility(View.GONE);
            sendMaxButton.setVisibility(View.GONE);
            pasteAddressImageButton.setVisibility(View.GONE);
            scanAddressImageButton.setVisibility(View.GONE);
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
            scanAddressImageButton.setVisibility(View.VISIBLE);
            feeTextView.setVisibility(View.GONE);
            addressTextView.setVisibility(View.GONE);
            amountTextView.setVisibility(View.GONE);
        }
    }

    private void pasteAddress(String address) {
        String modifiedAddress = address.replace("monero:", "").split("\\?")[0];
        boolean isValid = Wallet.isAddressValid(modifiedAddress);
        if (isValid) {
            addressEditText.setText(modifiedAddress);
        } else {
            Toast.makeText(getActivity(), getString(R.string.send_address_invalid), Toast.LENGTH_SHORT).show();
        }
    }


}