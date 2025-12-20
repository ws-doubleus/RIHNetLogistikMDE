package at.rihnet.rihnetlogistikmde.ui.loading;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.color.MaterialColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.loadingindicator.LoadingIndicator;

import at.rihnet.rihnetlogistikmde.R;

public class LoadingDialogFragment extends DialogFragment {
    private static final String ARG_MESSAGE = "message";

    public LoadingDialogFragment() {
        // Required empty public constructor
    }

    public static LoadingDialogFragment newInstance(String message) {
        LoadingDialogFragment fragment = new LoadingDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MESSAGE, message);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        final Context themedContext = new ContextThemeWrapper(requireActivity(), R.style.Theme_RIHNetLogistikMDE_Dialog);

        LayoutInflater inflater = LayoutInflater.from(themedContext);
        View view = inflater.inflate(R.layout.fragment_loading_dialog, null);

        TextView tv_message = view.findViewById(R.id.tv_message);

        String message = "";
        if (getArguments() != null) {
            message = getArguments().getString(ARG_MESSAGE);
        }
        tv_message.setText(message);

        LoadingIndicator li = view.findViewById(R.id.bp);

        if (li != null) {
            int primary = MaterialColors.getColor(li, at.rihnet.rihnetlogistikmde.R.attr.colorPrimary);
            int primaryContainer = MaterialColors.getColor(li, at.rihnet.rihnetlogistikmde.R.attr.colorPrimaryContainer);

            li.setIndicatorColor(primary);
            li.setContainerColor(primaryContainer);
        }



        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(themedContext);
        builder.setView(view);
        setCancelable(false);

        return builder.create();
    }
}
