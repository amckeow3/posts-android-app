package com.example.mckeown_hw05;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mckeown_hw05.databinding.FragmentPostsListBinding;
import com.example.mckeown_hw05.databinding.PostLineItemBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

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

    ArrayList<Post> postsList = new ArrayList<>();
    ArrayList<Post> mPosts = new ArrayList<>();

    private String mToken;
    private String mName;
    private int mId;

    PostsListAdapter adapter;
    LinearLayoutManager layoutManager;
    RecyclerView recyclerView;

    int page;

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

        getPostsList(mToken, 1);
    }

    void getPostsList(String token, int page) {
        Request request = new Request.Builder()
                .url("https://www.theappsdr.com/posts?page=" + page)
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

                    postsList.clear();

                    // Parsing the returned list of posts into an ArrayList containing the parsed Post objects
                    try {
                        JSONObject json = new JSONObject(body);
                        JSONArray postsJson = json.getJSONArray("posts");

                        for (int i=0; i < postsJson.length(); i++) {
                            JSONObject postJsonObject = postsJson.getJSONObject(i);

                            Post post = new Post();

                            post.setCreator(postJsonObject.getString("created_by_name"));
                            post.setId(postJsonObject.getString("post_id"));
                            post.setId(postJsonObject.getString("created_by_uid"));
                            post.setText(postJsonObject.getString("post_text"));
                            post.setDateTime(postJsonObject.getString("created_at"));

                            // Parsed post is added to the array list
                            postsList.add(post);
                        }
                        Log.d(TAG, "Posts List: " + postsList);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                recyclerView = binding.postsRecyclerView;
                                recyclerView.setHasFixedSize(false);

                                layoutManager = new LinearLayoutManager(getContext());
                                recyclerView.setLayoutManager(layoutManager);
                                Log.d(TAG, "Adapter list: " + postsList);
                                adapter = new PostsListAdapter(postsList);
                                recyclerView.setAdapter(adapter);
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
    }

    void deletePost(String token, int post_id) {
        FormBody formBody = new FormBody.Builder()
                .add("post_id", String.valueOf(post_id))
                .build();

        Request request = new Request.Builder()
                .url("https://www.theappsdr.com/posts/delete")
                .post(formBody)
                .addHeader("Authorization", "BEARER " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Log.d(TAG, "onDeleteResponse: " + Thread.currentThread().getId());
                if (response.isSuccessful()) {
                    Log.d(TAG, "Post Successfully Deleted!");
                    ResponseBody responseBody = response.body();
                    String body = responseBody.string();
                    Log.d(TAG, "DeletePost() onResponse: " + body);
                    getPostsList(mToken, page);
                } else {
                    ResponseBody responseBody = response.body();
                    String body = responseBody.string();
                    Log.d(TAG, "Post deletion was unsuccessful:" + body + " for post_id {" + post_id + "}");
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

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
        setupUI();

    }

    private void setupUI() {
        getActivity().setTitle("Posts");

        page = 1;
        getPostsList(mToken, page);

        // The greeting TextView shows “Hello XX” where XX is the name of the logged in
        // user. (This information was captured from the response of either the /posts/login or /posts/signup apis.)
        binding.textViewWelcome.setText("Welcome " + mName);

        binding.buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Upon user clicking the "Logout" button, the token and user information are deleted from the shared preferences,
                // and isLoggedIn is set to false.
                SharedPreferences mPreferences = getContext().getSharedPreferences("USER_AUTH", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.remove("authToken");
                editor.remove("authUser");
                editor.putInt("userId", 0);
                editor.putBoolean("isLoggedIn", false);
                editor.apply();

                Log.d(TAG, "on Logout(): ");
                Log.d(TAG, "isLoggedIn = " + mPreferences.getBoolean("isLoggedIn", false));
                mListener.goToLogin();
            }
        });

        binding.buttonCreatePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences mPreferences = getContext().getSharedPreferences("USER_AUTH", Context.MODE_PRIVATE);
                String authToken = mPreferences.getString("authToken", "");
                String fullName = mPreferences.getString("authUser", "");
                int userId = mPreferences.getInt("userId", 0);

                mListener.goToCreatePost(authToken, fullName, userId);
            }
        });
    }

    class PostsListAdapter extends RecyclerView.Adapter<PostsListAdapter.PostsListViewHolder> {
        ArrayList<Post> mPosts;

        public PostsListAdapter(ArrayList<Post> data){
            this.mPosts = data;
        }

        @NonNull
        @Override
        public PostsListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            PostLineItemBinding binding = PostLineItemBinding.inflate(getLayoutInflater(), parent, false);
            return new PostsListViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull PostsListViewHolder holder, int position) {
            Post post = mPosts.get(position);
            holder.setupUI(post);
        }

        @Override
        public int getItemCount() {
            return this.mPosts.size();
        }

        class PostsListViewHolder extends RecyclerView.ViewHolder {
            PostLineItemBinding mBinding;
            Post mPost;

            public PostsListViewHolder(PostLineItemBinding binding) {
                super(binding.getRoot());
                mBinding = binding;
            }

            public void setupUI(Post post) {
                mPost = post;

                mBinding.imageViewTrashIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deletePost(mToken, Integer.parseInt(mPost.id));
                        adapter.notifyDataSetChanged();

                    }
                });

                mBinding.textViewPostCreator.setText(mPost.creator);
                mBinding.textViewPostText.setText(mPost.text);
                mBinding.textViewTimeStamp.setText(mPost.dateTime);


            }
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (PostsListFragment.PostsListFragmentListener) context;
    }

    interface PostsListFragmentListener {
        void goToLogin();
        void goToCreatePost(String token, String fullName, int userId);
        ArrayList<Post> deleteAndRefresh(Post post);
    }
}