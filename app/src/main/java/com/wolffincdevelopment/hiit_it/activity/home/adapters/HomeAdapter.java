package com.wolffincdevelopment.hiit_it.activity.home.adapters;

import android.content.Context;
import android.databinding.ViewDataBinding;

import com.wolffincdevelopment.hiit_it.BR;
import com.wolffincdevelopment.hiit_it.R;
import com.wolffincdevelopment.hiit_it.RxJavaBus;
import com.wolffincdevelopment.hiit_it.activity.home.listeners.HomeListItemListener;
import com.wolffincdevelopment.hiit_it.activity.home.viewmodel.HomeListItem;
import com.wolffincdevelopment.hiit_it.databinding.ViewHomeListItemBinding;
import com.wolffincdevelopment.hiit_it.service.model.TrackData;
import com.wolffincdevelopment.hiit_it.util.BaseDataBindingAdapter;
import com.wolffincdevelopment.hiit_it.util.DataBoundViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kyle Wolff on 1/29/17.
 */

public class HomeAdapter extends BaseDataBindingAdapter {

    private List<HomeListItem> homeListItems;
    private HomeListItemListener listener;

    private Context context;

    public HomeAdapter(Context context, HomeListItemListener listener) {

        this.context = context;
        this.listener = listener;
        homeListItems = new ArrayList<>();
    }

    public void updateData(List<HomeListItem> homeListItems) {

        this.homeListItems = homeListItems;

        notifyDataSetChanged();
    }

    @Override
    protected void bindItem(DataBoundViewHolder holder, int position, List payloads) {

        ViewDataBinding binding = holder.binding;
        ViewHomeListItemBinding itemBinding = (ViewHomeListItemBinding) holder.binding;

        if (binding != null) {
            binding.setVariable(BR.item, homeListItems.get(position));
            binding.setVariable(BR.listener, listener);
            binding.setVariable(BR.imageButton, itemBinding.optionsIcon);
            binding.setVariable(BR.binding, binding);
            binding.executePendingBindings();
        }
    }

    @Override
    public int getItemViewType(int position) {
        return R.layout.view_home_list_item;
    }

    @Override
    public int getItemCount() {
        return homeListItems.size();
    }
}
