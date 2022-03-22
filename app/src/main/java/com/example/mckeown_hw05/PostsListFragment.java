package com.example.mckeown_hw05;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mckeown_hw05.databinding.FragmentPostsListBinding;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class PostsListFragment extends Fragment {
    private static final String TAG = "Posts List Frag";
    PostsListFragment.PostsListFragmentListener mListener;
    FragmentPostsListBinding binding;

    private final OkHttpClient client = new OkHttpClient();

    private static final String ARG_PARAM_TOKEN = "ARG_PARAM_TOKEN";
    private static final String ARG_PARAM_NAME = "ARG_PARAM_NAME";
    private static final String ARG_PARAM_ID = "ARG_PARAM_ID";

    private String mToken;
    private String mName;
    private int mId;

    public PostsListFragment() {
        // Required empty public constructor
    }

    public static PostsListFragment newInstance(String token, String fullName, int userId) {
        PostsListFragment fragment = new PostsListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM_TOKEN, token);
        args.putString(ARG_PARAM_NAME, fullName);
        args.putInt(ARG_PARAM_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mToken = getArguments().getString(ARG_PARAM_TOKEN);
            mName = getArguments().getString(ARG_PARAM_NAME);
            mId = getArguments().getInt(ARG_PARAM_ID);
        }

        SharedPreferences mPreferences = getContext().getSharedPreferences("USER_AUTH", Context.MODE_PRIVATE);
        Boolean isLoggedIn = mPreferences.getBoolean("isLoggedIn", false);

        Log.d(TAG, "Preferences on POSTSLIST Create():");
        Log.d(TAG, "---------- token = " + mToken);
        Log.d(TAG, "---------- name = " + mName);
        Log.d(TAG, "---------- id = " + mId);
        Log.d(TAG, "---------- isLoggedIn = " + isLoggedIn);

        getPostsList(mToken);
    }

    void getPostsList(String token) {
        Request request = new Request.Builder()
                .url("https://www.theappsdr.com/posts")
                .addHeader("Authorization", "BEARER " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Log.d(TAG, "onResponse: " + Thread.currentThread().getId());

                if (response.isSuccessful()) {
                    ResponseBody responseBody = response.body();
                    String body = responseBody.string();
                    Log.d(TAG, "onResponse: " + body);
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPostsListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Posts");

        binding.textViewWelcome.setText("Welcome " + mName);

        binding.buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Clicking the "Logout" button clears all the stored token and name information stored in the shared preference,
                // sets the id to 0 and sets isLoggedIn to false
                SharedPreferences mPreferences = getContext().getSharedPreferences("USER_AUTH", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.remove("authToken");
                editor.remove("authUser");
                editor.putInt("userId", 0);
                editor.putBoolean("isLoggedIn", false);
                editor.apply();

                mListener.goToLogin();
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (PostsListFragment.PostsListFragmentListener) context;
    }

    interface PostsListFragmentListener {
        void goToLogin();
    }
}