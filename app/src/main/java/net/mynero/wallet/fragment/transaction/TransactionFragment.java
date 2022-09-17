package net.mynero.wallet.fragment.transaction;

import static net.mynero.wallet.util.DateHelper.DATETIME_FORMATTER;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import net.mynero.wallet.R;
import net.mynero.wallet.model.TransactionInfo;
import net.mynero.wallet.util.Constants;
import net.mynero.wallet.util.Helper;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

public class TransactionFragment extends Fragment {

    private TransactionViewModel mViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transaction, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone(); //get the local time zone.
        DATETIME_FORMATTER.setTimeZone(tz);

        mViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        Bundle args = getArguments();
        if(args != null) {
            TransactionInfo txInfo = getArguments().getParcelable(Constants.NAV_ARG_TXINFO);
            mViewModel.init(txInfo);
        }

        bindObservers(view);
        bindListeners(view);
    }

    private void bindListeners(View view) {
        ImageButton copyTxHashImageButton = view.findViewById(R.id.copy_txhash_imagebutton);
        copyTxHashImageButton.setOnClickListener(view1 -> {
            TransactionInfo txInfo = mViewModel.transaction.getValue();
            if(txInfo != null) {
                Helper.clipBoardCopy(getContext(), "transaction_hash", txInfo.hash);
            }
        });

        ImageButton copyTxAddressImageButton = view.findViewById(R.id.copy_txaddress_imagebutton);
        copyTxAddressImageButton.setOnClickListener(view1 -> {
            TransactionInfo txInfo = mViewModel.transaction.getValue();
            if(txInfo != null) {
                String destination = mViewModel.destination.getValue();
                if(destination != null) {
                    Helper.clipBoardCopy(getContext(), "transaction_address", destination);
                }
            }
        });
    }

    private void bindObservers(View view) {
        TextView txHashTextView = view.findViewById(R.id.transaction_hash_textview);
        TextView txConfTextView = view.findViewById(R.id.transaction_conf_textview);
        TextView txAddressTextView = view.findViewById(R.id.transaction_address_textview);
        ImageButton copyTxAddressImageButton = view.findViewById(R.id.copy_txaddress_imagebutton);
        TextView txDateTextView = view.findViewById(R.id.transaction_date_textview);
        TextView txAmountTextView = view.findViewById(R.id.transaction_amount_textview);

        mViewModel.transaction.observe(getViewLifecycleOwner(), transactionInfo -> {
            txHashTextView.setText(transactionInfo.hash);
            txConfTextView.setText(""+transactionInfo.confirmations);
            txDateTextView.setText(getDateTime(transactionInfo.timestamp));
            txAmountTextView.setText(getResources().getString(R.string.tx_amount_no_prefix, Helper.getDisplayAmount(transactionInfo.amount)));
        });

        mViewModel.destination.observe(getViewLifecycleOwner(), s -> {
            txAddressTextView.setText(Objects.requireNonNullElse(s, "-"));
            if(s == null) {
                copyTxAddressImageButton.setVisibility(View.INVISIBLE);
            }
        });
    }

    private String getDateTime(long time) {
        return DATETIME_FORMATTER.format(new Date(time * 1000));
    }
}