package at.rihnet.rihnetlogistikmde.ui.wareneingang.freierwareneingang.queue;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.models.Artikel;

public class QueueRecyclerViewAdapter extends RecyclerView.Adapter<QueueRecyclerViewAdapter.QueueHolder> {
    private List<Artikel> mArtikelList;
    private List<Artikel> mFilteredList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public QueueRecyclerViewAdapter(List<Artikel> mArtikelList) {
        this.mArtikelList = mArtikelList;
        mFilteredList = new ArrayList<>(mArtikelList);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public QueueHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_queue, parent, false);
        return new QueueHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull QueueHolder holder, int position) {
        Artikel currentItem = mFilteredList.get(position);
        holder.tv_artikelnummer.setText(currentItem.getArtikelnummer());
        holder.tv_bezeichnung.setText(currentItem.getBezeichnung());
        holder.tv_menge.setText(String.valueOf(currentItem.getMenge()));
        holder.tv_lager.setText(currentItem.getLager());
        holder.tv_lagerplatz.setText(currentItem.getLagerplatz());
        if (currentItem.getSerieCharge().equals("S")) {
            holder.tv_seriennummercharge_label.setText(R.string.artikel_seriennummer);
            holder.tv_seriennummercharge.setText(currentItem.getSeriennummer());
        } else if (currentItem.getSerieCharge().equals("C")) {
            holder.tv_seriennummercharge_label.setText(R.string.artikel_charge);
            holder.tv_seriennummercharge.setText(currentItem.getCharge());
        } else {
            holder.tv_seriennummercharge_label.setText("---:");
            holder.tv_seriennummercharge.setText("");
        }
    }

    @Override
    public int getItemCount() {
        if (mFilteredList != null) {
            return mFilteredList.size();
        } else {
            return 0;
        }
    }

    public static class QueueHolder extends RecyclerView.ViewHolder {
        private final TextView tv_artikelnummer;
        private final TextView tv_bezeichnung;
        private final TextView tv_menge;
        private final TextView tv_lager;
        private final TextView tv_lagerplatz;
        private final TextView tv_seriennummercharge_label;
        private final TextView tv_seriennummercharge;

        public QueueHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            tv_artikelnummer = itemView.findViewById(R.id.tv_artikelnummer);
            tv_bezeichnung = itemView.findViewById(R.id.tv_bezeichnung);
            tv_menge = itemView.findViewById(R.id.tv_menge);
            tv_lager = itemView.findViewById(R.id.tv_lager);
            tv_lagerplatz = itemView.findViewById(R.id.tv_lagerplatz);
            tv_seriennummercharge_label = itemView.findViewById(R.id.tv_seriennummercharge_label);
            tv_seriennummercharge = itemView.findViewById(R.id.tv_seriennummercharge);
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

    public void setQueue(List<Artikel> queue) {
        mArtikelList = queue;
        mFilteredList = new ArrayList<>(queue);
    }

    public Artikel getQueueAt(int position) {
        return mFilteredList.get(position);
    }

    public void setFilteredList(List<Artikel> filteredList) {
        mFilteredList = filteredList;
        notifyDataSetChanged();
    }

    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String filterQuery = charSequence.toString().toLowerCase().trim();
                List<Artikel> filteredItems = new ArrayList<>();
                for (Artikel item : mArtikelList) {
                    if (item.getSeriennummer().toLowerCase().contains(filterQuery) ||
                            item.getCharge().toLowerCase().contains(filterQuery) ||
                            item.getArtikelnummer().toLowerCase().contains(filterQuery)) {
                        filteredItems.add(item);
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredItems;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                mFilteredList = (List<Artikel>) filterResults.values;
                setFilteredList(mFilteredList);
            }
        };
    }
}
