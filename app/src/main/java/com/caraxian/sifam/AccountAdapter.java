package com.caraxian.sifam;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class AccountAdapter extends ArrayAdapter<Account> {
    Context context;
    int layoutResourceId;
    ArrayList<Account> data = null;

    public AccountAdapter(Context context, int layoutResourceId, ArrayList<Account> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        AccountHolder holder = null;
        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new AccountHolder();
            holder.loadedIcon = (ImageView) row.findViewById(R.id.account_isLoaded);
            holder.invalidIcon= (ImageView) row.findViewById(R.id.account_isInvalid);
            holder.accountName = (TextView) row.findViewById(R.id.account_name);
            holder.folderIcon = (ImageView) row.findViewById(R.id.account_isFolder);
            holder.info = (TextView) row.findViewById(R.id.account_info);
            row.setTag(holder);
        } else {
            holder = (AccountHolder) row.getTag();
        }
        Account account = data.get(position);
        holder.accountName.setText(account.displayName);
        //holder.accountIcon.setImageResource(account.image);






        if (account.isFolder){
            MainActivity.debug(account.name + " is Folder");
            holder.folderIcon.setVisibility(View.VISIBLE);
            holder.invalidIcon.setVisibility(View.INVISIBLE);
            holder.loadedIcon.setVisibility(View.INVISIBLE);
            holder.info.setVisibility(View.INVISIBLE);
        }else{

            if (account.lastLoaded == 0) {
                holder.info.setText("Last Loaded: Never");
            } else {
                Long ct = System.currentTimeMillis();
                Long timeSinceLoaded = ct - account.lastLoaded;
                holder.info.setText("Last Loaded: " + TimeAgo.toDuration(timeSinceLoaded));
            }

            if (!account.isValid){
                MainActivity.debug(account.name + " is Invalid");
                holder.folderIcon.setVisibility(View.INVISIBLE);
                holder.invalidIcon.setVisibility(View.VISIBLE);
                holder.loadedIcon.setVisibility(View.INVISIBLE);
                holder.info.setVisibility(View.VISIBLE);
            }else{
                if (account.isCurrent){
                    MainActivity.debug(account.name + " is Current");
                    holder.folderIcon.setVisibility(View.INVISIBLE);
                    holder.invalidIcon.setVisibility(View.INVISIBLE);
                    holder.loadedIcon.setVisibility(View.VISIBLE);
                    holder.info.setVisibility(View.VISIBLE);
                }else{
                    MainActivity.debug(account.name + " is Normal");
                    holder.folderIcon.setVisibility(View.INVISIBLE);
                    holder.invalidIcon.setVisibility(View.INVISIBLE);
                    holder.loadedIcon.setVisibility(View.INVISIBLE);
                    holder.info.setVisibility(View.VISIBLE);
                }
            }
        }




        return row;
    }

    static class AccountHolder {
        ImageView loadedIcon;
        ImageView invalidIcon;
        ImageView folderIcon;
        TextView accountName;
        TextView info;
    }
}
