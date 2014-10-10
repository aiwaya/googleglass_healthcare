package com.aki.utils;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class SalesforceCreateTask extends AsyncTask<String, String, String>
		implements SalesforceTask {

	static public String TAG = "SalesforceCreateTask";

	private int code;

	private Context context;
	private SObject sobject;

	public SalesforceCreateTask(Context context, SObject sobject) {
		this.context = context;
		this.sobject = sobject;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected String doInBackground(String... params) {
		Log.d(TAG, "onInBackground");
		String objectId = null;
		try {
			Log.d(TAG, "SObject:" + sobject.toString());
			String token = params[0];
			SalesforceClient client = new SalesforceClient(token);
			objectId = client.createSObject(sobject);
			Log.d(TAG, objectId);
		} catch (MySalesforceException e) {
			Log.e(TAG, e.toString());
			return null;
		} catch (Exception e) {
			Log.e(TAG, e.toString());
			return null;
		}
		return objectId;
	}

	@Override
	protected void onProgressUpdate(String... values) {
		Toast.makeText(context, values[0], Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onPostExecute(String id) {
		super.onPostExecute(id);
		Log.d(TAG, "onPostExecute");
		if (id == null) {
			((OnSalesforceTaskCompleted) context).errorSalesforceTask(code,
					new MySalesforceException("create record failed"));
		} else {
			Map<String, Object> result = new HashMap<String, Object>();
			result.put("objectId", id);
			((OnSalesforceTaskCompleted) context).successSalesforceTask(code,
					result);
		}
	}

	@Override
	public void setCode(int code) {
		this.code = code;
	}
}
