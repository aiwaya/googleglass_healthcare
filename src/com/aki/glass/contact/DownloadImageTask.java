package com.aki.glass.contact;

import java.net.URL;
import java.util.List;

import org.json.JSONObject;

import com.google.android.glass.app.Card;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

public class DownloadImageTask extends AsyncTask<Void, Void, Bitmap> {

	private static final String TAG = "DownloadImagesTask";
	private DetailScrollActivity activity;
	private JSONObject contact;
	
	public DownloadImageTask(DetailScrollActivity activity, JSONObject contact) {
		this.activity = activity;
		this.contact = contact;
	}
	
	@Override
	protected Bitmap doInBackground(Void...voids) {
		Bitmap bmp = null;
		try {
			Log.d(TAG, "before url");
			URL url = new URL(contact.getString("PicURL__c"));
			bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
			Log.d(TAG, "after url" + bmp.getHeight());
		} catch (Exception e) {
			Log.d(TAG, "eeeee");
			Log.e(TAG, e.toString());

		}
		return bmp;
	}
	
	@Override
	protected void onPostExecute(Bitmap bitmap) {
		this.activity.showContactDetailCards(contact, bitmap);
	}
}
