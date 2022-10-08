package net.mynero.wallet.fragment.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import net.mynero.wallet.R;
import net.mynero.wallet.model.Wallet;
import net.mynero.wallet.model.WalletManager;
import net.mynero.wallet.util.Helper;

public class WalletKeysBottomSheetDialog extends BottomSheetDialogFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.wallet_keys_bottom_sheet_dialog, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageButton copyViewKeyImageButton = view.findViewById(R.id.copy_viewkey_imagebutton);
        TextView informationTextView = view.findViewById(R.id.information_textview); // seed
        TextView viewKeyTextView = view.findViewById(R.id.viewkey_textview);
        TextView restoreHeightTextView = view.findViewById(R.id.restore_height_textview);

        Wallet wallet = WalletManager.getInstance().getWallet();
        String seed = wallet.getSeed("");
        String privateViewKey = wallet.getSecretViewKey();

        informationTextView.setText(seed);
        viewKeyTextView.setText(privateViewKey);
        restoreHeightTextView.setText(wallet.getRestoreHeight()+"");

        copyViewKeyImageButton.setOnClickListener(view1 -> Helper.clipBoardCopy(getContext(), "private view-key", privateViewKey));
    }
}