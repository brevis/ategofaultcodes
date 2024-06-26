package com.craftific.ategofaultcodes;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.TreeSet;


public class CustomAdapter extends BaseAdapter {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_HEADER = 1;

    private final ArrayList<TransactionObject> mData = new ArrayList<TransactionObject>();
    private final TreeSet<Integer> sectionHeader = new TreeSet<Integer>();

    private final LayoutInflater mInflater;

    public CustomAdapter(Context context) {
        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addItem(final TransactionObject item) {
        mData.add(item);
        notifyDataSetChanged();
    }

    public void addSectionHeaderItem(final TransactionObject item) {
        mData.add(item);
        sectionHeader.add(mData.size() - 1);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return sectionHeader.contains(position) ? TYPE_HEADER : TYPE_ITEM;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public TransactionObject getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        int rowType = getItemViewType(position);

        if (convertView == null) {
            holder = new ViewHolder();
            switch (rowType) {
                case TYPE_ITEM:
                    convertView = mInflater.inflate(R.layout.row_item, null);
                    holder.textView = convertView.findViewById(R.id.txtName);
                    break;
                case TYPE_HEADER:
                    convertView = mInflater.inflate(R.layout.header_item, null);
                    holder.textView = convertView.findViewById(R.id.textSeparator);
                    break;
            }
            assert convertView != null;
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.textView.setText(mData.get(position).getName());

        return convertView;
    }

    public static class ViewHolder {
        public TextView textView;
    }
}
