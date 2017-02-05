package com.wolffincdevelopment.hiit_it;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;
import com.wolffincdevelopment.hiit_it.manager.UserManager;
import com.wolffincdevelopment.hiit_it.util.StringUtils;

/**
 * Created by Kyle Wolff on 2/2/17.
 */

public class FireBaseHelper {

    private boolean peristenceEnabled;
    private static FireBaseHelper fireBaseHelper;

    private UserManager userManager;

    public static FireBaseHelper getInstance(UserManager userManager) {

        if (fireBaseHelper == null) {
            fireBaseHelper = new FireBaseHelper().setUserManager(userManager);
        }

        return fireBaseHelper;
    }

    public FireBaseHelper setUserManager(UserManager userManager) {
        this.userManager = userManager;
        return this;
    }

    private DatabaseReference getDatabaseReference() {

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

        if (!peristenceEnabled) {
            firebaseDatabase.setPersistenceEnabled(true);
            peristenceEnabled = true;
        }

        return firebaseDatabase.getReference();
    }

    /**
     * Set value of a specific child. Hier is tracks -> userKey -> {child}
     *
     * @param child
     * @param o     the object you want to save
     */
    public void setValue(String child, Object o) {
        if (getTracksAndUserKeyChild() != null) {
            setValue(getTracksAndUserKeyChild().child(child), o);
        }
    }

    private void setValue(DatabaseReference databaseReference, Object o) {
        databaseReference.setValue(o);
    }

    public DatabaseReference getTracksAndUserKeyChild() {
        if (!StringUtils.isEmptyOrNull(userManager.getPrefUserKey())) {
            return getDatabaseReference().child("users").child(userManager.getPrefUserKey()).child("tracks");
        } else {
           return null;
        }
    }

    public String getRandomKey() {
        return getDatabaseReference().push().getKey();
    }
}
