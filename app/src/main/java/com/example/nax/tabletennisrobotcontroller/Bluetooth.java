package com.example.nax.tabletennisrobotcontroller;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

public class Bluetooth extends AppCompatActivity {
    protected ListView deviceList;
    protected Button permissionsBtn, scanBtn, bondedBtn;
    protected Set<BluetoothDevice> pairedDevices;
    protected BluetoothAdapter btAdapter;
    protected ArrayList<String> najdeneZariadeniaMena = new ArrayList<>();
    protected ArrayList<BluetoothDevice> najdeneZariadenia = new ArrayList<>();
    protected ArrayList<String> parovaneZariadenia = new ArrayList<>();
    protected ProgressDialog progressDialog;
    protected BluetoothDevice btDevice;
    protected String address;
    protected boolean isPaired = false;

    /**
     * BroadcastReceiver na hľadanie Bluetooth zariadení
     */
    final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            /**
             * Inicializácia ProgressDialogu pri začatí vyhľadávania BT zariadení
             */
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
                najdeneZariadenia.clear();
                najdeneZariadeniaMena.clear();
                progressDialog = new ProgressDialog(Bluetooth.this);
                progressDialog.setMessage("Hľadanie zariadení...");
                progressDialog.setTitle("Bluetooth zariadenia");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.show();
                progressDialog.setCancelable(true);
                progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        btAdapter.cancelDiscovery();
                    }
                });
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(17000);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        progressDialog.cancel();
                    }
                }).start();
            }

            /**
             * Ak sa nájde BT zariadenie, vypíše sa jeho meno a pridá sa do ArrayListu
             */
            if (BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (!najdeneZariadeniaMena.contains(device.getAddress())){
                    Toast.makeText(getApplicationContext(),"Nájdené zariadenie " + device.getName(), Toast.LENGTH_SHORT).show();
                    najdeneZariadenia.add(device);
                    najdeneZariadeniaMena.add("Name:     " + device.getName() + "\n"+ "Address: " + device.getAddress());
                }
            }

            /**
             * Ak sa nenájde žiadne BT zariadenie
             */
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                if (najdeneZariadenia.size() < 1){
                    toast("Zariadenie nenájdené");
                }
            }
            final ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(Bluetooth.this, android.R.layout.simple_list_item_1, najdeneZariadeniaMena){

                @Override
                @NonNull
                public View getView(int position, View convertView, @NonNull ViewGroup parent){
                    View view1 = super.getView(position, convertView, parent);
                    TextView textView = view1.findViewById(android.R.id.text1);
                    textView.setTextColor(Color.parseColor("#FFCFD8DC"));
                    return view1;
                }
            };
            deviceList.setAdapter(adapter2);
        }
    };

    /**
     * BroadcastReceiver na sledovaie zmeny stavu Bluetoothu, ak sa vypné, dlačidlá sú neaktívne
     */
    final BroadcastReceiver receiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                if (state == BluetoothAdapter.STATE_ON){
                    bondedBtn.setEnabled(true);
                    scanBtn.setEnabled(true);
                }else {
                    bondedBtn.setEnabled(false);
                    scanBtn.setEnabled(false);
                }
            }
        }
    };

    /**
     * BroadcastReceiver na sledovanie párovania Bluetooth zariadení a následné spustenie aktivity
     * s ovládaním
     */
    final BroadcastReceiver receiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)){
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING){
                    toast("Spárované");
                    Intent ovladanie = new Intent(Bluetooth.this, Controller.class);
                    ovladanie.putExtra("device", btDevice);
                    ovladanie.putExtra("address", address);
                    startActivity(ovladanie);
                }
                else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){
                    toast("Odpárované");
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        deviceList = findViewById(R.id.deviceList);
        permissionsBtn = findViewById(R.id.permissionsBtn);
        scanBtn = findViewById(R.id.scanBtn);
        bondedBtn = findViewById(R.id.bondedBtn);
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        /**
         * Výber BT zariadenia zo zoznamu nájdených zariadení, ak nie je párované, pokúsi sa
         * spárovať, ak je, inicializuje sa aktivita Controller
         */
        deviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                pairedDevices = btAdapter.getBondedDevices();

                if (!isPaired) {
                    btDevice = najdeneZariadenia.get(i);
                    address = btDevice.getAddress();
                    if (!pairedDevices.contains(btDevice)) {
                        pairDevices(btDevice);
                    }
                    else {
                        String info = ((TextView) view).getText().toString();
                        address = info.substring(info.length() -17);
                        Intent ovladanie = new Intent(Bluetooth.this, Controller.class);
                        ovladanie.putExtra("address", address);
                        startActivity(ovladanie);
                    }
                }
                else {
                    String info = ((TextView) view).getText().toString();
                    address = info.substring(info.length() -17);
                    Intent ovladanie = new Intent(Bluetooth.this, Controller.class);
                    ovladanie.putExtra("address", address);
                    startActivity(ovladanie);
                }
            }
        });


    }

    /**
     * Odregistrovanie BroadcastReceiverov pri zničení aktivity a zrušenie vyhľadávania
     */
    @Override
    protected void onStop(){
        super.onStop();

        btAdapter.cancelDiscovery();

        if (receiver.isOrderedBroadcast()){
        unregisterReceiver(receiver);
        }
        if (receiver2.isOrderedBroadcast()){
        unregisterReceiver(receiver2);
        }
        if (receiver3.isOrderedBroadcast()){
            unregisterReceiver(receiver3);
        }
    }

    /**
     * Vyhľadávanie nových Bluetooth zariadení
     */
    public void scan(View view){
        isPaired = false;
        deviceList.setAdapter(null);
        najdeneZariadenia.clear();
        najdeneZariadeniaMena.clear();
        btAdapter.startDiscovery();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver, filter);
    }

    /**
     * Párovanie zariadení
     */
    public void pairDevices(BluetoothDevice device){
        IntentFilter parovanie = new IntentFilter();
        parovanie.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(receiver3, parovanie);

        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Výpis párovaných zariadení
     */
    public void bonded(View view){
        isPaired = true;
        deviceList.setAdapter(null);
        parovaneZariadenia.clear();
        toast("Párované zariadenia");
        pairedDevices = btAdapter.getBondedDevices();
        for(BluetoothDevice bt : pairedDevices){
            parovaneZariadenia.add("Name:     " + bt.getName() + "\n" + "Address: " + bt.getAddress());
        }
        if (parovaneZariadenia.isEmpty()){
            toast("Žiadne párované zariadenia");
        }
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,parovaneZariadenia){

            @Override
            @NonNull
            public View getView(int position, View convertView, @NonNull ViewGroup parent){
                View view1 = super.getView(position, convertView, parent);
                TextView textView = view1.findViewById(android.R.id.text1);
                textView.setTextColor(Color.parseColor("#FFCFD8DC"));
                return view1;
            }
        };
        deviceList.setAdapter(adapter);
    }

    /**
     * Toast
     */
    protected void toast(String text){
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    /**
     * Kontorola potrebných povolení
     */
    public void permissions(View view){
        if (ContextCompat.checkSelfPermission(Bluetooth.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(Bluetooth.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},0);
        }
        else{
            if (!btAdapter.isEnabled()){
                Intent zapnutie = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(zapnutie);
                toast("Zapnite Bluetooth");
                IntentFilter zmena = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                registerReceiver(receiver2, zmena);
            }
            else{
                bondedBtn.setEnabled(true);
                scanBtn.setEnabled(true);
            }
        }
    }

    /**
     * Kontrola zmeny povolení
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults){
        switch (requestCode){
            case 0: if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                        if (btAdapter.isEnabled()){
                            bondedBtn.setEnabled(true);
                            scanBtn.setEnabled(true);
                        }else{
                            Intent zapnutie = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivity(zapnutie);
                            IntentFilter zmena = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                            registerReceiver(receiver2, zmena);
                        }
                    }else {
                        toast("Povoľte vyhľadávanie polohy");
                    }
                    break;
        }
    }

    /**
     * Vytvorenie horného menu pre zmenu aktivity a ukončenie aplikácie
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Výber položky z menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            //prepnutie sa do aktivity Controller
            case R.id.controller:
                Intent intent = new Intent(Bluetooth.this, Controller.class);
                startActivity(intent);
                return true;
            //ukončenie aplikácie
            case R.id.exit:
                System.exit(0);
            default: return super.onOptionsItemSelected(item);
        }
    }
}
