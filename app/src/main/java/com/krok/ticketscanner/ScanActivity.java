package com.krok.ticketscanner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
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
import com.krok.json.UserJson;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

public class ScanActivity extends AppCompatActivity {


    final Activity activity = this;
    private TextView eventName;
    private TextView scanNext;
    private Context context;
    private int eventId;
    private int userId;
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
                    ticket = createTickerJsonFromJSONObject(jo);
                } catch (JSONException e) {
                    logger.info(e.getMessage());
                    e.printStackTrace();
                }

                logger.info("Received:/n" + sbJSON);

                mAuthTask = new TicketSendTask(ticket);
                mAuthTask.execute();

//                pm.execute(jo.toString(), ConstantsHolder.IP_ADDRESS +
//                        "/scann");

//                try {
//                    Toast.makeText(context, pm.get().trim(),
//                            Toast.LENGTH_SHORT).show();
//                    if (pm.get().trim().equals(ConstantsHolder.QR_UPDATED_EXIT)) {
//                        //Jezeli wychodzi to 3x beep bedzie
//                        ToneGenerator toneG =
//                                new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
//                        toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 750);
//                    } else{
//                        System.out.println();
//                    }
//
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                } catch (ExecutionException e) {
//                    e.printStackTrace();
//                }
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private TicketJson createTickerJsonFromJSONObject(JSONObject ticketData) throws JSONException {
        TicketJson json = new TicketJson();
        Calendar calendar;
        calendar = Calendar.getInstance();

        json.setName(ticketData.getString("name"));
        json.setSurname(ticketData.getString("surname"));
        json.setEmail(ticketData.getString("email"));
        json.setBirthDate(calendar.getTime());
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
                logger.info(result.getStatusCode().toString() + "DUPSKO");
                Toast.makeText(context, "Pierwszy raz", Toast.LENGTH_SHORT).show();

            } else if (result.getStatusCode().equals(HttpStatus.NO_CONTENT)) {
                //No W takim wypadku wychodzi, wiec trzeba piknac ze 3 razy
                ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 50);
                toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);

            } else if (result.getStatusCode().equals(HttpStatus.IM_USED)) {
                ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 80);
                toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
                Toast.makeText(context, "SOME_CRAZY_ERROR", Toast.LENGTH_SHORT).show();
                logger.info(result.getStatusCode().toString() + "DUPSKO2");

            }
        }

    }
}
