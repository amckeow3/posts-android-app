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

import com.example.mckeown_hw05.databinding.FragmentLoginBinding;

import okhttp3.OkHttpClient;


public class LoginFragment extends Fragment {
    private static final Object MODE_PRIVATE = "mode_private";
    LoginFragment.LoginFragmentListener mListener;
    FragmentLoginBinding binding;

    private final OkHttpClient client = new OkHttpClient();

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public LoginFragment() {
        // Required empty public constructor
    }

    public static LoginFragment newInstance(String param1, String param2) {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Fetching the stored data
        // from the SharedPreference
        SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);

        String s1 = sharedPreferences.getString("name", "");
        String s2 = sharedPreferences.getString("password", "");

        // Setting the fetched data
        // in the EditTexts
        //name.setText(s1);
        //age.setText(String.valueOf(a));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle("Login");

        //  clicking “Create New Account” replaces the Login fragment with the Create New Account Fragment.
        binding.textViewNewAcct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.goToRegistration();
            }
        });

        binding.buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (binding.editTextLoginEmail.getText() == null) {
                    Toast.makeText(getActivity().getApplicationContext(), "Email is required", Toast.LENGTH_SHORT).show();
                } else if (binding.editTextLoginPassword.getText() == null) {
                    Toast.makeText(getActivity().getApplicationContext(), "Password is required", Toast.LENGTH_SHORT).show();
                }
            }
        });


        //if the user has successfully logged in, then the shared preferences is used to store the retrieved
        // information. Implies that if the user has a token then they are authenticated.
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (LoginFragment.LoginFragmentListener) context;
    }

    interface LoginFragmentListener {
        void goToRegistration();
    }
}