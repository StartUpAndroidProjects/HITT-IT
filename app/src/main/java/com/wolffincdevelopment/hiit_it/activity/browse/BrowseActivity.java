package com.wolffincdevelopment.hiit_it.activity.browse;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.databinding.DataBindingUtil;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;

import com.wolffincdevelopment.hiit_it.HiitItIntents;
import com.wolffincdevelopment.hiit_it.activity.HiitItActivity;
import com.wolffincdevelopment.hiit_it.activity.HiitItIntent;
import com.wolffincdevelopment.hiit_it.activity.browse.adapter.BrowseAdapter;
import com.wolffincdevelopment.hiit_it.activity.browse.viewmodel.BrowseItem;
import com.wolffincdevelopment.hiit_it.activity.browse.viewmodel.ListItem;
import com.wolffincdevelopment.hiit_it.databinding.ActivityBrowseBinding;
import com.wolffincdevelopment.hiit_it.service.model.TrackData;
import com.wolffincdevelopment.hiit_it.util.ActionBarUtils;
import com.wolffincdevelopment.hiit_it.R;
import com.wolffincdevelopment.hiit_it.util.DialogBuilder;
import com.wolffincdevelopment.hiit_it.util.PermissionUtil;

import java.util.List;


/*
 * Created by kylewolff on 6/4/2016.
 */
public class BrowseActivity extends HiitItActivity implements BrowseItem.BrowseItemCallback {


    private ActivityBrowseBinding binding;
    private BrowseItem browseItem;
    private BrowseAdapter browseAdapter;
    private Snackbar snackbar;
    private PermissionUtil permissionUtil;

    private boolean checkedPermission;

    @Override
    protected void onCreate(Bundle savedInstances) {
        super.onCreate(savedInstances);

        browseItem = new BrowseItem(this, this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_browse);
        binding.setItem(browseItem);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        ActionBarUtils.showUpButton(this);

        binding.browseListView.setLayoutManager(new LinearLayoutManager(this));

        browseAdapter = new BrowseAdapter(browseItem);
        binding.browseListView.setAdapter(browseAdapter);

        permissionUtil = new PermissionUtil();

        snackbar = Snackbar.make(binding.getRoot(), R.string.cannot_access_media, Snackbar.LENGTH_LONG);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!checkedPermission) {
            checkedPermission = true;

            if (permissionUtil.checkReadStoragePermission(this)) {
                browseItem.setPermitted(true);
                checkedPermission = false;
            }
        }

        browseItem.onViewResumed();
    }

    @Override
    protected void onStart() {
        super.onStart();
        browseItem.onViewAttached(this);
    }

    @Override
    protected void onDestroy() {
        browseItem.onViewDetached();
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        final Activity activity = this;

        if (requestCode == 0) {

            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

                    showDialogOK("Read Storage Permission is required for this app. Try again?",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    switch (which) {

                                        case DialogInterface.BUTTON_POSITIVE:
                                            permissionUtil.checkReadStoragePermission(activity);

                                            break;

                                        case DialogInterface.BUTTON_NEGATIVE:
                                            if (!snackbar.isShown()) {
                                                showSnackBar();
                                            }

                                            break;
                                    }
                                }
                            });
                } else {
                    showSnackBar();
                }
            } else {
                browseItem.setPermitted(true);
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void showSnackBar() {
        snackbar.show();
    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        DialogBuilder dialogBuilder = new DialogBuilder(message, this);
        dialogBuilder.setButtons("Ok", "Cancel", okListener);
        dialogBuilder.create();
        dialogBuilder.show();
    }

    @Override
    public void onShowProgressView(boolean isVisible, @StringRes int textResourceId) {

    }

    @Override
    public void onItemClicked(ListItem listItem) {

        if (getIntent().getBooleanExtra(HiitItIntents.EXTRA_FINISH_ACTIVITY, false)) {
            setResult(RESULT_OK, new Intent().putExtra(HiitItIntents.EXTRA_ITEM, listItem.getTrackData()));
            finish();
        } else {
            //startActivity(HiitItIntents.createAddTrackIntent(this, data));
        }

    }

    @Override
    public void onVerifyPermissions() {
        startActivity(HiitItIntent.createSettingsPermission(this));
        finish();
    }

    @Override
    public void onDataReady(List<TrackData> trackDataList) {
        browseAdapter.updateData(trackDataList);
    }
}
