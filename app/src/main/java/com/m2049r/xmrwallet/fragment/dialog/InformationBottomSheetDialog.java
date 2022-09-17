package com.m2049r.xmrwallet.fragment.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.m2049r.xmrwallet.R;
import com.m2049r.xmrwallet.util.Helper;

public class InformationBottomSheetDialog extends BottomSheetDialogFragment {
    public boolean showCopyButton = false;
    public String information = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.information_bottom_sheet_dialog, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageButton copyInformationImageButton = view.findViewById(R.id.copy_information_imagebutton);
        if (showCopyButton) {
            copyInformationImageButton.setVisibility(View.VISIBLE);
        } else {
            copyInformationImageButton.setVisibility(View.INVISIBLE);
        }
        TextView informationTextView = view.findViewById(R.id.information_textview);
        informationTextView.setText(information);
        copyInformationImageButton.setOnClickListener(view1 -> Helper.clipBoardCopy(getContext(), "information", information));
    }
}