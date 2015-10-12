package gr.hua.dit.smartt;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by nsouliotis on 7/10/2015.
 */
public class AlertListAdapter extends BaseAdapter {

    ArrayList<GetLinesNearStop> mData;
    Context mContext;
    LayoutInflater inflater;

    public AlertListAdapter(ArrayList<GetLinesNearStop> data, Context context) {
        mData = data;
        mContext = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) mContext
                    .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.simplerow, null);
        }
        TextView tvTitle = (TextView) convertView.findViewById(R.id.rowTextView);

        tvTitle.setText(mData.get(position).getStopName());

        return convertView;
    }
}