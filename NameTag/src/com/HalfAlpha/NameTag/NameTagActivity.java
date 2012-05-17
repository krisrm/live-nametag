package com.HalfAlpha.NameTag;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ScrollView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class NameTagActivity extends Activity {
	private static final int SEC_LENGTH_MS = 1000;
	private static final int MIN_LENGTH_MS = 60*SEC_LENGTH_MS;
	public static final String T = "NAMETAG";
	protected static final int ERR_REFRESH = 0;
	private static final int ERR_BT = 1;
	
	private Handler h = new Handler();
	protected GDGTInfo currentInfo;
	private EditText cycle;
	private EditText autoRefreshInterval;
	private TextView gdgtData;
	private EditText gdgtUser;
	private CheckBox autoRefresh;
	private ScrollView mainScrollView;
	
	BluetoothAdapter mBluetoothAdapter;
	BluetoothDevice mmDevice;
	BluetoothSocket mmSocket;
	OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    int counter;
    volatile boolean stopWorker;
    EditText myTextbox;


	
	private Runnable displayValues = new Runnable() {

		@Override
		public void run() {
			//call Arduino with data
			try {
				sendData();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			cycleValues();
		}
	};
	private Runnable refreshData = new Runnable(){
		@Override
		public void run() {
			try {
				gdgtData.setText("Loading...");
				// Perform action on click
				String user = gdgtUser.getText().toString().trim();
				if (user.isEmpty()){
					gdgtUser.setText("tgd");
				}
				if (currentInfo == null || !currentInfo.username.equals(user)){
					currentInfo = new GDGTInfo(user);
				}
				mainScrollView.scrollTo(0, mainScrollView.getHeight());
				currentInfo.fillFromNetwork(dataRefreshed);
			} catch (Exception e) {
				Log.e(T, e.toString());
				showDialog(ERR_REFRESH);
			}
		}
	};
	private Runnable dataRefreshed = new Runnable() {
		@Override
		public void run() {
			Log.v(T, "Refreshed user");
			if (currentInfo.error != null) {
				showDialog(ERR_REFRESH);
				return;
			}
			gdgtData.setText(currentInfo.toString());
			if (autoRefresh.isChecked()){
				h.removeCallbacks(refreshData);
				autoRefresh();
			}
			h.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					mainScrollView.scrollTo(0, mainScrollView.getHeight());
				}
			}, 30);
			displayValues.run();
		}
	};
	


		@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		gdgtUser = (EditText) findViewById(R.id.gdgtUser);
		gdgtData = (TextView) findViewById(R.id.gdgtData);
		final Button refreshButton = (Button) findViewById(R.id.refreshButton);
		autoRefresh = (CheckBox) findViewById(R.id.autoRefresh);
		autoRefreshInterval = (EditText) findViewById(R.id.autoRefreshInterval);
		cycle = (EditText) findViewById(R.id.cycleInterval);
		mainScrollView = (ScrollView) findViewById(R.id.mainScroll);
		findBT();
		try {
			openBT();
		} catch (IOException e) {
			e.printStackTrace();
		}

		refreshButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				
				refreshData.run();
			}
		});
		cycle.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}

			@Override
			public void afterTextChanged(Editable s) {
				h.removeCallbacks(displayValues);
				cycleValues();
			}
		});
		autoRefreshInterval.setEnabled(autoRefresh.isChecked());
		autoRefresh.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				autoRefreshInterval.setEnabled(isChecked);
				if (isChecked){
					autoRefresh();
				} else {
					h.removeCallbacks(refreshData);
				}
			}
		});
		cycleValues();
	}

	private void cycleValues() {
		String timeText = cycle.getText().toString();
		int time = 0;
		try {
			time = Integer.parseInt(timeText);
			time = time > 100 ? 100 : time;
		} catch (Exception e) {
			time = 0;
		}
		if (time > 0) {
			h.postDelayed(displayValues, time * SEC_LENGTH_MS);
		}
	}
	
	private void autoRefresh(){
		String timeText = autoRefreshInterval.getText().toString();
		int time = 0;
		try {
			time = Integer.parseInt(timeText);
			time = time > 100 ? 100 : time;
		} catch (Exception e) {
			time = 0;
		}
		if (time > 0) {
			h.postDelayed(refreshData, time * MIN_LENGTH_MS);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		switch (id) {
		case ERR_REFRESH:
			dialog = errorDialog("Could not refresh the stats for the given user!");
			break;
		case ERR_BT:
			dialog = errorDialog("Bluetooth failed in an unexpected way!");
			break;
		}
		return dialog;
	}

	private Dialog errorDialog(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message).setCancelable(true)
				.setTitle("Unexpected Error");

		return builder.create();
	}
	
	public void finishDialogNoBluetooth() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.alert_dialog_no_bt)
        .setIcon(android.R.drawable.ic_dialog_info)
        .setTitle(R.string.app_name)
        .setCancelable( false )
        .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       finish();            	
                	   }
               });
        AlertDialog alert = builder.create();
        alert.show(); 
    }
	
	void findBT()
    {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null)
        {
        	showDialog(ERR_BT);
        }

        if(!mBluetoothAdapter.isEnabled())
        {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0)
        {
            for(BluetoothDevice device : pairedDevices)
            {
            	Log.d(T,device.getName());
                if(device.getName().equals("X10")) 
                {
                    mmDevice = device;
                    break;
                }
            }
        }
    }
	
	void openBT() throws IOException
    {
		if (mmDevice == null){
			showDialog(ERR_BT);
			return;
		}
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard //SerialPortService ID
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);        
        mmSocket.connect();
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();

//        beginListenForData();

    }

//    void beginListenForData()
//    {
//        final Handler handler = new Handler(); 
//        final byte delimiter = 10; //This is the ASCII code for a newline character
//
//        stopWorker = false;
//        readBufferPosition = 0;
//        readBuffer = new byte[1024];
//        workerThread = new Thread(new Runnable()
//        {
//            public void run()
//            {                
//               while(!Thread.currentThread().isInterrupted() && !stopWorker)
//               {
//                    try 
//                    {
//                        int bytesAvailable = mmInputStream.available();                        
//                        if(bytesAvailable > 0)
//                        {
//                            byte[] packetBytes = new byte[bytesAvailable];
//                            mmInputStream.read(packetBytes);
//                            for(int i=0;i<bytesAvailable;i++)
//                            {
//                                byte b = packetBytes[i];
//                                if(b == delimiter)
//                                {
//                                    byte[] encodedBytes = new byte[readBufferPosition];
//                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
//                                    final String data = new String(encodedBytes, "US-ASCII");
//                                    readBufferPosition = 0;
//
//                                    handler.post(new Runnable()
//                                    {
//                                        public void run()
//                                        {
//                                            myLabel.setText(data);
//                                        }
//                                    });
//                                }
//                                else
//                                {
//                                    readBuffer[readBufferPosition++] = b;
//                                }
//                            }
//                        }
//                    } 
//                    catch (IOException ex) 
//                    {
//                        stopWorker = true;
//                    }
//               }
//            }
//        });
//
//        workerThread.start();
//    }

    void sendData() throws IOException
    {
    	if (currentInfo == null){
    		return;
    	}
        String msg = currentInfo.toString();
        msg += "\n";
        mmOutputStream.write(msg.getBytes());
        //mmOutputStream.write('A');
    }

    void closeBT() throws IOException
    {
        stopWorker = true;
        mmOutputStream.close();
        mmInputStream.close();
        mmSocket.close();
    }

	@Override
	public boolean isFinishing() {
		
		
		return super.isFinishing();
	}
}