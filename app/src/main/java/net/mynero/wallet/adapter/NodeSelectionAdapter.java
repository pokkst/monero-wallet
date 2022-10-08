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
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import net.mynero.wallet.R;
import net.mynero.wallet.data.DefaultNodes;
import net.mynero.wallet.data.Node;
import net.mynero.wallet.service.PrefService;
import net.mynero.wallet.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class NodeSelectionAdapter extends RecyclerView.Adapter<NodeSelectionAdapter.ViewHolder> {

    private List<Node> localDataSet;
    private NodeSelectionAdapterListener listener = null;

    /**
     * Initialize the dataset of the Adapter.
     */
    public NodeSelectionAdapter(NodeSelectionAdapterListener listener) {
        this.listener = listener;
        this.localDataSet = new ArrayList<>();
    }

    public void submitList(List<Node> dataSet) {
        this.localDataSet = dataSet;
        notifyDataSetChanged();
    }

    public void updateSelectedNode() {
        notifyDataSetChanged();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.node_selection_item, viewGroup, false);

        return new ViewHolder(listener, view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        Node node = localDataSet.get(position);
        viewHolder.bind(node);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localDataSet.size();
    }

    public interface NodeSelectionAdapterListener {
        void onSelectNode(Node node);
        boolean onSelectEditNode(Node node);
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final NodeSelectionAdapterListener listener;

        public ViewHolder(NodeSelectionAdapterListener listener, View view) {
            super(view);
            this.listener = listener;
        }

        public void bind(Node node) {
            String currentNodeString = PrefService.getInstance().getString(Constants.PREF_NODE, "");
            Node currentNode = Node.fromString(currentNodeString);
            boolean match = node.equals(currentNode);
            if (match) {
                itemView.setBackgroundColor(itemView.getResources().getColor(R.color.oled_colorSecondary));
            } else {
                itemView.setBackgroundColor(itemView.getResources().getColor(android.R.color.transparent));

            }
            TextView nodeNameTextView = itemView.findViewById(R.id.node_name_textview);
            TextView nodeAddressTextView = itemView.findViewById(R.id.node_uri_textview);
            nodeNameTextView.setText(node.getName());
            nodeAddressTextView.setText(node.getAddress());

            itemView.setOnLongClickListener(view -> {
                if(match) {
                    Toast.makeText(itemView.getContext(), itemView.getResources().getString(R.string.cant_edit_current_node), Toast.LENGTH_SHORT).show();
                    return false;
                } else if(isDefaultNode(node)) {
                    Toast.makeText(itemView.getContext(), itemView.getResources().getString(R.string.cant_edit_default_nodes), Toast.LENGTH_SHORT).show();
                    return false;
                } else {
                    return listener.onSelectEditNode(node);
                }
            });
            itemView.setOnClickListener(view -> listener.onSelectNode(node));
        }

        private boolean isDefaultNode(Node currentNode) {
            boolean isDefault = false;
            for(DefaultNodes defaultNode : DefaultNodes.values()) {
                if(currentNode.toNodeString().equals(defaultNode.getUri()))
                    isDefault = true;
            }

            return isDefault;
        }
    }
}

