package com.android.proximus;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.proximus.util.MD5Util;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.List;

public class UserAdapter extends ArrayAdapter<ParseUser> {

    protected Context mContext;
    protected List<ParseUser> mUsers;


    public UserAdapter(Context context, List<ParseUser> users) {
        super(context, R.layout.user_item, users);

        mContext = context;
        mUsers = users;

    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.user_item, null);
            holder = new ViewHolder();
            holder.userImageView = (ImageView) convertView.findViewById(R.id.userImageView);
            holder.nameLabel = (TextView) convertView.findViewById(R.id.nameLabel);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ParseUser user = mUsers.get(position);
        String email = user.getEmail().toLowerCase();

        if (email.equals("")) {
            holder.userImageView.setImageResource(R.drawable.place_holder);
        } else {

            String hash = MD5Util.md5Hex(email);
            String url = "http://www.gravatar.com/avatar/" + hash + "?s=204&d=404";

            Log.i("TEST", url);

            Picasso.with(mContext)
                    .load(url)
                    .placeholder(R.drawable.place_holder)
                    .into(holder.userImageView);
        }


        holder.nameLabel.setText(user.getUsername());
        return convertView;

    }

    public void refill(List<ParseUser> users) {

        mUsers.clear();
        mUsers.addAll(users);
        notifyDataSetChanged();
    }

    public static class ViewHolder {

        ImageView userImageView;
        TextView nameLabel;
    }
}
