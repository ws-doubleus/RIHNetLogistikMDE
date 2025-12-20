package at.rihnet.rihnetlogistikmde.ui.warenausgang.logistik.gebucht;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.models.Belegposition;

public class GebuchtRecyclerViewAdapter extends RecyclerView.Adapter<GebuchtRecyclerViewAdapter.GebuchtHolder> {
    private List<Belegposition> mBelegpositionList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public GebuchtRecyclerViewAdapter(List<Belegposition> mBelegpositionList) {
        this.mBelegpositionList = mBelegpositionList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public GebuchtHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_warenausgang_gebucht, parent, false);
        return new GebuchtHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull GebuchtHolder holder, int position) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(holder.itemView.getContext());
        boolean autoworker = prefs.getBoolean("autoworker", false);
        Belegposition currentItem = mBelegpositionList.get(position);
        holder.tv_artikelnummer.setText(currentItem.getArtikelnummer());
        holder.tv_bezeichnung.setText(currentItem.getBezeichnung());
        holder.tv_menge.setText(String.valueOf(currentItem.getMenge()));
        holder.tv_seriecharge.setText(String.valueOf(currentItem.getSeriennummer()));
        if(autoworker){
            holder.tv_paket_label.setVisibility(VISIBLE);
            holder.tv_paket.setVisibility(VISIBLE);
            holder.tv_paket.setText(String.valueOf(currentItem.getPaketNummer()));
        }else {
            holder.tv_paket_label.setVisibility(GONE);
            holder.tv_paket.setVisibility(GONE);
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

    public Belegposition getBelegpositionAt(int position) {
        return mBelegpositionList.get(position);
    }

    public void setBelegpositionen(List<Belegposition> belegpositions) {
        mBelegpositionList = belegpositions;
        mBelegpositionList = new ArrayList<>(belegpositions);
    }

    public static class GebuchtHolder extends RecyclerView.ViewHolder {
        private final TextView tv_artikelnummer;
        private final TextView tv_bezeichnung;
        private final TextView tv_menge;
        private final TextView tv_seriecharge;
        private final TextView tv_paket_label;
        private final TextView tv_paket;

        public GebuchtHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            tv_artikelnummer = itemView.findViewById(R.id.tv_artikelnummer);
            tv_bezeichnung = itemView.findViewById(R.id.tv_bezeichnung);
            tv_menge = itemView.findViewById(R.id.tv_menge);
            tv_seriecharge = itemView.findViewById(R.id.tv_seriecharge);
            tv_paket_label = itemView.findViewById(R.id.tv_paket_label);
            tv_paket = itemView.findViewById(R.id.tv_paket);

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
