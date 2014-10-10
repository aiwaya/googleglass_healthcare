package com.aki.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.TimeZone;

import android.util.Log;

public class SObject extends HashMap<String, String> {

	static private String TAG = "SObject";

	private static final long serialVersionUID = 1L;

	private String objectName;
	private String objectId;

	public SObject(String objectName) {
		super();
		this.objectName = objectName;
	}

	public String getId() {
		return objectId;
	}

	public void setId(String objectId) {
		this.objectId = objectId;
	}

	public String getName() {
		return objectName;
	}

	static public String convertDateToString(Date date) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.000Z");
		return df.format(date);
	}
	
	static public Calendar convertStringToCalendar(String strDateTime) throws ParseException {
 		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd\'T\'HH:mm:ss");
		TimeZone tz = TimeZone.getTimeZone("GMT");
		dateFormat.setTimeZone(tz);
		
		Date utcDate = dateFormat.parse(strDateTime);
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(utcDate);
		return cal;
	}
	
	static public String convertDefaultDateTime(String strDateTime) throws ParseException {
		Calendar cal = convertStringToCalendar(strDateTime);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 ");
        StringBuffer buf = new StringBuffer();
        buf.append(sdf.format(cal.getTime()));
        sdf.applyPattern("a hh:mm");
        buf.append(sdf.format(cal.getTime()));
        return buf.toString();
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("ObjectName:"+objectName);
		buf.append("--------------");
		buf.append(super.toString());
		return buf.toString();
	}
}
