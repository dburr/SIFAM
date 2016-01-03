package com.caraxian.sifam;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class AccountListAdapter extends ArrayAdapter<Account> {
    Context context;
    int layoutResourceId;
    ArrayList<Account> data = null;

    public AccountListAdapter(Context context, int layoutResourceId, ArrayList<Account> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row;
        AccountHolder holder;
        LayoutInflater inflater = LayoutInflater.from(context);
        row = inflater.inflate(layoutResourceId, parent, false);
        holder = new AccountHolder();
        holder.loadedIcon = (ImageView) row.findViewById(R.id.account_isLoaded);
        holder.accountName = (TextView) row.findViewById(R.id.account_name);
        holder.folderIcon = (ImageView) row.findViewById(R.id.account_isFolder);
        holder.info = (TextView) row.findViewById(R.id.account_info);
        holder.serverId = (TextView) row.findViewById(R.id.account_server);
        holder.lockedIcon = (ImageView) row.findViewById(R.id.account_locked);
        holder.accountNotSelected= (ImageView) row.findViewById(R.id.account_not_selected);
        holder.accountSelected = (ImageView) row.findViewById(R.id.account_selected);
        Account account = data.get(position);
        holder.accountName.setText(account.name);
        if (account.locked == true) {
            holder.lockedIcon.setVisibility(View.VISIBLE);
        } else {
            holder.lockedIcon.setVisibility(View.GONE);
        }
        if (account.isFolder) {
            holder.folderIcon.setVisibility(View.VISIBLE);
            holder.loadedIcon.setVisibility(View.INVISIBLE);
            holder.info.setVisibility(View.GONE);
            holder.serverId.setVisibility(View.GONE);
            holder.accountSelected.setVisibility(View.GONE);
            holder.accountNotSelected.setVisibility(View.GONE);
        } else {
            holder.serverId.setVisibility(View.VISIBLE);
            holder.serverId.setText(account.server);
            holder.folderIcon.setVisibility(View.GONE);
            holder.info.setVisibility(View.VISIBLE);
            if (account.loaded == 0) holder.info.setText("Last Loaded: Never");
            else
                holder.info.setText("Last Loaded: " + TimeAgo.toDuration(System.currentTimeMillis() - account.loaded));
            if (account.isCurrent()) {
                holder.loadedIcon.setVisibility(View.VISIBLE);
            } else {
                holder.loadedIcon.setVisibility(View.INVISIBLE);
            }
            if (MainActivity.selectedAccounts.size() == 0 || account.locked){
                holder.accountSelected.setVisibility(View.GONE);
                holder.accountNotSelected.setVisibility(View.GONE);
            }else {
                if (MainActivity.selectedAccounts.contains(account.id)) {
                    holder.accountSelected.setVisibility(View.VISIBLE);
                    holder.accountNotSelected.setVisibility(View.GONE);
                } else {
                    holder.accountSelected.setVisibility(View.GONE);
                    holder.accountNotSelected.setVisibility(View.VISIBLE);
                }
            }
        }
        return row;
    }

    static class AccountHolder {
        ImageView loadedIcon;
        ImageView folderIcon;
        TextView accountName;
        TextView serverId;
        TextView info;
        ImageView lockedIcon;
        ImageView accountSelected;
        ImageView accountNotSelected;
    }

}
