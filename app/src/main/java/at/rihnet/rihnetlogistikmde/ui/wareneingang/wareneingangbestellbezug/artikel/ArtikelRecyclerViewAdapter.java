package at.rihnet.rihnetlogistikmde.ui.wareneingang.wareneingangbestellbezug.artikel;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.models.Belegposition;

public class ArtikelRecyclerViewAdapter extends RecyclerView.Adapter<ArtikelRecyclerViewAdapter.QueueHolder> {
    private List<Belegposition> mBelegpositionList;
    private List<Belegposition> mFilteredList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public ArtikelRecyclerViewAdapter(List<Belegposition> mBelegpositionList) {
        this.mBelegpositionList = mBelegpositionList;
        mFilteredList = new ArrayList<>(mBelegpositionList);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public QueueHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_artikel, parent, false);
        return new QueueHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull QueueHolder holder, int position) {
        Context context = holder.itemView.getContext();
        Belegposition currentItem = mFilteredList.get(position);
        holder.tv_belegnummer.setText(currentItem.getBelegnummer());
        holder.tv_position.setText(currentItem.getPostext());
        holder.tv_artikelnummer.setText(currentItem.getArtikelnummer());
        holder.tv_bezeichnung.setText(currentItem.getBezeichnung());
        holder.tv_menge.setText(String.valueOf(currentItem.getMenge()));
        holder.tv_offen.setText(String.valueOf(currentItem.getOffen()));
        if (currentItem.getOffen() > 0) {
            holder.tv_offen.setTextColor(ContextCompat.getColor(context, R.color.red_500));
            holder.itemView.setEnabled(true);
//            TypedValue typedValue = new TypedValue();
//            context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true);
//            Drawable selectableItemBackground = ContextCompat.getDrawable(context, typedValue.resourceId);
//            holder.cv_item.setForeground(selectableItemBackground);
        } else {
            holder.tv_offen.setTextColor(ContextCompat.getColor(context, R.color.green_500));
            holder.itemView.setEnabled(false);
//            Drawable colorDrawable = new ColorDrawable(ContextCompat.getColor(context, R.color.green_500));
//            holder.cv_item.setBackground(colorDrawable);
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
        //private final CardView cv_item;
        private final TextView tv_belegnummer;
        private final TextView tv_position;
        private final TextView tv_artikelnummer;
        private final TextView tv_bezeichnung;
        private final TextView tv_menge;
        private final TextView tv_offen;


        public QueueHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            //cv_item = itemView.findViewById(R.id.cv_item);
            tv_belegnummer = itemView.findViewById(R.id.tv_belegnummer);
            tv_position = itemView.findViewById(R.id.tv_position);
            tv_artikelnummer = itemView.findViewById(R.id.tv_artikelnummer);
            tv_bezeichnung = itemView.findViewById(R.id.tv_bezeichnung);
            tv_menge = itemView.findViewById(R.id.tv_menge);
            tv_offen = itemView.findViewById(R.id.tv_offen);

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

    public void setBelegpositionen(List<Belegposition> belegpositionen) {
        mBelegpositionList = belegpositionen;
        mFilteredList = new ArrayList<>(belegpositionen);
    }

    public Belegposition getBelegpositionAt(int position) {
        return mFilteredList.get(position);
    }

    public void setFilteredList(List<Belegposition> filteredList) {
        mFilteredList = filteredList;
        notifyDataSetChanged();
    }

    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String filterQuery = charSequence.toString().toLowerCase().trim();
                List<Belegposition> filteredItems = new ArrayList<>();
                for (Belegposition item : mBelegpositionList) {
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
                mFilteredList = (List<Belegposition>) filterResults.values;
                setFilteredList(mFilteredList);
            }
        };
    }


}
