package com.example.mckeown_hw05;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity implements LoginFragment.LoginFragmentListener, RegisterFragment.RegisterFragmentListener,
        CreatePostFragment.CreatePostFragmentListener, PostsListFragment.PostsListFragmentListener {

    private static final String TAG = "Main Activity: ";
    private SharedPreferences mPreferences;
    private String mAuthToken;
    private Boolean isLoggedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPreferences = this.getSharedPreferences("AUTH_USER", Context.MODE_PRIVATE);
        mAuthToken = mPreferences.getString("authToken", "");
        isLoggedIn = mPreferences.getBoolean("isLoggedIn", false);
        Log.d(TAG, "On MainActivity Create(): " + " token = " + mAuthToken + "---------- isLoggedIn =" + isLoggedIn);

        if (isLoggedIn != false && mAuthToken != null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.rootView, PostsListFragment.newInstance(mAuthToken))
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.rootView, new LoginFragment())
                    .commit();
        }
    }

    @Override
    public void goToRegistration() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView, new RegisterFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void goToLogin() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView, new LoginFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void goToPostsList(String token, String fullName, int userId) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView, PostsListFragment.newInstance(token, fullName, userId))
                .addToBackStack(null)
                .commit();
    }
}