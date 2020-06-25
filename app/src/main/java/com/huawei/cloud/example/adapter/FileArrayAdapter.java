package com.huawei.cloud.example.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.huawei.cloud.example.model.Item;
import com.huawei.cloud.example.R;

import java.util.List;


public class FileArrayAdapter extends ArrayAdapter<Item> {

    private Context cntxt;
    private int id;
    private List<Item> items;

    public FileArrayAdapter(Context context, int textViewResourceId,
                            List<Item> objects) {
        super(context, textViewResourceId, objects);
        cntxt = context;
        id = textViewResourceId;
        items = objects;
    }

    public Item getItem(int i) {
        return items.get(i);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) cntxt.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(id, null);
        }

        final Item o = items.get(position);
        if (o != null) {
            TextView t1 = v.findViewById(R.id.TextView01);
            TextView t2 = v.findViewById(R.id.TextView02);
            TextView t3 = v.findViewById(R.id.TextViewDate);

            ImageView imgIcon = v.findViewById(R.id.fd_Icon1);
            String uri = "drawable/" + o.getImage();
            int imageResource = cntxt.getResources().getIdentifier(uri, null, cntxt.getPackageName());
            Drawable image = cntxt.getResources().getDrawable(imageResource);
			imgIcon.setImageDrawable(image);

            if (t1 != null)
                t1.setText(o.getName());
            if (t2 != null)
                t2.setText(o.getData());
            if (t3 != null)
                t3.setText(o.getDate());

        }
        return v;
    }

}
