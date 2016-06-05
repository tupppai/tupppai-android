package com.pires.wesee.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.pires.wesee.Utils;
import com.pires.wesee.model.Transactions;
import com.pires.wesee.ui.widget.AvatarImageView;
import com.pires.wesee.R;

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
            viewHolder.status = (TextView) convertView.findViewById(R.id.item_transactions_status);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Transactions transactions = list.get(position);
//        if(transactions.get)
//        PsGodImageLoader.getInstance().displayImage(transactions.getAvatar(), viewHolder.avatar
//                , Constants.DISPLAY_IMAGE_OPTIONS_AVATAR);
        viewHolder.avatar.getImage().
                setImageDrawable(new ColorDrawable(Color.parseColor("#00000000")));
        ViewGroup.LayoutParams params = viewHolder.avatar.getLayoutParams();
        if (params != null) {
            params.height = Utils.dpToPx(context, 38);
            params.width = Utils.dpToPx(context, 43);
        } else {
            params = new ViewGroup.LayoutParams(Utils.dpToPx(context, 43),
                    Utils.dpToPx(context, 38));
        }
        viewHolder.avatar.setLayoutParams(params);
        if (transactions.getType().equals("1")) {
            viewHolder.count.setTextColor(Color.parseColor("#e6f5a623"));
            viewHolder.count.setText(String.format("+%.2f", transactions.getAmount()));
            viewHolder.avatar.getImage().setBackgroundResource(R.mipmap.ic_detail_deposit);
        } else {
            viewHolder.count.setText(String.format("-%.2f", transactions.getAmount()));
            viewHolder.avatar.getImage().setBackgroundResource(R.mipmap.ic_detail_withdraw);
            viewHolder.count.setTextColor(Color.parseColor("#e68dc81b"));

        }
        if (transactions.getStatus().equals("1")) {
            viewHolder.status.setText("交易成功");
        } else {
            viewHolder.status.setText("交易失败");
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
        TextView status;

    }


}
