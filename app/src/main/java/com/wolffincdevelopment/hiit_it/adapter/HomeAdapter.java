package com.wolffincdevelopment.hiit_it.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wolffincdevelopment.hiit_it.BR;
import com.wolffincdevelopment.hiit_it.HomeFooter;
import com.wolffincdevelopment.hiit_it.Item;
import com.wolffincdevelopment.hiit_it.R;
import com.wolffincdevelopment.hiit_it.TrackItem;
import com.wolffincdevelopment.hiit_it.TrackListener;
import com.wolffincdevelopment.hiit_it.util.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kyle Wolff on 11/11/16.
 */

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.HomeAdapterViewHolder> implements TrackListener {

    private SharedPreferencesUtil sharedPreferencesUtil;

    private List<Item> items;

    public HomeAdapter(List<TrackItem> trackItems) {

        items = new ArrayList<>();

        sharedPreferencesUtil = SharedPreferencesUtil.getInstance();
        updateData(trackItems);

    }

    public void updateData(List<TrackItem> trackItems) {

        items.clear();

        for(TrackItem item : trackItems) {
            items.add(item);
        }

        items.add(new HomeFooter());

        notifyDataSetChanged();
    }

    @Override
    public HomeAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if(viewType == Item.ItemType.TRACK_ITEM.hashCode()) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.base_activity_row, parent, false);
            return new HomeAdapterViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.base_activity_foot_row, parent, false);
            return new HomeAdapterViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(HomeAdapterViewHolder holder, int position) {

        ViewDataBinding binding = holder.getBinding();

        if(binding != null) {

            if(items.get(position).getItemType() == Item.ItemType.TRACK_ITEM) {
                binding.setVariable(BR.trackItem, items.get(position));
            } else if(items.get(position).getItemType() == Item.ItemType.FOOTER) {
                binding.setVariable(BR.homeFooter, items.get(position));
            }

            binding.setVariable(BR.listener, this);
            binding.executePendingBindings();
        }
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getItemType().hashCode();
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onFooterClicked(Context context) {

        if(sharedPreferencesUtil.getRepeat(context)) {
            sharedPreferencesUtil.setRepeat(context, false);
        } else {
            sharedPreferencesUtil.setRepeat(context, true);
        }

        notifyDataSetChanged();
    }


    public static class HomeAdapterViewHolder extends RecyclerView.ViewHolder {

        private ViewDataBinding binding;

        public HomeAdapterViewHolder(View itemView) {
            super(itemView);

            binding = DataBindingUtil.bind(itemView);
        }

        public ViewDataBinding getBinding() {
            return binding;
        }
    }
}
