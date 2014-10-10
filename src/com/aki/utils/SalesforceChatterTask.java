package com.aki.utils;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class SalesforceChatterTask extends
		AsyncTask<String, String, String> implements SalesforceTask {

	static public String TAG = "SalesforceChatterTask";

	private Context context;
	
	private ProgressDialog dialog;

	private int code;

	public SalesforceChatterTask(Context context) {
		this.context = context;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		dialog = SalesforceUtils.createProgressDialog(context, "Please wait",
				"Upload Chatter");
		dialog.show();		
	}

	@Override
	protected String doInBackground(String... params) {
		Log.d(TAG, "onInBackground");
		try {
			String token = params[0];
			String filename = params[1];
			String targetRecordId = params[2];
			String text = params[3];
			String desc = params[4];
			if(token == null || filename == null || targetRecordId == null || text == null) 
				throw new MySalesforceException("token, filename, targetRecordid, or text is null");
			SalesforceClient client = new SalesforceClient(token);
			client.chatter(filename, text, desc, targetRecordId);
		} catch (MySalesforceException e) {
			Log.e(TAG, e.toString());
			return e.toString();
		} catch (Exception e) {
			Log.e(TAG, e.toString());
			return e.toString();
		}
		return null;

	}

	@Override
	protected void onProgressUpdate(String... values) {
		Toast.makeText(context, values[0], Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onPostExecute(String result) {
		//super.onPostExecute(list);
		Log.d(TAG, "onPostExecute");
		if (dialog.isShowing())
			dialog.dismiss();		
		
		if(result == null) {
		((OnSalesforceTaskCompleted) context).errorSalesforceTask(code, new MySalesforceException("chatter creation error"));
		} else {
			((OnSalesforceTaskCompleted) context).successSalesforceTask(code, null);
		}
	}

	@Override
	public void setCode(int code) {
		this.code = code;

	}
}
