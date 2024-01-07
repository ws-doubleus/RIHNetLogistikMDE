package at.rihnet.rihnetlogistikmde.ui.info;

import android.app.Dialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.pm.PackageInfoCompat;
import androidx.fragment.app.DialogFragment;

import java.util.Objects;

import at.rihnet.rihnetlogistikmde.R;

public class InfoDialogFragment extends DialogFragment {
    public InfoDialogFragment() {
        // Required empty public constructor
    }

    public static InfoDialogFragment newInstance() {
        return new InfoDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_info_dialog, null);
        TextView tv_versionnummer = view.findViewById(R.id.tv_versionnummer);
        TextView tv_versionname = view.findViewById(R.id.tv_versionname);
        try {
            PackageInfo pinfo = requireActivity(). getPackageManager().getPackageInfo(requireActivity().getPackageName(), 0);
            tv_versionnummer.setText(getString(R.string.info_versionnummer,  PackageInfoCompat.getLongVersionCode(pinfo)));
            tv_versionname.setText(getString(R.string.info_versionname, pinfo.versionName));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        builder.setView(view)
                .setPositiveButton(R.string.btn_ok, (dialog, id) -> Objects.requireNonNull(InfoDialogFragment.this.getDialog()).cancel())
                .setTitle(R.string.title_info);
        return builder.create();
    }
}
