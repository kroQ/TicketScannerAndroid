package com.krok.ticketscanner;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.krok.json.EventJson;
import com.krok.json.TicketJson;

import org.springframework.http.HttpStatus;
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
    List<String> listDataHeader2 = new ArrayList<>();
    TextView header;
    List<String> top250;
    Intent intent;

    private int userId;
    private MyEventsTask mEventsTask = null;

    private static Logger logger = Logger.getLogger(
            Thread.currentThread().getStackTrace()[0].getClassName());

    HashMap<String, List<String>> listDataChild = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = getApplicationContext();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_events);
        header = this.findViewById(R.id.tvHeader);
        intent = getIntent();
        mEventsTask = new MyEventsTask();
        mEventsTask.execute();

        userId = getUserIdFromSharedPref(getApplicationContext());
    }

    public int getUserIdFromSharedPref(Context context) {
        SharedPreferences settings;
        settings = context.getSharedPreferences(ConstantsHolder.SHARED_PREF_KEY, Context.MODE_PRIVATE);
        return settings.getInt(ConstantsHolder.USER_ID, 0);
    }

    private void addHeadersToList(String name) {
        listDataHeader2.add(name);
    }

    private class MyEventsTask extends AsyncTask<String, String, ResponseEntity<EventJson[]>> {
        @Override
        protected ResponseEntity<EventJson[]> doInBackground(String... strings) {
            String url = ConstantsHolder.IP_ADDRESS + ConstantsHolder.URL_EVENT_ALL + userId;
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<EventJson[]> responseEntity = restTemplate.getForEntity(url, EventJson[].class);
            return responseEntity;
        }

        @Override
        protected void onPostExecute(ResponseEntity<EventJson[]> responseEntity) {
            mEventsTask = null;

            if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {

                // Adding child data
                top250 = new ArrayList<String>();
                top250.add("Jan Kowalski");
                top250.add("Marek Kowalski");

                for (int i = 0; i < responseEntity.getBody().length; i++) {
                    EventJson eventJson = responseEntity.getBody()[i];
                    addHeadersToList(eventJson.getName());

                    List<TicketJson> a = new ArrayList<>(eventJson.getAllTickets());
                    List<String> ls = new ArrayList<>();
                    for (TicketJson t : a) {
                        ls.add(t.getName() + " " + t.getSurname());
                    }
                    listDataChild.put(eventJson.getName(), ls);
                }

                // get the listview
                expListView = (ExpandableListView) findViewById(R.id.lvExp);
                // preparing list data
                listAdapter = new ExpandableListAdapter(context, listDataHeader2, listDataChild);
                // setting list adapter
                expListView.setAdapter(listAdapter);

            } else if (responseEntity.getStatusCode().equals(HttpStatus.NO_CONTENT)) {
                Toast.makeText(context, "You have no events yet", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            mEventsTask = null;
        }


    }

}
