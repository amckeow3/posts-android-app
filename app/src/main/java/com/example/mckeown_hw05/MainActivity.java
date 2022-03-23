

//  Homework 05
//  McKeown_HW05
//  Adrianna McKeown

package com.example.mckeown_hw05;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements LoginFragment.LoginFragmentListener, RegisterFragment.RegisterFragmentListener,
        CreatePostFragment.CreatePostFragmentListener, PostsListFragment.PostsListFragmentListener {

    private static final String TAG = "Main Activity: ";
    private SharedPreferences mPreferences;
    private String mAuthToken;
    private String mName;
    private int mId;
    private Boolean isLoggedIn;

    final ArrayList<Post> posts = new ArrayList<>();    //Array list for Expenses
    Post post;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // When the Main Activity starts, it check the shared preferences for the presence of the token and user information.
        mPreferences = this.getSharedPreferences("USER_AUTH", Context.MODE_PRIVATE);
        mAuthToken = mPreferences.getString("authToken", "");
        mName = mPreferences.getString("authUser", "");
        mId = mPreferences.getInt("userId", 0);
        isLoggedIn = mPreferences.getBoolean("isLoggedIn", false);

        Log.d(TAG, "Preferences on MainActivity Create():");
        Log.d(TAG, "---------- token =" + mAuthToken);
        Log.d(TAG, "---------- name = " + mName);
        Log.d(TAG, "---------- id = " + mId);
        Log.d(TAG, "---------- isLoggedIn = " + isLoggedIn);

        // If the token and user information are present and isLoggedIn is true, the the user is authenticated and the app displays the Posts List Fragment.
        if (isLoggedIn != false && mAuthToken != null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.rootView, PostsListFragment.newInstance(mAuthToken, mName, mId), "posts-list-fragment")
                    .commit();
        }
        // If the token and user information are not present in the shared preferences then the Login Fragment is displayed.
        else {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.rootView, new LoginFragment(), "login-fragment")
                    .commit();
        }
    }

    @Override
    public void goToRegistration() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView, new RegisterFragment(), "register-fragment")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void goToLogin() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView, new LoginFragment(), "login-fragment")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void goToCreatePost(String token, String fullName, int userId) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.rootView, new CreatePostFragment(), "create-post-fragment")
                    .addToBackStack(null)
                    .commit();
            CreatePostFragment.getUserDetails(token, fullName, userId);

    }

    @Override
    public ArrayList<Post> deleteAndRefresh(Post post) {
        posts.remove(post);
        return posts;
    }

    @Override
    public void cancelAccountRegistration() {
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void goToPostsList(String token, String fullName, int userId) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView, PostsListFragment.newInstance(token, fullName, userId), "posts-list-fragment")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void submitNewPost() {
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void cancelNewPost() {
        getSupportFragmentManager().popBackStack();
    }
}