package com.example.billz;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.ViewHolder> {

    private List<Language> languages;
    private OnLanguageClickListener listener;
    private int selectedPosition = -1;

    public interface OnLanguageClickListener {
        void onLanguageClick(Language language);
    }

    public LanguageAdapter(List<Language> languages, OnLanguageClickListener listener) {
        this.languages = languages;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_language, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Language language = languages.get(position);
        holder.textNativeName.setText(language.getNativeName());
        holder.textEnglishName.setText(language.getName());

        if (position == selectedPosition) {
            holder.card.setStrokeColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.reports_tab_selected));
            holder.card.setStrokeWidth(4);
            holder.card.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
        } else {
            holder.card.setStrokeColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.reports_divider));
            holder.card.setStrokeWidth(2);
            holder.card.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
        }

        holder.itemView.setOnClickListener(v -> {
            int previousSelected = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previousSelected);
            notifyItemChanged(selectedPosition);
            listener.onLanguageClick(language);
        });
    }

    @Override
    public int getItemCount() {
        return languages.size();
    }

    public Language getSelectedLanguage() {
        if (selectedPosition != -1) {
            return languages.get(selectedPosition);
        }
        return null;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView card;
        TextView textNativeName, textEnglishName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cardLanguage);
            textNativeName = itemView.findViewById(R.id.textNativeName);
            textEnglishName = itemView.findViewById(R.id.textEnglishName);
        }
    }
}
