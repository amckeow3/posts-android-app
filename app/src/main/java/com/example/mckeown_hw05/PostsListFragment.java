package com.example.mckeown_hw05;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.TextView;

import com.example.mckeown_hw05.databinding.FragmentPostsListBinding;
import com.example.mckeown_hw05.databinding.PagerCardViewBinding;
import com.example.mckeown_hw05.databinding.PostLineItemBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
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
    ArrayList<Integer> pages = new ArrayList<>();

    private String mToken;
    private String mName;
    private int mId;
    int postsCount;
    int pageCount;
    int selectedPage = 1;

    private PostsListAdapter postsAdapter;
    private PagerAdapter pagerAdapter;
    LinearLayoutManager postsLayoutManager, pagerLayoutManager;
    RecyclerView postsRecyclerView, pagerRecyclerView;



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
        Log.d(TAG, "Posts List onCreate Called");
        getPostsList(mToken, selectedPage);
    }

    // Uses the OkHttp library to make an http connection and API call to the /posts API. The /posts api returns a single page
    // of posts based on the provided “page” query parameter, which indicates which result page is being requested.
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

                        // The /posts API returns the totalCount which is the total number of posts currently stored in the system.
                        postsCount = json.getInt("totalCount");
                        pages.clear();
                        pageCount = postsCount/10;
                        for (int i=0; i < pageCount; i++) {
                            pages.add(i+1);
                        }

                        // An array of posts based on the provided page parameter is returned
                        // Each post returned includes the post id, post text, post creation date/time, and the post’s creator id and name
                        for (int i=0; i < postsJson.length(); i++) {
                            JSONObject postJsonObject = postsJson.getJSONObject(i);

                            // New Post class
                            Post post = new Post();

                            // The returned list of posts is parsed into an ArrayList
                            // containing the parsed Post objects.
                            post.setCreator(postJsonObject.getString("created_by_name"));
                            post.setId(postJsonObject.getString("post_id"));
                            post.setId(postJsonObject.getString("created_by_uid"));
                            post.setText(postJsonObject.getString("post_text"));
                            post.setDateTime(postJsonObject.getString("created_at"));

                            // Parsed post is added to the array list
                            postsList.add(post);
                        }

                        // The parsed list of Post objects is used to display the posts list in the RecyclerView.
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                binding.textViewPagerSummary.setText("Showing Page " + page + " out of " + pageCount);

                                // Posts Recycler View
                                postsRecyclerView = binding.postsRecyclerView;
                                postsRecyclerView.setHasFixedSize(true);
                                postsLayoutManager = new LinearLayoutManager(getContext());
                                postsRecyclerView.setLayoutManager(postsLayoutManager);
                                postsAdapter = new PostsListAdapter(postsList);
                                postsRecyclerView.setAdapter(postsAdapter);

                                // Pager Recycler View
                                pagerRecyclerView = binding.pagerRecyclerView;
                                pagerRecyclerView.setHasFixedSize(false);
                                pagerLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false);
                                pagerRecyclerView.setHasFixedSize(true);
                                pagerRecyclerView.setLayoutManager(pagerLayoutManager);
                                pagerAdapter = new PagerAdapter(pages);
                                pagerRecyclerView.setAdapter(pagerAdapter);
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    void deletePost(String token, String post_id) {
        FormBody formBody = new FormBody.Builder()
                .add("post_id", post_id)
                .build();

        Request request = new Request.Builder()
                .url("https://www.theappsdr.com/posts/delete")
                .post(formBody)
                .addHeader("Authorization", "BEARER " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                if (response.isSuccessful()) {
                    Log.d(TAG, "Post Successfully Deleted!");
                    ResponseBody responseBody = response.body();
                    String body = responseBody.string();
                    Log.d(TAG, "DeletePost() onResponse: " + body);
                } else {
                    ResponseBody responseBody = response.body();
                    String body = responseBody.string();
                    Log.d(TAG, "Post deletion was unsuccessful:" + body + " for post_id {" + post_id + "}");
                }
                getPostsList(mToken, selectedPage);
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
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
        getPostsList(mToken, selectedPage);
        Log.d(TAG, "PostsList Fragment onViewCreated Called");
    }

    private void setupUI() {
        getActivity().setTitle("Posts");

        getPostsList(mToken, selectedPage);

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

                // Posts List Fragment is replaced with the Login Fragment
                mListener.goToLogin();
            }
        });

        // Clicking the “Create Post” button should replaces the Posts List Fragment with the Create Post Fragment,
        // and the Posts Lists Fragment is put on the back stack.
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

                mBinding.textViewPostCreator.setText(mPost.creator);
                mBinding.textViewPostText.setText(mPost.text);
                mBinding.textViewTimeStamp.setText(mPost.dateTime);

                SharedPreferences mPreferences = getContext().getSharedPreferences("USER_AUTH", Context.MODE_PRIVATE);
                int user_id = mPreferences.getInt("userId", 0);

                if (user_id == Integer.valueOf(mPost.id)) {
                    mBinding.imageViewTrashIcon.setImageResource(R.drawable.ic_trash);
                    mBinding.imageViewTrashIcon.setVisibility(View.VISIBLE);
                    mBinding.imageViewTrashIcon.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            deletePost(mToken, mPost.id);
                        }
                    });
                } else {
                    mBinding.imageViewTrashIcon.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    class PagerAdapter extends RecyclerView.Adapter<PagerAdapter.PagerAdapterViewHolder> {
        ArrayList <Integer> mPages;

        public PagerAdapter(ArrayList<Integer> data) {
            this.mPages = data;
        }

        @NonNull
        @Override
        public PagerAdapter.PagerAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            PagerCardViewBinding binding = PagerCardViewBinding.inflate(getLayoutInflater(), parent, false);
            return new PagerAdapter.PagerAdapterViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull PagerAdapter.PagerAdapterViewHolder holder, int position) {
            int page = mPages.get(position);
            holder.setupUI(page);
        }

        @Override
        public int getItemCount() {
            return mPages.size();
        }

        class PagerAdapterViewHolder extends RecyclerView.ViewHolder {
            PagerCardViewBinding mBinding;
            int mPage;

            public PagerAdapterViewHolder(PagerCardViewBinding binding) {
                super(binding.getRoot());
                mBinding = binding;
            }

            // For the posts list, each post row item displays the post text, creators name, creation date
            public void setupUI(int page) {
                mPage = page;
                mBinding.textViewPageNumber.setText(String.valueOf(page));

                mBinding.textViewPageNumber.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selectedPage = page;
                        getPostsList(mToken, selectedPage);
                    }
                });
            }
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        Log.d(TAG, "PostsList Fragment onAttach Called: ");
        super.onAttach(context);
        mListener = (PostsListFragment.PostsListFragmentListener) context;
    }

    interface PostsListFragmentListener {
        void goToLogin();
        void goToCreatePost(String token, String fullName, int userId);
        ArrayList<Post> deleteAndRefresh(Post post);
    }
}