package com.example.edoskop;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class RecognizedItemsAdapter extends ArrayAdapter<String> {

    public RecognizedItemsAdapter(Context context, List<String> items) {
        super(context, R.layout.item_recognized, items); // Здесь ссылка на item_recognized
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_recognized, parent, false);
        }

        TextView itemNameTextView = convertView.findViewById(R.id.itemNameTextView);
        itemNameTextView.setText(getItem(position));

        return convertView;
    }
}