package net.mynero.wallet.fragment.dialog;

import android.content.Context;
import android.os.Bundle;
import android.util.Patterns;
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
import net.mynero.wallet.data.Node;
import net.mynero.wallet.service.PrefService;
import net.mynero.wallet.util.Constants;
import net.mynero.wallet.util.Helper;

import org.json.JSONArray;
import org.json.JSONException;

public class EditNodeBottomSheetDialog extends BottomSheetDialogFragment {
    public EditNodeListener listener = null;
    public String nodeString = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.edit_node_bottom_sheet_dialog, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button deleteNodeButton = view.findViewById(R.id.delete_node_button);
        Button doneEditingButton = view.findViewById(R.id.done_editing_button);
        EditText addressEditText = view.findViewById(R.id.address_edittext);
        EditText nodeNameEditText = view.findViewById(R.id.node_name_edittext);
        ImageButton pasteAddressImageButton = view.findViewById(R.id.paste_address_imagebutton);

        Node node = Node.fromString(nodeString);
        addressEditText.setText(node.getAddress());
        nodeNameEditText.setText(node.getName());

        pasteAddressImageButton.setOnClickListener(view1 -> {
            Context ctx = getContext();
            if (ctx != null) {
                addressEditText.setText(Helper.getClipBoardText(ctx));
            }
        });
        deleteNodeButton.setOnClickListener(view1 -> {
            listener.onNodeDeleted(Node.fromString(nodeString));
            dismiss();
        });
        doneEditingButton.setOnClickListener(view1 -> {
            String nodeAddress = addressEditText.getText().toString();
            String nodeName = nodeNameEditText.getText().toString();
            if (nodeAddress.contains(":") && !nodeName.isEmpty()) {
                String[] nodeParts = nodeAddress.split(":");
                if (nodeParts.length == 2) {
                    String address = nodeParts[0];
                    int port = Integer.parseInt(nodeParts[1]);
                    String newNodeString = address + ":" + port + "/mainnet/" + nodeName;
                    listener.onNodeEdited(Node.fromString(nodeString), Node.fromString(newNodeString));
                }
            }
            dismiss();
        });
    }

    public interface EditNodeListener {
        void onNodeDeleted(Node node);
        void onNodeEdited(Node oldNode, Node newNode);
    }
}