package at.rihnet.rihnetlogistikmde.ui.loading;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.databinding.ActivityMainBinding;
import at.rihnet.rihnetlogistikmde.databinding.FragmentLoadingDialogBinding;

public class LoadingDialogFragment extends DialogFragment {
    private final String message;

    public LoadingDialogFragment(String message){
        this.message = message;
    }

    public static LoadingDialogFragment newInstance(String message) {
        return new LoadingDialogFragment(message);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
//        at.rihnet.rihnetlogistikmde.databinding.FragmentLoadingDialogBinding binding = FragmentLoadingDialogBinding.inflate(getLayoutInflater());
//        TextView tvMessage = binding.tvMessage;
//        tvMessage.setText(message);
//        setCancelable(false);
//        return super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_loading_dialog, null);
        TextView tv_message = view.findViewById(R.id.tv_message);
        tv_message.setText(message);
        builder.setView(view).setCancelable(false);
        return builder.create();
    }
}
