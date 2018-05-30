package com.krok.ticketscanner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.krok.json.TicketJson;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;

public class ScanActivity extends AppCompatActivity {


    final Activity activity = this;
    private TextView eventName;
    private TextView scanNext;
    private Context context;
    private int eventId;
    private int userId;
    private int deviceId;
    TicketSendTask mAuthTask = null;

    private static Logger logger = Logger.getLogger(
            Thread.currentThread().getStackTrace()[0].getClassName());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_scan);
        eventName = findViewById(R.id.tvEventName);
        scanNext = findViewById(R.id.tvScanNext);
        eventName.setText(getString(R.string.base) + getBaseNameFromSP(context));
        SharedPreferences settings;
        settings = context.getSharedPreferences(ConstantsHolder.SHARED_PREF_KEY, Context.MODE_PRIVATE);
        eventId = settings.getInt(ConstantsHolder.EVENT_ID, 0);
        userId = settings.getInt(ConstantsHolder.USER_ID, 0);
        deviceId = settings.getInt(ConstantsHolder.DEVICE_ID, 0);
    }

    public String getBaseNameFromSP(Context context) {
        SharedPreferences settings;
        settings = context.getSharedPreferences(ConstantsHolder.SHARED_PREF_KEY, Context.MODE_PRIVATE);
        return settings.getString(ConstantsHolder.EVENT_NAME, null);
    }

    public void onClickBtSkanuj(View v) {
        //Ustawianie parametrow dla biblioteki skanujacej
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        IntentIntegrator integrator = new IntentIntegrator(activity);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Scan");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(false);

        integrator.setOrientationLocked(false);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        scanNext.setTextColor(Color.GRAY);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
            } else {

                //Tworzenie JSON'a z zeskanowanego kodu qr
                StringBuilder sbJSON = new StringBuilder();
                sbJSON.append("{");
                sbJSON.append(result.getContents());
                sbJSON.append("}");

                JSONObject jo = null;
                try {
                    jo = new JSONObject(sbJSON.toString());
                    jo.put("user_id", userId);
                    jo.put("event_id", eventId);
                } catch (JSONException e) {
                    logger.info(e.getMessage());
                    e.printStackTrace();
                }

                //przypisanie imienia i nazwiska
                String nameAndSurname = (jo.optString("name") + " "
                        + jo.optString("surname"));
                scanNext.setText(nameAndSurname);
                TicketJson ticket = new TicketJson();
                try {
                    ticket = createTicketJsonFromJSONObject(jo);
                } catch (JSONException e) {
                    logger.info(e.getMessage());
                    e.printStackTrace();
                }

                mAuthTask = new TicketSendTask(ticket);
                mAuthTask.execute();

            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private TicketJson createTicketJsonFromJSONObject(JSONObject ticketData) throws JSONException {
        TicketJson json = new TicketJson();
//        Calendar calendar;
//        calendar = Calendar.getInstance();

        json.setName(ticketData.getString("name"));
        json.setSurname(ticketData.getString("surname"));
        json.setEmail(ticketData.getString("email"));
//        json.setBirthDate(calendar.getTime());

        DateFormat format = new SimpleDateFormat("dd-MM-YYYY", Locale.ENGLISH);
        Date date = null;
        try {
            date = format.parse(ticketData.getString("birth_date"));
        } catch (ParseException e) {
            logger.info("Cannot parse birth_date");
            e.printStackTrace();
        }

        json.setBirthDate(date);
        json.setCity(ticketData.getString("city"));
        json.setCode(ticketData.getString("code"));
        json.setFlatNr(ticketData.getString("frat_nr"));
        json.setPhone(ticketData.getInt("phone"));
        json.setSeatNumber(ticketData.getString("seat_nr"));
        json.setSex(ticketData.getString("sex").charAt(0));
        json.setStreet(ticketData.getString("street"));
        json.setEventId(ticketData.getInt("event_id"));
        json.setUserId(ticketData.getInt("user_id"));

        return json;
    }

    private class TicketSendTask extends AsyncTask<String, String, ResponseEntity<TicketJson>> {

        private final TicketJson mTicketJson;

        TicketSendTask(TicketJson ticketJson) {
            mTicketJson = ticketJson;
        }

        @Override
        protected ResponseEntity<TicketJson> doInBackground(String... params) {
            String url = ConstantsHolder.IP_ADDRESS + ConstantsHolder.URL_TICKET + "//" + userId + "//" + eventId;
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<TicketJson> entity = new HttpEntity<>(mTicketJson, headers);

            return restTemplate.exchange(url, HttpMethod.POST, entity, TicketJson.class);
        }

        @Override
        protected void onPostExecute(ResponseEntity<TicketJson> result) {
            mAuthTask = null;
            if (result.getStatusCode().equals(HttpStatus.OK)) {
                Toast.makeText(context, "Pierwszy raz", Toast.LENGTH_SHORT).show();

            } else if (result.getStatusCode().equals(HttpStatus.NO_CONTENT)) {
                //No W takim wypadku wychodzi, wiec trzeba piknac ze 3 razy

                Toast.makeText(context, "Someone left?", Toast.LENGTH_SHORT).show();
                scanNext.setTextColor(Color.RED);
                ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 750);

            } else {
                Toast.makeText(context, "SOME_ERROR", Toast.LENGTH_SHORT).show();
                logger.info(result.getStatusCode().toString() + "SOME_ERROR");

            }
        }

    }
}
