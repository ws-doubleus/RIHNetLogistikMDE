package at.rihnet.rihnetlogistikmde.ui.warenausgang.logistik.offen;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import java.util.List;
import java.util.Objects;

import at.rihnet.rihnetlogistikmde.CommunicationSql;
import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.audio.SoundPoolManager;
import at.rihnet.rihnetlogistikmde.databinding.FragmentWarenausgangArtikelDialogBinding;
import at.rihnet.rihnetlogistikmde.models.Belegposition;
import at.rihnet.rihnetlogistikmde.ui.warenausgang.logistik.LogistikViewModel;

public class ArtikelDialogFragment extends DialogFragment {
    //private static final String TAG = "RIHNet";

    private int position;
    private Belegposition belegposition;
    private Belegposition belegpositionGebucht;

    private FragmentWarenausgangArtikelDialogBinding binding;
    private AppCompatImageButton btn_remove;
    private AppCompatImageButton btn_add;
    private EditText et_menge;
    private EditText et_seriennummer;
    private Button positiveBtn;
    private LogistikViewModel logistikViewModel;
    private float bestand = 0;

    public ArtikelDialogFragment() {
    }

    public ArtikelDialogFragment(int position, Belegposition belegposition) {
        this.position = position;
        this.belegposition = belegposition;
        this.belegpositionGebucht = belegposition.clone(); // Kopie
    }

