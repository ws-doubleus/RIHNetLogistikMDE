package at.rihnet.rihnetlogistikmde.ui.settings;

import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;

import androidx.annotation.NonNull;
import androidx.preference.EditTextPreference;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import at.rihnet.rihnetlogistikmde.R;

public class SettingsFragment extends PreferenceFragmentCompat {
    private boolean internalChange = false;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        EditTextPreference preferenceKennwort = findPreference("kennwort");
        if (preferenceKennwort != null) {
            preferenceKennwort.setSummaryProvider(p -> {
                String getPassword = PreferenceManager.getDefaultSharedPreferences(requireActivity()).getString("kennwort", "Nicht festgelegt");
                if (getPassword.equals("Nicht festgelegt")) {
                    return getPassword;
                } else {
                    return (setAsterisks(getPassword.length()));
                }
            });

            preferenceKennwort.setOnBindEditTextListener(editText -> {
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                preferenceKennwort.setSummaryProvider(p -> setAsterisks(editText.getText().toString().length()));
            });
        }

        EditTextPreference preferencePassword = findPreference("password");
        if (preferencePassword != null) {
            preferencePassword.setSummaryProvider(p -> {
                String getPassword = PreferenceManager.getDefaultSharedPreferences(requireActivity()).getString("password", "Nicht festgelegt");
                if (getPassword.equals("Nicht festgelegt")) {
                    return getPassword;
                } else {
                    return (setAsterisks(getPassword.length()));
                }
            });

            preferencePassword.setOnBindEditTextListener(editText -> {
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                preferencePassword.setSummaryProvider(p -> setAsterisks(editText.getText().toString().length()));
            });
        }

        EditTextPreference preferenceDruckplatz = findPreference("druckplatz");
        if (preferenceDruckplatz != null) {
            preferenceDruckplatz.setOnBindEditTextListener(editText -> {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                editText.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
            });
        }

        MultiSelectListPreference pref = findPreference("belegtyp");
        if (pref != null) {
            pref.setSummaryProvider((Preference.SummaryProvider<MultiSelectListPreference>) preference -> {
                Set<String> values = preference.getValues();

                if (values == null || values.isEmpty()) {
                    return "Nicht festgelegt";  // oder leer lassen
                }

                // 1) Kopiere in eine Liste
                List<String> sortedList = new ArrayList<>(values);

                // 2) Sortiere die Liste (alphabetisch)
                Collections.sort(sortedList);

                // 3) Erzeuge eine kommaseparierte Ausgabe
                return TextUtils.join(", ", sortedList);
            });
        }

        MultiSelectListPreference prefPaketlabel = findPreference("paketlabel_belegtyp");
        if (prefPaketlabel != null) {
            prefPaketlabel.setSummaryProvider((Preference.SummaryProvider<MultiSelectListPreference>) preference -> {
                Set<String> values = preference.getValues();

                if (values == null || values.isEmpty()) {
                    return "Nicht festgelegt";  // oder leer lassen
                }

                // 1) Kopiere in eine Liste
                List<String> sortedList = new ArrayList<>(values);

                // 2) Sortiere die Liste (alphabetisch)
                Collections.sort(sortedList);

                // 3) Erzeuge eine kommaseparierte Ausgabe
                return TextUtils.join(", ", sortedList);
            });
        }

        SwitchPreference paketPref = findPreference("paketanbindung");
        SwitchPreference autoPref  = findPreference("autoworker");
        if (paketPref != null && autoPref != null) {
            if (paketPref.isChecked() && autoPref.isChecked()) {
                autoPref.setChecked(false);
            }
            applyExclusionRules(paketPref, autoPref);
            paketPref.setOnPreferenceChangeListener((p, newVal) -> {
                applyExclusionRules(paketPref, autoPref, (Boolean) newVal);
                return true;
            });
            autoPref.setOnPreferenceChangeListener((p, newVal) -> {
                applyExclusionRules(autoPref, paketPref, (Boolean) newVal);
                return true;
            });
        }
    }

    private void applyExclusionRules(SwitchPreference first, SwitchPreference second) {
        applyExclusionRules(first, second, first.isChecked());
    }

    private void applyExclusionRules(@NonNull SwitchPreference primary, @NonNull SwitchPreference secondary, boolean primaryState) {
        if (internalChange) return;
        internalChange = true;
        if (primaryState) {
            if (secondary.isChecked()) secondary.setChecked(false);
            secondary.setEnabled(false);
        } else {
            secondary.setEnabled(true);
        }
        internalChange = false;
    }

    private String setAsterisks(int length) {
        if (length > 0) {
            StringBuilder sb = new StringBuilder();
            for (int s = 0; s < length; s++) {
                sb.append("*");
            }
            return sb.toString();
        } else {
            return "Nicht festgelegt";
        }
    }
}