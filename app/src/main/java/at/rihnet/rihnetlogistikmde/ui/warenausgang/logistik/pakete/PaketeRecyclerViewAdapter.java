package at.rihnet.rihnetlogistikmde.ui.warenausgang.logistik.pakete;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipDescription;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.models.Belegposition;
import at.rihnet.rihnetlogistikmde.models.Paket;
import at.rihnet.rihnetlogistikmde.ui.drag.CardShadowBuilder;
import at.rihnet.rihnetlogistikmde.ui.drag.PaketShadowBuilder;

/**
 * 1 Card = 1 Paket (äußere RV)  –  Kind‑Positionen in verschachtelter RV.
 * * Swipe:*   Position löschen
 * * Drag :*   Position per Long‑Press in anderes Paket verschieben
 */
public class PaketeRecyclerViewAdapter
        extends RecyclerView.Adapter<PaketeRecyclerViewAdapter.PaketVH> {

    private final List<Paket> daten;

    public PaketeRecyclerViewAdapter(List<Paket> pakete) {
        this.daten = pakete;
    }

    @NonNull
    @Override
    public PaketVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_warenausgang_pakete, parent, false);
        return new PaketVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PaketVH holder, int position) {
        holder.bind(daten.get(position), position);          // ▲ NEU (index mitgeben)
    }

    @Override
    public int getItemCount() {
        return daten == null ? 0 : daten.size();
    }

    public Paket removePaketAndReturn(int index) {
        Paket removed = daten.remove(index);
        notifyItemRemoved(index);
        return removed;
    }

    void restorePaketAt(int index, Paket paket) {
        daten.add(index, paket);
        notifyItemInserted(index);
    }

    /* --------------------------------------------------------------
       ViewHolder für die Paket‑Karte
       -------------------------------------------------------------- */
  public  class PaketVH extends RecyclerView.ViewHolder {

        private final TextView tvNummer;
        private final ImageView ivArrow;
        private final RecyclerView rvPos;

        private PositionenAdapter childAdapter;

        PaketVH(@NonNull View itemView) {
            super(itemView);
            tvNummer = itemView.findViewById(R.id.tv_nummer);
            ivArrow = itemView.findViewById(R.id.iv_arrow);
            rvPos = itemView.findViewById(R.id.rvPositionen);

            LinearLayoutManager layoutManager = new LinearLayoutManager(itemView.getContext());
            rvPos.setLayoutManager(layoutManager);
            DividerItemDecoration divider = new DividerItemDecoration(itemView.getContext(), layoutManager.getOrientation());
            rvPos.addItemDecoration(divider);
            rvPos.setNestedScrollingEnabled(false);

            /* ---------- Karten‑Drop‑Listener ---------------------------------------- */
            itemView.setOnDragListener((view, event) -> {
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        return event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN);
                    case DragEvent.ACTION_DROP:          // Item hierher verschieben
                        int tgtPakIdx = getAdapterPosition();
                        if (tgtPakIdx == RecyclerView.NO_POSITION) return true;

                        String text = event.getClipData().getItemAt(0).getText().toString();
                        String[] parts = text.split(";");
                        int srcPakIdx = Integer.parseInt(parts[0]);
                        int srcItemIdx = Integer.parseInt(parts[1]);

                        if (srcPakIdx == tgtPakIdx) return true;        // gleiches Paket

                        Paket srcPak = daten.get(srcPakIdx);
                        Paket tgtPak = daten.get(tgtPakIdx);

                        Belegposition moved = srcPak.getPositionen().remove(srcItemIdx);
                        tgtPak.getPositionen().add(moved);

                        /* Externe & interne Adapter refreshen */
                        notifyItemChanged(srcPakIdx);
                        notifyItemChanged(tgtPakIdx);
                        return true;
                    default:
                        return true;
                }
            });
        }

        void bind(Paket paket, int paketIndex) {
            tvNummer.setText(itemView.getContext().getString(R.string.pakete_nummer, paket.getNummer()));

            /* ---------- Kind‑Adapter + Swipe‑Helper ------------------------------- */
            if (childAdapter == null) {
                childAdapter = new PositionenAdapter(paket.getPositionen(), paketIndex);
                rvPos.setAdapter(childAdapter);

                /* --------- 1. Item herausnehmen -------------- */
                /* --------- 2. Snackbar anzeigen -------------- */
                /* --------- 3. Bei Klick wieder einfügen --------- */
                // optional: zum Item springen
                ItemTouchHelper swipeHelper = new ItemTouchHelper(
                        new ItemTouchHelper.SimpleCallback(0,
                                ItemTouchHelper.START | ItemTouchHelper.END) {
                            @Override
                            public boolean onMove(@NonNull RecyclerView r,
                                                  @NonNull RecyclerView.ViewHolder v1,
                                                  @NonNull RecyclerView.ViewHolder v2) {
                                return false;
                            }

                            @Override
                            public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int dir) {
                                int idx = vh.getAdapterPosition();

                                /* --------- 1. Item herausnehmen -------------- */
                                Belegposition deleted = childAdapter.removeAndReturn(idx);

                                /* --------- 2. Snackbar anzeigen -------------- */
                                Snackbar.make(rvPos, "Belegposition gelöscht!", Snackbar.LENGTH_LONG)
                                        .setAction("WIEDERHERSTELLEN", v -> {
                                            /* --------- 3. Bei Klick wieder einfügen --------- */
                                            childAdapter.restoreAt(idx, deleted);
                                            rvPos.scrollToPosition(idx);          // optional: zum Item springen
                                        })
                                        .show();
                            }
                        });
                swipeHelper.attachToRecyclerView(rvPos);
            } else {
                childAdapter.update(paket.getPositionen(), paketIndex);
            }

            /* ---------- Expand / Collapse ----------------------------------------- */
            boolean expanded = paket.isExpanded();
            rvPos.setVisibility(expanded ? View.VISIBLE : View.GONE);
            ivArrow.setRotation(expanded ? 180f : 0f);

            itemView.setOnClickListener(v -> {
                paket.setExpanded(!paket.isExpanded());
                ivArrow.animate().rotation(paket.isExpanded() ? 180f : 0f)
                        .setDuration(180).start();
                rvPos.setVisibility(paket.isExpanded() ? View.VISIBLE : View.GONE);
            });
        }
    }

    /* --------------------------------------------------------------
       Innerer Adapter für Belegpositionen
       -------------------------------------------------------------- */
    private static class PositionenAdapter extends RecyclerView.Adapter<PositionenAdapter.PosVH> {
        private List<Belegposition> posList;
        private int paketIdx;

        PositionenAdapter(List<Belegposition> daten, int pakIndex) {
            posList = daten;
            paketIdx = pakIndex;
        }

        @SuppressLint("NotifyDataSetChanged")
        void update(List<Belegposition> newData, int pakIndex) {
            posList = newData;
            paketIdx = pakIndex;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public PosVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_warenausgang_pakete_row, parent, false);
            return new PosVH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull PosVH holder, int position) {
            holder.bind(posList.get(position), paketIdx, position);
        }

        @Override
        public int getItemCount() {
            return posList == null ? 0 : posList.size();
        }

        Belegposition removeAndReturn(int index) {
            Belegposition removed = posList.remove(index);
            notifyItemRemoved(index);
            return removed;
        }

        void restoreAt(int index, Belegposition item) {
            posList.add(index, item);
            notifyItemInserted(index);
        }

        /* ---------- ViewHolder für Position ---------- */
        static class PosVH extends RecyclerView.ViewHolder {
            final TextView tvTitle;
            final TextView tv_menge;

            PosVH(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvItemTitle);
                tv_menge = itemView.findViewById(R.id.tv_menge);
            }

            void bind(Belegposition bp, int pakIndex, int itemIndex) {
                tvTitle.setText(bp.getBezeichnung());
                tv_menge.setText(String.valueOf(bp.getMenge()));

                /* ---------- Drag‑Start (Long‑Press) ------------------------------- */
                itemView.setOnLongClickListener(view -> {
                    String payload = pakIndex + ";" + itemIndex;          // "2;0"
                    ClipData data = ClipData.newPlainText("dragPos", payload);
                    //PaketShadowBuilder shadow = new PaketShadowBuilder(view);
                    CardShadowBuilder shadow = new CardShadowBuilder(view);
                    view.startDragAndDrop(data, shadow, null, 0);
                    return true;
                });
            }
        }
    }
}
