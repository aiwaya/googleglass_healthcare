package com.aki.glass.contact;

import java.util.Date;
import java.util.List;

import org.json.JSONObject;

import com.aki.utils.MySalesforceException;
import com.aki.utils.SalesforceClient;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class BroadcastService extends Service  {
	private static final String TAG = "BroadcastService";
	public static final String BROADCAST_ACTION = "com.websmithing.broadcasttest.displayevent";
	private final Handler handler = new Handler();
	Intent intent;
	int counter = 0;
	
	private AppBean bean;
	
	@Override
	public void onCreate() {
		super.onCreate();
		bean = (AppBean) this.getApplication();
    	intent = new Intent(BROADCAST_ACTION);	
	}
	
    @Override
    public void onStart(Intent intent, int startId) {
        handler.removeCallbacks(sendUpdatesToUI);
        handler.postDelayed(sendUpdatesToUI, 1000); // 1 second
   
    }

    private Runnable sendUpdatesToUI = new Runnable() {
    	public void run() {
    		requestToSalesforce();
    	    handler.postDelayed(this, 2000); // 2 seconds
    	}
    };    
    
    public void requestToSalesforce() {
    	LiveDataTask task = new LiveDataTask(this);
    	task.execute(bean.token, Utils.getSensorAData());
    }
    
    public void displaySensorData(String data) {
    	intent.putExtra("data", data);
    	sendBroadcast(intent);
    }
    
    private void DisplayLoggingInfo() {
    	Log.d(TAG, "entered DisplayLoggingInfo");

    	intent.putExtra("counter", String.valueOf(++counter));
    	sendBroadcast(intent);
    }
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {		
        handler.removeCallbacks(sendUpdatesToUI);		
		super.onDestroy();
	}		
}