package com.aki.glass.contact;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StartDetailScrollActivityReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context content, Intent intent) {
		Intent activity = new Intent(content, DetailScrollActivity.class);
		activity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		content.startActivity(activity);
	}
}
