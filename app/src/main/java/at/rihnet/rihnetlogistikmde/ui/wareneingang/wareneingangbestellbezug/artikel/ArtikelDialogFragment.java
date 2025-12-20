package at.rihnet.rihnetlogistikmde.ui.wareneingang.wareneingangbestellbezug.artikel;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.room.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.databinding.FragmentWareneingangBestellbezugArtikelDialogBinding;
import at.rihnet.rihnetlogistikmde.models.Belegposition;
import at.rihnet.rihnetlogistikmde.models.Kategorie;
import at.rihnet.rihnetlogistikmde.sqlite.MyDatabase;
import at.rihnet.rihnetlogistikmde.sqlite.QueueBelegpositionDAO;
import at.rihnet.rihnetlogistikmde.ui.wareneingang.wareneingangbestellbezug.WareneingangBestellbezugViewModel;

public class ArtikelDialogFragment extends DialogFragment {

    private static final String TAG = "RIHNet";

    private FragmentWareneingangBestellbezugArtikelDialogBinding binding;

    // Aus dem Konstruktor / newInstance
    private int position;
    private Belegposition belegposition;
    private Belegposition belegpositionQueue; // Kopie zum Einfügen in DB

    // Room-DAO
    private QueueBelegpositionDAO queueBelegpositionDAO;

    public ArtikelDialogFragment() {
        // Default-Konstruktor
    }

    public ArtikelDialogFragment(int position, Belegposition belegposition) {
        this.position = position;
        this.belegposition = belegposition;
        this.belegpositionQueue = belegposition.clone(); // Kopie
    }

    public static ArtikelDialogFragment newInstance(int position, Belegposition belegposition) {
        return new ArtikelDialogFragment(position, belegposition);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        setCancelable(false);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        binding = FragmentWareneingangBestellbezugArtikelDialogBinding.inflate(requireActivity().getLayoutInflater());

        // View-Referenzen
        TextView tv_belegnummer = binding.tvBelegnummer;
        TextView tv_position = binding.tvPosition;
        TextView tv_artikelnummer = binding.tvArtikelnummer;
        TextView tv_bezeichnung = binding.tvBezeichnung;
        EditText et_menge = binding.etMenge;
        AppCompatImageButton btn_remove = binding.btnRemove;
        AppCompatImageButton btn_add = binding.btnAdd;
        TextView tv_seriennummer_label = binding.tvSeriennummerLabel;
        EditText et_seriennummer = binding.etSeriennummer;

        // UI initialisieren
        initUI(tv_belegnummer, tv_position, tv_artikelnummer, tv_bezeichnung, et_menge,
                btn_remove, btn_add, tv_seriennummer_label, et_seriennummer);

        // Positive / Negative Buttons
        setupDialogButtons(builder, et_menge);

        // Room-Datenbank initialisieren (ohne allowMainThreadQueries!)
//        MyDatabase myDatabase = Room.databaseBuilder(requireContext(), MyDatabase.class, "rihnetdatabase")
//                .fallbackToDestructiveMigration()
//                .build(); // <-- kein allowMainThreadQueries()
        MyDatabase myDatabase = MyDatabase.getInstance(requireContext());
        queueBelegpositionDAO = myDatabase.getQueueBelegpositionDAO();

        builder.setView(binding.getRoot());
        return builder.create();
    }

    private void initUI(TextView tv_belegnummer,
                        TextView tv_position,
                        TextView tv_artikelnummer,
                        TextView tv_bezeichnung,
                        EditText et_menge,
                        AppCompatImageButton btn_remove,
                        AppCompatImageButton btn_add,
                        TextView tv_seriennummer_label,
                        EditText et_seriennummer) {

        if (!Objects.equals(belegposition.getSerieCharge(), "O")) {
            tv_seriennummer_label.setVisibility(View.VISIBLE);
            et_seriennummer.setVisibility(View.VISIBLE);
        }

        tv_belegnummer.setText(belegposition.getBelegnummer());
        tv_position.setText(belegposition.getPostext());
        tv_artikelnummer.setText(belegposition.getArtikelnummer());
        tv_bezeichnung.setText(belegposition.getBezeichnung());

        // Menge-TextWatcher
        et_menge.addTextChangedListener(createMengeTextWatcher(et_menge, btn_remove));
        et_menge.setText(String.valueOf(belegposition.getOffen()));

        // Minus-Button
        btn_remove.setOnClickListener(v -> handleRemoveClick(et_menge));

        // Plus-Button
        btn_add.setOnClickListener(v -> handleAddClick(et_menge));
    }

