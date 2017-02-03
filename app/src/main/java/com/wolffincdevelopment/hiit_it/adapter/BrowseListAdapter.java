/*
package com.wolffincdevelopment.hiit_it.adapter;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wolffincdevelopment.hiit_it.R;
import com.wolffincdevelopment.hiit_it.listeners.BrowseTrackListener;
import com.wolffincdevelopment.hiit_it.viewmodel.HeaderItem;
import com.wolffincdevelopment.hiit_it.viewmodel.Item;
import com.wolffincdevelopment.hiit_it.viewmodel.TrackItem;

import java.util.ArrayList;
import java.util.List;

*/
/**
 * Created by kylewolff on 6/4/2016.
 *//*

public class BrowseListAdapter extends RecyclerView.Adapter<BrowseListAdapter.BaseAdapterViewHolder> {

    private ArrayList<Item> itemArrayList;
    private BrowseTrackListener listener;
    private String headerChar;
    private String previousChar;

    public BrowseListAdapter(List<TrackItem> items, BrowseTrackListener listener) {

        itemArrayList = new ArrayList<>();
        this.listener = listener;

        updateData(items);
    }

    public void updateData(List<TrackItem> items) {

        for(TrackItem item : items) {

            headerChar = String.valueOf(item.getArtistName().charAt(0));
            HeaderItem headerItem = new HeaderItem(headerChar);

            if(itemArrayList.isEmpty()) {
                itemArrayList.add(headerItem);
                itemArrayList.add(item);
            } else {

                if(previousChar.compareTo(headerChar) != 0) {
                    itemArrayList.add(headerItem);
                }

                itemArrayList.add(item);

            }

            previousChar = headerChar;
        }

        notifyDataSetChanged();
    }

    @Override
    public BaseAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if(viewType == Item.ItemType.TRACK_ITEM.hashCode()) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.browse_listview_row_normal, parent, false);
            return new BaseAdapterViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.browse_section_headers, parent, false);
            return new BaseAdapterViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(BaseAdapterViewHolder holder, int position) {

        ViewDataBinding binding = holder.getBinding();

        if(binding != null) {

            if(itemArrayList.get(position).getItemType() == Item.ItemType.TRACK_ITEM) {
                binding.setVariable(BR.trackItem, itemArrayList.get(position));
            } else if(itemArrayList.get(position).getItemType() == Item.ItemType.HEADER) {
                binding.setVariable(BR.trackItem, itemArrayList.get(position));
                binding.setVariable(BR.headerItem, itemArrayList.get(position));
            }

            binding.setVariable(BR.listener, listener);
            binding.executePendingBindings();
        }
    }

    @Override
    public int getItemViewType(int position) {
        return itemArrayList.get(position).getItemType().hashCode();
    }

    @Override
    public int getItemCount() {
        return itemArrayList.size();
    }

    public static class BaseAdapterViewHolder extends RecyclerView.ViewHolder {

        private ViewDataBinding binding;

        public BaseAdapterViewHolder(View itemView) {
            super(itemView);

            binding = DataBindingUtil.bind(itemView);
        }

        public ViewDataBinding getBinding() {
            return binding;
        }
    }
}

*/
