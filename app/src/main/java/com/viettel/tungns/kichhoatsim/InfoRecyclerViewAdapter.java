package com.viettel.tungns.kichhoatsim;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class InfoRecyclerViewAdapter extends RecyclerView.Adapter<InfoRecyclerViewAdapter.ViewHolder>{
    private SimInfo simInfo;
    private Context context;
    private OnItemClick onItemClick;
    private ArrayList<String> listKey;

    public InfoRecyclerViewAdapter(SimInfo simInfo, Context context) {
        this.simInfo = simInfo;
        this.context = context;
        if (context instanceof OnItemClick) {
            this.onItemClick = (OnItemClick) context;
        }
        listKey = new ArrayList<>();
        for (Map.Entry entry: simInfo.getMapInfo().entrySet()) {
            listKey.add((String) entry.getKey());
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_info, null);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(InfoRecyclerViewAdapter.ViewHolder holder, final int position) {
        holder.mTvNameInfo.setText(listKey.get(position));
        holder.mSpnContentInfo.setAdapter(getAdapterSpinner(simInfo.getMapInfo().get(listKey.get(position))));
    }

    @Override
    public int getItemCount() {
        return simInfo.getMapInfo().size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView mTvNameInfo;
        Spinner mSpnContentInfo;

        public ViewHolder(View itemView) {
            super(itemView);
            mTvNameInfo = itemView.findViewById(R.id.tv_name_info);
            mSpnContentInfo = itemView.findViewById(R.id.spn_content_info);
        }
    }

    private ArrayAdapter<String> getAdapterSpinner(ArrayList<String> listChoice) {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, listChoice);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        return arrayAdapter;
    }
}