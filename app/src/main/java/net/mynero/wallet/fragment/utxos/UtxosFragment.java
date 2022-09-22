package net.mynero.wallet.fragment.utxos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import net.mynero.wallet.R;
import net.mynero.wallet.adapter.CoinsInfoAdapter;
import net.mynero.wallet.fragment.dialog.SendBottomSheetDialog;
import net.mynero.wallet.model.CoinsInfo;
import net.mynero.wallet.service.UTXOService;

import java.util.ArrayList;

public class UtxosFragment extends Fragment implements CoinsInfoAdapter.CoinsInfoAdapterListener {

    private UtxosViewModel mViewModel;
    private ArrayList<String> selectedUtxos = new ArrayList<>();
    private CoinsInfoAdapter adapter = new CoinsInfoAdapter(this);
    private Button sendUtxosButton;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_utxos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(UtxosViewModel.class);
        bindListeners(view);
        bindObservers(view);
    }

    private void bindListeners(View view) {
        sendUtxosButton = view.findViewById(R.id.send_utxos_button);
        sendUtxosButton.setVisibility(View.GONE);
        sendUtxosButton.setOnClickListener(view1 -> {
            SendBottomSheetDialog sendDialog = new SendBottomSheetDialog();
            sendDialog.selectedUtxos = selectedUtxos;
            sendDialog.show(getActivity().getSupportFragmentManager(), null);
        });
    }

    private void bindObservers(View view) {
        RecyclerView utxosRecyclerView = view.findViewById(R.id.transaction_history_recyclerview);
        UTXOService utxoService = UTXOService.getInstance();
        utxosRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        utxosRecyclerView.setAdapter(adapter);
        if (utxoService != null) {
            utxoService.utxos.observe(getViewLifecycleOwner(), utxos -> {
                ArrayList<CoinsInfo> filteredUtxos = new ArrayList<>();
                for(CoinsInfo coinsInfo : utxos) {
                    if(!coinsInfo.isSpent()) {
                        filteredUtxos.add(coinsInfo);
                    }
                }
                if (filteredUtxos.isEmpty()) {
                    utxosRecyclerView.setVisibility(View.GONE);
                } else {
                    adapter.submitList(filteredUtxos, selectedUtxos);
                    utxosRecyclerView.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    @Override
    public void onUtxoSelected(CoinsInfo coinsInfo) {
        boolean selected = selectedUtxos.contains(coinsInfo.getKeyImage());
        if(selected) {
            selectedUtxos.remove(coinsInfo.getKeyImage());
        } else {
            selectedUtxos.add(coinsInfo.getKeyImage());
        }

        if(selectedUtxos.isEmpty()) {
            sendUtxosButton.setVisibility(View.GONE);
        } else {
            sendUtxosButton.setVisibility(View.VISIBLE);
        }

        adapter.updateSelectedUtxos(selectedUtxos);
    }
}