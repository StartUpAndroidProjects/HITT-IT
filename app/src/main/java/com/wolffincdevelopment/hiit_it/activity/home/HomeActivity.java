package com.wolffincdevelopment.hiit_it.activity.home;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.wolffincdevelopment.hiit_it.IconizedMenu;
import com.wolffincdevelopment.hiit_it.R;
import com.wolffincdevelopment.hiit_it.activity.HiitItActivity;
import com.wolffincdevelopment.hiit_it.activity.home.adapters.HomeAdapter;
import com.wolffincdevelopment.hiit_it.activity.home.viewmodel.HomeItem;
import com.wolffincdevelopment.hiit_it.activity.home.viewmodel.HomeListItem;
import com.wolffincdevelopment.hiit_it.databinding.ActivityHomeBinding;
import com.wolffincdevelopment.hiit_it.service.model.TrackData;
import com.wolffincdevelopment.hiit_it.util.StringUtils;
import com.wolffincdevelopment.hiit_it.widget.MediaControllerView;

import java.util.List;

/**
 * Created by Kyle Wolff on 1/28/17.
 */

public class HomeActivity extends HiitItActivity implements HomeItem.HomeItemCallback, MediaControllerView.MediaControllerListener {

    private ActivityHomeBinding binding;
    private HomeItem homeItem;
    private HomeAdapter homeAdapter;
    private Animation rotateForward;
    private Animation rotateBackward;
    private IconizedMenu firstMenuSelected;

    private boolean fabMenuIsShowing;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        homeItem = new HomeItem(this, userManager, getRxJavaBus(), fireBaseHelper);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_home);
        binding.setItem(homeItem);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        binding.controllerView.setListener(this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        homeAdapter = new HomeAdapter(this, homeItem);
        binding.recyclerView.setAdapter(homeAdapter);

        rotateForward = AnimationUtils.loadAnimation(getBaseContext(), R.anim.rotate_forward);
        rotateBackward = AnimationUtils.loadAnimation(getBaseContext(), R.anim.rotate_backward);

        if (StringUtils.isEmptyOrNull(userManager.getPrefUserKey())) {
            userManager.setUserKey(fireBaseHelper.getRandomKey());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        homeItem.onViewAttached(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        homeItem.onViewResumed();
    }

    @Override
    protected void onDestroy() {
        homeItem.onViewDetached();
        super.onDestroy();
    }

    @Override
    public void onShowProgressView(boolean isVisible, @StringRes int textResourceId) {

    }

    @Override
    public void onDataReady(List<TrackData> trackDataList) {
        homeAdapter.updateData(trackDataList, getRxJavaBus());
    }

    @Override
    public void onPlay() {

    }

    @Override
    public void onNext() {

    }

    @Override
    public void onPrev() {

    }

    @Override
    public void onItemClicked() {
        Toast.makeText(this, "Item Clicked", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onOptionsClicked(View view, HomeListItem homeListItem) {

        final IconizedMenu popup = new IconizedMenu(this, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.pop_up_menu, popup.getMenu());
        popup.show();

        if (firstMenuSelected != null && firstMenuSelected.isShowing() && popup.isShowing()) {
            firstMenuSelected.dismiss();
        }

        firstMenuSelected = popup;

        popup.setOnMenuItemClickListener(new IconizedMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {

                return true;
            }
        });
    }

    @Override
    public void onBrowseClicked() {
        Toast.makeText(this, "Browse Was Clicked", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFabMenuClicked() {

        if (fabMenuIsShowing) {
            closeFABMenu();
        } else {
            showFABMenu();
        }
    }

    private void showFABMenu() {

        fabMenuIsShowing = true;

        binding.fab.startAnimation(rotateForward);

        binding.fabBrowse.animate().translationY(-getResources().getDimension(R.dimen.fab_marginBottom_Browse_animation))
                .alpha(1f).setDuration(300);

        binding.fabSpotify.animate().translationY(-getResources().getDimension(R.dimen.fab_marginBottom_Spotify_animation))
                .alpha(1f).setDuration(300);
    }

    private void closeFABMenu() {

        fabMenuIsShowing = false;

        binding.fab.startAnimation(rotateBackward);

        binding.fabBrowse.animate().translationY(0).alpha(0).setDuration(300);
        binding.fabSpotify.animate().translationY(0).alpha(0).setDuration(300);
    }
}
