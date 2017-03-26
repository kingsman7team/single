package com.example.t420_note2.myapplication;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import android.provider.Settings.Secure;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
    private static final String SERVER_URL = "http://192.168.1.8:8080/myfirstspring/";
    Button btn1, btn2;
    Button btn11, btn12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        btn1 = new Button(getApplicationContext());
        btn1.setText("Connect Test1");
        btn1.setOnClickListener(this);
        btn2 = new Button(getApplicationContext());
        btn2.setText("Connect Test2");
        btn2.setOnClickListener(this);

        btn11 = new Button(getApplicationContext());
        btn11.setText("WiFi send Test1");
        btn11.setOnClickListener(this);
        btn12 = new Button(getApplicationContext());
        btn12.setText("WiFi get Test1");
        btn12.setOnClickListener(this);

        final LinearLayout lm = (LinearLayout) findViewById(R.id.ll);
        lm.setOrientation(LinearLayout.VERTICAL);
        lm.addView(btn1);
        lm.addView(btn2);
        lm.addView(btn11);
        lm.addView(btn12);
    }

    private void makeConnectTest(Map<String,String> params){
        NetworkTask networkTask = new NetworkTask();
//        Map<String,String> params = new HashMap<String,String>();
//        params.put("URL", url);
//        params.put("title","title message");
//        params.put("memo","memo message");

        networkTask.execute(params);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View v) {
        if(v == btn1){
            // test
            Map<String,String> params = new HashMap<String,String>();
            params.put("TYPE", "respond_test1");
            params.put("title","title message");
            params.put("memo","memo message");
            makeConnectTest(params);
        }
        else if(v == btn2){
            Map<String,String> params = new HashMap<String,String>();
            params.put("TYPE", "respond_test2");
            params.put("title","title message");
            params.put("memo","memo message");
            makeConnectTest(params);
        }
        else if(v == btn11){
            Map<String,String> params = new HashMap<String,String>();
            params.put("TYPE", "wifi_info_from_phone");
            params.put("id", getWiFiID());
            params.put("pw", getWiFiPW());
            params.put("user",getUserID());
            makeConnectTest(params);
        }
        else if(v == btn12){
            Map<String,String> params = new HashMap<String,String>();
            params.put("TYPE", "wifi_info_from_server");
            makeConnectTest(params);
        }
    }
    private String getUserID(){
        return Secure.getString(getContentResolver(), Secure.ANDROID_ID);
    }
    private String getWiFiID(){
        return "WiFi_ID";
    }
    private String getWiFiPW(){
        return "WiFi_PW";
    }

    public class NetworkTask extends AsyncTask<Map<String,String>, Integer, String> {

        /**
         * doInBackground 실행되기 이전에 동작한다.
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        /**
         * 본 작업을 쓰레드로 처리해준다.
         * @return
         */
        @Override
        protected String doInBackground(Map<String,String>... maps) {
            Log.d("aa", "Start doInBackground");
            // HTTP 요청 준비 작업
//            HttpClient.Builder http = new HttpClient.Builder("POST", "http://172.30.1.30:8080/myfirst/wifi_info_from_phone");
//            HttpClient.Builder http = new HttpClient.Builder("POST", "http://192.168.10.39:8080/myfirstspring/wifi_info_from_phone2");
            final String requestType = maps[0].get("TYPE");
            maps[0].remove("TYPE");

            HttpClient.Builder http = new HttpClient.Builder("POST", SERVER_URL + requestType);
            //http.addOrReplace("test", "English_한글"
            http.addAllParameters(maps[0]);
            Log.d("aa", "End http.addAllParameters(maps[0])");

            // HTTP 요청 전송
            HttpClient post = http.create();
            post.request();

            // 응답 상태코드 가져오기
            int statusCode = post.getHttpStatusCode();

            // 응답 본문 가져오기
            String body = post.getBody();

            Log.d("aa", "End doInBackground");
            return body;
        }

        /**
         * doInBackground 종료되면 동작한다.
         * @param s : doInBackground가 리턴한 값이 들어온다.
         */
        @Override
        protected void onPostExecute(String s) {
            Log.d("aa", "Start onPostExecute");
            Log.d("HTTP_RESULT", s);
            Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
            Log.d("aa", "End onPostExecute");
        }
    }
}
