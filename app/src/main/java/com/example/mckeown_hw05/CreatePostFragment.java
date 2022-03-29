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
import android.widget.Toast;

import com.example.mckeown_hw05.databinding.FragmentCreatePostBinding;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CreatePostFragment extends Fragment {
    private static final String TAG = "Create Post Fragment: ";
    CreatePostFragment.CreatePostFragmentListener mListener;
    FragmentCreatePostBinding binding;

    private final OkHttpClient client = new OkHttpClient();

    private static final String ARG_PARAM_TOKEN = "ARG_PARAM_TOKEN";
    private static final String ARG_PARAM_NAME = "ARG_PARAM_NAME";
    private static final String ARG_PARAM_ID = "ARG_PARAM_ID";

    private static String mToken;
    private static String mName;
    private static int mId;


    public CreatePostFragment() {
        // Required empty public constructor
    }

    public static CreatePostFragment newInstance(String token, String name, int id) {
        CreatePostFragment fragment = new CreatePostFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM_TOKEN, token);
        args.putString(ARG_PARAM_NAME, name);
        args.putInt(ARG_PARAM_ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCreatePostBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupUI();
    }

    private void setupUI() {
        getActivity().setTitle("Create Post");

        binding.textViewCancelNewPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.cancelNewPost();
            }
        });

        binding.buttonSubmitPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String postText = binding.editTextEnterPost.getText().toString();
                if (postText.isEmpty() || postText == null) {
                    Toast.makeText(getActivity().getApplicationContext(), "Post text cannot be blank", Toast.LENGTH_SHORT).show();
                } else {
                    createPost(mToken, postText);
                }
            }
        });
    }

     void createPost(String token, String postText) {
         FormBody formBody = new FormBody.Builder()
                 .add("post_text", postText)
                 .build();

         Request request = new Request.Builder()
                 .url("https://www.theappsdr.com/posts/create")
                 .post(formBody)
                 .addHeader("Authorization", "BEARER " + token)
                 .build();

         client.newCall(request).enqueue(new Callback() {
             @Override
             public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                 Log.d(TAG, "onCreatePostResponse: " + Thread.currentThread().getId());
                 if (response.isSuccessful()) {
                     Log.d(TAG, "New Post Successfully Created!");
                     mListener.submitNewPost();
                 } else {
                     Log.d(TAG, "Unable to create new post");
                 }
             }

             @Override
             public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
             }

         });
     }

    public static void getUserDetails(String token, String name, int id) {
        mToken = token;
        mName = name;
        mId = id;
        Log.d(TAG, "getUserDetails: User Details Received: ");
        Log.d(TAG, "token = " + mToken + " name = " + mName + " id = " + mId);
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (CreatePostFragment.CreatePostFragmentListener) context;
    }

    interface CreatePostFragmentListener {
        void submitNewPost();
        void cancelNewPost();
    }
}