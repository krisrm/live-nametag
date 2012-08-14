package com.HalfAlpha.NameTag;

import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.util.Log;
import android.widget.Toast;

public class NameTag {
	private static NameTag I;
	private NameTagActivity activity;
	private BluetoothSerialService bt;
	private Handler h = new Handler(new Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			Log.v(C.T, msg.toString());
			switch (msg.what) {
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
				activity.connected();
				break;
			case C.MESSAGE_TOAST:
				Toast.makeText(activity,
						msg.getData().getString(C.TOAST), Toast.LENGTH_LONG);
				Log.d(C.T, msg.getData().getString(C.TOAST));
				break;

			}
			return false;
		}
	});
	
	private NameTag(){
		bt = new BluetoothSerialService(activity, h);
	}
	
	public static NameTag get(NameTagActivity activity){
		if (I == null){
			I = new NameTag();
		}
		I.activity = activity;
		return I;
	}
	
	public void reconnect() {
		if (bt == null
				|| bt.getState() != BluetoothSerialService.STATE_CONNECTED) {
			connect();
		} else {
			disconnect();
			connect();
		}
		
	}
	public void connect(){
		if (bt.getState() == BluetoothSerialService.STATE_CONNECTED){
			return; //we're already connected...
		}
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();
		BluetoothDevice mBluetoothDevice = null;
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			activity.startActivityForResult(enableBtIntent, C.REQUEST_ENABLE_BT);
		}

		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
				.getBondedDevices();
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
				if (C.DEVICE_NAME.equals(device.getName())) {
					mBluetoothDevice = device;
				}
			}
		}
		if (mBluetoothDevice == null) {
			activity.showDialog(C.ERR_BT);
			return;
		}

		bt.connect(mBluetoothDevice);
		
	}
	
	public void disconnect(){
		bt.disconnect();
	}
	
	public void printMessage(String message){
		bt.write("<"+message+">");
	}
	
	public void setBrightness(int currentBrightness){
		if (currentBrightness < 100){								
			bt.write("*0"+currentBrightness+"e");
		} else {
			bt.write("*"+currentBrightness+"e");
		}
	}
	
	public void clearDisplay(){
		bt.write("*c");
	}

}
