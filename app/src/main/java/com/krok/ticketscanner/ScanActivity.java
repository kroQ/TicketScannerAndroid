package com.krok.ticketscanner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONObject;

public class ScanActivity extends AppCompatActivity {


    final Activity activity = this;
    private TextView dataBaseName;
    private TextView scanNext;
    private Context context;
    private int eventId;
    private int scnId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_scan);
        dataBaseName = findViewById(R.id.tvBaza);
        scanNext =  findViewById(R.id.tvScanNext);
        dataBaseName.setText(getString(R.string.base) + getBaseNameFromSP(context));
        SharedPreferences settings;
        settings = context.getSharedPreferences(ConstantsHolder.SHARED_PREF_KEY, Context.MODE_PRIVATE);
        eventId = settings.getInt(ConstantsHolder.EVENT_ID, 0);
        scnId = settings.getInt(ConstantsHolder.USER_ID, 0);
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
                //Log.d("MainActivity", "Cancelled scan");
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
            } else {
                //Log.d("MainActivity", "Scanned");
                //Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                //Tworzenie JSON'a z zeskanowanego kodu qr
                StringBuilder sbJSON = new StringBuilder();
                sbJSON.append("{");
                sbJSON.append(result.getContents());
                sbJSON.append("}");
                JSONObject jo = null;
//                try {
//                    jo = new JSONObject(sbJSON.toString());
//                    jo.put("scn_id", scnId);
//                    jo.put("evt_id", eventId);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
                //przypisanie imienia i nazwiska
//                String nameAndSurname = (jo.optString("name") + " "
//                        + jo.optString("surname"));
//                scanNext.setText(nameAndSurname);
                //wyslanie POST'a z QR do bazy
//                PostMethod pm = new PostMethod(this);

                System.out.println(sbJSON);
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
}
