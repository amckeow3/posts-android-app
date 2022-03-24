package com.example.mckeown_hw05;

import android.app.AlertDialog;
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
import android.widget.EditText;
import android.widget.Toast;

import com.example.mckeown_hw05.databinding.FragmentRegisterBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class RegisterFragment extends Fragment {
    private static final String TAG = "Registration Frag";
    RegisterFragment.RegisterFragmentListener mListener;
    FragmentRegisterBinding binding;
    
    private final OkHttpClient client = new OkHttpClient();

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        Log.d(TAG, "onCreate: " + Thread.currentThread().getId());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle("Create New Account");

        binding.textViewCancelRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.cancelAccountRegistration();
            }
        });

        binding.buttonSubmitRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = binding.editTextFullName.getText().toString();
                String email = binding.editTextRegistrationEmail.getText().toString();
                String password = binding.editTextRegistrationPassword.getText().toString();
                if (name.isEmpty() || name == null) {
                    Toast.makeText(getActivity().getApplicationContext(), "Name is required", Toast.LENGTH_SHORT).show();
                } else if (email.isEmpty() || email == null){
                    Toast.makeText(getActivity().getApplicationContext(),"Email is required", Toast.LENGTH_SHORT).show();
                } else if (password.isEmpty() || password == null) {
                    Toast.makeText(getActivity().getApplicationContext(), "Password is required", Toast.LENGTH_SHORT).show();
                } else {
                    createAccount(email, password, name);
                }
            }
        });

        //if the user has successfully registered, then the shared preferences is used to store the retrieved
        // information. Implies that if the user has a token then they are authenticated.
    }

    void createAccount(String email, String password, String name) {
        FormBody formBody = new FormBody.Builder()
                .add("email", email)
                .add("password", password)
                .add ("name", name)
                .build();

        Request request = new Request.Builder()
                .url("https://www.theappsdr.com/posts/signup")
                .post(formBody)
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
                    Log.d(TAG, "Registration Successful! " + body);

                    try {
                        JSONObject json = new JSONObject(body);

                        String mToken = json.getString("token");
                        Log.d(TAG, "token = " + mToken);

                        String mName = json.getString("user_fullname");
                        Log.d(TAG, "full name = " + mName);
                        
                        int mId = json.getInt("user_id");
                        Log.d(TAG, "user id = " + mId);

                        SharedPreferences mPreferences = getContext().getSharedPreferences("USER_AUTH", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = mPreferences.edit();
                        editor.putString("authToken", mToken);
                        editor.putString("authUser", mName);
                        editor.putInt("userId", mId);
                        editor.putBoolean("isLoggedIn", true);
                        editor.apply();

                        String authToken = mPreferences.getString("authToken", "");
                        String name = mPreferences.getString("authUser", "");
                        int id = mPreferences.getInt("userId", 0);
                        Boolean loggedIn = mPreferences.getBoolean("isLoggedIn", false);

                        Log.d(TAG, "Preferences on REGISTER()");
                        Log.d(TAG, "---------- token = " + authToken);
                        Log.d(TAG, "---------- name = " + name);
                        Log.d(TAG, "---------- id = " + id);
                        Log.d(TAG, "---------- isLoggedIn = " + loggedIn);
                        mListener.goToPostsList(mToken, mName, mId);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    ResponseBody responseBody = response.body();
                    String body = responseBody.string();
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity().getApplicationContext());
                    alertDialog.setMessage(body)
                            .setTitle(("Registration Unsuccessful"))
                            .setCancelable(true);

                    alertDialog.create().show();
                }
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (RegisterFragment.RegisterFragmentListener) context;
    }

    interface RegisterFragmentListener {
        void cancelAccountRegistration();
        void goToPostsList(String token, String fullName, int userId);
    }
}