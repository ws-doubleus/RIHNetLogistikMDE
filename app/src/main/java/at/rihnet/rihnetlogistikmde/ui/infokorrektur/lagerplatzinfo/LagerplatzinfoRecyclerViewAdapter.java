package at.rihnet.rihnetlogistikmde.ui.infokorrektur.lagerplatzinfo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.models.Lagerplatzinfo;

public class LagerplatzinfoRecyclerViewAdapter extends RecyclerView.Adapter<LagerplatzinfoRecyclerViewAdapter.LagerplatzinfoHolder> {
    //private static final String TAG = "RIHNet";
    private List<Lagerplatzinfo> lagerplatzinfoList;

    public LagerplatzinfoRecyclerViewAdapter(List<Lagerplatzinfo> lagerplatzinfoList) {
        this.lagerplatzinfoList = lagerplatzinfoList;
    }

    @NonNull
    @Override
    public LagerplatzinfoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lagerplatzinfo, parent, false);
        return new LagerplatzinfoHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull LagerplatzinfoHolder holder, int position) {
        Lagerplatzinfo currentItem = lagerplatzinfoList.get(position);
            holder.tv_artikelnummer.setText(currentItem.getArtikelnummer());
            holder.tv_bezeichnung.setText(currentItem.getBezeichnung());
            holder.tv_menge.setText(String.valueOf(currentItem.getMenge()));

    }

    @Override
    public int getItemCount() {
        if (lagerplatzinfoList != null) {
            return lagerplatzinfoList.size();
        } else {
            return 0;
        }
    }

    public static class LagerplatzinfoHolder extends RecyclerView.ViewHolder {
        private final TextView tv_artikelnummer;
        private final TextView tv_bezeichnung;
        private final TextView tv_menge;

        public LagerplatzinfoHolder(@NonNull View itemView) {
            super(itemView);
            tv_artikelnummer = itemView.findViewById(R.id.tv_artikelnummer);
            tv_bezeichnung = itemView.findViewById(R.id.tv_bezeichnung);
            tv_menge = itemView.findViewById(R.id.tv_menge);
        }
    }

    public void setLagerplatzinfo(List<Lagerplatzinfo> lagerplatzinfos) {
        lagerplatzinfoList = lagerplatzinfos;
    }

}
