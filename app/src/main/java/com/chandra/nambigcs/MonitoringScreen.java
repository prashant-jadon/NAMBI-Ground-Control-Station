package com.chandra.nambigcs;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.opencsv.CSVWriter;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class MonitoringScreen extends Activity {

    private static final String TAG = "BlueTest5-MainActivity";
    private int mMaxChars = 50000;//Default
    private UUID mDeviceUUID;
    private Button CommandSend;
    private BluetoothSocket mBTSocket;
    public boolean shouldStop = false;
    private ReadInput mReadThread = null;

    private Button Chart;

    //SIMULATION
    private Button simulation;
    private boolean mIsUserInitiatedDisconnect = false;


    private BluetoothSocket mSocket;

    // All controls here
    private TextView modeDownload;
    private TextView altitudeDownload;
    private TextView voltageDownload;
    private TextView softStatusDownload;
    private TextView gpsLatitudeDownload;
    private TextView gpsLongitudeDownload;
    private TextView gpsStatsDownload;
    private TextView gpsAltitudeDownload;
    private TextView gyroSpinrateDownload;
    private TextView temperatureDownload;
    private TextView cmdEchoDownload;
    private TextView accelerometerDownload;


    private  Button downloadCSV;
    private TextView boot,idle,launch,deploy,port,baud;

    private  TextView latitudeEnum,longitudeEnum;


    private Timer timer;
    private boolean timerStarted = false;



    //All controls end

 public Button res;
 public TextView pause;


    private Button mBtnClearInput;
    private ScrollView scrollView;
    private CheckBox chkScroll;
    private CheckBox chkReceiveText;
    private Button sendbtn;
    private EditText sentxt;
    public String restext = "PAUSE";

    private Handler handler = new Handler();
    private Runnable runnable;



    private boolean mIsBluetoothConnected = false;

    private BluetoothDevice mDevice;

    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_monitoring_screen);
        ActivityHelper.initialize(this);

        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        mDevice = b.getParcelable(MainActivity.DEVICE_EXTRA);
        mDeviceUUID = UUID.fromString(b.getString(MainActivity.DEVICE_UUID));
        mMaxChars = b.getInt(MainActivity.BUFFER_SIZE);
        Log.d(TAG, "Ready");




        //Controls Assign
        modeDownload = (TextView) findViewById(R.id.modeRecieve);
        altitudeDownload = (TextView) findViewById(R.id.altitudeRecieve);
        voltageDownload = (TextView) findViewById(R.id.voltageRecieve);
        softStatusDownload = (TextView) findViewById(R.id.softstatusRieceve);
        gpsLatitudeDownload = (TextView)findViewById(R.id.gpslatitudeReiceve);
        gpsLongitudeDownload = (TextView) findViewById(R.id.gpslongitudeRecieve);
        gpsStatsDownload = (TextView) findViewById(R.id.gpsstatsRecieve);
        gpsAltitudeDownload = (TextView) findViewById(R.id.gpsaltiutudeReiceve);
        gyroSpinrateDownload = (TextView) findViewById(R.id.gryoSpinRecieve);
        temperatureDownload = (TextView) findViewById(R.id.temperatureRecieve);
        cmdEchoDownload = (TextView) findViewById(R.id.commandRecieve);
        accelerometerDownload = (TextView) findViewById(R.id.accelerometerRecieve);

        CommandSend = (Button) findViewById(R.id.commandSend);
        boot = (TextView) findViewById(R.id.boot);
        launch = (TextView) findViewById(R.id.launch);
        deploy = (TextView) findViewById(R.id.deploy);
        idle = (TextView) findViewById(R.id.idle);

        latitudeEnum = (TextView) findViewById(R.id.latitudenum);
        longitudeEnum = (TextView) findViewById(R.id.longitudeEnum);

        downloadCSV = (Button) findViewById(R.id.DownloadCSV);
        port = (TextView) findViewById(R.id.portOfArd);
        baud = (TextView) findViewById(R.id.baud);
        //Controls Assign End

        //chart
        Chart = (Button) findViewById(R.id.charts);


        //Simulation
        simulation = (Button) findViewById(R.id.simulation);

        res = (Button) findViewById(R.id.buttonResume);
        pause = (TextView) findViewById(R.id.pasue);


        chkScroll = (CheckBox) findViewById(R.id.chkScroll);
        chkReceiveText = (CheckBox) findViewById(R.id.chkReceiveText);
        //scrollView = (ScrollView) findViewById(R.id.viewScroll);
        mBtnClearInput = (Button) findViewById(R.id.btnClearInput);
        sendbtn = (Button) findViewById(R.id.sendbutton);


        modeDownload.setMovementMethod(new ScrollingMovementMethod());







        CommandSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCommandDialog();
            }
        });

        simulation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("text/csv");
                startActivityForResult(intent, 1);
            }
        });


        downloadCSV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("text/csv");
                intent.putExtra(Intent.EXTRA_TITLE, "data.csv");
                startActivityForResult(intent, 1);
            }
        });




        sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                OutputStream outputStream = null;
                try {
                    if (ContextCompat.checkSelfPermission(MonitoringScreen.this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_DENIED)
                    {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                        {
                            ActivityCompat.requestPermissions(MonitoringScreen.this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 3);

                        }
                    }
                    outputStream = mBTSocket.getOutputStream();
                    outputStream.write("1".getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });




        long start_time = System.currentTimeMillis();
        long wait_time = 5000;
        long end_time = start_time + wait_time;


        mBtnClearInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runLoop();
            }
        });





        res.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent2 = getIntent();
                finish();
                startActivity(intent2);
            }
        });
    }

    private void showCommandDialog() {

        View dialogView = LayoutInflater.from(this).inflate(R.layout.comand_layout, null);

        // Find the GraphView element in the layout

        // Create and show the dialog box
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.show();
    }


    private class ReadCsvTask extends AsyncTask<Uri, Void, Void> {


        @Override
        protected Void doInBackground(Uri... uris) {
            Uri uri = uris[0];
            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;


                while ((line = reader.readLine()) != null) {
                    StringTokenizer tokenizer = new StringTokenizer(line, ",");
                    int arraySize = tokenizer.countTokens();
                    String[] outputArray = new String[arraySize];
                    for (int i = 0; i < arraySize; i++) {
                        outputArray[i] = tokenizer.nextToken();
                    }
                    // Display the values in outputArray
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            modeDownload.setText(outputArray[0].toString());
                            altitudeDownload.setText(outputArray[1].toString());
                            voltageDownload.setText(outputArray[2].toString());
                            softStatusDownload.setText(outputArray[3].toString());
                            gpsLatitudeDownload.setText(outputArray[4].toString());
                            gpsLongitudeDownload.setText(outputArray[5].toString());
                            gpsStatsDownload.setText(outputArray[6].toString());
                            gpsAltitudeDownload.setText(outputArray[7].toString());
                            gyroSpinrateDownload.setText(outputArray[8].toString());
                            temperatureDownload.setText(outputArray[9].toString());
                            cmdEchoDownload.setText(outputArray[10].toString());
                            accelerometerDownload.setText(outputArray[11].toString());
                            boot.setText(outputArray[12].toString());
                            idle.setText(outputArray[13].toString());
                            launch.setText(outputArray[14].toString());
                            deploy.setText(outputArray[15].toString());
                            port.setText(outputArray[16].toString());
                            baud.setText(outputArray[17].toString());


                            double[] dataArray = { Double.parseDouble(outputArray[8]), Double.parseDouble(outputArray[9]), Double.parseDouble(outputArray[5]) };
                            Chart.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    showCustomDialog(dataArray);
                                }
                            });

                        }
                    });
                    // Pause for 1 second
                    Thread.sleep(1000);

                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            // Do any cleanup or UI updates after the task completes
        }
    }

    // Call the AsyncTask from onActivityResult
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            new ReadCsvTask().execute(uri);
        }
    }




    private void showCustomDialog(double[] data) {
        // Inflate the dialog layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_box, null);

        // Find the GraphView element in the layout
        GraphView graphView = dialogView.findViewById(R.id.graph);

        // Create a LineGraphSeries to plot the data
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
        for (int i = 0; i < data.length; i++) {
            series.appendData(new DataPoint(i, data[i]), true, data.length);
        }

        // Customize the graph
        series.setColor(Color.BLUE);
        series.setThickness(4);

        // Add the series to the graph
        graphView.addSeries(series);

        // Create and show the dialog box
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.show();
    }

    private void runLoop() {
        long startTime = System.currentTimeMillis();

        while (true) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            if (elapsedTime >= 10 * 1000) {
                break;
            }
            // do some processing here
            modeDownload.setText("");
            altitudeDownload.setText("");
            voltageDownload.setText("");
            softStatusDownload.setText("");
            gpsLatitudeDownload.setText("");
            gpsLongitudeDownload.setText("");
            gpsStatsDownload.setText("");
            gpsAltitudeDownload.setText("");
            gyroSpinrateDownload.setText("");
            temperatureDownload.setText("");
            cmdEchoDownload.setText("");
            accelerometerDownload.setText("");
        }
    }


    private class ReadInput implements Runnable {
        private boolean bStop = false;
        private Thread t;
        public ReadInput() {
            t = new Thread(this, "Input Thread");
            t.start();
        }
        public boolean isRunning() {
            return t.isAlive();
        }
        @Override
        public void run() {
            InputStream inputStream;
            try {
                inputStream = mBTSocket.getInputStream();
                while (!bStop) {
                    byte[] buffer = new byte[256];
                    if (inputStream.available() > 0) {
                        inputStream.read(buffer);
                        int i = 0;
                        for (i = 0; i < buffer.length && buffer[i] != 0; i++) {
                        }
                        final String strInput = new String(buffer, 0, i);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                StringTokenizer tokenizer = new StringTokenizer(strInput, ",");

                                int arraySize = tokenizer.countTokens();
                                String[] outputArray = new String[arraySize];
                                for (int i = 0; i < arraySize; i++) {
                                    outputArray[i] = tokenizer.nextToken();
                                }
                                modeDownload.setText(outputArray[0].toString());
                                altitudeDownload.setText(outputArray[1].toString());
                                voltageDownload.setText(outputArray[2].toString());
                                softStatusDownload.setText(outputArray[3].toString());
                                gpsLatitudeDownload.setText(outputArray[4].toString());
                                gpsLongitudeDownload.setText(outputArray[5].toString());
                                gpsStatsDownload.setText(outputArray[6].toString());
                                gpsAltitudeDownload.setText(outputArray[7].toString());
                                gyroSpinrateDownload.setText(outputArray[8].toString());
                                temperatureDownload.setText(outputArray[9].toString());
                                cmdEchoDownload.setText(outputArray[10].toString());
                                accelerometerDownload.setText(outputArray[11].toString());
                            }
                        });
                    }
                    Thread.sleep(500);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        public void stop() {
            bStop = true;
        }
    }



    private class DisConnectBT extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... params) {

            if (mReadThread != null) {
                mReadThread.stop();
                while (mReadThread.isRunning())
                    ; // Wait until it stops
                mReadThread = null;

            }

            try {
                mBTSocket.close();
            } catch (IOException e) {
// TODO Auto-generated catch block
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            mIsBluetoothConnected = false;
            if (mIsUserInitiatedDisconnect) {
                finish();
            }
        }

    }

    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        if (mBTSocket != null && mIsBluetoothConnected) {
            new DisConnectBT().execute();
        }
        Log.d(TAG, "Paused");
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (mBTSocket == null || !mIsBluetoothConnected) {
            new ConnectBT().execute();
        }
        Log.d(TAG, "Resumed");
        super.onResume();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "Stopped");
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
// TODO Auto-generated method stub
        super.onSaveInstanceState(outState);
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void> {

        private boolean mConnectSuccessful = true;

        @Override
        protected void onPreExecute() {
            if (ContextCompat.checkSelfPermission(MonitoringScreen.this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_DENIED)
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                {
                    ActivityCompat.requestPermissions(MonitoringScreen.this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 3);

                }
            }
            //progressDialog = ProgressDialog.show(MonitoringScreen.this, "Hold on", "Connecting");// http://stackoverflow.com/a/11130220/1287554
        }

        @Override
        protected Void doInBackground(Void... devices) {

            try {
                if (mBTSocket == null || !mIsBluetoothConnected) {
                    if (ContextCompat.checkSelfPermission(MonitoringScreen.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED)
                    {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                        {
                            ActivityCompat.requestPermissions(MonitoringScreen.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);

                        }
                    }
                    mBTSocket = mDevice.createInsecureRfcommSocketToServiceRecord(mDeviceUUID);
                    if (ContextCompat.checkSelfPermission(MonitoringScreen.this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_DENIED)
                    {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                        {
                            ActivityCompat.requestPermissions(MonitoringScreen.this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 3);

                        }
                    }

                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    if (ContextCompat.checkSelfPermission(MonitoringScreen.this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_DENIED)
                    {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                        {
                            ActivityCompat.requestPermissions(MonitoringScreen.this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 3);

                        }
                    }
                    mBTSocket.connect();
                }
            } catch (IOException e) {
// Unable to connect to device
                e.printStackTrace();
                mConnectSuccessful = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (!mConnectSuccessful) {
                Toast.makeText(getApplicationContext(), "Could not connect to device. Is it a Serial device? Also check if the UUID is correct in the settings", Toast.LENGTH_LONG).show();
                finish();
            } else {
                msg("Connected to device");
                mIsBluetoothConnected = true;
                mReadThread = new ReadInput(); // Kick off input reader
            }

           // progressDialog.dismiss();
        }

    }



}
