package com.example.edoskop;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecognizedItemsAdapter extends RecyclerView.Adapter<RecognizedItemsAdapter.ViewHolder> {

    private Context context;
    private List<String> recognizedItems;

    public RecognizedItemsAdapter(Context context, List<String> recognizedItems) {
        this.context = context;
        this.recognizedItems = recognizedItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String item = recognizedItems.get(position);
        holder.textView.setText(item);

        // Удаление элемента при клике
        holder.itemView.setOnClickListener(v -> showDeleteConfirmationDialog(position));
    }

    @Override
    public int getItemCount() {
        return recognizedItems.size();
    }

    private void showDeleteConfirmationDialog(int position) {
        new AlertDialog.Builder(context)
                .setTitle("Удалить элемент?")
                .setMessage("Вы уверены, что хотите удалить \"" + recognizedItems.get(position) + "\"?")
                .setPositiveButton("Да", (dialog, which) -> {
                    recognizedItems.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, recognizedItems.size());
                })
                .setNegativeButton("Нет", (dialog, which) -> dialog.dismiss())
                .show();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}
