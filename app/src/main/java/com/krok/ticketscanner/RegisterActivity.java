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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.krok.json.UserJson;

import org.springframework.http.HttpAuthentication;
import org.springframework.http.HttpBasicAuthentication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Base64;


import static android.text.TextUtils.isEmpty;

public class RegisterActivity extends AppCompatActivity {

    private UserRegisterTask mAuthTask = null;
    private Context context = this;
    private EditText name;
    private EditText surname;
    private EditText login;
    private EditText email;
    private EditText password;
    private EditText password2;
    private View mProgressView;
    private int deviceId;

    private static Logger logger = Logger.getLogger(
            Thread.currentThread().getStackTrace()[0].getClassName());


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefRegister;
        setContentView(R.layout.activity_register);
        name = findViewById(R.id.et_name_register);
        surname = findViewById(R.id.et_surname_register);
        login = findViewById(R.id.et_login_register);
        email = findViewById(R.id.et_email_register);
        password = findViewById(R.id.et_password_register);
        password2 = findViewById(R.id.et_repeat_password);
        mProgressView = findViewById(R.id.login_progress_reg);
        prefRegister = context.getSharedPreferences(ConstantsHolder.SHARED_PREF_KEY, Context.MODE_PRIVATE);
        deviceId = prefRegister.getInt(ConstantsHolder.DEVICE_ID, 0);


        //wyswietlenie tylko raz mozliwosci logiwania
        if (prefRegister.getBoolean(getString(R.string.is_after_register), false)) {
            Intent intent = new Intent(this, HeadquartersActivity.class);
            startActivity(intent);
            finish();
        }

