package org.curchod.util;

import com.parse.starter.R;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class IconAdapter extends BaseAdapter {
    private Context context;

    public IconAdapter(Context c) {
        context = c;
    }

    public int getCount() {
        return icon_ids.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) 
    {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) 
    {
        View image_view;
        if (convertView == null) 
        {
        	image_view = new ImageView(context);
        	image_view.setLayoutParams(new GridView.LayoutParams(85, 85));
        	//image_view.setScaleType(ImageView.ScaleType.CENTER_CROP);
        	image_view.setPadding(8, 8, 8, 8);
            LayoutInflater li = ((Activity)context).getLayoutInflater();
            image_view = (View)li.inflate(com.parse.starter.R.layout.grid_item, null);
            // Add The Text
            TextView tv = (TextView)image_view.findViewById(com.parse.starter.R.id.grid_item_text);
            tv.setText(item_names[position]);
            // Add The Image     
            ImageView iv = (ImageView)image_view.findViewById(com.parse.starter.R.id.grid_item_image);
            iv.setImageResource(icon_ids[position]);
        } else {
        	image_view = (View) convertView;
        }
        // image_view.setImageResource(icon_ids[position]);
        return image_view;
    }

    // references to our images
    private Integer[] icon_ids = {
    		com.parse.starter.R.drawable.sync, 
    		com.parse.starter.R.drawable.templates,
    		com.parse.starter.R.drawable.send_sms, 
    		com.parse.starter.R.drawable.contacts,
    		com.parse.starter.R.drawable.group
    };
    private Integer[] sub_item_ids = {0,1,2,3,4};
    
    private String[] item_names = {
    		context.getResources().getString(R.string.sync_contacts),
    		context.getResources().getString(R.string.template_name_label),
    		context.getResources().getString(R.string.send_new_sms_message),
    		context.getResources().getString(R.string.app_contacts),
    		context.getResources().getString(R.string.groups)
    };
}