package com.notesapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotActivity extends AppCompatActivity {

    private EditText mforgotPassword;
    private Button mpasswordrecoverybutton;
    private TextView mgobacktologin;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot);
        getSupportActionBar().hide();
        initViews();
    }

    private void initViews() {
        mforgotPassword=findViewById(R.id.forgotpassword);
        mpasswordrecoverybutton=findViewById(R.id.passwordrecoverbutton);
        mgobacktologin=findViewById(R.id.gobacktologin);

        firebaseAuth= FirebaseAuth.getInstance();

        mgobacktologin.setOnClickListener(view -> {
            onBackPressed();
        });

        mpasswordrecoverybutton.setOnClickListener(view -> {
            String mail=mforgotPassword.getText().toString().trim();
            if(mail.isEmpty()){
                Toast.makeText(getApplicationContext(),"Enter your mail first", Toast.LENGTH_SHORT).show();
            }
            else{

                firebaseAuth.sendPasswordResetEmail(mail).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){
                            Toast.makeText(getApplicationContext(),"Mail Sent,You recover your password using mail",Toast.LENGTH_SHORT).show();
                            finish();
                            startActivity(new Intent(ForgotActivity.this, LoginRegisterActivity.class));

                        }else{
                            onBackPressed();
                            Toast.makeText(getApplicationContext(),"Email is wrong or Account doesn't Exist",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}