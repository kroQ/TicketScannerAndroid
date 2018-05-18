package com.krok.ticketscanner;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.krok.json.EventJson;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

import static android.text.TextUtils.isEmpty;

public class HeadquartersActivity extends AppCompatActivity {

    private Button btConnect;
    private EditText etCode;
    private Context context;
    private TextView tvLoggedAs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (LoginActivity.instance != null) {
            LoginActivity.instance.finish();
        }

        setContentView(R.layout.activity_headquartes);
        context = getApplicationContext();
        btConnect = this.findViewById(R.id.bt_connect_db);
        etCode = this.findViewById(R.id.et_database_code);
        tvLoggedAs = this.findViewById(R.id.tv_logged_as);

        tvLoggedAs.setText(getString(R.string.logged_as) + getUserLoginFromSharedPref(context));

        btConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean is_valid = true;
                etCode.setError(null);
                if (isEmpty(etCode.getText().toString().trim())) {
                    etCode.setError(getString(R.string.error_field_required));
                    is_valid = false;
                }
                if (is_valid) {
                    EventJson event = new EventJson();
                    event.setCode(etCode.getText().toString());
                    EventIdTask eventIdTask = new EventIdTask(event);
                    eventIdTask.execute();
                }
            }
        });
    }

    public String getUserLoginFromSharedPref(Context context) {
        SharedPreferences settings;
        settings = context.getSharedPreferences(ConstantsHolder.SHARED_PREF_KEY, Context.MODE_PRIVATE);
        return settings.getString(ConstantsHolder.USER_LOGIN, null);
    }

//    public void onClickBtCreateNewBD(View v) {
//        Intent intent = new Intent(context, NewDatabaseActivity.class);
//        startActivity(intent);
//    }
//
//    public void onClickBtBazaDanych(View v) {
//        SharedPreferences pref;
//        pref = getSharedPreferences(ConstantsHolder.SHARED_PREF_KEY, Context.MODE_PRIVATE);
//        int id = pref.getInt(ConstantsHolder.USER_ID, 0);
//        String idek = Integer.toString(id);
//        GetEventCode gec = new GetEventCode(HeadquartersActivity.this, idek, context);
//        gec.execute();
//    }

//    public void onClickBtLogOut(View v) {
//        //wyczyszczenie SharedPreferences i przeniesienie do glownej aktywnosci
//        context.getSharedPreferences(ConstantsHolder.SHARED_PREF_KEY, 0).edit().clear().apply();
//        Intent intent = new Intent(context, MainActivity.class);
//        startActivity(intent);
//    }


    public class EventIdTask extends AsyncTask<String, String, ResponseEntity<EventJson>> {

        private final EventJson mEventJson;

        EventIdTask(EventJson eventJson) {
            mEventJson = eventJson;
        }

        @Override
        protected ResponseEntity<EventJson> doInBackground(String... params) {
            String url = ConstantsHolder.IP_ADDRESS + ConstantsHolder.URL_EVENT + mEventJson.getCode();
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<EventJson> entity = new HttpEntity<>(mEventJson, headers);

            return restTemplate.exchange(url, HttpMethod.GET, entity, EventJson.class);
        }

        @Override
        protected void onPostExecute(ResponseEntity<EventJson> result) {
            if (result.getStatusCode().equals(HttpStatus.OK)) {
                SharedPreferences settingsJson;
                SharedPreferences.Editor editor;
                settingsJson = context.getSharedPreferences(ConstantsHolder.SHARED_PREF_KEY,
                        Context.MODE_PRIVATE);
                editor = settingsJson.edit();
                editor.putString(ConstantsHolder.EVENT_CODE, result.getBody().getCode());
                editor.putInt(ConstantsHolder.EVENT_ID, result.getBody().getId());
                editor.apply();
                Toast.makeText(context, ConstantsHolder.REGISTER_COMPLETED, Toast.LENGTH_SHORT).show();

            } else if (result.getStatusCode().equals(HttpStatus.NO_CONTENT)) {
                etCode.setError(getString(R.string.error_event_not_found));
                etCode.requestFocus();
            } else {
                //TODO change to ScanActivity
                Intent intent = new Intent(context, LoginActivity.class);
                intent.putExtra(ConstantsHolder.EVENT_CODE, result.getBody().getCode());
                startActivity(intent);
            }
        }
    }

}
