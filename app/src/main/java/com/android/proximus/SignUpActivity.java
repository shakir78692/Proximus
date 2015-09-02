package com.android.proximus;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class SignUpActivity extends AppCompatActivity {

    protected EditText mUsername;
    protected EditText mPassword;
    protected EditText mEmail;
    protected Button mCancel;
    protected Button mSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);


        mUsername = (EditText) findViewById(R.id.usernameEditTextSignup);
        mPassword = (EditText) findViewById(R.id.passwordEditTextSignup);
        mEmail = (EditText) findViewById(R.id.emailEditTextSignup);
        mSignUp = (Button) findViewById(R.id.signupButton);
        mCancel = (Button) findViewById(R.id.cancelButton);

        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });

        mSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = mUsername.getText().toString();
                String password = mPassword.getText().toString();
                String email = mEmail.getText().toString();

                username.trim();
                password.trim();
                email.trim();

                if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                    builder.setTitle(R.string.signup_error_title)
                            .setMessage(R.string.signup_error_message)
                            .setPositiveButton(android.R.string.ok, null);

                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {

                    ParseUser user = new ParseUser();
                    user.setUsername(username);
                    user.setPassword(password);
                    user.setEmail(email);
                    user.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {

                            if (e == null) {
                                Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                                // Sign up didn't succeed. Look at the ParseException
                                // to figure out what went wrong
                                AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                                builder.setTitle(R.string.signup_error_title)
                                        .setMessage(e.getMessage())
                                        .setPositiveButton(android.R.string.ok, null);

                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                        }
                    });
                }
            }
        });
    }
}
