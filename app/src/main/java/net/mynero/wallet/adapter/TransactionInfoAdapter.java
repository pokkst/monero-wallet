/*
 * Copyright (c) 2017 m2049r
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.mynero.wallet.adapter;

import static net.mynero.wallet.util.DateHelper.DATETIME_FORMATTER;

import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.CircularProgressIndicator;

import net.mynero.wallet.R;
import net.mynero.wallet.data.UserNotes;
import net.mynero.wallet.model.TransactionInfo;
import net.mynero.wallet.util.Helper;
import net.mynero.wallet.util.ThemeHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class TransactionInfoAdapter extends RecyclerView.Adapter<TransactionInfoAdapter.ViewHolder> {

    private List<TransactionInfo> localDataSet;
    private TxInfoAdapterListener listener = null;

    /**
     * Initialize the dataset of the Adapter.
     */
    public TransactionInfoAdapter(TxInfoAdapterListener listener) {
        this.listener = listener;
        this.localDataSet = new ArrayList<>();
    }

    public void submitList(List<TransactionInfo> dataSet) {
        this.localDataSet = dataSet;
        notifyDataSetChanged();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.transaction_history_item, viewGroup, false);

        return new ViewHolder(listener, view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        TransactionInfo tx = localDataSet.get(position);
        viewHolder.bind(tx);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localDataSet.size();
    }

    public interface TxInfoAdapterListener {
        void onClickTransaction(TransactionInfo txInfo);
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final int outboundColour;
        private final int inboundColour;
        private final int pendingColour;
        private final int failedColour;
        private TxInfoAdapterListener listener = null;
        private TextView amountTextView = null;

        public ViewHolder(TxInfoAdapterListener listener, View view) {
            super(view);
            inboundColour = ThemeHelper.getThemedColor(view.getContext(), R.attr.positiveColor);
            outboundColour = ThemeHelper.getThemedColor(view.getContext(), R.attr.negativeColor);
            pendingColour = ThemeHelper.getThemedColor(view.getContext(), R.attr.neutralColor);
            failedColour = ThemeHelper.getThemedColor(view.getContext(), R.attr.neutralColor);
            this.listener = listener;
            Calendar cal = Calendar.getInstance();
            TimeZone tz = cal.getTimeZone(); //get the local time zone.
            DATETIME_FORMATTER.setTimeZone(tz);
        }

        public void bind(TransactionInfo txInfo) {
            String displayAmount = Helper.getDisplayAmount(txInfo.amount, Helper.DISPLAY_DIGITS_INFO);

            TextView confirmationsTextView = ((TextView) itemView.findViewById(R.id.tvConfirmations));
            CircularProgressIndicator confirmationsProgressBar = ((CircularProgressIndicator) itemView.findViewById(R.id.pbConfirmations));
            confirmationsProgressBar.setMax(TransactionInfo.CONFIRMATION);
            this.amountTextView = ((TextView) itemView.findViewById(R.id.tx_amount));
            ((TextView) itemView.findViewById(R.id.tx_failed)).setVisibility(View.GONE);
            if (txInfo.isFailed) {
                ((TextView) itemView.findViewById(R.id.tx_amount)).setText(itemView.getContext().getString(R.string.tx_list_amount_negative, displayAmount));
                ((TextView) itemView.findViewById(R.id.tx_failed)).setVisibility(View.VISIBLE);
                setTxColour(failedColour);
                confirmationsTextView.setVisibility(View.GONE);
                confirmationsProgressBar.setVisibility(View.GONE);
            } else if (txInfo.isPending) {
                setTxColour(pendingColour);
                confirmationsProgressBar.setVisibility(View.GONE);
                confirmationsProgressBar.setIndeterminate(true);
                confirmationsProgressBar.setVisibility(View.VISIBLE);
                confirmationsTextView.setVisibility(View.GONE);
            } else if (txInfo.direction == TransactionInfo.Direction.Direction_In) {
                setTxColour(inboundColour);
                if (!txInfo.isConfirmed()) {
                    confirmationsProgressBar.setVisibility(View.VISIBLE);
                    final int confirmations = (int) txInfo.confirmations;
                    confirmationsProgressBar.setProgressCompat(confirmations, true);
                    final String confCount = Integer.toString(confirmations);
                    confirmationsTextView.setText(confCount);
                    if (confCount.length() == 1) // we only have space for character in the progress circle
                        confirmationsTextView.setVisibility(View.VISIBLE);
                    else
                        confirmationsTextView.setVisibility(View.GONE);
                } else {
                    confirmationsProgressBar.setVisibility(View.GONE);
                    confirmationsTextView.setVisibility(View.GONE);
                }
            } else {
                setTxColour(outboundColour);
                confirmationsProgressBar.setVisibility(View.GONE);
                confirmationsTextView.setVisibility(View.GONE);
            }

            if (txInfo.direction == TransactionInfo.Direction.Direction_Out) {
                ((TextView) itemView.findViewById(R.id.tx_amount)).setText(itemView.getContext().getString(R.string.tx_list_amount_negative, displayAmount));
            } else {
                ((TextView) itemView.findViewById(R.id.tx_amount)).setText(itemView.getContext().getString(R.string.tx_list_amount_positive, displayAmount));
            }

            TextView paymentIdTextView = ((TextView) itemView.findViewById(R.id.tx_paymentid));
            String tag = null;
            String info = "";
            UserNotes userNotes = new UserNotes(txInfo.notes);
            if ((txInfo.addressIndex != 0) && (txInfo.direction == TransactionInfo.Direction.Direction_In))
                tag = txInfo.getDisplayLabel();
            if ((userNotes.note.isEmpty())) {
                if (!txInfo.paymentId.equals("0000000000000000")) {
                    info = txInfo.paymentId;
                }
            } else {
                info = userNotes.note;
            }
            if (tag == null) {
                paymentIdTextView.setText(info);
            } else {
                Spanned label = Html.fromHtml(itemView.getContext().getString(R.string.tx_details_notes,
                        Integer.toHexString(ThemeHelper.getThemedColor(itemView.getContext(), R.attr.positiveColor) & 0xFFFFFF),
                        Integer.toHexString(ThemeHelper.getThemedColor(itemView.getContext(), android.R.attr.colorBackground) & 0xFFFFFF),
                        tag, info.isEmpty() ? "" : ("&nbsp; " + info)));
                paymentIdTextView.setText(label);
            }
            ((TextView) itemView.findViewById(R.id.tx_datetime)).setText(getDateTime(txInfo.timestamp));
            itemView.setOnClickListener(view -> {
                listener.onClickTransaction(txInfo);
            });
        }

        private void setTxColour(int clr) {
            amountTextView.setTextColor(clr);
        }

        private String getDateTime(long time) {
            return DATETIME_FORMATTER.format(new Date(time * 1000));
        }
    }
}

