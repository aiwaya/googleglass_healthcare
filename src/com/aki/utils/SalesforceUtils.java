package com.aki.utils;

import android.app.ProgressDialog;
import android.content.Context;

public class SalesforceUtils {


	
	static public ProgressDialog createProgressDialog(Context context,
			String title, String message) {
		ProgressDialog dialog = new ProgressDialog(context);
		dialog.setTitle(title);
		dialog.setMessage(message);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		return dialog;
	}	

}
