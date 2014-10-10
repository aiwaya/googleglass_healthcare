package com.aki.utils;

import java.util.HashMap;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class SalesforceLoginTask extends AsyncTask<Void, String, String>
		implements SalesforceTask {
	private ProgressDialog dialog;

	static public String TAG = "SalesforceLoginTask";

	private Context context;

	private int code;

	public SalesforceLoginTask(Context context) {
		this.context = context;
		Log.d(TAG, "Constructor");
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		Log.d(TAG, "onPreExecute");
		dialog = SalesforceUtils.createProgressDialog(context, "Please wait",
				"Login..");
		dialog.show();
	}

	// 	@Override
	protected String doInBackground(Void... params) {
		Log.d(TAG, "onInBackground");
		String token = null;
		try {
			// android.os.Debug.waitForDebugger();
			SalesforceClient client = new SalesforceClient();
			token = client.getToken();
			Log.d(TAG, "token is received :" + token);
		} catch (MySalesforceException e) {
			Log.e(TAG, e.toString());
			return "";
		} catch (Exception e) {
			Log.e(TAG, e.toString());
			return "";
		}
		return token;
	}

	@Override
	protected void onProgressUpdate(String... values) {
		Toast.makeText(context, values[0], Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onPostExecute(String token) {
		//super.onPostExecute(token);
		Log.d(TAG, "onPostExecute");
		if (dialog.isShowing())
			dialog.dismiss();
		
		if(token == null) {
			((OnSalesforceTaskCompleted) context).errorSalesforceTask(code,
					new MySalesforceException("login failure"));			
		} else {
			Map<String, Object> result = new HashMap<String, Object>();
			result.put("token", token);
			((OnSalesforceTaskCompleted) context).successSalesforceTask(code,
					result);			
		}

	}

	@Override
	public void setCode(int code) {
		this.code = code;
	}
}
