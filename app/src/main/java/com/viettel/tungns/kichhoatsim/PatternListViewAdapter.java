package com.viettel.tungns.kichhoatsim;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.HashMap;

public class PatternListViewAdapter extends RecyclerView.Adapter<PatternListViewAdapter.ViewHolder>{
    private ArrayList<ScanPattern> scanPatternList;
    private Context context;
    private OnItemClick onItemClick;

    private HashMap<Integer, ScanPattern> map = new HashMap<>();
    public PatternListViewAdapter(ArrayList<ScanPattern> scanPatternList, Context context) {
        this.scanPatternList = scanPatternList;
        this.context = context;
        if (context instanceof OnItemClick) {
            this.onItemClick = (OnItemClick) context;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        for (int i = 0; i < scanPatternList.size(); i ++) {
            map.put(i, scanPatternList.get(i));
        }
        View v = LayoutInflater.from(context).inflate(R.layout.item_pattern, null);
        v.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.mTvPatternName.setText(scanPatternList.get(position).getKey());
        holder.mTvPatternContent.setText(scanPatternList.get(position).getValue());
        holder.mLlItemConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClick.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return scanPatternList.size();
    }

    public HashMap<Integer, ScanPattern> getLastScanPattern () {
        return map;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView mTvPatternName;
        TextView mTvPatternContent;
        LinearLayout mLlItemConfig;

        public ViewHolder(View itemView) {
            super(itemView);
            mTvPatternName = itemView.findViewById(R.id.tv_pattern_name);
            mTvPatternContent = itemView.findViewById(R.id.tv_pattern_content);
            mLlItemConfig = itemView.findViewById(R.id.ll_item_config);
        }
    }

}
