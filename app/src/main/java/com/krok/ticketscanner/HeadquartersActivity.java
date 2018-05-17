package com.krok.ticketscanner;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class HeadquartersActivity extends Activity {

    private Button btConnect;
    private EditText etCode;
    private Context context;
    private TextView tvLoggedAs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (LoginActivity.instance != null) {
            try {
                LoginActivity.instance.finish();
            } catch (Exception e) {
            }
        }

        setContentView(R.layout.activity_headquartes);
        context = getApplicationContext();
//        btConnect = (Button) this.findViewById(R.id.btPolacz);
//        etCode = (EditText) this.findViewById(R.id.etDataBaseCode);
//        tvLoggedAs = (TextView) this.findViewById(R.id.tvLoggedAs);
        //ChooseDatabaseActivity chooseDatabaseActivity = this;

//        btConnect.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Boolean is_valid = true;
//                etCode.setError(null);
//                if (isEmpty(etCode.getText().toString())) {
//                    etCode.setError("Podaj kod bazy");
//                    is_valid = false;
//                }
//                if (is_valid) {
//                    GetEventId gei = new GetEventId(HeadquartersActivity.this, etCode, context);
//                    gei.execute();
//                }
//            }
//        });
    }

    //wyciagniecie danych z SharedPreferences
    public String getJsonUser(Context context) {
        SharedPreferences settings;
        settings = context.getSharedPreferences(ConstantsHolder.SHARED_PREF_KEY, Context.MODE_PRIVATE);
        return settings.getString(ConstantsHolder.USER_IN_JSON, null);
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
}
