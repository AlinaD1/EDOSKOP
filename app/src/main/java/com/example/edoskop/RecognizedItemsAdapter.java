package com.example.edoskop;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RecognizedItemsAdapter extends RecyclerView.Adapter<RecognizedItemsAdapter.ViewHolder> {

    private Context context;
    private List<String> recognizedItems;

    public RecognizedItemsAdapter(Context context, List<String> recognizedItems) {
        this.context = context;
        this.recognizedItems = recognizedItems;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.textView.setText(recognizedItems.get(position));
    }

    @Override
    public int getItemCount() {
        return recognizedItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}
