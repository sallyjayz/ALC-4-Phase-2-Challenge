package com.example.travelmantics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FirebaseUtil {
    public static FirebaseDatabase firebaseDatabase;
    public static DatabaseReference databaseReference;
    public static FirebaseStorage storage;
    public static StorageReference storageReference;
    private static FirebaseUtil firebaseUtil;
    public static FirebaseAuth firebaseAuth;
    public static FirebaseAuth.AuthStateListener authStateListener;
    private static final int RC_SIGN_IN = 123;
    private static UserActivity caller;
    public static ArrayList<HolidayDeal> holidayDeals;
    public static boolean isAdmin;

    private FirebaseUtil(){};

    public static void openFirebaseReference(String ref, final UserActivity callerActivity) {
        if(firebaseUtil == null) {
            firebaseUtil = new FirebaseUtil();
            firebaseDatabase = FirebaseDatabase.getInstance();
            firebaseAuth = FirebaseAuth.getInstance();
            caller = callerActivity;
            authStateListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    if(firebaseAuth.getCurrentUser() == null) {
                        FirebaseUtil.signIn();
                    }else {
                        String userId = firebaseAuth.getUid();
                        checkAdmin(userId);
                    }
                }
            };
            connectStorage();
        }
        holidayDeals = new ArrayList<HolidayDeal>();
        databaseReference = firebaseDatabase.getReference().child(ref);
    }

    private static void signIn() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );

        caller.startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder()
                .setAvailableProviders(providers).build(),
                RC_SIGN_IN
        );
    }


    private static void checkAdmin(String uid) {
        FirebaseUtil.isAdmin = false;
        DatabaseReference ref = firebaseDatabase.getReference()
                .child("administrators").child(uid);
        ChildEventListener listener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                FirebaseUtil.isAdmin=true;
                caller.showMenu();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        ref.addChildEventListener(listener);
    }

    public static void attachListener() {
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    public static void detachListener() {
        firebaseAuth.removeAuthStateListener(authStateListener);
    }

    private static void connectStorage() {
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference().child("deals_pictures/");
    }

}
