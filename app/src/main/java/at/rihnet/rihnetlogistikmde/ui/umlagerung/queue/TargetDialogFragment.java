package at.rihnet.rihnetlogistikmde.ui.umlagerung.queue;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import java.util.List;
import java.util.Objects;

import at.rihnet.rihnetlogistikmde.CommunicationSelectLine;
import at.rihnet.rihnetlogistikmde.CommunicationSql;
import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.databinding.FragmentTargetDialogBinding;
import at.rihnet.rihnetlogistikmde.models.Artikel;
import at.rihnet.rihnetlogistikmde.ui.umlagerung.UmlagerungViewModel;

public class TargetDialogFragment extends DialogFragment {
    private static final String TAG = "RIHNet";
    private FragmentTargetDialogBinding binding;
    private Artikel artikel;
    private UmlagerungViewModel umlagerungViewModel;

    public TargetDialogFragment() {

    }

    public TargetDialogFragment(Artikel artikel) {
        this.artikel = artikel;
    }

    public static TargetDialogFragment newInstance(Artikel artikel) {
        return new TargetDialogFragment(artikel);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_target_dialog, null);

        AppCompatSpinner acs_spinner = view.findViewById(R.id.acs_lager);
        EditText et_lagerplatz = view.findViewById(R.id.et_lagerplatz);
        EditText et_menge = view.findViewById(R.id.et_menge);
        AppCompatImageButton btn_remove = view.findViewById(R.id.btn_remove);
        AppCompatImageButton btn_add = view.findViewById(R.id.btn_add);

        umlagerungViewModel = new ViewModelProvider(requireActivity()).get(UmlagerungViewModel.class);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        String standort = prefs.getString("standort", null);
        List<String> lager = CommunicationSql.getZiellager(artikel.getLager(), standort);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.item_spinner, lager);
        acs_spinner.setAdapter(adapter);

        TextWatcher mengeTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().equals("0")) {
                    et_menge.setText("1");
                }
                if (editable.toString().length() == 0 || editable.toString().equals("0") || editable.toString().equals("1")) {
                    btn_remove.setEnabled(false);
                    btn_remove.setImageAlpha(50);
                } else {
                    btn_remove.setEnabled(true);
                    btn_remove.setImageAlpha(255);
                }
                if (editable.toString().length() > 0 && Integer.parseInt(editable.toString()) >= artikel.getMenge()) {
                    btn_add.setEnabled(false);
                    btn_add.setImageAlpha(50);
                } else {
                    btn_add.setEnabled(true);
                    btn_add.setImageAlpha(255);
                }
            }
        };
        et_menge.addTextChangedListener(mengeTextWatcher);
        et_menge.setText(String.valueOf(artikel.getMenge()));

        btn_remove.setOnClickListener(v -> {
            if (et_menge.getText().length() > 0) {
                int value = Integer.parseInt(et_menge.getText().toString());
                if (value > 1) {
                    value--;
                } else {
                    value = 1;
                }
                et_menge.setText(String.valueOf(value));
            } else {
                et_menge.setText("1");
            }
        });

        btn_add.setOnClickListener(v -> {
            if (et_menge.getText().length() > 0) {
                et_menge.setText(String.valueOf(Integer.parseInt(et_menge.getText().toString()) + 1));
            } else {
                et_menge.setText("1");
            }
        });

        builder.setView(view).setPositiveButton(R.string.btn_ok, (dialog, id) -> {
                    try {
                        String appKey = prefs.getString("appkey", null);
                        String baseAddress = prefs.getString("baseaddress", null);
                        String userName = prefs.getString("username", null);
                        String password = prefs.getString("password", null);
                        if (CommunicationSelectLine.login(appKey, baseAddress, userName, password)) {
                            android.util.Log.i(TAG, "Anmeldung erfolgreich!");
//                            ManualStorageCreated manualStorageCreated = CommunicationSelectLine.createManualStorage();
//                            if (manualStorageCreated != null) {
//                                android.util.Log.i(TAG, "Belegnummer: " + manualStorageCreated.getManualStorageNumber() + " | Manuelle Lagerung => Beleg erfolgreich erstellt!");
//                                ManualStorageCreated msc = CommunicationSelectLine.storePosition(manualStorageCreated.getManualStorageNumber(), artikel, acs_spinner.getSelectedItem().toString(), 1);
//                                if (msc != null) {
//                                    android.util.Log.i(TAG, "Belegnummer: " + msc.getManualStorageNumber() + " | Manuelle Lagerung => Belegposition erfolgreich erstellt!");
//                                    umlagerungViewModel.removeQueue(artikel);
//                                    umlagerungViewModel.addLog(new Log("Artikelnummer: " + artikel.getArtikelnummer() + " wurde erfolgreich umgelagert!", ContextCompat.getColor(requireContext(), R.color.green_500)));
//                                }
//                            }
                        } else {
                            android.util.Log.e(TAG, "Anmeldung war nicht erfolgreich!");
                        }
                    } catch (Exception e) {
                        android.util.Log.e(TAG, e.getMessage());
                    }
                })
                .setNegativeButton(R.string.btn_abbrechen, (dialog, id) -> {
                    Objects.requireNonNull(TargetDialogFragment.this.getDialog()).cancel();
                })
                .setTitle(R.string.title_ziellager);
        return builder.create();
    }


}
