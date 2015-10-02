package gr.hua.dit.smartt;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


/**
 * Created by nsouliotis on 27/9/2015.
 */
public class MarkerInfoAdapter extends BaseAdapter {
    private static ArrayList<String> propertyNames;
    private LayoutInflater mInflater;
    int selectedPosition = -1;
    private ListView parentAdapter;
    Context context;
    public MarkerInfoAdapter(Context context, ArrayList<String> results, ListView parentAdapter) {
        propertyNames = results;
        this.parentAdapter = parentAdapter;
        mInflater = LayoutInflater.from(context);
        this.context = context;
    }
    @Override
    public int getCount() {
        return propertyNames.size();
    }

    @Override
    public Object getItem(int position) {
        return propertyNames.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    static class ViewHolder {
        TextView Name;
        ImageView image;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        try{
            if(convertView==null)
            {
                convertView=mInflater.inflate(R.layout.simplerow, null);
                holder=new ViewHolder();
                holder.Name=(TextView)convertView.findViewById(R.id.rowTextView);
                convertView.setTag(holder);
            }
            else
            {
                holder=(ViewHolder)convertView.getTag();
            }
            holder.Name.setText(propertyNames.get(position));
//            if(selectedPosition == position){
//                convertView.setBackgroundResource(R.drawable.blue_marker_bg);
//                ((TextView)convertView.findViewById(R.id.ProeprtyName)).setTextColor(Color.WHITE);
//                convertView.setMinimumHeight(80);
//            }else{
//                convertView.setBackgroundResource(R.drawable.directories_list_bg);
//                ((TextView)convertView.findViewById(R.id.ProeprtyName)).setTextColor(Color.BLACK);
//                convertView.setMinimumHeight(80);
//            }
            ((TextView)convertView.findViewById(R.id.rowTextView)).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    setSelected(position);
                    Toast.makeText(context, "dfs " + position, Toast.LENGTH_SHORT).show();
                    parentAdapter.setSelection(position);//propertyNames.get(position).getitemIndex()
                }
            });
        }catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;
    }
    public void setSelected(int position) {
        selectedPosition = position;
        notifyDataSetChanged();
    }

}

