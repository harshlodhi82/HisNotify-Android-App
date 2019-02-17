package com.android.h4r5.hisnotify;

import android.content.Intent;
import android.os.Handler;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AdditionalUserInfo;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Map;

public class LogInActivity extends AppCompatActivity {



    private FirebaseAuth mAuth;
    FirebaseUser currentUser ;
    GoogleSignInOptions gso;
    GoogleSignInClient mGoogleSignInClient;
    int SPLASH_TIME_OUT = 2000;
    private static final int RC_SIGN_IN = 234;
    CheckBox checkBox;


    @Override
    protected void onStart() {
        super.onStart();

        currentUser = mAuth.getCurrentUser();


        updateUI(currentUser);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();




        updateUI(currentUser);
        if(currentUser==null)
        {
            setContentView(R.layout.activity_log_in);
            gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.client_Id))
                    .requestEmail()
                    .build();

            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

            checkBox = findViewById(R.id.checkBox);
            //checkBox.isChecked();

            LinearLayout lyBtn = findViewById(R.id.SignIn);
            lyBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(checkBox.isChecked())
                    {
                        Log.e("This", "sign in btn clicked");
                        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                        startActivityForResult(signInIntent, RC_SIGN_IN);
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),"check 1st",Toast.LENGTH_SHORT).show();
                    }

                }
            });

//            Button signInBtn = findViewById(R.id.SignIn);
//            signInBtn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//
//                }
//            });
        }



    }

    private void updateUI(FirebaseUser firebaseUser)
    {
        if(firebaseUser!=null)
        {
            setContentView(R.layout.splash_screen);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent sIntent = new Intent(LogInActivity.this, MainActivity.class);
                    sIntent.putExtra("UserInfo","OLD");
                    String userID = mAuth.getUid();
                    sIntent.putExtra("UserID",userID);
                    sIntent.putExtra("loginTime","later");
                    startActivity(sIntent);
                    finish();
                }
            },SPLASH_TIME_OUT);

        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Log.e("This", "u r at on Activity result");
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase

                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);

            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.e("This is login Act", "Google sign in failed", e);
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("This is ID ", "firebaseAuthWithGoogle:" + acct.getId());

        final AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //task.getResult().getAdditionalUserInfo().isNewUser();
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            Log.d("This", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Intent mIntent = new Intent(LogInActivity.this,MainActivity.class);
                            if(task.getResult().getAdditionalUserInfo().isNewUser())
                            {
                                mIntent.putExtra("UserInfo","NEW");
                            }
                            else if(!task.getResult().getAdditionalUserInfo().isNewUser())
                            {
                                mIntent.putExtra("UserInfo","OLD");
                            }
                            String userID = mAuth.getUid();
                            mIntent.putExtra("UserID",userID);
                            mIntent.putExtra("loginTime","just");
                            startActivity(mIntent);
                        } else {
                            Log.w("This", "signInWithCredential:failure", task.getException());
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }



}
