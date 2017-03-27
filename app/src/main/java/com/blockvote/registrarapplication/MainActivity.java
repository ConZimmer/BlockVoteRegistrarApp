package com.blockvote.registrarapplication;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.Toast;

import com.blockvote.registrarapplication.qrCode.GenerateQRActivity;
import com.blockvote.registrarapplication.qrCode.ReadQRActivity;
import com.github.lzyzsd.circleprogress.ArcProgress;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.integration.android.IntentIntegrator;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences dataStore;
    private int progress = 0;
    private ListView incompleteList;
    private ListView completedList;
    List<SavedQRCode> savedQRcodeList;
    LinkedList<String> incompleteVoterIDs;
    LinkedList<String> completedVoterIDs;
    private Intent loginIntent;
    private Intent readQRIntent;
    private Intent viewQRIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        readQRIntent = new Intent(this, ReadQRActivity.class);
        readQRIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        viewQRIntent = new Intent(this, GenerateQRActivity.class);
        viewQRIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        loginIntent = new Intent(this, LoginActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        startActivity(loginIntent);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar myActionBar = (ActionBar) getSupportActionBar();
        myActionBar.setTitle("Block Vote - Registrar");
        myActionBar.setLogo(R.mipmap.ic_launcher);

        Spannable text = new SpannableString(myActionBar.getTitle());
        text.setSpan(new ForegroundColorSpan(Color.WHITE), 0, text.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        myActionBar.setTitle(text);

        setOverflowButtonColor(this, Color.WHITE);


        TabHost host = (TabHost)findViewById(R.id.main_tab_menu);
        host.setup();

        //Tab 1
        TabHost.TabSpec spec = host.newTabSpec("Tab One");
        spec.setContent(R.id.tab1_register_voter);
        spec.setIndicator("Add Voter");
        host.addTab(spec);

        //Tab 2
        spec = host.newTabSpec("Tab Two");
        spec.setContent(R.id.tab2_incomplete_registrations);
        spec.setIndicator("Incomplete");
        host.addTab(spec);

        //Tab 3
        spec = host.newTabSpec("Tab Three");
        spec.setContent(R.id.tab3_completed_registrations);
        spec.setIndicator("Completed");
        host.addTab(spec);

        incompleteList = (ListView) findViewById(R.id.list_incomplete);
        completedList = (ListView) findViewById(R.id.list_completed);
        incompleteVoterIDs = new LinkedList<String>();
        completedVoterIDs = new LinkedList<String>();


        dataStore = getPreferences(MODE_PRIVATE);
        if (!dataStore.contains("access_token")){
            //Need to login
            //Intent lockIntent = new Intent(this, LoginActivity.class);
            //startActivity(lockIntent);
        }


        ImageButton addVoter = (ImageButton)findViewById(R.id.imageButtonAddVoter);
        addVoter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(readQRIntent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        SharedPreferences.Editor editor;

        switch (item.getItemId()) {
            case R.id.Main_Option_LogOut:
                dataStore = getSharedPreferences("RegistrarData", MODE_PRIVATE);
                editor = dataStore.edit();
                editor.clear();
                editor.commit();

                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                return true;

            case R.id.Main_Option_ClearAllVoterData:
                dataStore = getSharedPreferences("SavedData", MODE_PRIVATE);
                editor = dataStore.edit();
                editor.clear();
                editor.commit();

                this.onResume();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Hide the status bar.
        //View decorView = getWindow().getDecorView();
        //int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        //decorView.setSystemUiVisibility(uiOptions);

        //TODO check if login credentials are still valid, if not, start login activity


        //Incomplete list
        dataStore = getSharedPreferences("SavedData", MODE_PRIVATE);
        SharedPreferences.Editor editor = dataStore.edit();

        Gson gson = new Gson();

        String json = dataStore.getString("SavedQRCodeList", "");
        if(json==null){
            Log.e("ERROR", "ERROR json is null");
        }

        Type type = new TypeToken<List<SavedQRCode>>(){}.getType();
        savedQRcodeList = gson.fromJson(json, type);

        incompleteVoterIDs.clear();
        completedVoterIDs.clear();

        if (savedQRcodeList != null)
        {
            for (SavedQRCode QRcode : savedQRcodeList) {
                if(QRcode.completed == true){
                    completedVoterIDs.add(QRcode.voterID);
                }
                else{
                    incompleteVoterIDs.add(QRcode.voterID);
                }
            }
        }


        ArrayAdapter<String> adapterIncomplete = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, incompleteVoterIDs);

        // Assign adapter to ListView
        incompleteList.setAdapter(adapterIncomplete);

        // ListView Item Click Listener
        incompleteList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // ListView Clicked item index
                int itemPosition     = position;

                // ListView Clicked item value
                String  itemValue    = (String) incompleteList.getItemAtPosition(position);

                viewQRCode(itemValue, false);
            }
        });


        ArrayAdapter<String> adapterCompleted = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, completedVoterIDs);

        // Assign adapter to ListView
        completedList.setAdapter(adapterCompleted);

        // ListView Item Click Listener
        completedList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // ListView Clicked item index
                int itemPosition     = position;

                // ListView Clicked item value
                String  itemValue    = (String) completedList.getItemAtPosition(position);

                viewQRCode(itemValue, true);
            }
        });
    }

    private void viewQRCode(String voterID, boolean registrationCompleted){
        viewQRIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        viewQRIntent.putExtra("ScannedQRCodeBlindedTokenString", "");
        viewQRIntent.putExtra("savedQRCode", true);
        viewQRIntent.putExtra("completedRegistration", registrationCompleted);
        viewQRIntent.putExtra("voterID", Integer.valueOf(voterID));
        startActivity(viewQRIntent);
    }


    @Override
    public void onBackPressed() {
        Log.d("MainActivity", "Back Button was pressed");

        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MainActivity.this.finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Hide the status bar.
                        //View decorView = getWindow().getDecorView();
                        //int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
                        //decorView.setSystemUiVisibility(uiOptions);
                    }
                })
                .show();
    }

    private static void setOverflowButtonColor(final Activity activity, final int color) {
        final String overflowDescription = activity.getString(R.string.abc_action_menu_overflow_description);
        final ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        final ViewTreeObserver viewTreeObserver = decorView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                final ArrayList<View> outViews = new ArrayList<View>();
                decorView.findViewsWithText(outViews, overflowDescription, View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
                if (outViews.isEmpty()) {
                    return;
                }
                AppCompatImageView overflow = (AppCompatImageView) outViews.get(0);
                overflow.setColorFilter(color);
            }
        });
    }

}
