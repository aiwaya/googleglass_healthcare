package com.aki.glass.contact;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import org.json.JSONArray;
import org.json.JSONObject;

import com.aki.utils.MySalesforceException;
import com.aki.utils.OnSalesforceTaskCompleted;
import com.aki.utils.SalesforceClient;
import com.aki.utils.SalesforceQueryTask;
import com.google.android.glass.app.Card;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
//import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class BLEService extends Service {

	final static String TAG = "BLEService";
	final static int FIND_ID = 0;
	private AppBean bean;
	private BluetoothManager bluetoothManager;
	private BluetoothAdapter mBluetoothAdapter;
	
	private List<String> beacons = new ArrayList<String>();

	@Override
	public void onCreate() {
		super.onCreate();
		bean = (AppBean) this.getApplication();
		bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();
		mBluetoothAdapter.startLeScan(mLeScanCallback);
		Log.d(TAG, "onCreate");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy");
		mBluetoothAdapter.stopLeScan(mLeScanCallback);
	}

	private String createBeaconID(String uuid, String major, String minor) {
		return uuid + ":" + major + ":" + minor;
	}
	
	private void findPatientId(String uuid) {
		Log.d(TAG, "uuid: " + uuid);
		//bean = (AppBean) this.getApplication();
		try {
		String query = Utils.getContactIdFromUUIDQuery(uuid);
		SalesforceClient client = new SalesforceClient(bean.token);
		List<JSONObject> records = client.soql(query);
		Log.d(TAG, records.toString());
		JSONObject obj = records.get(0);
		
		JSONObject p = obj.getJSONObject("Patients__r");
		Log.d(TAG, p.toString());
		JSONArray a = p.getJSONArray("records");
		Log.d(TAG, a.toString());
		JSONObject v = a.getJSONObject(0);
		Log.d(TAG, v.toString());
		String id = v.getString("Id");
		Log.d(TAG, id);		
		
		bean.contactId = id;

		Intent Views = new Intent();
		Views.setAction("com.aki.glass.contact.ViewAction.VIEW");
		sendBroadcast(Views);
		

		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
	}

	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
			String[] result = Utils.getScanResult(device, rssi, scanRecord);
			String uuid = result[0];
			String major = result[1];
			String minor = result[2];
			
			if(bean.token == null)
				return;

			if (uuid == null || major == null || minor == null)
				return;

			String key = createBeaconID(uuid, major, minor);
			if (beacons.contains(key))
				return;

			beacons.add(key);
			Log.d(TAG, "-----GET BEACON1-----");
			findPatientId(key);
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}