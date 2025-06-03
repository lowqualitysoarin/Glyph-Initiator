package com.lowqualitysoarin.glyphinitiator.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lowqualitysoarin.glyphinitiator.R;
import com.lowqualitysoarin.glyphinitiator.entry.OggEntry;

import java.util.ArrayList;

public class OggEntryAdapter extends RecyclerView.Adapter<OggEntryAdapter.ViewHolder> {

    private static ArrayList<OggEntry> oggEntries;
    private OnItemInteractionListener listener;

    public interface OnItemInteractionListener {
        void onDeleteClicked(int position);
        void onRenameClicked(int position);
        void onItemClicked(int position); // For playing or other actions
    }

    public OggEntryAdapter(ArrayList<OggEntry> oggEntries, OnItemInteractionListener listener) {
        this.oggEntries = oggEntries;
        this.listener = listener;
    }

    public static OggEntry pickRandom() {
        return oggEntries.get((int) (oggEntries.size() * Math.random()));
    }

    public static OggEntry getEntry(String entryName) {
        for (int i = 0; i < oggEntries.size(); i++) {
            if (oggEntries.get(i).getName().equals(entryName)) {
                return oggEntries.get(i);
            }
        }
        return null;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_ogg_entry, parent, false); // Create list_item_ogg_entry.xml
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OggEntry entry = oggEntries.get(position);
        holder.nameTextView.setText(entry.getName());
        // You could also display the URI or other info if needed
        // holder.uriTextView.setText(entry.getUriString());
    }

    @Override
    public int getItemCount() {
        return oggEntries.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        // TextView uriTextView; // Optional
        ImageButton deleteButton;
        ImageButton renameButton;

        ViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.text_view_name);
            // uriTextView = itemView.findViewById(R.id.text_view_uri); // Optional
            deleteButton = itemView.findViewById(R.id.button_delete);
            renameButton = itemView.findViewById(R.id.button_rename);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClicked(position);
                    }
                }
            });

            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onDeleteClicked(position);
                    }
                }
            });

            renameButton.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onRenameClicked(position);
                    }
                }
            });
        }
    }
}
