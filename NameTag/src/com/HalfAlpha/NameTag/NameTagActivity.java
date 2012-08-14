package com.HalfAlpha.NameTag;

import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class NameTagActivity extends Activity {

	
	protected GDGTInfo currentInfo;
	private EditText cycle;
	private EditText autoRefreshInterval;
	private TextView gdgtData;
	private EditText gdgtUser;
	private CheckBox autoRefresh;
	private ScrollView mainScrollView;
	private int currentDisplayed = 4;
	private String currentOutput = "";
	private Spinner currentDisplaySpinner;
	private SeekBar brightnessSeekbar;
	private EditText customMessage;
	private NameTag device;
	private Handler h = new Handler();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		gdgtUser = (EditText) findViewById(R.id.gdgtUser);
		gdgtData = (TextView) findViewById(R.id.gdgtData);
		final Button refreshButton = (Button) findViewById(R.id.refreshButton);
		autoRefresh = (CheckBox) findViewById(R.id.autoRefresh);
		autoRefreshInterval = (EditText) findViewById(R.id.autoRefreshInterval);
		customMessage = (EditText) findViewById(R.id.customMessage);
		cycle = (EditText) findViewById(R.id.cycleInterval);
		mainScrollView = (ScrollView) findViewById(R.id.mainScroll);
		currentDisplaySpinner = (Spinner) findViewById(R.id.showStatistic);
		brightnessSeekbar = (SeekBar) findViewById(R.id.brightnessBar);
		
		device = NameTag.get(this);

		refreshButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				
				device.connect();
				h.removeCallbacks(dataRefreshed);
				h.removeCallbacks(displayValues);
				h.removeCallbacks(refreshData);
				refreshData.run();
			}
		});
		
		customMessage.addTextChangedListener(new TextWatcher() {
			
			
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				Log.d(C.T, s.toString());
				if ("".equals(s.toString())){
					device.clearDisplay();
				}
				h.post(displayValues);
				
			}
		});
		
		cycle.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				h.removeCallbacks(displayValues);
				cycleValues();
			}
		});
		currentDisplaySpinner
				.setOnItemSelectedListener(new OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int selected, long arg3) {
						if (selected != 0) {
							currentDisplayed = selected - 1;
							h.removeCallbacks(displayValues);
							displayValues.run();
						}
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
					}
				});
		brightnessSeekbar
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

					@Override
					public void onProgressChanged(SeekBar seekbar,
							int progress, boolean fromUser) {
						if (fromUser) {
							int currentBrightness = (int) Math.floor(progress*2 + 55);
							Log.d(C.T,"'"+currentBrightness);
							device.setBrightness(currentBrightness);
						}
					}

					@Override
					public void onStartTrackingTouch(SeekBar arg0) {

					}

					@Override
					public void onStopTrackingTouch(SeekBar arg0) {

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


//	protected void btConnected() {
//		bt.write("*c");
//		cycleValues();
//	}

	private Runnable displayValues = new Runnable() {

		@Override
		public void run() {
			String custom = customMessage.getText().toString();
			Log.d(C.T, "displaying");
			if (!"".equals(custom)){
				device.clearDisplay();
				device.printMessage(custom);
				return;
			}
			
			if (currentInfo != null) {
				String tmpCurrentOutput = currentInfo
						.toLcdString(currentDisplayed);
				if (!currentOutput.equals(tmpCurrentOutput)) {
					currentOutput = tmpCurrentOutput;
					device.clearDisplay();
					device.printMessage(currentOutput);
				}
			}

			if (currentDisplaySpinner.getSelectedItemPosition() == 0) {
				currentDisplayed++;
				if (currentDisplayed > C.MAX_DISPLAY_CYCLE) {
					currentDisplayed = 0;
				}
			}

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
					currentInfo = new GDGTInfo(user, NameTagActivity.this);
				}
				mainScrollView.scrollTo(0, mainScrollView.getHeight());
				currentInfo.fillFromNetwork(dataRefreshed);
				device.clearDisplay();
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
			h.removeCallbacks(displayValues);
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

	public void connected() {
		device.clearDisplay();
		h.post(displayValues);
	}

	// @Override
	// protected void onResume() {
	// if (bt != null)
	// bt.start();
	// super.onResume();
	// }
	//
	// @Override
	// public boolean isFinishing() {
	// if (bt != null)
	// bt.stop();
	// return super.isFinishing();
	// }
}