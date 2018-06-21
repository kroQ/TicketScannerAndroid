package com.krok.ticketscanner;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.krok.json.DeviceJson;
import com.krok.json.UserJson;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import java.util.logging.Logger;

import static android.text.TextUtils.isEmpty;

public class LoginActivity extends AppCompatActivity {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private SecurityLogin mAuthTask = null;
    private DeviceSaveTask mDeviceTask = null;
    private Context context = this;
    private Long androidId;
    private int deviceId;
    SharedPreferences pref;

    private static Logger logger = Logger.getLogger(
            Thread.currentThread().getStackTrace()[0].getClassName());

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
        instance = this;
        mPasswordView = findViewById(R.id.etPasswordLog);
        mLoginFormView = findViewById(R.id.login);
        mProgressView = findViewById(R.id.login_progress);

        //wyswietlenie tylko raz mozliwosci logiwania
        if (pref.getBoolean(getString(R.string.is_after_login), false)) {
            Intent intent = new Intent(this, HeadquartersActivity.class);
            startActivity(intent);
            finish();
        }

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
            @Override
            public void onClick(View view) {
                context = getApplicationContext();
                Intent intent = new Intent(context, RegisterActivity.class);
                startActivity(intent);
            }
        });

        final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_PHONE_STATE)) {
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
            }
        }

        androidId = Long.valueOf(tm.getDeviceId());

        DeviceJson deviceJson = new DeviceJson();
        deviceJson.setAndroidId(androidId);
        deviceJson.setName(android.os.Build.MODEL);
        deviceJson.setDeviceType(Build.MANUFACTURER);

        //TODO To jakos zamienic
        if(!pref.getBoolean(getString(R.string.is_after_login), false)){
            mDeviceTask = new DeviceSaveTask(deviceJson);
            mDeviceTask.execute();
        }
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
            userJson.setDeviceId(deviceId);
//            userJson.setPassword(BCrypt.hashpw(password, ConstantsHolder.PASWD_SALT));
            userJson.setPassword(password);

            mAuthTask = new SecurityLogin(userJson.getLogin(), userJson.getPassword());
            mAuthTask.execute();
//

