package com.krok.ticketscanner;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.krok.json.EventJson;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

public class MyEventsListActivity extends AppCompatActivity {
    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    Context context;


    private int userId;
    private MyEventsTask mEventsTask = null;

    public List<String> getListDataHeader2() {
        return listDataHeader2;
    }

    List<String> listDataHeader2 = new ArrayList<>();
    List<String> listUsers;
    TextView header;
    List<String> top250;

    private static Logger logger = Logger.getLogger(
            Thread.currentThread().getStackTrace()[0].getClassName());



    HashMap<String, List<String>> listDataChild= new HashMap<String, List<String>>();
    Intent intt;

    public HashMap<String, List<String>> getListDataChild() {
        return listDataChild;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context =getApplicationContext();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_events);
        header = (TextView) this.findViewById(R.id.tvHeader);
        intt = getIntent();
        mEventsTask = new MyEventsTask();
        mEventsTask.execute();


//        prepareListData();

        userId = getUserIdFromSharedPref(getApplicationContext());

    }

    public int getUserIdFromSharedPref(Context context) {
        SharedPreferences settings;
        settings = context.getSharedPreferences(ConstantsHolder.SHARED_PREF_KEY, Context.MODE_PRIVATE);
        return settings.getInt(ConstantsHolder.USER_ID, 0);
    }

    private void addHeadersToList(String name){
        listDataHeader2.add(name);
    }

    /*
     * Preparing the list data
     */
//    private void prepareListData() {
//
////        listDataHeader2 = intt.getStringArrayListExtra("eventsList");
////        listDataHeader2.add("Temat1");
////        listDataHeader2.add("Temat2");
////        listDataHeader2.add("Temat3");
////        listDataHeader2.add("Temat4");
////
////        listDataChild = new HashMap<String, List<String>>();
////        gus = new GetUserScanned(DetailedDataActivity.this, listDataHeader2.get(0), DetailedDataActivity.this);
////        gus.execute();
//        intt = getIntent();
//        listUsers = intt.getStringArrayListExtra("listUsers");
//
//        // Adding child data
////        listDataHeader.add("Jan Kowalski");
////        listDataHeader.add("Krystian Klimek");
////        listDataHeader.add("Tomasz Krystian");
//
//        // Adding child data
//        top250 = new ArrayList<String>();
//        top250.add("Jan");
//        top250.add("Kowalski");
//        top250.add("23");
//        top250.add("jan@kowalski.com");
//        top250.add("Ladna 27");
//        top250.add("Kraków");
//        top250.add("true");
//
//        List<String> nowShowing = new ArrayList<String>();
//        nowShowing.add("The Conjuring");
//        nowShowing.add("Despicable Me 2");
//        nowShowing.add("Turbo");
//        nowShowing.add("Grown Ups 2");
//        nowShowing.add("Red 2");
//        nowShowing.add("The Wolverine");
//
//        List<String> comingSoon = new ArrayList<String>();
//        comingSoon.add("2 Guns");
//        comingSoon.add("The Smurfs 2");
//        comingSoon.add("The Spectacular Now");
//        comingSoon.add("The Canyons");
//        comingSoon.add("Europa Report");
//
//        if(listDataHeader2.isEmpty()){
//            header.setText("Brak utworzonych baz...");
//            Toast.makeText(this, "NIE UWTORZYLES JESZCZE BAZY", Toast.LENGTH_LONG).show();
//        } else {
//            listDataChild.put(listDataHeader2.get(0), top250); // Header, Child data
//            listDataChild.put(listDataHeader2.get(1), nowShowing);
//            listDataChild.put(listDataHeader2.get(2), comingSoon);
//        }
//    }


    private class MyEventsTask extends AsyncTask<String, String, ResponseEntity<EventJson[]>> {
        @Override
        protected ResponseEntity<EventJson[]>doInBackground(String... strings) {
            String url = ConstantsHolder.IP_ADDRESS + ConstantsHolder.URL_EVENT_ALL + userId;
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<EventJson[]> responseEntity = restTemplate.getForEntity(url, EventJson[].class);
            return responseEntity;
        }

        @Override
        protected void onPostExecute(ResponseEntity<EventJson[]> responseEntity) {
            mEventsTask = null;
            logger.info("MyEventsTask: " + responseEntity.getStatusCode());

            //        // Adding child data
        top250 = new ArrayList<String>();
        top250.add("Jan");
        top250.add("Kowalski");
        top250.add("23");
        top250.add("jan@kowalski.com");
        top250.add("Ladna 27");
        top250.add("Kraków");
        top250.add("true");

            for(int i = 0; i < responseEntity.getBody().length; i ++){
                logger.info("Body: ["+ i + "] "+ responseEntity.getBody()[i].getName());
//                listDataChild.put(responseEntity.getBody()[i].getName(), top250);
//                listDataHeader2.add(responseEntity.getBody()[i].getName());
                addHeadersToList(responseEntity.getBody()[i].getName());
                listDataChild.put(responseEntity.getBody()[i].getName(), top250);
            }

            // get the listview
            expListView = (ExpandableListView) findViewById(R.id.lvExp);

            // preparing list data
            listAdapter = new ExpandableListAdapter(context, listDataHeader2, listDataChild);
            // setting list adapter
            expListView.setAdapter(listAdapter);
        }

        @Override
        protected void onCancelled() {
            mEventsTask = null;
        }
    }

}
