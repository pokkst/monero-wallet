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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import net.mynero.wallet.R;
import net.mynero.wallet.model.CoinsInfo;
import net.mynero.wallet.model.Wallet;

import java.util.ArrayList;
import java.util.List;

public class CoinsInfoAdapter extends RecyclerView.Adapter<CoinsInfoAdapter.ViewHolder> {

    private List<CoinsInfo> localDataSet;
    private List<String> selectedUtxos;
    private CoinsInfoAdapterListener listener = null;

    /**
     * Initialize the dataset of the Adapter.
     */
    public CoinsInfoAdapter(CoinsInfoAdapterListener listener) {
        this.listener = listener;
        this.localDataSet = new ArrayList<>();
        this.selectedUtxos = new ArrayList<>();
    }

    public void submitList(List<CoinsInfo> dataSet, List<String> selectedUtxos) {
        this.localDataSet = dataSet;
        this.selectedUtxos = selectedUtxos;
        notifyDataSetChanged();
    }

    public void updateSelectedUtxos(List<String> selectedUtxos) {
        this.selectedUtxos = selectedUtxos;
        notifyDataSetChanged();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.utxo_selection_item, viewGroup, false);

        return new ViewHolder(listener, view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        CoinsInfo tx = localDataSet.get(position);
        viewHolder.bind(tx, selectedUtxos);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localDataSet.size();
    }

    public interface CoinsInfoAdapterListener {
        void onUtxoSelected(CoinsInfo coinsInfo);
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private CoinsInfoAdapterListener listener = null;

        public ViewHolder(CoinsInfoAdapterListener listener, View view) {
            super(view);
            this.listener = listener;
        }

        public void bind(CoinsInfo coinsInfo, List<String> selectedUtxos) {
            boolean selected = selectedUtxos.contains(coinsInfo.getKeyImage());
            TextView pubKeyTextView = itemView.findViewById(R.id.utxo_pub_key_textview);
            TextView amountTextView = itemView.findViewById(R.id.utxo_amount_textview);
            amountTextView.setText(Wallet.getDisplayAmount(coinsInfo.getAmount()));
            pubKeyTextView.setText(coinsInfo.getPubKey());
            itemView.setOnLongClickListener(view -> {
                boolean unlocked = coinsInfo.isUnlocked();
                if(unlocked) {
                    listener.onUtxoSelected(coinsInfo);
                }
                return unlocked;
            });

            if(!coinsInfo.isUnlocked()) {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.oled_locked_utxo));
            } else if(selected) {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.oled_negativeColor));
            } else {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), android.R.color.transparent));
            }
        }
    }
}

