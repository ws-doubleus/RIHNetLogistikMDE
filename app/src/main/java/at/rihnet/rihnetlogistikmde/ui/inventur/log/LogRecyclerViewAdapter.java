package at.rihnet.rihnetlogistikmde.ui.inventur.log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.models.Log;

public class LogRecyclerViewAdapter extends RecyclerView.Adapter<LogRecyclerViewAdapter.LogHolder>{
    private List<Log> logList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public LogRecyclerViewAdapter(List<Log> logList) {
        this.logList = logList;
    }

    public static class LogHolder extends RecyclerView.ViewHolder {
        private final TextView tv_timestamp;
        private final TextView tv_message;

        public LogHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            tv_timestamp = itemView.findViewById(R.id.tv_timestamp);
            tv_message = itemView.findViewById(R.id.tv_message);
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                    }
                }
            });
        }
    }

    @NonNull
    @Override
    public LogHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_log, parent, false);
        return new LogHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull LogHolder logHolder, int position) {
        Log currentItem = logList.get(position);
        logHolder.tv_timestamp.setText(currentItem.getTimestamp());
        logHolder.tv_message.setText(currentItem.getMessage());
        if(currentItem.getColor() != 0){
            logHolder.tv_message.setTextColor(currentItem.getColor());
        }
    }

    @Override
    public int getItemCount() {
        if (logList != null) {
            return logList.size();
        } else {
            return 0;
        }
    }

    public void setLogs(List<Log> logs){
        logList = logs;
    }

    public Log getLogAt(int position){
        return logList.get(position);
    }
}
