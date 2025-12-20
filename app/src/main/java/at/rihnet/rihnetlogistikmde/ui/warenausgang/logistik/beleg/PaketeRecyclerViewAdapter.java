package at.rihnet.rihnetlogistikmde.ui.warenausgang.logistik.beleg;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.models.Paket;

public class PaketeRecyclerViewAdapter extends RecyclerView.Adapter<PaketeRecyclerViewAdapter.PaketeHolder> {
    private List<Paket> mPaketList;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public PaketeRecyclerViewAdapter(List<Paket> pakete) {
        this.mPaketList = pakete;
    }

    @NonNull
    @Override
    public PaketeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_warenausgang_beleg_pakete, parent, false);
        return new PaketeHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PaketeHolder holder, int position) {
        Context context = holder.itemView.getContext();
        Paket currentItem = mPaketList.get(position);
        holder.tv_nummer.setText(context.getString(R.string.pakete_nummer, currentItem.getNummer()));
        holder.tv_gewicht.setText(context.getString(R.string.pakete_gewicht, currentItem.getGewicht()));
        holder.tv_wert.setText(context.getString(R.string.pakete_wert, currentItem.getWert()));
    }

    @Override
    public int getItemCount() {
        if (mPaketList != null) {
            return mPaketList.size();
        } else {
            return 0;
        }
    }

    public Paket getBelegpositionAt(int position) {
        return mPaketList.get(position);
    }

    public void setPakete(List<Paket> pakete) {
        mPaketList = pakete;
        mPaketList = new ArrayList<>(pakete);
    }

    public static class PaketeHolder extends RecyclerView.ViewHolder {
        private final TextView tv_nummer;
        private final TextView tv_gewicht;
        private final TextView tv_wert;


        public PaketeHolder(@NonNull View itemView) {
            super(itemView);
            tv_nummer = itemView.findViewById(R.id.tv_nummer);
            tv_gewicht = itemView.findViewById(R.id.tv_gewicht);
            tv_wert = itemView.findViewById(R.id.tv_wert);
        }
    }
}
