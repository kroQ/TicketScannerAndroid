package com.krok.ticketscanner;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.krok.json.EventJson;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static android.text.TextUtils.isEmpty;

public class HeadquartersActivity extends AppCompatActivity {

    private EditText etCode;
    private Context context;
    private EventConnectTask mAuthTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (LoginActivity.instance != null) {
            LoginActivity.instance.finish();
        }

        setContentView(R.layout.activity_headquartes);
        context = getApplicationContext();
        Button btConnect = this.findViewById(R.id.bt_connect_event);
        FloatingActionButton btNewEvent = this.findViewById(R.id.fab_new_db);
        etCode = this.findViewById(R.id.et_event_code_hq);
        TextView tvLoggedAs = this.findViewById(R.id.tv_logged_as);

        tvLoggedAs.setText(getString(R.string.logged_as) + getUserLoginFromSharedPref(context));

        btNewEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                        Intent intent = new Intent(context, NewEventActivity.class);
        startActivity(intent);
            }
        });

        btConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mAuthTask != null){
                    return;
                }

                View focusView = null;
                boolean isValid = true;
                etCode.setError(null);

                if (isEmpty(etCode.getText().toString().trim())) {
                    etCode.setError(getString(R.string.error_field_required));
                    isValid = false;
                    focusView = etCode;
                }
                if (isValid) {
                    EventJson event = new EventJson();
                    event.setCode(etCode.getText().toString());
                    mAuthTask = new EventConnectTask(event);
                    mAuthTask.execute();
                } else {
                    focusView.requestFocus();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_log_out:
                //wyczyszczenie SharedPreferences i przeniesienie do glownej aktywnosci
                context.getSharedPreferences(ConstantsHolder.SHARED_PREF_KEY, 0).edit().clear().apply();
                Intent intent = new Intent(context, LoginActivity.class);
                startActivity(intent);
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    public String getUserLoginFromSharedPref(Context context) {
        SharedPreferences settings;
        settings = context.getSharedPreferences(ConstantsHolder.SHARED_PREF_KEY, Context.MODE_PRIVATE);
        return settings.getString(ConstantsHolder.USER_LOGIN, null);
    }

    public class EventConnectTask extends AsyncTask<String, String, ResponseEntity<EventJson>> {

        private final EventJson mEventJson;

        EventConnectTask(EventJson eventJson) {
            mEventJson = eventJson;
        }

        @Override
        protected ResponseEntity<EventJson> doInBackground(String... params) {
            String url = ConstantsHolder.IP_ADDRESS + ConstantsHolder.URL_EVENT + mEventJson.getCode();
            RestTemplate restTemplate = new RestTemplate();

            return restTemplate.getForEntity(url, EventJson.class);
        }

        @Override
        protected void onPostExecute(ResponseEntity<EventJson> result) {
            mAuthTask = null;
            if (result.getStatusCode().equals(HttpStatus.OK)) {
                SharedPreferences settingsJson;
                SharedPreferences.Editor editor;
                settingsJson = context.getSharedPreferences(ConstantsHolder.SHARED_PREF_KEY,
                        Context.MODE_PRIVATE);
                editor = settingsJson.edit();
                editor.putString(ConstantsHolder.EVENT_NAME, result.getBody().getName());
                editor.putInt(ConstantsHolder.EVENT_ID, result.getBody().getId());
                editor.apply();
                Toast.makeText(context, "Elo mordo", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context, ScanActivity.class);
                startActivity(intent);
            } else if (result.getStatusCode().equals(HttpStatus.NO_CONTENT)) {
                etCode.setError(getString(R.string.error_event_not_found));
                etCode.requestFocus();
            } else {
                Toast.makeText(context, "SOME_CRAZY_ERROR", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }

}
