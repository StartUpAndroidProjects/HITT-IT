package com.wolffincdevelopment.hiit_it.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.wolffincdevelopment.hiit_it.BR;
import com.wolffincdevelopment.hiit_it.IconizedMenu;
import com.wolffincdevelopment.hiit_it.databinding.BaseActivityRowBinding;
import com.wolffincdevelopment.hiit_it.listeners.FooterListener;
import com.wolffincdevelopment.hiit_it.listeners.MenuListener;
import com.wolffincdevelopment.hiit_it.listeners.TrackListener;
import com.wolffincdevelopment.hiit_it.viewmodel.HomeFooter;
import com.wolffincdevelopment.hiit_it.viewmodel.Item;
import com.wolffincdevelopment.hiit_it.R;
import com.wolffincdevelopment.hiit_it.viewmodel.TrackItem;
import com.wolffincdevelopment.hiit_it.util.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kyle Wolff on 11/11/16.
 */

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.HomeAdapterViewHolder> implements FooterListener {

    private SharedPreferencesUtil sharedPreferencesUtil;
    private TrackListener listener;
    private MenuListener menuListener;
    private IconizedMenu firstMenuSelected;

    private List<Item> items;

    public HomeAdapter(List<TrackItem> trackItems, TrackListener listener, MenuListener menuListener) {

        items = new ArrayList<>();

        sharedPreferencesUtil = SharedPreferencesUtil.getInstance();

        this.listener = listener;
        this.menuListener = menuListener;

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

                final TrackItem item = (TrackItem) items.get(position);

                binding.setVariable(BR.trackItem, items.get(position));
                binding.setVariable(BR.trackListener, listener);

                BaseActivityRowBinding activityRowBinding = (BaseActivityRowBinding) holder.getBinding();
                activityRowBinding.optionsIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showMenu(view, item);
                    }
                });

            } else if(items.get(position).getItemType() == Item.ItemType.FOOTER) {
                binding.setVariable(BR.homeFooter, items.get(position));
                binding.setVariable(BR.footerListener, this);
            }

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

    // The method to display the popUp menu
    private void showMenu(View v, final TrackItem trackItem) {

        final IconizedMenu popup = new IconizedMenu(v.getContext(), v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.pop_up_menu, popup.getMenu());
        popup.show();

        if(firstMenuSelected != null && firstMenuSelected.isShowing() && popup.isShowing()) {
            firstMenuSelected.dismiss();
        }

        firstMenuSelected = popup;

        popup.setOnMenuItemClickListener(new IconizedMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {

                if(menuListener != null) {
                    menuListener.onMenuItemSelected(trackItem, item);
                }

                return true;
            }
        });
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