        Button mSignOnButton = findViewById(R.id.bt_sign_on);
        mSignOnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryToRegister();
            }
        });
    }

    private void tryToRegister() {
        if (mAuthTask != null) {
            return;
        }

        String loginSpaceless = login.getText().toString().replaceAll(" ", "");
        Boolean is_valid = true;
        name.setError(null);
        surname.setError(null);
        login.setError(null);
        email.setError(null);
        password.setError(null);
        password2.setError(null);
        View focusView = null;

        String mName = name.getText().toString();
        String mSurname = surname.getText().toString();
        String mLogin = login.getText().toString();
        String mEmail = email.getText().toString();
        String mPassword = password.getText().toString();
        String mPassword2 = password2.getText().toString();

        //Fields validation
        if (isEmpty(mName)) {
            name.setError(getString(R.string.error_field_required));
            focusView = name;
            is_valid = false;
        }
        if (isEmpty(mSurname)) {
            surname.setError(getString(R.string.error_field_required));
            focusView = surname;
            is_valid = false;
        }
        if (isEmpty(mLogin)) {
            login.setError(getString(R.string.error_field_required));
            focusView = login;
            is_valid = false;
        } else if (!(mLogin.equals(loginSpaceless))) {
            login.setError(getString(R.string.error_login_cannot_include_space));
            focusView = login;
            is_valid = false;
        }
        if (isEmpty(mEmail)) {
            email.setError(getString(R.string.error_field_required));
            is_valid = false;
            focusView = email;
            //TODO unncoment it
//        } else if (!Patterns.EMAIL_ADDRESS.matcher(mEmail).matches()) {
//            email.setError(getString(R.string.error_invalid_email));
//            focusView = email;
//            is_valid = false;
        }
        if (isEmpty(mPassword)) {
            password.setError(getString(R.string.error_field_required));
            focusView = password;
            is_valid = false;
        }
        if (isEmpty(mPassword2)) {
            password2.setError(getString(R.string.error_field_required));
            focusView = password2;
            is_valid = false;
        } else if (!(mPassword2.equals(mPassword))) {
            password2.setError(getString(R.string.error_passwords_not_equals));
            focusView = password2;
            is_valid = false;
        }

        if (is_valid) {

            UserJson userJson = new UserJson();
            userJson.setLogin(mLogin);
            userJson.setPassword(BCrypt.hashpw(mPassword, ConstantsHolder.PASWD_SALT));
            userJson.setName(mName);
            userJson.setSurname(mSurname);
            userJson.setEmail(mEmail);
            userJson.setDeviceId(deviceId);

            mAuthTask = new UserRegisterTask(userJson);
            mAuthTask.execute();

        } else {
            focusView.requestFocus();
        }
    }

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

    public class UserRegisterTask extends AsyncTask<String, String, ResponseEntity<UserJson>> {

        private final UserJson mUserJson;

        UserRegisterTask(UserJson userJson) {
            mUserJson = userJson;
        }

        @Override
        protected ResponseEntity<UserJson> doInBackground(String... strings) {
            String url = ConstantsHolder.IP_ADDRESS + ConstantsHolder.URL_REGISTER;
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<UserJson> entity = new HttpEntity<>(mUserJson, headers);

            return restTemplate.exchange(url, HttpMethod.POST, entity, UserJson.class);
        }

        @Override
        protected void onPostExecute(final ResponseEntity<UserJson> result) {
            mAuthTask = null;
            showProgress(false);

            if (result.getStatusCode().equals(HttpStatus.OK)) {


                SecurityLogin securityLogin = new SecurityLogin(mUserJson.getLogin());
                securityLogin.execute();

                //zapisanie obiektu uzytkownika w SharedPreferences
                SharedPreferences preferences;
                SharedPreferences.Editor editor;
                preferences = context.getSharedPreferences(ConstantsHolder.SHARED_PREF_KEY,
                        Context.MODE_PRIVATE);
                editor = preferences.edit();
                editor.putString(ConstantsHolder.USER_LOGIN, result.getBody().getLogin());
                editor.putInt(ConstantsHolder.USER_ID, result.getBody().getId());
                editor.apply();

                SharedPreferences.Editor edRegister = preferences.edit();
                edRegister.putBoolean(getString(R.string.is_after_register), true);
                edRegister.putBoolean(getString(R.string.is_after_login), true);
                edRegister.apply();

                context = getApplicationContext();
                Intent intent = new Intent(context, HeadquartersActivity.class);
                startActivity(intent);
                Toast.makeText(context, ConstantsHolder.REGISTER_COMPLETED, Toast.LENGTH_SHORT).show();

                //finish nie daje mozliwosci powrotu do tego wydoku z nastepnego
                finish();
            } else if (result.getStatusCode().equals(HttpStatus.IM_USED)) {
                login.setError(getString(R.string.error_login_used));
                login.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }


    public class SecurityLogin extends AsyncTask<String, String, ResponseEntity> {

        private String mLogin, mPassword;

        SecurityLogin(String login) {
            mLogin = login;
            mPassword = password.getText().toString();
        }

        @Override
        protected ResponseEntity doInBackground(String... strings) {
            logger.info("Rozpoczynam wysylanie secure loginu: L " + mLogin + " P: " + mPassword);
            String url = ConstantsHolder.IP_ADDRESS + ConstantsHolder.URL_SECURITY_LOGIN;


            MultiValueMap<String, String> loginAndPassword = new LinkedMultiValueMap<>();
            loginAndPassword.add("username", mLogin);
            loginAndPassword.add("password", password.getText().toString());


//            HttpAuthentication authHeader = new HttpBasicAuthentication(mLogin, mPassword);
//            HttpHeaders requestHeaders = new HttpHeaders();
//            requestHeaders.setAuthorization(authHeader);
//            HttpEntity<?> requestEntity = new HttpEntity<Object>(requestHeaders);
//
//            RestTemplate restTemplate = new RestTemplate();
//
//            restTemplate.getMessageConverters().add(new StringHttpMessageConverter());

//            HttpAuthentication authHeader = new HttpBasicAuthentication(mLogin, mPassword);
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.add("Content-Type", "application/x-www-form-urlencoded");
//            requestHeaders.setAuthorization(authHeader);
            requestHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            HttpEntity entity = new HttpEntity(loginAndPassword, requestHeaders);

            return restTemplate.exchange(url, HttpMethod.POST, entity, UserJson.class);
        }

//
//        @Override
//        protected void onPostExecute(final ResponseEntity result) {
//            if (result.getStatusCode().equals(HttpStatus.OK)) {
//                Toast.makeText(context, "No to mozna strzelac", Toast.LENGTH_LONG).show();
//            } else {
//                Toast.makeText(context, "DUPSKO do 2", Toast.LENGTH_LONG).show();
//            }
//        }

    }

}
