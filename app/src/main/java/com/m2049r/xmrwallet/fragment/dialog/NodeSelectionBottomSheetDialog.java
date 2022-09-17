package com.m2049r.xmrwallet.fragment.dialog;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.m2049r.xmrwallet.R;
import com.m2049r.xmrwallet.adapter.NodeSelectionAdapter;
import com.m2049r.xmrwallet.data.DefaultNodes;
import com.m2049r.xmrwallet.data.Node;
import com.m2049r.xmrwallet.model.TransactionInfo;
import com.m2049r.xmrwallet.model.WalletManager;
import com.m2049r.xmrwallet.service.PrefService;
import com.m2049r.xmrwallet.util.Constants;
import com.m2049r.xmrwallet.util.Helper;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;

public class NodeSelectionBottomSheetDialog extends BottomSheetDialogFragment implements NodeSelectionAdapter.NodeSelectionAdapterListener {
    private NodeSelectionAdapter adapter = null;
    public NodeSelectionDialogListener listener = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.node_selection_bottom_sheet_dialog, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ArrayList<Node> nodes = new ArrayList<>();
        adapter = new NodeSelectionAdapter(this);

        RecyclerView recyclerView = view.findViewById(R.id.node_selection_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        Button addNodeButton = view.findViewById(R.id.add_node_button);
        addNodeButton.setOnClickListener(view1 -> {
            if(listener != null) {
                listener.onClickedAddNode();
            }
            dismiss();
        });

        try {
            String nodesArray = PrefService.getInstance().getString(Constants.PREF_CUSTOM_NODES, "[]");
            JSONArray jsonArray = new JSONArray(nodesArray);
            for(int i = 0; i < jsonArray.length(); i++) {
                String nodeString = jsonArray.getString(i);
                Node node = Node.fromString(nodeString);
                if(node != null) {
                    nodes.add(node);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for(DefaultNodes defaultNode : DefaultNodes.values()) {
            nodes.add(Node.fromString(defaultNode.getUri()));
        }
        adapter.submitList(nodes);
    }

    @Override
    public void onSelectNode(Node node) {
        PrefService.getInstance().edit().putString(Constants.PREF_NODE, node.getAddress()).apply();
        WalletManager.getInstance().setDaemon(node);
        adapter.updateSelectedNode();
    }

    public interface NodeSelectionDialogListener {
        void onClickedAddNode();
    }
}