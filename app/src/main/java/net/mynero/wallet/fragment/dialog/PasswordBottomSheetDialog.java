package net.mynero.wallet.fragment.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import net.mynero.wallet.R;
import net.mynero.wallet.model.WalletManager;
import net.mynero.wallet.util.Constants;
import net.mynero.wallet.util.Helper;

import java.io.File;

public class PasswordBottomSheetDialog extends BottomSheetDialogFragment {
    public PasswordListener listener = null;
    public boolean cancelable = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.password_bottom_sheet_dialog, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setCancelable(cancelable);
        File walletFile = new File(getActivity().getApplicationInfo().dataDir, Constants.WALLET_NAME);

        ImageButton pastePasswordImageButton = view.findViewById(R.id.paste_password_imagebutton);
        EditText passwordEditText = view.findViewById(R.id.wallet_password_edittext);
        Button unlockWalletButton = view.findViewById(R.id.unlock_wallet_button);

        pastePasswordImageButton.setOnClickListener(view1 -> {
            passwordEditText.setText(Helper.getClipBoardText(view.getContext()));
        });

        unlockWalletButton.setOnClickListener(view1 -> {
            String password = passwordEditText.getText().toString();
            boolean success = checkPassword(walletFile, password);
            if (success) {
                listener.onPasswordSuccess(password);
                dismiss();
            } else {
                listener.onPasswordFail();
            }
        });
    }

    private boolean checkPassword(File walletFile, String password) {
        return WalletManager.getInstance().verifyWalletPasswordOnly(walletFile.getAbsolutePath() + ".keys", password);
    }

    public interface PasswordListener {
        void onPasswordSuccess(String password);

        void onPasswordFail();
    }
}