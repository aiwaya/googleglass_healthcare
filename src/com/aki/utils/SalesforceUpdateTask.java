package com.aki.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class SalesforceUpdateTask extends AsyncTask<String, String, String>
		implements SalesforceTask {

	static public String TAG = "SalesforceUpdateTask";
	private ProgressDialog dialog;

	private Context context;
	private SObject sobject;
	private int code;

	public SalesforceUpdateTask(Context context, SObject sobject) {
		this.context = context;
		this.sobject = sobject;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		dialog = SalesforceUtils.createProgressDialog(context, "Please wait",
				"Update record..");
		dialog.show();		
	}

	@Override
	protected String doInBackground(String... params) {
		Log.d(TAG, "doInBackground");
		try {
			String token = params[0];
			if(token == null) 
				throw new MySalesforceException("token is null");
			SalesforceClient client = new SalesforceClient(token);
			client.updateSObject(sobject);
		} catch (MySalesforceException e) {
			Log.e(TAG, e.toString());
			return e.msg;
			//publishProgress("Something wrong for update at sfdc");
		} catch (Exception e) {
			Log.e(TAG, e.toString());			
			return e.getMessage();
		}
		return null;
	}

	@Override
	protected void onProgressUpdate(String... values) {
		Toast.makeText(context, values[0], Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onPostExecute(String result) {
		//super.onPostExecute(id);
		if (dialog.isShowing())
			dialog.dismiss();		
		Log.d(TAG, "onPostExecute");
		if(result == null) {
			((OnSalesforceTaskCompleted) context).successSalesforceTask(code, null);
		} else {
			((OnSalesforceTaskCompleted) context).errorSalesforceTask(code, new MySalesforceException(result));
		}
	}

	@Override
	public void setCode(int code) {
		this.code = code;
	}
}
