package at.rihnet.rihnetlogistikmde.ui.freierwareneingang.artikel;

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
import at.rihnet.rihnetlogistikmde.models.SeriennummerCharge;

public class ChargeRecyclerViewAdapter extends RecyclerView.Adapter<ChargeRecyclerViewAdapter.ChargeHolder> {
    private final List<SeriennummerCharge> mSeriennummerChargeList;
    private List<SeriennummerCharge> mFilteredList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public ChargeRecyclerViewAdapter(List<SeriennummerCharge> seriennummerChargeList) {
       mSeriennummerChargeList = seriennummerChargeList;
        mFilteredList = new ArrayList<>(seriennummerChargeList);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public ChargeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_charge, parent, false);
        return new ChargeHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ChargeHolder holder, int position) {
        SeriennummerCharge currentItem = mFilteredList.get(position);
        holder.tv_charge.setText(currentItem.getNummer());
        holder.tv_bestand.setText(String.valueOf((currentItem.getBestand())));
    }

    @Override
    public int getItemCount() {
        if (mFilteredList != null) {
            return mFilteredList.size();
        } else {
            return 0;
        }
    }

    public static class ChargeHolder extends RecyclerView.ViewHolder {
        private final TextView tv_charge;
        private final TextView tv_bestand;

        public ChargeHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            tv_charge = itemView.findViewById(R.id.tv_charge);
            tv_bestand = itemView.findViewById(R.id.tv_bestand);

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

    public SeriennummerCharge GetSeriennummerChargeAt(int position){
        return mFilteredList.get(position);
    }

    public void setFilteredList(List<SeriennummerCharge> filteredList) {
        mFilteredList = filteredList;
        notifyDataSetChanged();
    }

    public Filter getFilter(){
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String filterQuery = charSequence.toString().toLowerCase().trim();
                List<SeriennummerCharge> filteredItems = new ArrayList<>();
                for (SeriennummerCharge item : mSeriennummerChargeList) {
                    if (item.getNummer().toLowerCase().contains(filterQuery)) {
                        filteredItems.add(item);
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredItems;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                mFilteredList = (List<SeriennummerCharge>) filterResults.values;
                setFilteredList(mFilteredList);
            }
        };
    }
}
