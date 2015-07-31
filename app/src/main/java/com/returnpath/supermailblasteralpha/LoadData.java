package com.returnpath.supermailblasteralpha;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class LoadData extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_load_data, menu);
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

    public void syncContextIO(View view) {
        // Testing access to preferences.
        // TODO: move somewhere else.
        Log.d(this.getLocalClassName(), "htoth: in syncContextIO");
        SharedPreferences s = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String syncEmail = s.getString("pref_email_address", "e@htoth.mailrp.net");
        Log.d(this.getLocalClassName(), "htoth: emailaddress = " + syncEmail);

//        String testUrl = "https://api.context.io/2.0/accounts/55ba5f4ddfc24787518b4567/contacts/hyrum.toth%40returnpath.com";
        // TODO: pass in URL(s)
        new ContextIOApi().execute();
    }
}
