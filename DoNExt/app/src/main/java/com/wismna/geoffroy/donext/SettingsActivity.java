package com.wismna.geoffroy.donext;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        populateSpinner();
    }

    private void populateSpinner() {
        Spinner spinner  = (Spinner) findViewById(R.id.max_lists_spinner);
        spinner.setSelection(getPreference(R.id.max_lists_spinner));
        //ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
        //int position = adapter.getPosition()
        /*ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.settings_max_lists_number, R.layout.support_simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);*/
    }

    protected int getPreference(int setting) {
        SharedPreferences sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        return sharedPreferences.getInt(getString(setting), 2);
    }
    protected void setPreference(int setting, int value) {
        SharedPreferences sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(getString(setting), value);
        editor.apply();
    }
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // Item was selected, do something
        //setPreference(R.id.max_lists_spinner, Integer.parseInt((String) parent.getItemAtPosition(pos)));
        setPreference(R.id.max_lists_spinner, pos);
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }
}
