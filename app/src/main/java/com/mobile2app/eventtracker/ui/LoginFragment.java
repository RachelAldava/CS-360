package com.mobile2app.eventtracker.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.mobile2app.eventtracker.DatabaseHandler;
import com.mobile2app.eventtracker.MainActivity;
import com.mobile2app.eventtracker.NotificationsManager;
import com.mobile2app.eventtracker.R;
import com.mobile2app.eventtracker.User;
import com.mobile2app.eventtracker.appEvent;
import com.mobile2app.eventtracker.databinding.FragmentLoginBinding;

import java.util.ArrayList;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    Button login, register;
    EditText usernameBox, passwordBox;
    TextView errorDisplay;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentLoginBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        login = binding.buttonLogin;
        register = binding.buttonNewUser;
        usernameBox = binding.userName;
        passwordBox = binding.userPassword;
        errorDisplay = binding.errorMessage;


        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String currUsername = usernameBox.getText().toString();
                String currPassword = passwordBox.getText().toString();
                boolean success = User.register(getContext(), currUsername, currPassword);
                if (success) errorDisplay.setText(R.string.loginSuccess);
                else errorDisplay.setText(R.string.loginFailure);
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String currUsername = usernameBox.getText().toString();
                String currPassword = passwordBox.getText().toString();


                //Toast.makeText(getContext(), currUsername + " | " + currPassword, Toast.LENGTH_LONG).show();

                boolean success = User.login(getContext(), currUsername, currPassword);
                if (success) {
                    errorDisplay.setText(R.string.loginOneMoment);
                    //MainActivity.navController.navigate(R.navigation.mobile_navigation.md_theme_error);
                }

                else errorDisplay.setText(R.string.loginUsernamePassNotFound);

            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}