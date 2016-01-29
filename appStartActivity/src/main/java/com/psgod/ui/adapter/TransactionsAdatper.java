package com.psgod.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.psgod.Constants;
import com.psgod.PsGodImageLoader;
import com.psgod.R;
import com.psgod.model.Transactions;
import com.psgod.ui.widget.AvatarImageView;

import java.util.List;

/**
 * Created by Administrator on 2016/1/28 0028.
 */
public class TransactionsAdatper extends BaseAdapter {

    private List<Transactions> list;
    private Context context;

    public TransactionsAdatper(Context context, List<Transactions> list) {
        this.list = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return list != null ? list.size() : 0;
    }

    @Override
    public Object getItem(int i) {
        return i < list.size() ? list.get(i) : null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    private static ViewHolder viewHolder;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_transactions, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.avatar = (AvatarImageView) convertView.findViewById(R.id.item_transactions_avatar);
            viewHolder.count = (TextView) convertView.findViewById(R.id.item_transactions_count);
            viewHolder.desc = (TextView) convertView.findViewById(R.id.item_transactions_desc);
            viewHolder.time = (TextView) convertView.findViewById(R.id.item_transactions_time);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Transactions transactions = list.get(position);
//        if(transactions.get)
        PsGodImageLoader.getInstance().displayImage(transactions.getAvatar(), viewHolder.avatar
                , Constants.DISPLAY_IMAGE_OPTIONS_AVATAR);
        if (transactions.getType().equals("1")) {
            viewHolder.count.setTextColor(Color.parseColor("#e68dc81b"));
            viewHolder.count.setText(String.format("+%.2f", transactions.getAmount()));
        } else {
            viewHolder.count.setTextColor(Color.parseColor("#e6f5a623"));
            viewHolder.count.setText(String.format("-%.2f", transactions.getAmount()));
        }
        viewHolder.time.setText(transactions.getCreated_at());
        viewHolder.desc.setText(transactions.getMemo());

        return convertView;
    }

    private static class ViewHolder {

        AvatarImageView avatar;
        TextView desc;
        TextView time;
        TextView count;

    }


}
