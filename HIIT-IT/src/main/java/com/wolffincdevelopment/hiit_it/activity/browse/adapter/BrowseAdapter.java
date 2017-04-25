package com.wolffincdevelopment.hiit_it.activity.browse.adapter;

import android.databinding.ViewDataBinding;

import com.wolffincdevelopment.hiit_it.BR;
import com.wolffincdevelopment.hiit_it.activity.browse.listener.BrowseListener;
import com.wolffincdevelopment.hiit_it.activity.browse.viewmodel.HeaderItem;
import com.wolffincdevelopment.hiit_it.activity.browse.viewmodel.Item;
import com.wolffincdevelopment.hiit_it.activity.browse.viewmodel.ListItem;
import com.wolffincdevelopment.hiit_it.service.model.TrackData;
import com.wolffincdevelopment.hiit_it.util.BaseDataBindingAdapter;
import com.wolffincdevelopment.hiit_it.util.DataBoundViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kyle Wolff on 2/4/17.
 */

public class BrowseAdapter extends BaseDataBindingAdapter {

    private ArrayList<Item> browseListItems;
    private BrowseListener listener;

    private String headerChar;
    private String previousChar;

    public BrowseAdapter(BrowseListener listener) {
        this.listener = listener;

        browseListItems = new ArrayList<>();
    }

    public void updateData(List<TrackData> trackList) {

        for (TrackData trackData : trackList) {

            headerChar = String.valueOf(trackData.getArtist().charAt(0));
            HeaderItem headerItem = new HeaderItem(headerChar);

            if (browseListItems.isEmpty()) {
                browseListItems.add(headerItem);
                browseListItems.add(new ListItem(trackData));
            } else {

                if (previousChar.compareTo(headerChar) != 0) {
                    browseListItems.add(headerItem);
                }

                browseListItems.add(new ListItem(trackData));

            }

            previousChar = headerChar;
        }

        notifyDataSetChanged();
    }

    @Override
    protected void bindItem(DataBoundViewHolder holder, int position, List payloads) {

        ViewDataBinding binding = holder.binding;

        if (binding != null) {
            binding.setVariable(BR.binding, binding);
            binding.setVariable(BR.item, browseListItems.get(position));
            binding.setVariable(BR.listener, listener);
        }

    }

    @Override
    public int getItemViewType(int position) {
        return browseListItems.get(position).resourceId;
    }

    @Override
    public int getItemCount() {
        return browseListItems.size();
    }
}