    /**
     * Baut den Dialog (Positive/Negative Buttons) auf.
     */
    private void setupDialogButtons(AlertDialog.Builder builder, EditText et_menge) {
        builder
                .setTitle(R.string.title_zur_queue_hinzufuegen)
                .setPositiveButton(R.string.btn_hinzufuegen, (dialog, id) -> {
                    Log.d(TAG, "PositiveButton geklickt");

                    WareneingangBestellbezugViewModel wareneingangBestellbezugViewModel =
                            new ViewModelProvider(requireActivity()).get(WareneingangBestellbezugViewModel.class);

                    // 1) Offen berechnen / Belegposition in VM anpassen (Main-Thread)
                    int offen = belegposition.getOffen() - Integer.parseInt(et_menge.getText().toString());
                    wareneingangBestellbezugViewModel.updateBelegposition(position, Math.max(0, offen));

                    // 2) Menge in belegpositionQueue übernehmen (Main-Thread)
                    belegpositionQueue.setMenge(Integer.parseInt(et_menge.getText().toString()));

                    // 3) Kopie der aktuellen Queue-Liste, um sie asynchron zu verarbeiten
                    List<Belegposition> queueCopy = new ArrayList<>();
                    List<Belegposition> belegpositionen = wareneingangBestellbezugViewModel.getQueue().getValue();
                    if (belegpositionen != null) {
                        queueCopy.addAll(belegpositionen);
                    }

                    // 4) Asynchron DB-Aufruf starten
                    wareneingangBestellbezugViewModel.getExecutor().execute(() -> {
                        // DB-Operationen aus handleQueueInsert(...) im Hintergrund
                        handleQueueInsertInBackground(queueCopy, wareneingangBestellbezugViewModel);

                        // Danach im Main-Thread Dialog schließen
                        requireActivity().runOnUiThread(dialog::dismiss);
                    });
                })
                .setNegativeButton(R.string.btn_abbrechen, (dialog, id) -> dialog.dismiss());
    }