    public static ArtikelDialogFragment newInstance(int position, Belegposition belegposition) {
        return new ArtikelDialogFragment(position, belegposition);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        setCancelable(false);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        binding = FragmentWarenausgangArtikelDialogBinding.inflate(requireActivity().getLayoutInflater());

        initUIReferences();
        initViewModel();
        setupDialogButtons(builder);

        String standort = PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("standort", "");
        boolean autoworker = PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean("autoworker", false);
        if(!autoworker){
            belegpositionGebucht.setPaketNummer(1);
        }
        String artikelnummer = belegpositionGebucht.getArtikelnummer();
        String lager = belegpositionGebucht.getLager();
        logistikViewModel.getExecutorService().execute(() -> bestand = CommunicationSql.getArtikelBestand(artikelnummer, standort, lager));

        builder.setView(binding.getRoot());
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        super.onStart();
        AlertDialog dlg = (AlertDialog) getDialog();
        if (dlg == null) return;

        positiveBtn = dlg.getButton(AlertDialog.BUTTON_POSITIVE);
        updatePositiveButtonState(et_seriennummer.getText().toString());

        if ("S".equals(belegposition.getSerieCharge())) {
            et_seriennummer.requestFocus();
            Objects.requireNonNull(dlg.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }

        // ENTER abfangen
        et_seriennummer.setOnKeyListener((v, keyCode, event) ->
                keyCode == KeyEvent.KEYCODE_ENTER
                        && event.getAction() == KeyEvent.ACTION_UP
                        && tryBuchen());

        et_seriennummer.setOnEditorActionListener((v, actionId, event) ->
                actionId == EditorInfo.IME_ACTION_DONE
                        && tryBuchen());

    }

    private boolean tryBuchen() {
        if (positiveBtn != null && positiveBtn.isEnabled()) {
            positiveBtn.performClick();
            return true;
        }
        return false;
    }

    private void initUIReferences() {
        TextView tv_belegnummer = binding.tvBelegnummer;
        TextView tv_position = binding.tvPosition;
        TextView tv_artikelnummer = binding.tvArtikelnummer;
        TextView tv_bezeichnung = binding.tvBezeichnung;
        TextView tv_seriennummer_label = binding.tvSeriennummerLabel;
        btn_remove = binding.btnRemove;
        btn_add = binding.btnAdd;
        et_menge = binding.etMenge;
        et_seriennummer = binding.etSeriennummer;

        if (!Objects.equals(belegposition.getSerieCharge(), "O")) {
            tv_seriennummer_label.setVisibility(View.VISIBLE);
            et_seriennummer.setVisibility(View.VISIBLE);
            //et_seriennummer.setFocusable(FOCUSABLE);
            if (Objects.equals(belegposition.getSerieCharge(), "S")) {
                et_menge.setEnabled(false);

            }
        }

        tv_belegnummer.setText(belegposition.getBelegnummer());
        tv_position.setText(belegposition.getPostext());
        tv_artikelnummer.setText(belegposition.getArtikelnummer());
        tv_bezeichnung.setText(belegposition.getBezeichnung());

        et_menge.addTextChangedListener(createMengeTextWatcher());

        if (Objects.equals(belegposition.getSerieCharge(), "S")) {
            et_menge.setText("1");
        } else {
            et_menge.setText(String.valueOf(belegposition.getOffen()));
        }

        et_seriennummer.addTextChangedListener(createTextWatcherSeriennummer());

        btn_remove.setOnClickListener(v -> handleRemoveClick(et_menge));

        btn_add.setOnClickListener(v -> handleAddClick(et_menge));
    }

    private void initViewModel() {
        logistikViewModel = new ViewModelProvider(requireActivity()).get(LogistikViewModel.class);
    }

    private void setupDialogButtons(AlertDialog.Builder builder) {
        builder.setTitle(R.string.title_artikel_buchen).setPositiveButton(R.string.btn_buchen, (dialog, id) -> logistikViewModel.getExecutorService().execute(() -> {
                    int gebuchteMenge = Objects.requireNonNull(logistikViewModel.getGebucht().getValue()).stream().filter(bp -> belegpositionGebucht.getArtikelnummer().equals(bp.getArtikelnummer()))
                            .mapToInt(Belegposition::getMenge)
                            .sum();
                    int menge = Integer.parseInt(et_menge.getText().toString());
                    if ((bestand - gebuchteMenge - menge) < 0) {
                        requireActivity().runOnUiThread(() -> {
                            SoundPoolManager.getInstance(requireContext()).playError();
                            Toast.makeText(getContext(), "Lagerbestand ist zu gering!", Toast.LENGTH_SHORT).show();
                        });
                        return;
                    }

                    if (belegpositionGebucht.getSerieCharge().equals("S")) {
                        List<Belegposition> gebucht = logistikViewModel.getGebucht().getValue();
                        assert gebucht != null;
                        boolean found = gebucht.stream()
                                .anyMatch(a -> Objects.equals(
                                        a.getSeriennummer(),
                                        et_seriennummer.getText().toString()
                                ));
                        if (found) {
                            requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Seriennummer: " + et_seriennummer.getText().toString() + " wurde schon gebucht!", Toast.LENGTH_SHORT).show());
                            return;
                        } else {
                            if (CommunicationSql.existsSerieCharge(belegpositionGebucht.getArtikelnummer(), et_seriennummer.getText().toString())) {
                                requireActivity().runOnUiThread(() -> {
                                    SoundPoolManager.getInstance(requireContext()).playError();
                                    Toast.makeText(getContext(), "Keine gültige Seriennummer!", Toast.LENGTH_SHORT).show();
                                });
                                return;
                            }
                        }
                    }

                    int offen = belegposition.getOffen() - Integer.parseInt(et_menge.getText().toString());
                    requireActivity().runOnUiThread(() -> logistikViewModel.updateOffen(position, Math.max(0, offen)));

                    belegpositionGebucht.setMenge(Integer.parseInt(et_menge.getText().toString()));

                    handleQueueInsert();

                    requireActivity().runOnUiThread(() -> {
                        if (Math.max(0, offen) == 0) {
                            logistikViewModel.removeOffen(position);
                        }
                        logistikViewModel.setSearchArtikel(null);
                        dialog.dismiss();
                    });
                }))
                .setNegativeButton(R.string.btn_abbrechen, (dialog, id) -> requireActivity().runOnUiThread(() -> {
                    logistikViewModel.setSearchArtikel(null);
                    dialog.dismiss();
                }));
    }

    private void handleQueueInsert() {
        Log.e("RIHNet", "belegpositionGebucht.getPaketNummer(): " + belegpositionGebucht.getPaketNummer());
        if (belegpositionGebucht.getSerieCharge().equals("S")) {
            belegpositionGebucht.setSeriennummer(et_seriennummer.getText().toString());
        } else {
            belegpositionGebucht.setSeriennummer("---");
        }
        requireActivity().runOnUiThread(() -> logistikViewModel.addGebucht(belegpositionGebucht));
    }

    private TextWatcher createMengeTextWatcher() {
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
                int offen = belegposition.getOffen() - Integer.parseInt(et_menge.getText().toString());


                if (text.isEmpty() || "0".equals(text) || "1".equals(text)) {
                    btn_remove.setEnabled(false);
                    btn_remove.setImageAlpha(50);
                } else {
                    btn_remove.setEnabled(true);
                    btn_remove.setImageAlpha(255);
                }

                if (belegposition.getSerieCharge().equals("S")) {
                    btn_add.setEnabled(false);
                    btn_add.setImageAlpha(50);
                }

                if (offen < 0) {
                    et_menge.setText("1");
                }
            }
        };
    }

    private TextWatcher createTextWatcherSeriennummer() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updatePositiveButtonState(s.toString());
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

    private void updatePositiveButtonState(String seriennummer) {
        if (positiveBtn != null) {
            if (belegpositionGebucht.getSerieCharge().equals("S")) {
                positiveBtn.setEnabled(!seriennummer.trim().isEmpty());
            } else {
                positiveBtn.setEnabled(true);
            }
        }
    }
}
