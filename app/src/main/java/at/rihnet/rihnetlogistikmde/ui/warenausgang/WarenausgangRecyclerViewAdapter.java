package at.rihnet.rihnetlogistikmde.ui.warenausgang;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.models.BelegInfo;

public class WarenausgangRecyclerViewAdapter extends RecyclerView.Adapter<WarenausgangRecyclerViewAdapter.WarenausgangHolder> {
    private List<BelegInfo> belegInfoList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public WarenausgangRecyclerViewAdapter(List<BelegInfo> belegList) {
        this.belegInfoList = belegList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public WarenausgangHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_warenausgang, parent, false);
        return new WarenausgangHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull WarenausgangHolder holder, int position) {
        BelegInfo currentItem = belegInfoList.get(position);
        holder.tv_belegtyp.setText(currentItem.getBelegtyp());
        holder.tv_belegnummer.setText(currentItem.getBelegnummer());
        holder.tv_adressnummer.setText(currentItem.getAdressnummer());
        holder.tv_anzeigename.setText(currentItem.getAnzeigename());
    }

    @Override
    public int getItemCount() {
        if (belegInfoList != null) {
            return belegInfoList.size();
        } else {
            return 0;
        }
    }

    public BelegInfo getBelegInfoAt(int position) {
        return belegInfoList.get(position);
    }

    public void setBelegInfos(List<BelegInfo> belegInfos){
        belegInfoList = belegInfos;
    }

    public static class WarenausgangHolder extends RecyclerView.ViewHolder {
        private final TextView tv_belegtyp;
        private final TextView tv_belegnummer;
        private final TextView tv_adressnummer;
        private final TextView tv_anzeigename;

        public WarenausgangHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            tv_belegtyp = itemView.findViewById(R.id.tv_belegtyp);
            tv_belegnummer = itemView.findViewById(R.id.tv_belegnummer);
            tv_adressnummer = itemView.findViewById(R.id.tv_adressnummer);
            tv_anzeigename = itemView.findViewById(R.id.tv_anzeigename);

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
}
