package com.krok.ticketscanner;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.krok.json.UserJson;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

import static android.text.TextUtils.isEmpty;

public class LoginActivity extends AppCompatActivity {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;
    private Context context = this;
    SharedPreferences pref;

    // UI references.
    private EditText mPasswordView;
    private View mProgressView;
    private EditText mLoginFormView;
    public static LoginActivity instance = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        pref = getSharedPreferences(ConstantsHolder.SHARED_PREF_KEY, Context.MODE_PRIVATE);

        //wyswietlenie tylko raz mozliwosci logiwania
        if (pref.getBoolean(getString(R.string.is_after_login), false)) {
            Intent intent = new Intent(this, HeadquartersActivity.class);
            startActivity(intent);
            finish();
        }
        instance = this;
        mPasswordView = findViewById(R.id.etPasswordLog);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
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
            }
        });

        Button mSignOnButton = findViewById(R.id.sign_on_button);
        mSignOnButton.setOnClickListener(new OnClickListener() {
            //TODO change activity to register view
            @Override
            public void onClick(View view) {
                context = getApplicationContext();
                Intent intent = new Intent(context, RegisterActivity.class);
                startActivity(intent);
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

        if (isValid) {
            showProgress(true);

            UserJson userJson = new UserJson();
            userJson.setLogin(login);
            userJson.setPassword(BCrypt.hashpw(password, ConstantsHolder.PASWD_SALT));

            mAuthTask = new UserLoginTask(userJson);
            mAuthTask.execute();

        } else {
            focusView.requestFocus();
        }
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
    public class UserLoginTask extends AsyncTask<String, String, ResponseEntity<UserJson>> {

        private final UserJson mUserJson;

        UserLoginTask(UserJson userJson) {
            mUserJson = userJson;
        }

        @Override
        protected ResponseEntity<UserJson> doInBackground(String... strings) {
            String url = ConstantsHolder.IP_ADDRESS + ConstantsHolder.URL_LOGIN;
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<UserJson> entity = new HttpEntity<>(mUserJson, headers);

            ResponseEntity<UserJson> result = restTemplate.exchange(url, HttpMethod.POST, entity, UserJson.class);

            System.out.println(result);
            System.out.println("Status************: " + result.getStatusCode());
            result.getStatusCode();
            return result;
        }

        @Override
        protected void onPostExecute(final ResponseEntity<UserJson> result) {
            mAuthTask = null;
            showProgress(false);

            if (result.getStatusCode().equals(HttpStatus.OK)) {
                System.out.println(result.getStatusCode() + " " + result.getStatusCode().getReasonPhrase());
                context = getApplicationContext();
                Intent intent = new Intent(context, HeadquartersActivity.class);
                startActivity(intent);
            } else if (result.getStatusCode().equals(HttpStatus.NO_CONTENT)) {
                System.out.println(result.getStatusCode() + " " + result.getStatusCode().getReasonPhrase());
                mLoginFormView.setError(getString(R.string.error_login_not_found));
                mLoginFormView.requestFocus();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                System.out.println(result.getStatusCode() + " " + result.getStatusCode().getReasonPhrase());
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

