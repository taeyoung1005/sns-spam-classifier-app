package com.example.smsspamclassifierapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SpamMessageAdapter extends RecyclerView.Adapter<SpamMessageAdapter.ViewHolder> {

    private List<SpamMessage> spamMessages;

    public SpamMessageAdapter(List<SpamMessage> spamMessages) {
        this.spamMessages = spamMessages;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_spam_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SpamMessage spamMessage = spamMessages.get(position);
        holder.phoneNumberTextView.setText(spamMessage.getPhoneNumber());
        holder.contentTextView.setText(spamMessage.getContent());
        holder.createdAtTextView.setText(spamMessage.getCreatedAt());
    }

    @Override
    public int getItemCount() {
        return spamMessages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView phoneNumberTextView;
        public TextView contentTextView;
        public TextView createdAtTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            phoneNumberTextView = itemView.findViewById(R.id.phone_number);
            contentTextView = itemView.findViewById(R.id.content);
            createdAtTextView = itemView.findViewById(R.id.created_at);
        }
    }
}
