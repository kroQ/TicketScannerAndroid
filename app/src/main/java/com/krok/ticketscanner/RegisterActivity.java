package com.krok.ticketscanner;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

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

public class RegisterActivity extends Activity {

    private UserRegisterTask mAuthTask = null;
    private Context context = this;
    private EditText name;
    private EditText surname;
    private EditText login;
    private EditText email;
    private EditText password;
    private EditText password2;
    private Boolean isLoginInUse;
    private View mProgressView;

    SharedPreferences prefRegister;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        name = findViewById(R.id.et_name_register);
        surname = findViewById(R.id.et_surname_register);
        login = findViewById(R.id.et_login_register);
        email = findViewById(R.id.et_email_register);
        password = findViewById(R.id.et_password_register);
        password2 = findViewById(R.id.et_repeat_password);
        mProgressView = findViewById(R.id.login_progress_reg);
        prefRegister = getSharedPreferences(ConstantsHolder.SHARED_PREF_KEY, Context.MODE_PRIVATE);

        //wyswietlenie tylko raz mozliwosci logiwania
        if (prefRegister.getBoolean("activity_register", false)) {
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
        isLoginInUse = true;
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

            mAuthTask = new UserRegisterTask(userJson);
            mAuthTask.execute();

//
//            if (!isLoginInUse) {
//                try {
//                    jo = new JSONObject(pm.get());
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                } catch (ExecutionException e) {
//                    e.printStackTrace();
//                }
//
//                //zapisanie obiektu uzytkownika w SharedPreferences
//                SharedPreferences settingsJson;
//                SharedPreferences.Editor editor;
//                settingsJson = context.getSharedPreferences(ConstantsHolder.SHARED_PREF_KEY,
//                        Context.MODE_PRIVATE);
//                editor = settingsJson.edit();
//                editor.putString(ConstantsHolder.USER_IN_JSON, jo.toString());
//                editor.putInt(ConstantsHolder.USER_ID, jo.optInt("scn_id"));
//                editor.apply();
//
//                SharedPreferences.Editor edRegister = prefRegister.edit();
//                edRegister.putBoolean("activity_register", true);
//                edRegister.putBoolean("activity_main", true);
//                edRegister.apply();
//
//                Intent intent = new Intent(context, LoginActivity.class);
//                startActivity(intent);
//                Toast.makeText(context, ConstantsHolder.REGISTER_COMPLETED, Toast.LENGTH_SHORT).show();
//                //finish nie daje mozliwosci powrotu do tego wydoku z nastepnego
//                finish();
//            }
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

            ResponseEntity<UserJson> result = restTemplate.exchange(url, HttpMethod.POST, entity, UserJson.class);
            return result;
        }

        @Override
        protected void onPostExecute(final ResponseEntity<UserJson> success) {
            mAuthTask = null;
            showProgress(false);

            if (success.getStatusCode().equals(HttpStatus.OK)) {
                System.out.println(success.getStatusCode() + " " + success.getStatusCode().getReasonPhrase());
                context = getApplicationContext();
                Intent intent = new Intent(context, HeadquartersActivity.class);
                startActivity(intent);
            } else if (success.getStatusCode().equals(HttpStatus.IM_USED)) {
                System.out.println(success.getStatusCode() + " " + success.getStatusCode().getReasonPhrase());
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

}
