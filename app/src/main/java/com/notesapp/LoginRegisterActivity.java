package com.notesapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.notesapp.utils.NetworkUtils;
import com.notesapp.utils.PDialog;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class LoginRegisterActivity extends AppCompatActivity {
    private static final String TAG = "LoginRegisterActivity";

    private TextInputLayout fieldEmail;
    private TextInputLayout fieldPassword;
    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);
        firebaseAuth= FirebaseAuth.getInstance();

        getSupportActionBar().hide();
        initViews();

        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            startActivity( new Intent(this, HomeActivity.class));
            this.finish();
        }
    }

    private void initViews() {
        fieldEmail = findViewById(R.id.fieldEmail);
        fieldPassword = findViewById(R.id.fieldPassword);

        findViewById(R.id.login).setOnClickListener(v -> {
            if (Objects.requireNonNull(fieldEmail.getEditText()).getText().toString().isEmpty() ||
                    Objects.requireNonNull(fieldPassword.getEditText()).getText().toString().isEmpty()) {
                Toast.makeText(getApplicationContext(), "Fields Cannot be empty!", Toast.LENGTH_SHORT).show();
            } else {
                if (NetworkUtils.isNetworkAvailable(getApplicationContext())) {
                    String sEmail = fieldEmail.getEditText().getText().toString();
                    String sFieldPassword = fieldPassword.getEditText().getText().toString();
                    if (sEmail.isEmpty() || sFieldPassword.isEmpty()) {
                        Toast.makeText(getApplicationContext(), "All Fields are required", Toast.LENGTH_SHORT).show();
                    } else if (sFieldPassword.length() < 7) {
                        Toast.makeText(getApplicationContext(), "Password Should Greater than 7 Digits", Toast.LENGTH_SHORT).show();
                    } else {
                        PDialog.method(LoginRegisterActivity.this,"Sign in with Email & Password!");
                        PDialog.show();
                        firebaseAuth.signInWithEmailAndPassword(sEmail,sFieldPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if(task.isSuccessful()){
                                    checkmailverification();
                                    PDialog.dismiss();
                                } else {
                                    PDialog.dismiss();
                                    Toast.makeText(getApplicationContext(),"Account Doesn't Exist",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "Internet is not available!\nOffline Mode", Toast.LENGTH_SHORT).show();
                }
            }

        });
        findViewById(R.id.register).setOnClickListener(view -> {
            if (NetworkUtils.isNetworkAvailable(getApplicationContext())) {
                String sEmail = fieldEmail.getEditText().getText().toString();
                String sFieldPassword = fieldPassword.getEditText().getText().toString();
                if (sEmail.isEmpty() || sFieldPassword.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "All Fields are required", Toast.LENGTH_SHORT).show();
                } else if (sFieldPassword.length() < 7) {
                    Toast.makeText(getApplicationContext(), "Password Should Greater than 7 Digits", Toast.LENGTH_SHORT).show();
                } else {
                    PDialog.method(LoginRegisterActivity.this,"Creating New User!");
                    PDialog.show();
                    firebaseAuth.createUserWithEmailAndPassword(sEmail, sFieldPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "Registration Successful", Toast.LENGTH_SHORT).show();
                                sendEmailVerification();
                            } else {
                                Toast.makeText(getApplicationContext(), "Failed to Register", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                }

            } else {
                Toast.makeText(getApplicationContext(), "Internet is not available!\nOffline Mode", Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.forgotPassword).setOnClickListener(view -> {
            startActivity(new Intent(LoginRegisterActivity.this,ForgotActivity.class));
        });
    }

    private void checkmailverification() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser.isEmailVerified()){
            PDialog.dismiss();
            Toast.makeText(getApplicationContext(),"Logged In",Toast.LENGTH_SHORT).show();
            finish();
            startActivity(new Intent(LoginRegisterActivity.this,HomeActivity.class));
        } else {
            PDialog.dismiss();
            Toast.makeText(getApplicationContext(),"Verify your mail first",Toast.LENGTH_SHORT).show();
            firebaseAuth.signOut();
        }
    }

    private void sendEmailVerification() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser!=null){
            firebaseUser.sendEmailVerification().addOnCompleteListener(task -> {
                Toast.makeText(getApplicationContext(),"Verification Email is sent,Verify and Log In Again",Toast.LENGTH_SHORT).show();
                firebaseAuth.signOut();
                PDialog.dismiss();
                /*finish();
                startActivity(new Intent(LoginRegisterActivity.this, HomeActivity.class));*/
            });
        }
        else{
            PDialog.dismiss();
            Toast.makeText(getApplicationContext(),"Failed To Send Verification Email",Toast.LENGTH_SHORT).show();
        }
    }
}