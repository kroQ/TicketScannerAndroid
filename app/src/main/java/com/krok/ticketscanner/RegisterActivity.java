package com.krok.ticketscanner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import static android.text.TextUtils.isEmpty;

public class RegisterActivity extends Activity {


    private Context context = this;
    private EditText name;
    private EditText surname;
    private EditText login;
    private EditText email;
    private EditText password;
    private EditText password2;
    private Boolean isLoginInUse;

    SharedPreferences prefRegister;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        name = (EditText) findViewById(R.id.et_name_register);
        surname = (EditText) findViewById(R.id.et_surname_register);
        login = (EditText) findViewById(R.id.et_login_register);
        email = (EditText) findViewById(R.id.et_email_register);
        password = (EditText) findViewById(R.id.et_password_register);
        password2 = (EditText) findViewById(R.id.et_repeat_password);
        prefRegister = getSharedPreferences(ConstantsHolder.SHARED_PREF_KEY, Context.MODE_PRIVATE);

        //wyswietlenie tylko raz mozliwosci logiwania
        if (prefRegister.getBoolean("activity_register", false)) {
            Intent intent = new Intent(this, HeadquartersActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private JSONObject addJSON() {
        JSONObject jo = new JSONObject();
        HashPassword hp = new HashPassword();
        try {
            jo.put("name", name.getText().toString().trim());
            jo.put("surname", surname.getText().toString().trim());
            jo.put("email", email.getText().toString().trim());
            jo.put("login", login.getText().toString().trim());
            jo.put("password", hp.hash(password.getText().toString()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jo;
    }

    public void onClickBtZarejestrujKonto(View view) {
        String loginSpaceless = login.getText().toString().replaceAll(" ", "");
        Boolean is_valid = true;
        name.setError(null);
        surname.setError(null);
        login.setError(null);
        email.setError(null);
        password.setError(null);
        password2.setError(null);
        isLoginInUse = true;

        //walidacja
        if (isEmpty(name.getText().toString())) {
            name.setError("Podaj imię");
            is_valid = false;
        }
        if (isEmpty(surname.getText().toString())) {
            surname.setError("Podaj nazwisko");
            is_valid = false;
        }
        if (isEmpty(login.getText().toString())) {
            login.setError("Podaj unikalny login");
            is_valid = false;
        } else if (!(login.getText().toString().equals(loginSpaceless))) {
            login.setError("Login nie może zawierać spacji");
            is_valid = false;
        }
        if (isEmpty(email.getText().toString())) {
            email.setError("Podaj adres e-mail");
            is_valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()) {
            email.setError("Podaj poprawny e-mail");
            is_valid = false;
        }
        if (isEmpty(password.getText().toString())) {
            password.setError("Podaj hasło");
            is_valid = false;
        }
        if (isEmpty(password2.getText().toString())) {
            password2.setError("Podaj ponownie hasło");
            is_valid = false;
        } else if (!(password2.getText().toString().equals(password.getText().toString()))) {
            password2.setError("Hasła nie są identyczne");
            is_valid = false;
        }

        if (is_valid) {
//            PostMethod pm = new PostMethod(this);
//            pm.execute(addJSON().toString(), ConstantsHolder.IP_ADDRESS + "/user");
//            JSONObject jo = null;
//
//            try {
//                if (!pm.get().trim().equals(ConstantsHolder.LOGIN_IN_USE)) {
//                    isLoginInUse = false;
//                } else {
//                    isLoginInUse = true;
//                    is_valid = false;
//                    login.setError("Login zajety");
//                }
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            } catch (ExecutionException e) {
//                e.printStackTrace();
//            }
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
        }
    }


}
