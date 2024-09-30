package at.rihnet.rihnetlogistikmde.ui.inventur;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.models.Inventurstatus;
import at.rihnet.rihnetlogistikmde.models.Inventurtyp;
import at.rihnet.rihnetlogistikmde.models.SelectLine.Inventory;

public class InventurRecyclerViewAdapter extends RecyclerView.Adapter<InventurRecyclerViewAdapter.InventoryHolder> {
    private List<Inventory> inventoryList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public InventurRecyclerViewAdapter(List<Inventory> inventoryList) {
        this.inventoryList = inventoryList;
    }

    public static class InventoryHolder extends RecyclerView.ViewHolder {
        private final TextView tv_datum;
        private final TextView tv_belegnummer;
        private final TextView tv_inventurtyp;
        private final TextView tv_inventurstatus;
        private final TextView tv_bemerkung;

        public InventoryHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            tv_datum = itemView.findViewById(R.id.tv_datum);
            tv_belegnummer = itemView.findViewById(R.id.tv_belegnummer);
            tv_inventurtyp = itemView.findViewById(R.id.tv_inventurtyp);
            tv_inventurstatus = itemView.findViewById(R.id.tv_inventurstatus);
            tv_bemerkung = itemView.findViewById(R.id.tv_bemerkung);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                    }
                }
            });
        }
    }

    @NonNull
    @Override
    public InventoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inventur, parent, false);
        return new InventoryHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull InventoryHolder inventoryHolder, int position) {
        Inventory currentItem = inventoryList.get(position);
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        String formattedDate = sdf.format(currentItem.getDate());
        inventoryHolder.tv_datum.setText(formattedDate);
        inventoryHolder.tv_belegnummer.setText(currentItem.getNumber());
        inventoryHolder.tv_inventurtyp.setText(Inventurtyp.values()[currentItem.getKindFlag()].beschreibung());
        inventoryHolder.tv_inventurstatus.setText(Inventurstatus.values()[Integer.parseInt(currentItem.getStatus())].toString());
        inventoryHolder.tv_bemerkung.setText(currentItem.getComment());
    }

    @Override
    public int getItemCount() {
        if (inventoryList != null) {
            return inventoryList.size();
        } else {
            return 0;
        }
    }

    public void setInventories(List<Inventory> logs) {
        inventoryList = logs;
    }

    public Inventory getInventoryAt(int position) {
        return inventoryList.get(position);
    }
}
