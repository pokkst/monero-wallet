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

package com.m2049r.xmrwallet.adapter;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.leanback.widget.DiffCallback;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.m2049r.xmrwallet.R;
import com.m2049r.xmrwallet.data.Crypto;
import com.m2049r.xmrwallet.data.UserNotes;
import com.m2049r.xmrwallet.model.TransactionInfo;
import com.m2049r.xmrwallet.util.Helper;
import com.m2049r.xmrwallet.util.ThemeHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

import timber.log.Timber;

public class TransactionInfoAdapter extends RecyclerView.Adapter<TransactionInfoAdapter.ViewHolder> {

    private List<TransactionInfo> localDataSet;
    private TxInfoAdapterListener listener = null;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TxInfoAdapterListener listener = null;
        public ViewHolder(TxInfoAdapterListener listener, View view) {
            super(view);
            this.listener = listener;
        }

        public void bind(TransactionInfo txInfo) {
            itemView.setOnClickListener(view -> {
                listener.onClickTransaction(txInfo);
            });
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     */
    public TransactionInfoAdapter(TxInfoAdapterListener listener) {
        this.listener = listener;
        this.localDataSet = new ArrayList<>();
    }

    public void submitList(List<TransactionInfo> dataSet) {
        this.localDataSet = dataSet;
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
}

