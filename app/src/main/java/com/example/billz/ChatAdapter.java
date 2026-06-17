package com.example.billz;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<ChatMessage> messages;
    private OnWhatsappClickListener whatsappClickListener;

    public interface OnWhatsappClickListener {
        void onWhatsappClick();
    }

    public ChatAdapter(List<ChatMessage> messages, OnWhatsappClickListener listener) {
        this.messages = messages;
        this.whatsappClickListener = listener;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = messages.get(position);

        holder.layoutBot.setVisibility(View.GONE);
        holder.layoutUser.setVisibility(View.GONE);
        holder.btnWhatsapp.setVisibility(View.GONE);

        if (message.getType() == ChatMessage.TYPE_BOT) {
            holder.layoutBot.setVisibility(View.VISIBLE);
            holder.textBot.setText(message.getMessage());
        } else if (message.getType() == ChatMessage.TYPE_USER) {
            holder.layoutUser.setVisibility(View.VISIBLE);
            holder.textUser.setText(message.getMessage());
        } else if (message.getType() == ChatMessage.TYPE_WHATSAPP_LINK) {
            holder.btnWhatsapp.setVisibility(View.VISIBLE);
            holder.btnWhatsapp.setOnClickListener(v -> {
                if (whatsappClickListener != null) whatsappClickListener.onWhatsappClick();
            });
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        View layoutBot, layoutUser;
        TextView textBot, textUser;
        MaterialButton btnWhatsapp;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutBot = itemView.findViewById(R.id.layoutBot);
            layoutUser = itemView.findViewById(R.id.layoutUser);
            textBot = itemView.findViewById(R.id.textBotMessage);
            textUser = itemView.findViewById(R.id.textUserMessage);
            btnWhatsapp = itemView.findViewById(R.id.btnWhatsappChat);
        }
    }
}
