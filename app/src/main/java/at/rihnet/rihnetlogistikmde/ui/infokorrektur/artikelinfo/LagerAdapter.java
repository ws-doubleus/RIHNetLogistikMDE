package at.rihnet.rihnetlogistikmde.ui.infokorrektur.artikelinfo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.models.LagerplatzBestand;

public class LagerAdapter extends BaseAdapter {
    private final List<LagerplatzBestand> lagerBestandList;
    private final LayoutInflater inflater;

    public LagerAdapter(Context context, List<LagerplatzBestand> lagerBestandList){
        this.lagerBestandList = lagerBestandList;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return lagerBestandList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflater.inflate(R.layout.item_lager, null);
        TextView tv_lager = view.findViewById(R.id.tv_lager);
        TextView tv_bestand = view.findViewById(R.id.tv_bestand);
        tv_lager.setText(lagerBestandList.get(i).getLager());
        //tv_bestand.setText(lagerBestandList.get(i).getBestand());
        return view;
    }
}
