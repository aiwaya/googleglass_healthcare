package com.aki.glass.contact;

import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.aki.utils.MySalesforceException;
import com.aki.utils.OnSalesforceTaskCompleted;
import com.aki.utils.SalesforceCardScrollAdapter;
import com.aki.utils.SalesforceQueryTask;
import com.google.android.glass.widget.CardScrollView;

public class PreviousActivity extends Activity implements
		OnSalesforceTaskCompleted {

	static public String TAG = "PreviousActivity";

	private CardScrollView mCardScrollView;
	private AppBean bean;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		bean = (AppBean) this.getApplication();
		String query = Utils.getPreviousEventsQuery(bean.contactId);
		SalesforceQueryTask queryTask = new SalesforceQueryTask(this);
		queryTask.setCode(EVENT_QUERY);
		queryTask.execute(bean.token, query);
	}

	static int EVENT_QUERY = 1;

	@Override
	public void successSalesforceTask(int code, Map<String, Object> result) {
		// public void successForQuery(List<JSONObject> records) {
		Log.d(TAG, "onTaskCompleted");

		if (EVENT_QUERY == code) {

			try {
				@SuppressWarnings("unchecked")
				List<JSONObject> records = (List<JSONObject>) result
						.get("records");
				List<View> mCards = Utils
						.createCardViewListFromVisitActivities(this, records);
				mCardScrollView = new CardScrollView(this);
				mCardScrollView.setAdapter(new SalesforceCardScrollAdapter(
						mCards));
				mCardScrollView.activate();
				setContentView(mCardScrollView);

			} catch (MySalesforceException e) {
				// TODO Auto-generated catch block
				Log.e(TAG, e.toString());
			}
		}
	}

	@Override
	public void errorSalesforceTask(int code, MySalesforceException e) {
		// TODO Auto-generated method stub

	}
}
