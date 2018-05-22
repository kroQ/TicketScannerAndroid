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

import com.krok.json.EventJson;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.logging.Logger;

import static org.springframework.util.StringUtils.isEmpty;

public class NewEventActivity extends AppCompatActivity {

    private EditText etEventCode;
    private EditText etEventName;
    private Button btAdd;
    private Context context = this;
    private boolean isValid;
    int userId;
    private EventCreateTask mAuthTask = null;
    private View mProgressView;

    private static Logger logger = Logger.getLogger(
            Thread.currentThread().getStackTrace()[0].getClassName());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_new_event);
        etEventCode = findViewById(R.id.et_event_code);
        etEventName = findViewById(R.id.et_event_name);
        btAdd = findViewById(R.id.bt_add_database);
        mProgressView = findViewById(R.id.event_progress);

        SharedPreferences sharedPref = this.getSharedPreferences(ConstantsHolder.SHARED_PREF_KEY,
                Context.MODE_PRIVATE);
        userId = sharedPref.getInt(ConstantsHolder.USER_ID, 0);

        btAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAuthTask != null) {
                    return;
                }

                View focusView = null;
                isValid = true;
                etEventCode.setError(null);
                etEventName.setError(null);

                if (isEmpty(etEventName.getText().toString())) {
                    etEventName.setError(getString(R.string.error_field_required));
                    isValid = false;
                    focusView = etEventName;
                }

                if (etEventCode.getText().length() < 6) {
                    isValid = false;
                    focusView = etEventCode;
                    etEventCode.setError(getString(R.string.error_event_code_range));
                }

                if (etEventName.getText().toString().trim().length() > 20) {
                    isValid = false;
                    etEventName.setError(getString(R.string.error_event_name_range));
                }

                if (isValid) {
                    Toast.makeText(context, ConstantsHolder.CREATED_EVENT, Toast.LENGTH_SHORT).show();
                    showProgress(true);

                    EventJson eventJson = new EventJson();
                    eventJson.setCode(etEventCode.getText().toString());
                    eventJson.setName(etEventName.getText().toString().trim());
                    eventJson.setOwnerId(userId);
                    eventJson.setEndEventDate(null);
                    eventJson.setStartEventDate(null);
                    eventJson.setTicketsPool(0);

                    mAuthTask = new EventCreateTask(eventJson);
                    mAuthTask.execute();

                } else {
                    assert focusView != null;
                    focusView.requestFocus();
                }

            }
        });
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

    public class EventCreateTask extends AsyncTask<String, String, ResponseEntity<EventJson>> {

        private final EventJson mEventJson;

        EventCreateTask(EventJson eventJson) {
            mEventJson = eventJson;
        }

        @Override
        protected ResponseEntity<EventJson> doInBackground(String... strings) {
            String url = ConstantsHolder.IP_ADDRESS + ConstantsHolder.URL_EVENT;
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<EventJson> entity = new HttpEntity<>(mEventJson, headers);

            ResponseEntity<EventJson> result = restTemplate.exchange(url, HttpMethod.POST, entity, EventJson.class);

            return result;
        }

        @Override
        protected void onPostExecute(final ResponseEntity<EventJson> result) {
            mAuthTask = null;
            showProgress(false);
            logger.info(result.getStatusCode().toString());

            if (result.getStatusCode().equals(HttpStatus.OK)) {

                SharedPreferences preferences;
                SharedPreferences.Editor editor;
                preferences = context.getSharedPreferences(ConstantsHolder.SHARED_PREF_KEY,
                        Context.MODE_PRIVATE);
                editor = preferences.edit();
                editor.putString(ConstantsHolder.EVENT_NAME, result.getBody().getName());
                editor.putInt(ConstantsHolder.EVENT_ID, result.getBody().getId());
                editor.apply();

                context = getApplicationContext();
                Intent intent = new Intent(context, ScanActivity.class);
                startActivity(intent);

            } else if (result.getStatusCode().equals(HttpStatus.IM_USED)) {
                etEventName.setError(getString(R.string.error_event_used));
                etEventName.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

}
