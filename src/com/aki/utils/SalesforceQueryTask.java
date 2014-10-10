package com.aki.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class SalesforceQueryTask extends
		AsyncTask<String, String, List<JSONObject>> implements SalesforceTask {

	static public String TAG = "SalesforceQueryTask";

	private Context context = null;
	
	private ProgressDialog dialog;

	private int code;
	
	public SalesforceQueryTask(Context context) {
		this.context = context;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		dialog = SalesforceUtils.createProgressDialog(context, "Please wait",
				"Retreive..");
		dialog.show();		
	}

	@Override
	protected List<JSONObject> doInBackground(String... params) {
		Log.d(TAG, "onInBackground");
		List<JSONObject> list = null;
		try {
			String token = params[0];
			String query = params[1];
			if(token == null || query == null) 
				throw new MySalesforceException("token or soql is null");
			Log.d(TAG, "soql : " + query);
			// publishProgress("calling salesforce");
			SalesforceClient client = new SalesforceClient(token);
			list = client.soql(query);
			// publishProgress("success query");
		} catch (MySalesforceException e) {
			Log.e(TAG, e.toString());
			list = null;
		} catch (Exception e) {
			Log.e(TAG, e.toString());
			list = null;
		}
		return list;

	}

	@Override
	protected void onProgressUpdate(String... values) {
		  Toast.makeText(context, values[0], Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onPostExecute(List<JSONObject> list) {
		//super.onPostExecute(list);
		Log.d(TAG, "onPostExecute");
		if (dialog.isShowing())
			dialog.dismiss();
		
		if(list == null) {
			((OnSalesforceTaskCompleted) context).errorSalesforceTask(code,
					new MySalesforceException("query error"));			
		} else {
			Log.d(TAG, "soql result: " + list.toString());
			Map<String, Object> result = new HashMap<String, Object>();
			result.put("records", list);
			((OnSalesforceTaskCompleted) context).successSalesforceTask(code,
					result);			
		}

	}

	@Override
	public void setCode(int code) {
		this.code = code;
	}
}
