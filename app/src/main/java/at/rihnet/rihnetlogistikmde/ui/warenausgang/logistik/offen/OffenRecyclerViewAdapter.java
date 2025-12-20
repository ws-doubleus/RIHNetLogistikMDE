package at.rihnet.rihnetlogistikmde.ui.warenausgang.logistik.offen;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.models.Belegposition;

public class OffenRecyclerViewAdapter extends RecyclerView.Adapter<OffenRecyclerViewAdapter.OffenHolder> {
    private List<Belegposition> mBelegpositionList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public OffenRecyclerViewAdapter(List<Belegposition> mBelegpositionList) {
        this.mBelegpositionList = mBelegpositionList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public OffenHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_warenausgang_offen, parent, false);
        return new OffenHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull OffenHolder holder, int position) {
        Context context = holder.itemView.getContext();
        Belegposition currentItem = mBelegpositionList.get(position);
        holder.tv_artikelnummer.setText(currentItem.getArtikelnummer());
        holder.tv_bezeichnung.setText(currentItem.getBezeichnung());
        holder.tv_menge.setText(String.valueOf(currentItem.getMenge()));
        holder.tv_offen.setText(String.valueOf(currentItem.getOffen()));
        holder.tv_seriecharge.setText(String.valueOf(currentItem.getSerieCharge()));

        if (currentItem.getOffen() > 0) {
            holder.tv_offen.setTextColor(ContextCompat.getColor(context, R.color.red_500));
            holder.itemView.setEnabled(true);
        } else {
            holder.tv_offen.setTextColor(ContextCompat.getColor(context, R.color.green_500));
            holder.itemView.setEnabled(false);


            //holder.itemView.post(() -> removeItemAt(holder.getAdapterPosition()));

        }
    }

    @Override
    public int getItemCount() {
        if (mBelegpositionList != null) {
            return mBelegpositionList.size();
        } else {
            return 0;
        }
    }

    public void removeItemAt(int position) {
        if (position < 0 || position >= mBelegpositionList.size()) return;

        mBelegpositionList.remove(position);
        notifyItemRemoved(position);          // nur diese eine Zeile aktualisieren
    }

    public Belegposition getBelegpositionAt(int position) {
        return mBelegpositionList.get(position);
    }

    public void setBelegpositionen(List<Belegposition> belegpositions) {
        mBelegpositionList = belegpositions;
        mBelegpositionList = new ArrayList<>(belegpositions);
    }

    public static class OffenHolder extends RecyclerView.ViewHolder {
        private final TextView tv_artikelnummer;
        private final TextView tv_bezeichnung;
        private final TextView tv_menge;
        private final TextView tv_offen;
        private final TextView tv_seriecharge;


        public OffenHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            tv_artikelnummer = itemView.findViewById(R.id.tv_artikelnummer);
            tv_bezeichnung = itemView.findViewById(R.id.tv_bezeichnung);
            tv_menge = itemView.findViewById(R.id.tv_menge);
            tv_offen = itemView.findViewById(R.id.tv_offen);
            tv_seriecharge = itemView.findViewById(R.id.tv_seriecharge);

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
