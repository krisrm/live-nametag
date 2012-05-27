package com.HalfAlpha.NameTag;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class NameTagActivity extends Activity {

	private Handler h = new Handler(new Callback() {
		
		@Override
		public boolean handleMessage(Message msg) {
			Log.v(C.T, msg.toString());
			switch (msg.what){
			case C.MESSAGE_STATE_CHANGE:
                switch (msg.arg1) {
                case BluetoothSerialService.STATE_CONNECTED:
                	Log.d(C.T, "Connected");
                    break;
                    
                case BluetoothSerialService.STATE_CONNECTING:
                	Log.d(C.T, "Connecting");
                   
                    break;
                    
                case BluetoothSerialService.STATE_LISTEN:
                case BluetoothSerialService.STATE_NONE:
                	Log.d(C.T, "Listen/None");

                    break;
                }
                break;
			case C.MESSAGE_DEVICE_NAME:
				Log.d(C.T, "Got Name");
				btConnected();
				break;
			case C.MESSAGE_TOAST:
				Toast.makeText(NameTagActivity.this, msg.getData().getString(C.TOAST), Toast.LENGTH_LONG);
				Log.d(C.T, msg.getData().getString(C.TOAST));
				break;
			
			}
			return false;
		}
	});
	protected GDGTInfo currentInfo;
	private EditText cycle;
	private EditText autoRefreshInterval;
	private TextView gdgtData;
	private EditText gdgtUser;
	private CheckBox autoRefresh;
	private ScrollView mainScrollView;
	private BluetoothSerialService bt;

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

		bt = new BluetoothSerialService(this, h);
		
		setupBT();
		
		
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
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				autoRefreshInterval.setEnabled(isChecked);
				if (isChecked) {
					autoRefresh();
				} else {
					h.removeCallbacks(refreshData);
				}
			}
		});
		
	
	}

	private void setupBT() {
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		BluetoothDevice mBluetoothDevice = null;
		if (!mBluetoothAdapter.isEnabled()) {
		    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    startActivityForResult(enableBtIntent, C.REQUEST_ENABLE_BT);
		}
		
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		if (pairedDevices.size() > 0) {
		    for (BluetoothDevice device : pairedDevices) {
		        if (C.DEVICE_NAME.equals(device.getName())){
		        	mBluetoothDevice = device;
		        }
		    }
		}
		if (mBluetoothDevice == null){
			showDialog(C.ERR_BT);
			return;
		}
		
		bt.connect(mBluetoothDevice);
		
	}
	
	protected void btConnected() {
		bt.write(currentInfo.toLcdString(1));
		cycleValues();
	}

	private Runnable displayValues = new Runnable() {

		@Override
		public void run() {
			// call Arduino with data 
			cycleValues();
		}
	};
	private Runnable refreshData = new Runnable() {
		@Override
		public void run() {
			try {
				gdgtData.setText("Loading...");
				// Perform action on click
				String user = gdgtUser.getText().toString().trim();
				if (user.isEmpty()) {
					gdgtUser.setText("tgd");
				}
				if (currentInfo == null || !currentInfo.username.equals(user)) {
					currentInfo = new GDGTInfo(user);
				}
				mainScrollView.scrollTo(0, mainScrollView.getHeight());
				currentInfo.fillFromNetwork(dataRefreshed);
			} catch (Exception e) {
				Log.e(C.T, e.toString());
				showDialog(C.ERR_REFRESH);
			}
		}
	};
	private Runnable dataRefreshed = new Runnable() {
		@Override
		public void run() {
			Log.v(C.T, "Refreshed user");
			if (currentInfo.error != null) {
				showDialog(C.ERR_REFRESH);
				return;
			}
			gdgtData.setText(currentInfo.toString());
			if (autoRefresh.isChecked()) {
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
			h.postDelayed(displayValues, time * C.SEC_LENGTH_MS);
		}
	}

	private void autoRefresh() {
		String timeText = autoRefreshInterval.getText().toString();
		int time = 0;
		try {
			time = Integer.parseInt(timeText);
			time = time > 100 ? 100 : time;
		} catch (Exception e) {
			time = 0;
		}
		if (time > 0) {
			h.postDelayed(refreshData, time * C.MIN_LENGTH_MS);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		switch (id) {
		case C.ERR_REFRESH:
			dialog = errorDialog("Could not refresh the stats for the given user!");
			break;
		case C.ERR_BT:
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
	
//	@Override
//	protected void onResume() {
//		if (bt != null)
//			bt.start();
//		super.onResume();
//	}
//
//	@Override
//	public boolean isFinishing() {
//		if (bt != null)
//			bt.stop();
//		return super.isFinishing();
//	}
}