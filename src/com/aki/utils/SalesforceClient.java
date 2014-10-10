package com.aki.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.util.Log;

public class SalesforceClient {

	static private String TAG = "SalesforceClient";

	// The following header forces sfdc to send back response as json with easy
	// readable format by human.
	static Header prettyPrintHeader = new BasicHeader("X-PrettyPrint", "1");

	static private String HOST_URL = "https://ap.salesforce.com";
	static private String LOGIN_URL = HOST_URL + "/services/oauth2/token";
	static private String DATA_URL = HOST_URL + "/services/data/v23.0";

	
	static private String USER_NAME = "xxxxx";
	static private String PASSWORD = "xxxxxx";
	static private String CLIENT_ID = "xxxxxxx";
	static private String CLIENT_SECRET = "xxxxxxx";	

	private String token;

	public SalesforceClient() throws MySalesforceException {
		login();
	}

	public SalesforceClient(String token) {
		this.token = token;
	}

	private void login() throws MySalesforceException {
		HttpClient client = null;
		try {
			StringBuffer requestBodyText = new StringBuffer(
					"grant_type=password");
			requestBodyText.append("&username=");
			requestBodyText.append(USER_NAME);
			requestBodyText.append("&password=");
			requestBodyText.append(PASSWORD);
			requestBodyText.append("&client_id=");
			requestBodyText.append(CLIENT_ID);
			requestBodyText.append("&client_secret=");
			requestBodyText.append(CLIENT_SECRET);

			StringEntity requestBody = new StringEntity(
					requestBodyText.toString());
			requestBody.setContentType("application/x-www-form-urlencoded");

			HttpPost post = new HttpPost(LOGIN_URL);
			post.setEntity(requestBody);
			post.addHeader(prettyPrintHeader);

			client = new DefaultHttpClient();
			HttpResponse response = client.execute(post);

			// receives response from sfdc
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent(), "UTF-8"));
			StringBuilder builder = new StringBuilder();
			for (String line = null; (line = reader.readLine()) != null;) {
				builder.append(line).append("\n");
			}
			JSONObject object = new JSONObject(new JSONTokener(
					builder.toString()));
			token = (String) object.get("access_token");
		} catch (Exception e) {
			throw new MySalesforceException(e, "can not login");

		} finally {
			client.getConnectionManager().shutdown();
		}
	}

	public String getToken() {
		return token;
	}

	public List<JSONObject> soql(String soql) throws MySalesforceException {
		HttpClient client = null;
		List<JSONObject> rows = new ArrayList<JSONObject>();
		try {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("q", soql));
			String uri = DATA_URL + "/query?"
					+ URLEncodedUtils.format(params, "UTF-8");

			HttpGet get = new HttpGet(uri);
			get.addHeader("Authorization", "OAuth " + token);

			client = new DefaultHttpClient();
			HttpResponse res = client.execute(get);

			// Process the result
			int statusCode = res.getStatusLine().getStatusCode();
			if (statusCode == 200) {
				String response_string = EntityUtils.toString(res.getEntity());
				try {
					JSONObject json = new JSONObject(response_string);

					JSONArray array = json.getJSONArray("records");
					for (int i = 0; i < array.length(); i++) {
						rows.add((JSONObject) array.get(i));
					}
				} catch (JSONException je) {
					throw new MySalesforceException(je,
							"failed to parse jsonobj ");
				}
			} else {
				System.out
						.println("Query was unsuccessful. Status code returned is "
								+ statusCode);
			}
		} catch (MySalesforceException e) {
			throw e;
		} catch (Exception e) {
			throw new MySalesforceException(e, "failed to query : " + soql);
		} finally {
			client.getConnectionManager().shutdown();
		}
		return rows;
	}

	public String createSObject(SObject sobject) throws MySalesforceException {
		String objId = null;
		HttpClient client = null;
		try {
			JSONObject obj = new JSONObject();
			for (String key : sobject.keySet()) {
				String value = sobject.get(key);
				obj.put(key, value);
			}

			String uri = DATA_URL + "/sobjects/" + sobject.getName() + "/";
			HttpPost post = new HttpPost(uri);
			post.addHeader("Authorization", "OAuth " + token);

			StringEntity postEntity = new StringEntity(obj.toString(), "UTF-8");
			postEntity.setContentType("application/json");
			post.setEntity(postEntity);

			client = new DefaultHttpClient();
			HttpResponse postResponse = client.execute(post);

			if (postResponse.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {

				JSONObject response = new JSONObject(
						new JSONTokener(EntityUtils.toString(
								postResponse.getEntity(), "UTF-8")));
				System.out.println("Create response: " + response.toString(2));

				if (response.getBoolean("success")) {
					Log.d(TAG, response.toString());
					Log.d(TAG, response.getString("id"));
					objId = response.getString("id");

				}
			}
			Log.d(TAG, objId);
			return objId;
		} catch (JSONException e) {
			Log.e(TAG, "json perse error" + e.toString());
			throw new MySalesforceException(e,
					"josn perse error with create record");
		} catch (Exception e) {
			Log.e(TAG, "create record error" + e.toString());
			throw new MySalesforceException(e, "create record error");
		} finally {
			client.getConnectionManager().shutdown();
		}
	}

	public void chatter(String filePath, String textMsg, String desc,
			String targetRecordId) throws MySalesforceException {
		HttpClient client = null;

		try {
			String uri = DATA_URL + "/chatter/feeds/record/" + targetRecordId
					+ "/feed-items";

			File contentFile = new File(filePath);
			String fileName = "Picture";

			client = new DefaultHttpClient();

			HttpPost httppost = new HttpPost(uri);
			httppost.addHeader("Authorization", "OAuth " + token);

			MultipartEntity mpEntity = new MultipartEntity();
			ContentBody cbFile = new FileBody(contentFile, "image/jpeg");

			mpEntity.addPart("feedItemFileUpload", cbFile);
			mpEntity.addPart("desc", new StringBody(desc));
			mpEntity.addPart("fileName", new StringBody(fileName));
			mpEntity.addPart("text", new StringBody(textMsg));

			httppost.setEntity(mpEntity);
			Log.d(TAG, "executing request " + httppost.getRequestLine());
			HttpResponse response = client.execute(httppost);
			HttpEntity resEntity = response.getEntity();

			System.out.println(response.getStatusLine());
			if (resEntity != null) {
				System.out.println(EntityUtils.toString(resEntity));
			}
			if (resEntity != null) {
				resEntity.consumeContent();
			}

		} catch (Exception e) {
			Log.e("SalesforceClient", e.toString());
			throw new MySalesforceException(e, "e");

		} finally {
			client.getConnectionManager().shutdown();
		}

	}

	public void updateSObject(SObject sobject) throws MySalesforceException {
		HttpClient client = new DefaultHttpClient();

		JSONObject obj = new JSONObject();
		try {
			for (String key : sobject.keySet()) {
				String value = sobject.get(key);
				obj.put(key, value);
			}

			String uri = DATA_URL + "/sobjects/" + sobject.getName() + "/"
					+ sobject.getId() + "?_HttpMethod=PATCH";
			Log.d(TAG, "uri: " + uri);

			HttpPost post = new HttpPost(uri);
			post.addHeader("Authorization", "OAuth " + token);

			StringEntity postEntity = new StringEntity(obj.toString(), "UTF-8");
			postEntity.setContentType("application/json");
			post.setEntity(postEntity);

			HttpResponse postResponse = client.execute(post);

			Log.d(TAG, "status code: "
					+ postResponse.getStatusLine().getStatusCode());
			if (postResponse.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
				JSONObject response = new JSONObject(
						new JSONTokener(EntityUtils.toString(
								postResponse.getEntity(), "UTF-8")));
				if (!response.getBoolean("success")) {
					Log.d(TAG, "update error: " + response.toString());
					throw new Exception();
				}
			}
		} catch (JSONException e) {
			Log.e(TAG, "json perse error" + e.toString());
			throw new MySalesforceException(e,
					"josn perse error with update record");
		} catch (Exception e) {
			Log.e(TAG, "update record error" + e.toString());
			throw new MySalesforceException(e, "update record error");
		} finally {
			client.getConnectionManager().shutdown();
		}

	}

}
