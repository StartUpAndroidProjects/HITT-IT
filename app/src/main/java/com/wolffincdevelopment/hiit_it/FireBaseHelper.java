package com.wolffincdevelopment.hiit_it;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.wolffincdevelopment.hiit_it.manager.UserManager;

/**
 * Created by Kyle Wolff on 2/2/17.
 */

public class FireBaseHelper {

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
        return FirebaseDatabase.getInstance().getReference();
    }

    /**
     * Set value of a specific child. Hier is tracks -> userKey -> {child}
     * @param child
     * @param o the object you want to save
     */
    public void setValue(String child, Object o) {
        setValue(getDatabaseReference().child("tracks").child(userManager.getPrefUserKey()).child(child), o);
    }

    private void setValue(DatabaseReference databaseReference, Object o) {
        databaseReference.setValue(o);
    }

    public DatabaseReference getTrackKeyChild() {
        return getDatabaseReference().child("tracks").child(userManager.getPrefUserKey());
    }

    public String getRandomKey() {
       return getDatabaseReference().push().getKey();
    }
}
