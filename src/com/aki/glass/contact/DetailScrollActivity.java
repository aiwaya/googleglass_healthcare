package com.aki.glass.contact;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.aki.utils.MySalesforceException;
import com.aki.utils.OnSalesforceTaskCompleted;
import com.aki.utils.SObject;
import com.aki.utils.SalesforceCardScrollAdapter;
import com.aki.utils.SalesforceChatterTask;
import com.aki.utils.SalesforceCreateTask;
import com.aki.utils.SalesforceLoginTask;
import com.aki.utils.SalesforceQueryTask;
import com.aki.utils.SalesforceUpdateTask;
import com.google.android.glass.content.Intents;
import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.widget.CardScrollView;

public class DetailScrollActivity extends Activity implements
		OnSalesforceTaskCompleted {

	private CardScrollView mCardScrollView;
	private static final String TAG = "DetailScrollActivity";
	private static final int VOICE_REQUEST = 0;
	private static final int VOICE_REQUEST_CHATTER = 2;
	private static final int VOICE_REQUEST_ACTIVITY = 3;
	private static final int TAKE_PICTURE_REQUEST = 1;

	private GestureDetector mGestureDetector;
	private AudioManager maManager;
	private SalesforceCardScrollAdapter cardAdapter;

	private AppBean bean = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		bean = (AppBean) this.getApplication();

		maManager = (AudioManager) getSystemService(this.AUDIO_SERVICE);
		mGestureDetector = createGestureDetector(this);

		String query = Utils.getContactQuery(bean.contactId);
		SalesforceQueryTask queryTask = new SalesforceQueryTask(this);
		queryTask.setCode(CONTACT_QUERY);
		queryTask.execute(bean.token, query);

		SObject sobject = Utils.getVisitActivityObjectForCreate(bean.contactId);
		SalesforceCreateTask objectTask = new SalesforceCreateTask(this,
				sobject);
		objectTask.setCode(EVENT_CREATE);
		objectTask.execute(bean.token);
	}

	private GestureDetector createGestureDetector(Context context) {
		GestureDetector gdDetector = new GestureDetector(context);
		// Create a base listener for generic gestures
		gdDetector.setBaseListener(new GestureDetector.BaseListener() {
			@Override
			public boolean onGesture(Gesture gesture) {
				if (gesture == Gesture.TAP) {
					// play the tap sound
					maManager.playSoundEffect(Sounds.TAP);
					// open the menu
					openOptionsMenu();
					return true;
				}
				return false;
			}
		});
		return gdDetector;
	}

	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
		if (mGestureDetector != null)
			return mGestureDetector.onMotionEvent(event);
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.d(TAG, "onCreateOptionsMenu");
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;

	}

	private void takePicture() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(intent, TAKE_PICTURE_REQUEST);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = null;
		switch (item.getItemId()) {
		case R.id.complete:
			intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
					"Description for Activity");
			startActivityForResult(intent, VOICE_REQUEST_ACTIVITY);
			return true;
		case R.id.camera:
			Log.d(TAG, "Camera");
			takePicture();
			return true;
			/*
			 * DO NOT DELETE 
			 * case R.id.livedata: Log.d(TAG, "LiveData"); intent
			 * = new Intent(this, LiveDataActivity.class);
			 * startActivity(intent); return true;
			 */
		case R.id.previous:
			Log.d(TAG, "previous");
			intent = new Intent(this, PreviousActivity.class);
			startActivity(intent);
			return true;
		case R.id.measure:
			Log.d(TAG, "text recognizer");
			intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
					"Lower BP, Upper BP, Temp, and Pulse");
			startActivityForResult(intent, VOICE_REQUEST);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}

	private String picPath = null;

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == TAKE_PICTURE_REQUEST && resultCode == RESULT_OK) {
			String thumbnailPath = data
					.getStringExtra(Intents.EXTRA_THUMBNAIL_FILE_PATH);
			String picturePath = data
					.getStringExtra(Intents.EXTRA_PICTURE_FILE_PATH);

			picPath = thumbnailPath;

			Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Chatter Commnet");
			startActivityForResult(intent, VOICE_REQUEST_CHATTER);

			Log.d(TAG, "chatter request");
		}

		if (requestCode == VOICE_REQUEST_CHATTER && resultCode == RESULT_OK) {
			List<String> list = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			StringBuffer buf = new StringBuffer();
			for (String item : list)
				buf.append(item + " ");

			SalesforceChatterTask t = new SalesforceChatterTask(this);
			t.setCode(CHATTER);
			t.execute(bean.token, picPath, bean.eventId, buf.toString(), "");
		}

		if (requestCode == VOICE_REQUEST && resultCode == RESULT_OK) {
			bean.vitalSigns = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			Log.d(TAG, "Vital signs: " + bean.vitalSigns.toString());
		}

		if (requestCode == VOICE_REQUEST_ACTIVITY && resultCode == RESULT_OK) {
			ArrayList<String> desc = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

			SObject sobject = Utils.getVisitActivityObjectForUpdate(
					bean.vitalSigns == null ? null : bean.vitalSigns.get(0), desc.get(0));
			Log.d(TAG, "update event with : " + sobject.toString());
			sobject.setId(bean.eventId);
			SalesforceUpdateTask updateTask = new SalesforceUpdateTask(this,
					sobject);
			updateTask.setCode(EVENT_UPDATE);
			updateTask.execute(bean.token);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	static int EVENT_UPDATE = 0;
	static int EVENT_CREATE = 1;
	static int CONTACT_QUERY = 2;
	static int CHATTER = 3;
	
	@Override
	public void successSalesforceTask(int code, Map<String, Object> result) {

		if (EVENT_UPDATE == code) {
			bean.reset();
			finish();
		}

		if (EVENT_CREATE == code) {
			bean.eventId = (String) result.get("objectId");
		}

		if (CONTACT_QUERY == code) {
     		@SuppressWarnings("unchecked")
			List<JSONObject> records = (List<JSONObject>) result.get("records");

     		// Get Bitmap for patient's image based on a url stored in Salesforce
     		DownloadImageTask task = new DownloadImageTask(this, records.get(0));
			task.execute();
		}

		if (CHATTER == code) {
		}
	}
	
	public void showContactDetailCards(JSONObject contact, Bitmap personBitmap) {
		List<View> mCards;
		try {
			mCards = Utils.createCardViewListFromContact(this, contact, personBitmap);
			mCardScrollView = new CardScrollView(this);
			cardAdapter = new SalesforceCardScrollAdapter(mCards);
			mCardScrollView.setAdapter(cardAdapter);
			mCardScrollView.activate();
			setContentView(mCardScrollView);				
		} catch (MySalesforceException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void errorSalesforceTask(int code, MySalesforceException e) {

	}
}
