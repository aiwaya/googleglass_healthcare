package com.aki.glass.contact;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.aki.utils.SalesforceClient;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class LiveDataTask extends AsyncTask<String, Void, String> {

	static public String TAG = "LiveDataTask";

	private BroadcastService sev;

	public LiveDataTask(BroadcastService sev) {
		this.sev = sev;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected String doInBackground(String... params) {
		Log.d(TAG, "onInBackground");
		String data = null;
		try {
			String token = params[0];
			String query = params[1];
			Log.d(TAG, "soql : " + query);

			SalesforceClient client = new SalesforceClient(token);

			List<JSONObject> list = client.soql(query);
			JSONObject obj = list.get(0);
			Log.d(TAG, "2 in thread");
			data = obj.getString("FloatValue__c");

		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
		return data;

	}

	public boolean isCancel() {
		return true;
	}

	@Override
	protected void onProgressUpdate(Void... values) {

	}

	@Override
	protected void onPostExecute(String data) {
		sev.displaySensorData(data);
	}

}
