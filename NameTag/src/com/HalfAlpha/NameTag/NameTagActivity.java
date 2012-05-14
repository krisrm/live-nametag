package com.HalfAlpha.NameTag;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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

public class NameTagActivity extends Activity {
	private static final int SEC_LENGTH_MS = 1000;
	private static final int MIN_LENGTH_MS = 60*SEC_LENGTH_MS;
	public static final String T = "NAMETAG";
	protected static final int ERR_REFRESH = 0;
	private Handler h = new Handler();
	protected GDGTInfo currentInfo;
	private EditText cycle;
	private EditText autoRefreshInterval;
	private TextView gdgtData;
	private EditText gdgtUser;
	private CheckBox autoRefresh;
	private ScrollView mainScrollView;
	
	private Runnable displayValues = new Runnable() {

		@Override
		public void run() {
			//call Arduino with data
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
		}
		return dialog;
	}

	private Dialog errorDialog(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message).setCancelable(true)
				.setTitle("Unexpected Error");

		return builder.create();
	}
}