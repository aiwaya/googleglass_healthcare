package com.aki.glass.contact;

import java.util.Date;
import java.util.List;

import android.app.Application;

public class AppBean extends Application {
	public String token;
	public Date startTime;
	public List<String> vitalSigns;
	public String eventId = null;
	public String contactId = null;

	@Override
	public void onCreate() {
		super.onCreate();
		startTime = new Date();
	}
	
	public void reset() {
		startTime = null;
		vitalSigns = null;
		eventId = null;
		contactId = null;
	}
}
