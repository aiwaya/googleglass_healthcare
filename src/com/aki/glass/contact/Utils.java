package com.aki.glass.contact;

import java.util.ArrayList;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.view.View;

import com.aki.utils.MySalesforceException;
import com.aki.utils.SObject;
import com.google.android.glass.app.Card;

import android.bluetooth.BluetoothDevice;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class Utils {
	static public String TAG = "Utils";
	static final String EVENT_SUBJECT = "Check Vital Signs";

	public static final String BR = System.getProperty("line.separator");

	static public String getContactIdFromUUIDQuery(String uuid) {
		return "SELECT Name, (SELECT Id FROM Patients__r WHERE Type__c = 'Patient') FROM Beacon__c WHERE Name = '8492E75F-4FD6-469D-B132-043FE94921D8:1F2C:4825'";
	}

	static public String getContactQuery(String contactId) {
		return "SELECT Name, Age__c, Birthdate, Gender__c, Height__c, Lower_BP__c, Medicine__c, Description, Pluse__c, Temp__c, Upper_BP__c, Weight__c, PicURL__c "
				+ " FROM Contact WHERE id = '" + contactId + "'";
	}

	static public String getPreviousEventsQuery(String contactId) {
		return "SELECT Subject, WhoId, Temp__c, Lower_BP__c, Upper_BP__c, EndDateTime, StartDateTime, Description FROM Event WHERE WhoId = '"
				+ contactId + "' AND Subject != '' LIMIT 5";
	}

	static public String getSensorAData() {
		return "SELECT FloatValue__c, Timestamp__c, CreatedDate FROM SensorData__c WHERE Sensor__c = 'a061000001ZMf5f'  ORDER BY Timestamp__c DESC LIMIT 1";
	}

	static public SObject getVisitActivityObjectForCreate(String contactId) {
		Date now = new Date();
		SObject sobject = new SObject("Event");
		sobject.put("WhoId", contactId);
		sobject.put("StartDateTime", SObject.convertDateToString(now));
		sobject.put("EndDateTime", SObject.convertDateToString(now));
		return sobject;
	}

	static public SObject getVisitActivityObjectForUpdate(String vitalSigns,
			String desc) {
		SObject sobject = new SObject("Event");
		sobject.put("Subject", EVENT_SUBJECT);
		sobject.put("EndDateTime", SObject.convertDateToString(new Date()));
		
		if (vitalSigns != null) {
			String low = "80", high = "120", temp = "97", pluse = "60";
			try {
				String[] array = vitalSigns.split(" ");
				low = array[0];
				high = array[1];
				temp = array[2];
				pluse = array[3];
			} catch (Exception e) {
			} finally {
				Log.d(TAG, "low:" + low + " high: " + high + " temp: " + temp
						+ " pluse: " + pluse);
				sobject.put("Pulse__c", pluse);
				sobject.put("Temp__c", temp);
				sobject.put("Upper_BP__c", high);
				sobject.put("Lower_BP__c", low);				
			}
		}
		sobject.put("Description", desc);
		return sobject;
	}

	static public void getContactIdFromUUIDQuery(List<JSONObject> records) {

	}

	static public List<View> createCardViewListFromVisitActivities(
			Activity act, List<JSONObject> records)
			throws MySalesforceException {
		List<View> cardList = new ArrayList<View>();
		try {
			Iterator<JSONObject> itr = records.iterator();
			StringBuffer buf = null;
			Card card = null;

			while (itr.hasNext()) {
				JSONObject record = (JSONObject) itr.next();
				String subject = record.getString("Subject");
				String endDateTime = SObject.convertDefaultDateTime(record
						.getString("EndDateTime"));
				String startDateTime = SObject.convertDefaultDateTime(record
						.getString("StartDateTime"));
				String desc = record.getString("Description");

				buf = new StringBuffer();
				buf.append(subject + BR);
				buf.append(desc + BR);

				card = new Card(act);
				card.setText(buf.toString());
				card.setFootnote(startDateTime);
				cardList.add(card.getView());
			}
			Log.d(TAG, "size of visit acitivities : " + records.size());
			return cardList;
		} catch (Exception e) {
			throw new MySalesforceException(e, "parse error at createCardView");
		}

	}

	static public List<View> createCardViewListFromContact(Activity act,
			JSONObject record, Bitmap personBitmap)
			throws MySalesforceException {
		List<View> cardList = new ArrayList<View>();
		StringBuffer buf = new StringBuffer();
		Card card = null;
		try {

			// Card1
			buf.append("Name: " + record.getString("Name") + BR);
			buf.append("Birthday: " + record.getString("Birthdate") + BR);
			buf.append("Age: " + record.getString("Age__c") + BR);
			buf.append("Sex: " + record.getString("Gender__c") + BR);
			buf.append("Height: " + record.getString("Height__c") + BR);
			buf.append("Weight: " + record.getString("Weight__c") + BR);
			card = createCard(act, buf.toString());
			// card.addImage(R.drawable.profile);
			card.addImage(personBitmap);
			cardList.add(card.getView());
			Log.d(TAG, "card1");

			// Card2
			buf = new StringBuffer();
			buf.append("Lower BP: " + record.getString("Lower_BP__c") + "mmHg"
					+ BR);
			buf.append("Upper BP: " + record.getString("Upper_BP__c") + "mmHg"
					+ BR);
			buf.append("Pluse: " + record.getString("Pluse__c") + "bpm" + BR);
			buf.append("Temperature" + record.getString("Temp__c") + "°F" + BR);

			card = createCard(act, buf.toString());
			card.addImage(personBitmap);
			cardList.add(card.getView());

			Log.d(TAG, "card2");

			// Card3
			buf = new StringBuffer();
			buf.append(record.getString("Description"));

			card = createCard(act, buf.toString());
			card.addImage(personBitmap);
			cardList.add(card.getView());

			Log.d(TAG, "card3");

			// Card4
			buf = new StringBuffer();

			buf.append(record.getString("Medicine__c") + BR);

			card = createCard(act, buf.toString());
			card.setFootnote("THE MEDICINES ARE PRESCRIBED");
			card.addImage(R.drawable.medicine1);
			card.setImageLayout(Card.ImageLayout.FULL);
			cardList.add(card.getView());
			Log.d(TAG, "card4");

		} catch (Exception e) {
			throw new MySalesforceException(e, "parse error");
		}
		return cardList;
	}

	static private Card createCard(Activity act, String value) {
		Card card = new Card(act);
		card.setText(value);
		return card;
	}

	static String[] getScanResult(BluetoothDevice device, int rssi,
			byte[] scanRecord) {
		String[] result = new String[3];
		if (scanRecord.length > 30) {
			if ((scanRecord[5] == (byte) 0x4c)
					&& (scanRecord[6] == (byte) 0x00)
					&& (scanRecord[7] == (byte) 0x02)
					&& (scanRecord[8] == (byte) 0x15)) {

				String uuid = IntToHex2(scanRecord[9] & 0xff)
						+ IntToHex2(scanRecord[10] & 0xff)
						+ IntToHex2(scanRecord[11] & 0xff)
						+ IntToHex2(scanRecord[12] & 0xff) + "-"
						+ IntToHex2(scanRecord[13] & 0xff)
						+ IntToHex2(scanRecord[14] & 0xff) + "-"
						+ IntToHex2(scanRecord[15] & 0xff)
						+ IntToHex2(scanRecord[16] & 0xff) + "-"
						+ IntToHex2(scanRecord[17] & 0xff)
						+ IntToHex2(scanRecord[18] & 0xff) + "-"
						+ IntToHex2(scanRecord[19] & 0xff)
						+ IntToHex2(scanRecord[20] & 0xff)
						+ IntToHex2(scanRecord[21] & 0xff)
						+ IntToHex2(scanRecord[22] & 0xff)
						+ IntToHex2(scanRecord[23] & 0xff)
						+ IntToHex2(scanRecord[24] & 0xff);

				String major = IntToHex2(scanRecord[25] & 0xff)
						+ IntToHex2(scanRecord[26] & 0xff);
				String minor = IntToHex2(scanRecord[27] & 0xff)
						+ IntToHex2(scanRecord[28] & 0xff);
				result[0] = uuid;
				result[1] = major;
				result[2] = minor;
			}
		}
		return result;
	}

	static public String IntToHex2(int i) {
		char hex_2[] = { Character.forDigit((i >> 4) & 0x0f, 16),
				Character.forDigit(i & 0x0f, 16) };
		String hex_2_str = new String(hex_2);
		return hex_2_str.toUpperCase();
	}

}
