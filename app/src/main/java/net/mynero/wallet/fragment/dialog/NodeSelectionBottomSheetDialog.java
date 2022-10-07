package net.mynero.wallet.fragment.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import net.mynero.wallet.R;
import net.mynero.wallet.adapter.NodeSelectionAdapter;
import net.mynero.wallet.data.DefaultNodes;
import net.mynero.wallet.data.Node;
import net.mynero.wallet.model.WalletManager;
import net.mynero.wallet.service.PrefService;
import net.mynero.wallet.util.Constants;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class NodeSelectionBottomSheetDialog extends BottomSheetDialogFragment implements NodeSelectionAdapter.NodeSelectionAdapterListener {
    public NodeSelectionDialogListener listener = null;
    private NodeSelectionAdapter adapter = null;

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
            if (listener != null) {
                listener.onClickedAddNode();
            }
            dismiss();
        });

        try {
            String nodesArray = PrefService.getInstance().getString(Constants.PREF_CUSTOM_NODES, "[]");
            JSONArray jsonArray = new JSONArray(nodesArray);
            for (int i = 0; i < jsonArray.length(); i++) {
                String nodeString = jsonArray.getString(i);
                Node node = Node.fromString(nodeString);
                if (node != null) {
                    nodes.add(node);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for (DefaultNodes defaultNode : DefaultNodes.values()) {
            nodes.add(Node.fromString(defaultNode.getUri()));
        }
        adapter.submitList(nodes);
    }

    @Override
    public void onSelectNode(Node node) {
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(() -> {
                Toast.makeText(activity, getString(R.string.node_selected), Toast.LENGTH_SHORT).show();
            });
        }
        PrefService.getInstance().edit().putString(Constants.PREF_NODE, node.getAddress()).apply();
        WalletManager.getInstance().setDaemon(node);
        adapter.updateSelectedNode();
        listener.onNodeSelected();
    }

    @Override
    public boolean onSelectEditNode(Node node) {
        if (listener != null) {
            listener.onClickedEditNode(node.toNodeString());
        }
        dismiss();
        return true;
    }

    public interface NodeSelectionDialogListener {
        void onNodeSelected();
        void onClickedEditNode(String nodeString);
        void onClickedAddNode();
    }
}