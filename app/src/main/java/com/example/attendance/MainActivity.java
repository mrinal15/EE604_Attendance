//TODO: Check the endpoints

package com.example.attendance;

import android.content.Intent;
import android.os.AsyncTask;
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

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private String ADMIN_TAG = "hooper";
    private EditText absentRollInput = null;
    private ListView displayAbsentRoll = null;
    private ArrayList<Integer> absentRollNumbers = new ArrayList<>();
    private String yourRollNumber = "200600";
    private ArrayAdapter<Integer> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button qrButton = findViewById(R.id.qr_code);
        absentRollInput = findViewById(R.id.input_absent);
        displayAbsentRoll = findViewById(R.id.display_absent);

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
                    absentRollNumbers.add(absentee);
                    adapter.notifyDataSetChanged();
                    Log.d(ADMIN_TAG, absentRollNumbers.toString());
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
        if(scanResult != null){
            Log.v(ADMIN_TAG, scanResult.toString());
            postMessage(scanResult, absentRollNumbers, yourRollNumber);
        }
    }

    private void postMessage(IntentResult scanResult, ArrayList<Integer> absentRollNumbers, String yourRollNumber) {
        String qrCodeContent = scanResult.getContents();
        if (isValidURL(qrCodeContent)) {
            for(int rollNumber : absentRollNumbers){
                new PostDataTask(rollNumber).execute(qrCodeContent);
            }
        }
    }
    private class PostDataTask extends AsyncTask<String, Void, Integer> {

        private int rollNumber;

        public PostDataTask(int rollNumber){
            this.rollNumber = rollNumber;
        }
        @Override
        protected Integer doInBackground(String... params) {
            try {
                String formData = "entry.1927997503="+ rollNumber + "&entry.698650085=" + 200600;
                URL url = new URL(params[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                OutputStream os = connection.getOutputStream();
                os.write(formData.getBytes());
                os.flush();
                os.close();

                return connection.getResponseCode();
            } catch (Exception e) {
                Log.e(ADMIN_TAG, "Error in POST Message: " + e.getMessage());
                return -1; // Return an error code or value
            }
        }
        @Override
        protected void onPostExecute(Integer responseCode) {
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Log.d(ADMIN_TAG, "POST request successful");
            } else {
                Log.e(ADMIN_TAG, "POST request failed. Response code: " + responseCode);
            }
        }
    }

    private boolean isValidURL(String url){
        try{
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }
}