//            mAuthTask = new UserLoginTask(userJson);
//            mAuthTask.execute();

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
    private class UserLoginTask extends AsyncTask<String, String, ResponseEntity<UserJson>> {

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

            return result;
        }

        @Override
        protected void onPostExecute(final ResponseEntity<UserJson> result) {
            mAuthTask = null;
            showProgress(false);

            if (result.getStatusCode().equals(HttpStatus.OK)) {

                //zapisanie obiektu uzytkownika w SharedPreferences
                SharedPreferences preferences;
                SharedPreferences.Editor editor;
                preferences = context.getSharedPreferences(ConstantsHolder.SHARED_PREF_KEY,
                        Context.MODE_PRIVATE);
                editor = preferences.edit();
                editor.putString(ConstantsHolder.USER_LOGIN, result.getBody().getLogin());
                editor.putInt(ConstantsHolder.USER_ID, result.getBody().getId());
                editor.apply();

                SharedPreferences.Editor edRegister = pref.edit();
                edRegister.putBoolean(getString(R.string.is_after_register), true);
                edRegister.putBoolean(getString(R.string.is_after_login), true);
                edRegister.apply();

                context = getApplicationContext();
                Intent intent = new Intent(context, HeadquartersActivity.class);
                startActivity(intent);

            } else if (result.getStatusCode().equals(HttpStatus.NO_CONTENT)) {
                mLoginFormView.setError(getString(R.string.error_login_not_found));
                mLoginFormView.requestFocus();
            } else if (result.getStatusCode().equals(HttpStatus.IM_USED)) {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            } else {
                logger.info("SOME_ERROR " + result.getStatusCode());
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

    private class DeviceSaveTask extends AsyncTask<String, String, ResponseEntity<DeviceJson>> {

        private final DeviceJson mDeviceJson;

        DeviceSaveTask(DeviceJson deviceJson) {
            mDeviceJson = deviceJson;
        }

        @Override
        protected ResponseEntity<DeviceJson> doInBackground(String... strings) {
            String url = ConstantsHolder.IP_ADDRESS + ConstantsHolder.URL_DEVICE;
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<DeviceJson> entity = new HttpEntity<>(mDeviceJson, headers);

            ResponseEntity<DeviceJson> result = restTemplate.exchange(url, HttpMethod.POST, entity, DeviceJson.class);

            return result;
        }

        @Override
        protected void onPostExecute(final ResponseEntity<DeviceJson> result) {

            deviceId = result.getBody().getId();
            mDeviceTask = null;
            showProgress(false);

            SharedPreferences preferences;
            SharedPreferences.Editor editor;
            preferences = context.getSharedPreferences(ConstantsHolder.SHARED_PREF_KEY,
                    Context.MODE_PRIVATE);
            editor = preferences.edit();
            editor.putInt(ConstantsHolder.DEVICE_ID, result.getBody().getId());
            editor.apply();

            if (result.getStatusCode().equals(HttpStatus.OK)) {
                Toast.makeText(context, ConstantsHolder.NEW_DEVICE, Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(context, ConstantsHolder.OLD_DEVICE, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mDeviceTask = null;
            showProgress(false);
        }
    }

    @Override
    public void finish() {
        super.finish();
        instance = null;
    }

    public class SecurityLogin extends AsyncTask<String, String, ResponseEntity<UserJson>> {

        private String mLogin, mPassword;

        SecurityLogin(String login, String password) {
            mLogin = login;
            mPassword = password;
        }

        @Override
        protected ResponseEntity<UserJson> doInBackground(String... strings) {
            logger.info("Rozpoczynam wysylanie secure loginu: L " + mLogin + " P: " + mPassword);
            String url = ConstantsHolder.IP_ADDRESS + "/login";

            MultiValueMap<String, String> loginAndPassword = new LinkedMultiValueMap<>();
            loginAndPassword.add("username", mLogin);
            loginAndPassword.add("password", mPassword);

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.add("Content-Type", "application/x-www-form-urlencoded");
            requestHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            HttpEntity entity = new HttpEntity<>(loginAndPassword, requestHeaders);

            return restTemplate.exchange(url, HttpMethod.POST, entity, UserJson.class);
        }

        @Override
        protected void onPostExecute(final ResponseEntity<UserJson> result) {
            mAuthTask = null;
            showProgress(false);

            if (result.getStatusCode().equals(HttpStatus.OK)) {

                //zapisanie obiektu uzytkownika w SharedPreferences
                SharedPreferences preferences;
                SharedPreferences.Editor editor;
                preferences = context.getSharedPreferences(ConstantsHolder.SHARED_PREF_KEY,
                        Context.MODE_PRIVATE);
                editor = preferences.edit();
                editor.putString(ConstantsHolder.USER_LOGIN, result.getBody().getLogin());
                editor.putInt(ConstantsHolder.USER_ID, result.getBody().getId());
                editor.apply();

                SharedPreferences.Editor edRegister = pref.edit();
                edRegister.putBoolean(getString(R.string.is_after_register), true);
                edRegister.putBoolean(getString(R.string.is_after_login), true);
                edRegister.apply();

                context = getApplicationContext();
                Intent intent = new Intent(context, HeadquartersActivity.class);
                startActivity(intent);

            } else if (result.getStatusCode().equals(HttpStatus.NO_CONTENT)) {
                mLoginFormView.setError(getString(R.string.error_login_not_found));
                mLoginFormView.requestFocus();
            } else if (result.getStatusCode().equals(HttpStatus.IM_USED)) {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            } else {
                logger.info("SOME_ERROR " + result.getStatusCode());
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

    }


}

