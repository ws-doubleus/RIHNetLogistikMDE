package at.rihnet.rihnetlogistikmde.ui.warenausgang.logistik.leistungen;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.models.Belegposition;

public class LeistungenRecyclerViewAdapter extends RecyclerView.Adapter<LeistungenRecyclerViewAdapter.LeistungenHolder> {
    private List<Belegposition> mBelegpositionList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public LeistungenRecyclerViewAdapter(List<Belegposition> mBelegpositionList) {
        this.mBelegpositionList = mBelegpositionList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public LeistungenHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_warenausgang_leistungen, parent, false);
        return new LeistungenHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull LeistungenHolder holder, int position) {
        Belegposition currentItem = mBelegpositionList.get(position);
        holder.tv_artikelnummer.setText(currentItem.getArtikelnummer());
        holder.tv_bezeichnung.setText(currentItem.getBezeichnung());
        holder.tv_menge.setText(String.valueOf(currentItem.getMenge()));
    }

    @Override
    public int getItemCount() {
        if (mBelegpositionList != null) {
            return mBelegpositionList.size();
        } else {
            return 0;
        }
    }

    public Belegposition getBelegpositionAt(int position) {
        return mBelegpositionList.get(position);
    }

    public void setBelegpositionen(List<Belegposition> belegpositions) {
        mBelegpositionList = belegpositions;
        mBelegpositionList = new ArrayList<>(belegpositions);
    }

    public static class LeistungenHolder extends RecyclerView.ViewHolder {
        private final TextView tv_artikelnummer;
        private final TextView tv_bezeichnung;
        private final TextView tv_menge;

        public LeistungenHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            tv_artikelnummer = itemView.findViewById(R.id.tv_artikelnummer);
            tv_bezeichnung = itemView.findViewById(R.id.tv_bezeichnung);
            tv_menge = itemView.findViewById(R.id.tv_menge);

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
