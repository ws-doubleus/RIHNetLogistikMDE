package at.rihnet.rihnetlogistikmde.ui.wareneingang.freierwareneingang.artikel;

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

public class SeriennummerRecyclerViewAdapter extends RecyclerView.Adapter<SeriennummerRecyclerViewAdapter.SeriennummerHolder> {
    private final List<SeriennummerCharge> mSeriennummerChargeList;
    private List<SeriennummerCharge> mFilteredList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public SeriennummerRecyclerViewAdapter(List<SeriennummerCharge> seriennummerChargeList) {
        mSeriennummerChargeList = seriennummerChargeList;
        mFilteredList = new ArrayList<>(seriennummerChargeList);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public SeriennummerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_seriennummer, parent, false);
        return new SeriennummerHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull SeriennummerHolder holder, int position) {
        SeriennummerCharge currentItem = mFilteredList.get(position);
        holder.tv_Seriennummer.setText(currentItem.getNummer());
    }

    @Override
    public int getItemCount() {
        if (mFilteredList != null) {
            return mFilteredList.size();
        } else {
            return 0;
        }
    }

    public static class SeriennummerHolder extends RecyclerView.ViewHolder {
        private final TextView tv_Seriennummer;

        public SeriennummerHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            tv_Seriennummer = itemView.findViewById(R.id.tv_seriennummer);
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
