package edu.temple.ollycontroller;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public class BoardControls extends AppCompatActivity {

    Button btnOn, btnOff, btnDis, btnStart, btnStop;
    SeekBar brightness;
    TextView lumn;
    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public static String EXTRA_ADDRESS = "device_address";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_controls);

        Intent newint = getIntent();
        address = newint.getStringExtra(MainActivity.EXTRA_ADDRESS); //receive the address of the bluetooth device


        String message = null;

        //call the widgtes
        btnOn = (Button)findViewById(R.id.onButton);
        btnOff = (Button)findViewById(R.id.offButton);
        btnDis = (Button)findViewById(R.id.button4);
        brightness = (SeekBar)findViewById(R.id.seekBar);
        lumn = (TextView)findViewById(R.id.lumn);
        btnStart = (Button)findViewById(R.id.startButton);
        btnStop = (Button)findViewById(R.id.stopButton);


        new ConnectBT().execute(); //Call the class to connect

        try{
            byte [] bytes_from_arduino = new byte[64];
            btSocket.getInputStream().read(bytes_from_arduino);
            message = bytes_from_arduino.toString();

        }
        catch (Exception e){
            //nah nothing will go wrong.......
        }

        if(message == "on"){

        }
        else if(message == "off"){

        }
        else if(message == "start"){

        }
        else if(message == "stop"){

        }
        else{
            Toast.makeText(this, "The message variable = " + message, Toast.LENGTH_LONG).show();
        }

        //------------------------------------------------------------------------------------------------------------------------------
        //commands to be sent to bluetooth

        //------------------turn on board------------------
        btnOn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                turnOnBoard();      //method to turn on
            }
        });

        //------------------turn off board------------------
        btnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                turnOffBoard();   //method to turn off
            }
        });

        //------------------start movement------------------
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                startBoard();   //method to start board
            }
        });

        //------------------stop movement------------------
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
               stopBoard();   //method to stop board
            }
        });


        btnDis.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Disconnect(); //close connection
            }
        });

        brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser==true)
                {
                    lumn.setText(String.valueOf(progress));
                    try
                    {
                        btSocket.getOutputStream().write(String.valueOf(progress).getBytes());
                    }
                    catch (IOException e)
                    {

                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }



    private void Disconnect()
    {
        if (btSocket!=null) //If the btSocket is busy
        {
            try
            {
                btSocket.close(); //close connection
            }
            catch (IOException e)
            { msg("Error");}
        }
        finish(); //return to the first layout

    }

    private void turnOffBoard()
    {
        if (btSocket!=null)
        {
            try
            {
                String message = "off";
                btSocket.getOutputStream().write(message.getBytes());

                finish();
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

    private void turnOnBoard()
    {
        if (btSocket!=null)
        {
            try
            {
                String message = "on";
                btSocket.getOutputStream().write(message.getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }


    private void startBoard(){
        if (btSocket!=null)
        {
            try
            {
                String message = "start";
                btSocket.getOutputStream().write(message.getBytes());
                message = "on";
                btSocket.getOutputStream().write(message.getBytes());


                //change activity to DriveMode
               // Intent i = new Intent(BoardControls.this, DriveMode.class);
                // i.putExtra(EXTRA_ADDRESS, address); //this will be received at ledControl (class) Activity
                // startActivity(i);

            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }


    private void stopBoard(){
        if (btSocket!=null)
        {
            try
            {
                String message = "stop";
                btSocket.getOutputStream().write(message.getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }



    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(BoardControls.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            }
            else
            {
                msg("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }

}
