package com.example.nax.tabletennisrobotcontroller;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.util.UUID;

public class Controller extends AppCompatActivity {
    private RadioGroup radioGroup1, radioGroup2, radioGroup3, rGZadavanie;
    private SeekBar seekBar1, seekBar2, seekBar3, seekBar4, seekBar5;
    private RadioButton radioButton1, radioButton2, radioButton3, radioButton4, radioButton5, radioButton6, radioButton7, radioButton8, radioButton9, rBMan, rBAut;
    private TextView hodnotaSB1, hodnotaSB2, hodnotaSB3, hodnotaSB4;
    private ScrollView scrollView;
    private BluetoothSocket btSocket = null;
    private String adresa = null;
    private static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    /**
     * Sledovanie stavu Bluetoothu, ak sa vypne, vráti sa do aktivity s úvodným pripájaním
     */
    final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                if (state == BluetoothAdapter.STATE_OFF){
                    toast("Bluetooth vypnutý");
                    finish();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        seekBar1 = findViewById(R.id.seekBar1);
        seekBar2 = findViewById(R.id.seekBar2);
        seekBar3 = findViewById(R.id.seekBar3);
        seekBar4 = findViewById(R.id.seekBar4);
        seekBar5 = findViewById(R.id.seekBar5);
        radioGroup1 = findViewById(R.id.radioGroup1);
        radioGroup2 = findViewById(R.id.radioGroup2);
        radioGroup3 = findViewById(R.id.radioGroup3);
        rGZadavanie = findViewById(R.id.rGZadavanie);
        radioButton1 = findViewById(R.id.radioButton1);
        radioButton2 = findViewById(R.id.radioButton2);
        radioButton3 = findViewById(R.id.radioButton3);
        radioButton4 = findViewById(R.id.radioButton4);
        radioButton5 = findViewById(R.id.radioButton5);
        radioButton6 = findViewById(R.id.radioButton6);
        radioButton7 = findViewById(R.id.radioButton7);
        radioButton8 = findViewById(R.id.radioButton8);
        radioButton9 = findViewById(R.id.radioButton9);
        hodnotaSB1 = findViewById(R.id.hodnotaSB1);
        hodnotaSB2 = findViewById(R.id.hodnotaSB2);
        hodnotaSB3 = findViewById(R.id.hodnotaSB3);
        hodnotaSB4 = findViewById(R.id.hodnotaSB4);
        rBMan = findViewById(R.id.rBMan);
        rBAut = findViewById(R.id.rBAut);
        Button cancel = findViewById(R.id.cancel);
        Button disconnect = findViewById(R.id.disconnect);
        scrollView = findViewById(R.id.scrollView);

        /**
         * Získanie adresy BT zariadenia na ktoré sa chceme pripojiť
         */
        adresa = getIntent().getStringExtra("address");
        if (adresa != null) {
            new ConnectBT().execute();
        }

        /**
         * Zaregistrovanie BroadcastReceivera pre sledovanie zmeny BT
         */
        IntentFilter btOdpojenie = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(receiver, btOdpojenie);

        fun4();

        /**
         * Nastavenie počiatočnej hodnoty SeekBarom
         */
        hodnotaSB1.setText(String.valueOf(seekBar1.getProgress()));
        hodnotaSB2.setText(String.valueOf(seekBar2.getProgress()));
        hodnotaSB3.setText(String.valueOf(seekBar3.getProgress()));
        hodnotaSB4.setText(String.valueOf(seekBar4.getProgress()));

        /**
         * Počiatočné vypnutie SeekBarov a RadioButtonov
         */
        vypnutieSB();
        vypnutieRB();

        /**
         * Manuálny režim
         */
        rBMan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    vypnutieRB();
                    zapnutieSB();
                    output("8888");

                    scrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            scrollView.scrollTo(0, 0);
                        }
                    });
                }
            }
        });

        /**
         * Prednastavený režim
         */
        rBAut.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    vypnutieSB();
                    zapnutieRB();
                    output("7777");

                    scrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            scrollView.scrollTo(0, scrollView.getBottom());
                        }
                    });
                }
            }
        });

        /**
         * Zmeny stavov RadioGroup 1-3
         */
        radioGroup1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                if(i != -1){
                    fun2();
                }
            }
        });

        radioGroup2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                if(i != -1){
                    fun1();
                }
            }
        });

        radioGroup3.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                if(i != -1){
                    fun3();
                }
            }
        });

        /**
         * Zmena stavu SeekBarov 1-5 na nastavovanie motorov
         * */
        seekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                hodnotaSB1.setText(String.valueOf(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (seekBar.getProgress()==0){
                    output("10");
                }
                else {
                    output("1" + (Integer.parseInt(hodnotaSB1.getText().toString()) + 50));
                    //toast(String.valueOf(Integer.parseInt(hodnotaSB1.getText().toString())+50));
                }
            }
        });

        seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                hodnotaSB2.setText(String.valueOf(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (seekBar.getProgress()==0){
                    output("20");
                }
                else {
                    output("2" + (Integer.parseInt(hodnotaSB2.getText().toString()) + 50));
                    //toast(String.valueOf(Integer.parseInt(hodnotaSB2.getText().toString())+50));
                }
            }
        });

        seekBar3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                hodnotaSB3.setText(String.valueOf(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (seekBar.getProgress()==0){
                    output("30");
                }
                else {
                    output("3" + (Integer.parseInt(hodnotaSB3.getText().toString()) + 50));
                    //toast(String.valueOf(Integer.parseInt(hodnotaSB3.getText().toString())+50));
                }
            }
        });

        seekBar4.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                hodnotaSB4.setText(String.valueOf(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (seekBar.getProgress()==0){
                    output("40");
                }
                else {
                    output("4" + (Integer.parseInt(hodnotaSB4.getText().toString()) + 60));
                    //toast(String.valueOf(Integer.parseInt(hodnotaSB4.getText().toString())+60));
                }
            }
        });

        seekBar5.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                output("5" + seekBar.getProgress());
            }
        });

        /**
         * Nastavovanie hodnôt motorom pomocou prednastavebých RadioButtonov 1-9
         */
        radioButton1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    output("1");
                }
            }
        });

        radioButton2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    output("2");
                }
            }
        });

        radioButton3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    output("3");
                }
            }
        });

        radioButton4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    output("4");
                }
            }
        });

        radioButton5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    output("5");
                }
            }
        });

        radioButton6.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    output("6");
                }
            }
        });

       radioButton7.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
           @Override
           public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
               if (b){
                   output("7");
               }
           }
       });

        radioButton8.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    output("8");
                }
            }
        });

        radioButton9.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    output("9");
                }
            }
        });

        /**
         * Tlačidlo na vypnutie motorov
         */
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });

        /**
         * Odpojenie od Bluetoothu a vrátenie sa na aktivitu Bluetooth
         */
        disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                disconnect();
                toast("Odpojené");
                finish();
            }
        });
    }

    /**
     * Odpojenie sa od BT zariadenia pri zničení aktivity
     */
    public void onDestroy(){
        super.onDestroy();
        disconnect();
    }

    /**
     * Metódy pre vypínanie ActionListenerov a ich opätovné nastavovanie, týmto sme vyriešili
     * problém pri ktorom nešlo umiestniť RadioButtony do štvorca 3x3 (fun1, fun2, fun3)
     * */
    protected void fun1(){
        radioGroup1.setOnCheckedChangeListener(null);
        radioGroup3.setOnCheckedChangeListener(null);
        radioGroup1.clearCheck();
        radioGroup3.clearCheck();
        radioGroup1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                fun2();
            }
        });
        radioGroup3.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                fun3();
            }
        });
    }

    protected void fun2(){
        radioGroup2.setOnCheckedChangeListener(null);
        radioGroup3.setOnCheckedChangeListener(null);
        radioGroup2.clearCheck();
        radioGroup3.clearCheck();
        radioGroup2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                fun1();
            }
        });
        radioGroup3.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                fun3();
            }
        });
    }

    protected  void fun3(){
        radioGroup1.setOnCheckedChangeListener(null);
        radioGroup2.setOnCheckedChangeListener(null);
        radioGroup1.clearCheck();
        radioGroup2.clearCheck();
        radioGroup1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                fun2();
            }
        });
        radioGroup2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                fun1();
            }
        });
    }

    /**
     * fun4 odškrtáva zaškrtnuté RadioButtony
     */
    protected void  fun4(){
        radioGroup1.clearCheck();
        radioGroup2.clearCheck();
        radioGroup3.clearCheck();
    }

    /**
     * Metóda, ktorá vypína zapnuté SeekBary
     */
    protected  void vypnutieSB(){
        seekBar1.setEnabled(false);
        seekBar2.setEnabled(false);
        seekBar3.setEnabled(false);
        seekBar4.setEnabled(false);
        seekBar5.setEnabled(false);
    }

    /**
     * Metóda, ktorá zapína vypnuté SeekBary
     */
    protected void zapnutieSB(){
        seekBar1.setEnabled(true);
        seekBar2.setEnabled(true);
        seekBar3.setEnabled(true);
        seekBar4.setEnabled(true);
        seekBar5.setEnabled(true);
    }

    /**
     * Metóda, ktorá vypína zapnuté RadioButtony
     */
    protected void  vypnutieRB(){
        radioButton1.setEnabled(false);
        radioButton2.setEnabled(false);
        radioButton3.setEnabled(false);
        radioButton4.setEnabled(false);
        radioButton5.setEnabled(false);
        radioButton6.setEnabled(false);
        radioButton7.setEnabled(false);
        radioButton8.setEnabled(false);
        radioButton9.setEnabled(false);
    }

    /**
     * Metóda, ktorá zapína vypnuté RadioButtony
     */
    protected void  zapnutieRB(){
        radioButton1.setEnabled(true);
        radioButton2.setEnabled(true);
        radioButton3.setEnabled(true);
        radioButton4.setEnabled(true);
        radioButton5.setEnabled(true);
        radioButton6.setEnabled(true);
        radioButton7.setEnabled(true);
        radioButton8.setEnabled(true);
        radioButton9.setEnabled(true);
    }

    /**
     * Toast
     */
    protected void toast(String text){
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    /**
     * Vnorená trieda, ktorá má za úlohu pripojiť sa na potrebné Bluetooth zariadenie a vytvoriť
     * ProgressDialog počas trvania pripájania
     */
    private class ConnectBT extends AsyncTask<Void, Void, Void>{
        private boolean connectSucces = true;
        private ProgressDialog progress;
        private BluetoothAdapter btAdapter = null;
        private boolean isBtConnected = false;

        //Inicializácia ProgressBaru
        @Override
        protected void onPreExecute(){
            progress = new ProgressDialog(Controller.this);
            progress.setTitle("Pripájanie...");
            progress.setMessage("Pripájam");
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.show();
            progress.setCancelable(false);
        }

        //Vytvorenie spojenia medzi zariadeniami
        @Override
        protected Void doInBackground(Void... devices){
            try {

                if (btSocket == null || !isBtConnected){
                    btAdapter = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice device = btAdapter.getRemoteDevice(adresa);
                    btSocket = device.createInsecureRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();
                }
            }catch (IOException e){
                connectSucces = false;
            }
            return null;
        }

        //Zrušenie ProgressBaru
        @Override
        protected void onPostExecute(Void result){
            super.onPostExecute(result);
            if (!connectSucces){
                toast("Pripojenie zlyhalo");
                finish();
            }
            else {
                toast("Pripojené");
            }
            progress.dismiss();
        }
    }

    /**
     * Metóda na vypnutie motorov
     */
    public void cancel(){
        output("9999");
        rGZadavanie.clearCheck();
        fun4();
        vypnutieRB();
        vypnutieSB();
        toast("Vypnuté");
    }

    /**
     * Metóda na odpojenie sa od BT zariadenia
     */
    public void disconnect(){
        if (btSocket != null){
            try {
                btSocket.close();
            }
            catch (IOException e){
                toast("Odpojenie zlyhalo");
            }
        }
    }

    /**
     * Odosielanie požadovaných hodnôt Arduinu cez Bluetooth
     */
    public void output(String cislo){
        byte[] hodnota = cislo.getBytes();
        //toast(cislo);

        if (btSocket != null) {
            try {
                btSocket.getOutputStream().write(hodnota);
            } catch (IOException e) {
                toast("Chyba odosielania");
            }
        }
    }
}