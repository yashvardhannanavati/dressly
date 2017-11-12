package com.agrawroh.ninja.dressly.login;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.agrawroh.ninja.dressly.R;
import com.agrawroh.ninja.dressly.base.BaseActivity;
import com.agrawroh.ninja.dressly.model.User;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.TwitterAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Rohit Agrawal
 * @version v1.0
 */
public class LoginActivity extends BaseActivity implements
        GoogleApiClient.OnConnectionFailedListener {
    private ProgressDialog progressDialog;

    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 9001;
    private static final int REQUEST_SIGNUP = 0;

    @BindView(R.id.input_email)
    protected EditText emailText;

    @BindView(R.id.input_password)
    protected EditText passwordText;

    @BindView(R.id.btn_login)
    protected Button loginButton;

    @BindView(R.id.link_signup)
    protected TextView signupLink;

    /* Google Login */
    private static GoogleApiClient mGoogleApiClient;

    /* Facebook Client */
    private CallbackManager mCallbackManager;

    /* Twitter Client */
    private TwitterAuthClient mLoginButton;

    /* FireBase Authentication */
    private static FirebaseAuth mAuth;

    @Override
    protected String getTAG() {
        return TAG;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    protected void initResAndListener() {
        super.initResAndListener();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Service Error!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        performLogin();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Initialize Facebook */
        FacebookSdk.sdkInitialize(getApplicationContext());

        /* Initialize Twitter */
        TwitterAuthConfig authConfig = new TwitterAuthConfig(
                getString(R.string.twitter_c_key),
                getString(R.string.twitter_c_secret));
        TwitterConfig twitterConfig = new TwitterConfig.Builder(this)
                .twitterAuthConfig(authConfig)
                .build();
        Twitter.initialize(twitterConfig);

        /* Set Content View */
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        /* Initialize Progress Dialog */
        progressDialog = new
                ProgressDialog(LoginActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        SpannableString spannableMessage = new SpannableString("Authenticating...");
        spannableMessage.setSpan(new RelativeSizeSpan(1.4f), 0, spannableMessage.length(), 0);
        progressDialog.setMessage(spannableMessage);

        /* FireBase Auth */
        mAuth = FirebaseAuth.getInstance();

        /* Facebook Login */
        ImageView facebookButton = findViewById(R.id.icon_fb);
        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                authenticateUsingFacebook(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                onLoginFailed();
            }

            @Override
            public void onError(FacebookException error) {
                onLoginFailed();
            }
        });
        facebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                facebookLogin();
            }
        });

        /* Twitter Login */
        ImageView twitterButton = findViewById(R.id.icon_tw);
        mLoginButton = new TwitterAuthClient();
        twitterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                twitterLogin();
            }
        });

        /* Google Login */
        ImageView googleButton = findViewById(R.id.icon_gp);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build())
                .build();
        googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleLogin();
            }
        });

        /* Email Login */
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                normalLogin();
            }
        });

        /* Sign Up Link */
        signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
                performLogin();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /* Facebook Auth */
        if (mCallbackManager.onActivityResult(requestCode, resultCode, data)) {
            return;
        }

        /* Twitter Auth */
        mLoginButton.onActivityResult(requestCode, resultCode, data);

        /* Google Auth */
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                authenticateUsingGoogle(result.getSignInAccount());
            } else {
                onLoginFailed();
            }
        }
    }

    /**
     * Perform Login
     */
    private void performLogin() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (null != currentUser) {
            onLoginSuccess(currentUser.getProviderData().get(1).getProviderId(), currentUser.getEmail());
        }
    }

    /**
     * Show Progress
     */
    private void showProgress() {
        progressDialog.show();
    }

    /**
     * Hide Progress
     */
    private void hideProgress() {
        progressDialog.dismiss();
    }

    /**
     * Authenticate With Facebook
     *
     * @param token
     */
    private void authenticateUsingFacebook(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            createUser(mAuth.getCurrentUser().getEmail(), mAuth.getCurrentUser().getDisplayName());
                            onLoginSuccess(currentUser.getProviderData().get(1).getProviderId(), currentUser.getEmail());
                        } else {
                            Toast.makeText(getBaseContext(), "Account Already Exists!", Toast.LENGTH_LONG).show();
                            onLoginFailed();
                        }
                    }
                });
    }

    /**
     * Authenticate With Twitter
     *
     * @param session
     */
    private void authenticateUsingTwitter(TwitterSession session) {
        AuthCredential credential = TwitterAuthProvider.getCredential(
                session.getAuthToken().token,
                session.getAuthToken().secret);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            createUser(mAuth.getCurrentUser().getEmail(), mAuth.getCurrentUser().getDisplayName());
                            onLoginSuccess(currentUser.getProviderData().get(1).getProviderId(), currentUser.getEmail());
                        } else {
                            Toast.makeText(getBaseContext(), "Account Already Exists!", Toast.LENGTH_LONG).show();
                            onLoginFailed();
                        }
                    }
                });
    }

    /**
     * Authenticate With Google
     *
     * @param account
     */
    private void authenticateUsingGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            createUser(mAuth.getCurrentUser().getEmail(), mAuth.getCurrentUser().getDisplayName());
                            onLoginSuccess(currentUser.getProviderData().get(1).getProviderId(), currentUser.getEmail());
                        } else {
                            onLoginFailed();
                        }
                    }
                });
    }

    /**
     * Login Success
     */
    private void onLoginSuccess(final String provider, final String userEmail) {
        hideProgress();
        Toast.makeText(getBaseContext(), "Authentication Successful!", Toast.LENGTH_LONG).show();

        Intent resultIntent = new Intent();
        resultIntent.putExtra("Provider", provider);
        resultIntent.putExtra("UserEmail", userEmail);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    /**
     * Create User
     */
    public static void createUser(final String userEmail, final String userName) {
        FirebaseDatabase.getInstance()
                .getReference().child("users").orderByChild("id").equalTo(userEmail)
                .limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    /* Create New User */
                    FirebaseDatabase.getInstance()
                            .getReference().child("users")
                            .push()
                            .setValue(new User(userEmail, userName, null, false));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                /* Do Nothing*/
            }
        });
    }

    /**
     * Login Failure
     */
    private void onLoginFailed() {
        hideProgress();
        Toast.makeText(getBaseContext(), "Authentication Failed!", Toast.LENGTH_LONG).show();
        loginButton.setEnabled(true);
    }

    /**
     * Facebook Login
     */
    private void facebookLogin() {
        showProgress();
        Collection<String> permissions = new ArrayList<>(Arrays.asList("email", "public_profile"));
        LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, permissions);
    }

    /**
     * Facebook Logout
     */
    private static void facebookLogout() {
        LoginManager.getInstance().logOut();
    }

    /**
     * Twitter Login
     */
    private void twitterLogin() {
        showProgress();
        mLoginButton.authorize(LoginActivity.this, new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> twitterSessionResult) {
                authenticateUsingTwitter(twitterSessionResult.data);
            }

            @Override
            public void failure(TwitterException e) {
                onLoginFailed();
            }
        });
    }

    /**
     * Twitter Logout
     */
    private static void twitterLogout() {
        TwitterCore.getInstance().getSessionManager().clearActiveSession();
    }

    /**
     * Google Login
     */
    private void googleLogin() {
        showProgress();
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /**
     * Google Logout
     */
    private static void googleLogout() {
        mGoogleApiClient.connect();
        mGoogleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(@Nullable Bundle bundle) {
                if (mGoogleApiClient.isConnected()) {
                    Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            if (status.isSuccess()) {
                                Log.d(TAG, "Google API Client Connection Closed Successfully!");
                            }
                        }
                    });
                }
            }

            @Override
            public void onConnectionSuspended(int i) {
                Log.d(TAG, "Google API Client Connection Suspended!");
            }
        });
    }

    /**
     * Normal Login
     */
    private void normalLogin() {
        /* Validate */
        if (!validateForm()) {
            onLoginFailed();
            return;
        }

        /* Disable Login Button */
        loginButton.setEnabled(false);

        /* Show Progress Dialog */
        showProgress();

        /* Extract Fields */
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        /* Begin Login */
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            onLoginSuccess(currentUser.getProviderData().get(1).getProviderId(), currentUser.getEmail());
                        } else {
                            onLoginFailed();
                        }
                    }
                });
    }

    /**
     * Validate Fields
     *
     * @return isSuccess
     */
    private boolean validateForm() {
        boolean valid = true;

        /* Extract Fields */
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        /* Validate Email */
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError("Enter a valid email address");
            valid = false;
        } else {
            emailText.setError(null);
        }

        /* Validate Password */
        if (password.isEmpty() || password.length() < 6 || password.length() > 15) {
            passwordText.setError("Enter a valid password between 4 - 15 characters");
            valid = false;
        } else {
            passwordText.setError(null);
        }

        /* Set Valid Status */
        return valid;
    }

    /**
     * Sign Out
     */
    public static void signOut(final String provider) {
        /* Disconnect FireBase */
        mAuth.signOut();

        /* Logout Provider */
        switch (provider) {
            /* Logout Facebook */
            case "facebook.com": {
                facebookLogout();
                break;
            }

            /* Logout Twitter*/
            case "twitter.com": {
                twitterLogout();
                break;
            }

            /* Logout Google */
            case "google.com": {
                googleLogout();
                break;
            }
        }
    }
}
