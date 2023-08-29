package com.example.attendance;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import io.appium.java_client.android.AndroidDriver;

public class MainActivity extends AppCompatActivity {

    private String ADMIN_TAG = "hooper";
    private EditText absentRollInput = null;
    private ListView displayAbsentRoll = null;
    private ArrayList<Integer> absentRollNumbers = new ArrayList<>();
    private ArrayAdapter<Integer> adapter;
    private AndroidDriver driver;
    DesiredCapabilities capabilities = new DesiredCapabilities();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button qrButton = findViewById(R.id.qr_code);
        absentRollInput = findViewById(R.id.input_absent);
        displayAbsentRoll = findViewById(R.id.display_absent);
        capabilities.setCapability("platformName" , "Android");
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, absentRollNumbers);
        displayAbsentRoll.setAdapter(adapter);
        qrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                integrator.initiateScan();
                Log.v(ADMIN_TAG , "Camera Open");
            }
        });

        absentRollInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    Log.v(ADMIN_TAG, absentRollInput.getText().toString());
                    Integer absentee = Integer.parseInt(absentRollInput.getText().toString());
                    if( absentee > 200000 && absentee < 220000) {
                        absentRollNumbers.add(absentee);
                        adapter.notifyDataSetChanged();
                        Log.d(ADMIN_TAG, absentRollNumbers.toString());
                    }
                    absentRollInput.getText().clear();
                    return true;
                }
                return false;
            }
        });

        displayAbsentRoll.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Integer rollNumberToDelete = absentRollNumbers.get(i);
                absentRollNumbers.remove(i);
                adapter.notifyDataSetChanged();
                Log.v(ADMIN_TAG, "Deleted: " + rollNumberToDelete);
                Toast.makeText(MainActivity.this, "Deleted: " + rollNumberToDelete, Toast.LENGTH_SHORT);
                return true;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode,resultCode, data);
        if(scanResult.getContents() != null){
            inputForm(scanResult.getContents());
            Log.v(ADMIN_TAG, scanResult.toString());
        }
    }

    private void inputForm(String webURL){
        try {
            driver = new AndroidDriver(new URL("http://172.23.67.106:4723/"), capabilities);
            driver.get(webURL);
            Log.v(ADMIN_TAG, driver.toString());
            driver.close();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}