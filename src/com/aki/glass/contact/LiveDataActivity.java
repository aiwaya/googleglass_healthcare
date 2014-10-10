package com.aki.glass.contact;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.aki.utils.MySalesforceException;
import com.aki.utils.OnSalesforceTaskCompleted;
import com.aki.utils.SalesforceCardScrollAdapter;
import com.aki.utils.SalesforceClient;
import com.aki.utils.SalesforceQueryTask;
import com.google.android.glass.app.Card;
import com.google.android.glass.widget.CardScrollView;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class LiveDataActivity extends Activity {

	private static final int CONTACT_QUERY = 0;
	private static final String TAG = "LiveDataActivity";
	private AppBean bean;
	private Intent intent;
	Card card;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		bean = (AppBean) this.getApplication();
		intent = new Intent(this, BroadcastService.class);

		card = new Card(this);
		card.setText("");
		card.addImage(R.drawable.profile);
		setContentView(card.getView());

	}

	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			updateUI(intent);
		}
	};

	@Override
	public void onResume() {
		super.onResume();
		startService(intent);
		registerReceiver(broadcastReceiver, new IntentFilter(
				BroadcastService.BROADCAST_ACTION));
	}

	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(broadcastReceiver);
		stopService(intent);
	}

	private void updateUI(Intent intent) {

		String data = intent.getStringExtra("data");
		try {
			Float f = Float.valueOf(data).floatValue();
			f = (float) Math.floor((double) f * 100) / 100;
			data = "" + f;
		} catch (Exception e) {
		}

		Log.d(TAG, data);
		StringBuffer buf = new StringBuffer();
		buf.append("Current Temperature" + Utils.BR + Utils.BR);
		buf.append(data + " ℃" + Utils.BR);
		card.setText(buf.toString());
		setContentView(card.getView());
	}

}
