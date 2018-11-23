package com.viettel.tungns.kichhoatsim;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ChooseCommandAdapter extends RecyclerView.Adapter<ChooseCommandAdapter.ViewHolder>{
    private List<Command> commandList;
    private Context context;
    private OnItemClick onItemClick;

    public ChooseCommandAdapter(List<Command> commandList, Context context) {
        this.commandList = commandList;
        this.context = context;
        if (context instanceof OnItemClick) {
            this.onItemClick = (OnItemClick) context;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_command, null);
        v.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.mTvCommandName.setText(commandList.get(position).getName());
        holder.mLlItemCommand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClick != null) {
                    onItemClick.onItemClick(holder.getAdapterPosition());
                    }
            }
        });
    }

    @Override
    public int getItemCount() {
        return commandList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView mTvCommandName;
        LinearLayout mLlItemCommand;

        public ViewHolder(View itemView) {
            super(itemView);
            mTvCommandName = itemView.findViewById(R.id.tv_command_name);
            mLlItemCommand = itemView.findViewById(R.id.ll_item_command);
        }
    }

}
