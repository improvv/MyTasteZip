package com.example.mytastezip.ui.add;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mytastezip.R;

public class CategoryAdapter extends ArrayAdapter<String> {
    private Context context;
    private String[] categories;

    public CategoryAdapter(Context context, String[] categories) {
        super(context, R.layout.category_item, categories);
        this.context = context;
        this.categories = categories;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    private View createView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.category_item, parent, false);
        }

        ImageView icon = convertView.findViewById(R.id.category_icon);
        TextView text = convertView.findViewById(R.id.category_text);

        String category = categories[position];
        text.setText(category);
        setCategoryIcon(icon, category);

        return convertView;
    }

    private void setCategoryIcon(ImageView imageView, String category) {
        if (category.equals("한식")) {
            imageView.setImageResource(R.drawable.rice_pin);
        } else if (category.equals("중식")) {
            imageView.setImageResource(R.drawable.dimsum_pin);
        } else if (category.equals("일식")) {
            imageView.setImageResource(R.drawable.sushi_pin);
        } else if (category.equals("양식")) {
            imageView.setImageResource(R.drawable.pasta_pin);
        } else if (category.equals("카페")) {
            imageView.setImageResource(R.drawable.coffee_pin);
        }
    }
}
