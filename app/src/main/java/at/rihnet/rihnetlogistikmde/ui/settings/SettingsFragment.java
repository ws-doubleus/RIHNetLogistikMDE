package at.rihnet.rihnetlogistikmde.ui.settings;

import android.os.Bundle;
import android.text.InputType;

import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import at.rihnet.rihnetlogistikmde.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        EditTextPreference preferenceKennwort = findPreference("kennwort");
        if (preferenceKennwort != null) {
            preferenceKennwort.setSummaryProvider(p -> {
                String getPassword = PreferenceManager.getDefaultSharedPreferences(requireActivity()).getString("kennwort", "Nicht festgelegt");
                assert getPassword != null;
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
                assert getPassword != null;
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