package com.krok.ticketscanner;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.krok.json.UserJson;

import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutionException;

import static android.text.TextUtils.isEmpty;

public class LoginActivity extends AppCompatActivity {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;
    private Context context = this;
    // UI references.
    private EditText mPasswordView;
    private View mProgressView;
    private EditText mLoginFormView;
    public static LoginActivity instance = null;
    int i = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        instance = this;
        mPasswordView = findViewById(R.id.etPasswordLog);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
//                    attemptLogin();
                    tryToLogin();
                    return true;
                }
                return false;
            }
        });

        Button mSignInButton = findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                tryToLogin();
//                attemptLogin();
            }
        });

        Button mSignOnButton = findViewById(R.id.sign_on_button);
        mSignOnButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                i++;
                final boolean show;
                if ( show = (i%2 ==0)) {
                    int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

//                    mLoginFormView.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
//                    mLoginFormView.animate().setDuration(shortAnimTime).alpha(
//                            show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
//                        @Override
//                        public void onAnimationEnd(Animator animation) {
//                            mLoginFormView.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
//                        }
//                    });

                    mProgressView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
                    mProgressView.animate().setDuration(shortAnimTime).alpha(
                            show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mProgressView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
                        }
                    });
                } else {
                    // The ViewPropertyAnimator APIs are not available, so simply show
                    // and hide the relevant UI components.
                    mProgressView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
                    //mLoginFormView.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
                }
            }
        });
        mLoginFormView = findViewById(R.id.login);
        mProgressView = findViewById(R.id.login_progress);
    }

    private void tryToLogin() {
        if (mAuthTask != null) {
            return;
        }

        mLoginFormView.setError(null);
        mPasswordView.setError(null);

        String password = mPasswordView.getText().toString();
        String login = mLoginFormView.getText().toString();
        Boolean isValid = true;
        View focusView = null;

        if (isEmpty(login)) {
            mLoginFormView.setError(getString(R.string.error_field_required));
            focusView = mLoginFormView;
            isValid = false;
        }

        if (isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            isValid = false;
        }

        UserJson userJson = new UserJson();
        userJson.setLogin(login);
        userJson.setPassword(password);

        if (isValid) {
            showProgress(true);

            // The connection URL
            String url = ConstantsHolder.IP_ADDRESS + ConstantsHolder.URL_LOGIN;

            // Create a new RestTemplate instance
            RestTemplate restTemplate = new RestTemplate();

            // Add the String message converter
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            restTemplate.getMessageConverters().add(new StringHttpMessageConverter());

            mAuthTask = new UserLoginTask(login, password);
            mAuthTask.execute();

            UserJson response = null;
            try {
                response = mAuthTask.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            if (response != null) {
                System.out.println("\n\n\n\n\n======== LECIMY ==========" + response.getId());
            }
        }
        else{
            focusView.requestFocus();
        }
    }

//    private void attemptLogin() {
//        boolean cancel = false;
//        View focusView = null;
//
//        // Check for a valid email address.
//        if (TextUtils.isEmpty(email)) {
//            mEmailView.setError(getString(R.string.error_field_required));
//            focusView = mEmailView;
//            cancel = true;
//        } else if (!isEmailValid(email)) {
//            mEmailView.setError(getString(R.string.error_invalid_email));
//            focusView = mEmailView;
//            cancel = true;
//        }
//
//        if (cancel) {
//            // There was an error; don't attempt login and focus the first
//            // form field with an error.
//            focusView.requestFocus();
//        } else {
//            // Show a progress spinner, and kick off a background task to
//            // perform the user login attempt.
//            showProgress(true);
//            mAuthTask = new UserLoginTask(email, password);
//            mAuthTask.execute((Void) null);
//        }
//    }
    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mProgressView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
                }
            });
        }


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<String, String, UserJson> {

        private final String mLogin;
        private final String mPassword;

        UserLoginTask(String login, String password) {
            mLogin = login;
            mPassword = password;
        }

        @Override
        protected UserJson doInBackground(String... strings) {
            UserJson userJson = new UserJson();
            userJson.setLogin(mLogin);
            userJson.setPassword(mPassword);
            String url = ConstantsHolder.IP_ADDRESS + ConstantsHolder.URL_LOGIN;
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            restTemplate.getMessageConverters().add(new StringHttpMessageConverter());

            try {
                UserJson userReturned = restTemplate.postForObject(url, userJson, UserJson.class);
                System.out.println("\n\n\n\n\n======== LECIMY doInBackground ==========");
                return userReturned;
            } catch (RuntimeException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(final UserJson success) {
            mAuthTask = null;
            showProgress(false);

            if (success != null && success.getId() > 0) {
                context = getApplicationContext();
                Intent intent = new Intent(context, HeadquartersActivity.class);
                startActivity(intent);
            } else if (success != null ){
                //finish();
                mLoginFormView.setError(getString(R.string.error_incorrect_login));

            }else{
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    @Override
    public void finish() {
        super.finish();
        instance = null;
    }
}