    /**
     * Führt die DB-Operationen asynchron aus (statt im Main-Thread).
     */
    private void handleQueueInsertInBackground(List<Belegposition> belegpositionen,
                                               WareneingangBestellbezugViewModel wareneingangBestellbezugViewModel) {
        // Hier läuft das, was bisher in "handleQueueInsert(...)" passierte:
        // DB-Zugriffe nur im Hintergrund, danach ggf. LiveData-Updates im Main-Thread.
        Optional<Belegposition> gefundeneBelegposition = belegpositionen.stream()
                .filter(b -> b.getArtikelnummer().equals(belegposition.getArtikelnummer())
                        && b.getBelegnummer().equals(belegposition.getBelegnummer())
                        && b.getPostext().equals(belegposition.getPostext()))
                .findFirst();

        if (gefundeneBelegposition.isPresent()) {
            // => es gibt bereits eine Belegposition in der Queue
            Belegposition bp = queueBelegpositionDAO.getBelegpositionByBelegtypBelegnummerArtikelnummer(
                    belegpositionQueue.getBelegtyp(),
                    belegpositionQueue.getBelegnummer(),
                    belegpositionQueue.getPostext(),
                    belegpositionQueue.getArtikelnummer(),
                    Kategorie.WARENEINGANGBESTELLBEZUG
            );
            if (bp != null) {
                int nochOffen = bp.getOffen() - bp.getMenge();
                if (nochOffen >= belegpositionQueue.getMenge()) {
                    queueBelegpositionDAO.updateBelegpositionById(bp.getId(), (bp.getMenge() + belegpositionQueue.getMenge()));
                    belegpositionQueue.setMenge(bp.getMenge() + belegpositionQueue.getMenge());
                } else {
                    int menge = belegpositionQueue.getMenge();
                    queueBelegpositionDAO.updateBelegpositionById(bp.getId(), bp.getOffen());
                    belegpositionQueue.setMenge(bp.getOffen());

                    Belegposition belegpositionOhneBelegnummer = queueBelegpositionDAO
                            .getBelegpositionByBelegtypBelegnummerArtikelnummer("-", "---", "---",
                                    belegpositionQueue.getArtikelnummer(), Kategorie.WARENEINGANGBESTELLBEZUG);

                    if (belegpositionOhneBelegnummer != null) {
                        queueBelegpositionDAO.updateBelegpositionById(
                                belegpositionOhneBelegnummer.getId(),
                                (belegpositionOhneBelegnummer.getMenge() + menge - nochOffen)
                        );
                    } else {
                        belegpositionQueue.setBelegtyp("-");
                        belegpositionQueue.setBelegnummer("---");
                        belegpositionQueue.setPostext("---");
                        belegpositionQueue.setKennung("");
                        belegpositionQueue.setMenge(menge - nochOffen);
                        queueBelegpositionDAO.insert(belegpositionQueue);
                    }
                }
            }
        } else {
            // => es existiert noch keine Belegposition mit diesen Attributen
            if (belegpositionQueue.getMenge() > belegpositionQueue.getOffen()) {
                int diff = belegpositionQueue.getMenge() - belegpositionQueue.getOffen();
                belegpositionQueue.setMenge(belegpositionQueue.getOffen());
                queueBelegpositionDAO.insert(belegpositionQueue);

                // LiveData aktualisieren => muss im Main-Thread erfolgen
                requireActivity().runOnUiThread(() -> wareneingangBestellbezugViewModel.addQueue1(belegpositionQueue));

                // Rest-Menge ggf. als '-','---','---' anlegen
                Belegposition belegpositionOhneBelegnummer = queueBelegpositionDAO
                        .getBelegpositionByBelegtypBelegnummerArtikelnummer("-", "---", "---",
                                belegpositionQueue.getArtikelnummer(), Kategorie.WARENEINGANGBESTELLBEZUG);

                if (belegpositionOhneBelegnummer != null) {
                    queueBelegpositionDAO.updateBelegpositionById(
                            belegpositionOhneBelegnummer.getId(),
                            (belegpositionOhneBelegnummer.getMenge() + diff)
                    );
                } else {
                    belegpositionQueue.setBelegtyp("-");
                    belegpositionQueue.setBelegnummer("---");
                    belegpositionQueue.setPostext("---");
                    belegpositionQueue.setKennung("");
                    belegpositionQueue.setMenge(diff);
                    queueBelegpositionDAO.insert(belegpositionQueue);

                    requireActivity().runOnUiThread(() -> wareneingangBestellbezugViewModel.addQueue1(belegpositionQueue));
                }
            } else {
                // Einfach direkt einfügen
                queueBelegpositionDAO.insert(belegpositionQueue);
                requireActivity().runOnUiThread(() -> wareneingangBestellbezugViewModel.addQueue1(belegpositionQueue));
            }
        }
    }

    // ----------------------------------------------------------------
    // Hilfs-Methoden für Menge (Plus/Minus)
    // ----------------------------------------------------------------

    private TextWatcher createMengeTextWatcher(EditText et_menge, AppCompatImageButton btn_remove) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { /* nichts */ }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { /* nichts */ }
            @Override
            public void afterTextChanged(Editable editable) {
                String text = editable.toString();
                if ("0".equals(text)) {
                    et_menge.setText("1");
                }
                if (text.isEmpty() || "0".equals(text) || "1".equals(text)) {
                    btn_remove.setEnabled(false);
                    btn_remove.setImageAlpha(50);
                } else {
                    btn_remove.setEnabled(true);
                    btn_remove.setImageAlpha(255);
                }
            }
        };
    }

    private void handleRemoveClick(EditText et_menge) {
        String currentText = et_menge.getText().toString();
        if (!currentText.isEmpty()) {
            int value = Integer.parseInt(currentText);
            if (value > 1) {
                value--;
            } else {
                value = 1;
            }
            et_menge.setText(String.valueOf(value));
        } else {
            et_menge.setText("1");
        }
    }

    private void handleAddClick(EditText et_menge) {
        String currentText = et_menge.getText().toString();
        if (!currentText.isEmpty()) {
            int value = Integer.parseInt(currentText) + 1;
            et_menge.setText(String.valueOf(value));
        } else {
            et_menge.setText("1");
        }
    }
}
