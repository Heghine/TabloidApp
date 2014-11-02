package com.science.inc.tabloid.backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.science.inc.tabloid.util.Constants;


public class API {
	
	public static final String SIGN_IN = "account/signIn";
	public static final String GET_ALL_TAGS = "tag/getAllTabloidTags";
	public static final String ADD_USER_TAGS = "tag/addUserTags";
	
	public static final String TAG = "API";
	
	public static String clientVersion = "t.i.1.1";
	public static String userId = "";
	public static String deviceId = "";
	
	private static RequestThread requestThread = new RequestThread();
	private static LinkedList<RequestData> requestStack = new LinkedList<RequestData>();
	
	static {
		requestThread.start();
	}
	
	public static void signInUser(String deviceId, String twitterId, RequestObserver observer) {
//		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
//		params.add(new BasicNameValuePair("deviceId", deviceId));
//		params.add(new BasicNameValuePair("twitterId", twitterId));
//		params.add(new BasicNameValuePair("clientVersion", clientVersion));
		
//		sendAsyncRequestPost(SIGN_IN, params, RequestObject.POST_METHOD, observer);
		sendAsyncRequestGet(SIGN_IN, "deviceId=" + deviceId + "&twitterId=" + twitterId, RequestData.GET_METHOD, observer);
	}
	
	public static void getAllTags(RequestObserver observer) {
		ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		
		
		sendAsyncRequestPost(GET_ALL_TAGS, params, RequestData.POST_METHOD, observer);
	}
	
	public static void addUserTags(JSONObject tags, RequestObserver observer) {
		HttpEntity entitiy = null;
		try {
			entitiy = new StringEntity(tags.toString());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String requestStr = Constants.SERVER_URL + ADD_USER_TAGS + "?" + "clientVersion=" + clientVersion;
		sendAsyncRequestPost(requestStr, ADD_USER_TAGS, entitiy, observer);
	}
	
	private static void sendAsyncRequestGet(String command, String requestStr, int requestMethod, RequestObserver observer) {
		requestStr = Constants.SERVER_URL + command + "?" + requestStr + "&clientVersion=" + clientVersion;
		
		sendAsyncRequestGet(command, requestStr, observer);
	}
	
	private static RequestData sendAsyncRequestGet(String command, String requestStr, RequestObserver observer) {
		RequestData requestData = new RequestData();
		requestData.requestObserver = observer;
		requestData.requestStr = requestStr;
		requestData.command = command;
		requestData.requestMethod = RequestData.GET_METHOD;
		synchronized (requestStack) {
			requestStack.add(requestData);
			requestStack.notifyAll();
		}
		return requestData;
	}
	
	private static void sendAsyncRequestPost(String command, ArrayList<BasicNameValuePair> params, int requestMethod, RequestObserver observer) {
		params.add(new BasicNameValuePair("clientVersion", clientVersion));
		params.add(new BasicNameValuePair(Constants.COOKIE_NAME, userId));
		params.add(new BasicNameValuePair(Constants.COOKIE_NAME, deviceId));
		
		String requestStr = Constants.SERVER_URL + command;

		try {
			sendAsyncRequestPost(requestStr, command, new UrlEncodedFormEntity(params), observer);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	private static RequestData sendAsyncRequestPost(String requestStr, String command, HttpEntity params, RequestObserver observer) {
		RequestData requestData = new RequestData();
		requestData.requestObserver = observer;
		requestData.requestStr = requestStr;
		requestData.command = command;
		requestData.params = params;
		requestData.requestMethod = RequestData.POST_METHOD;
		synchronized (requestStack) {
			requestStack.add(requestData);
			requestStack.notifyAll();
		}
		return requestData;
	}
	
	public static String convertResponseToString(HttpResponse response) {
		StringBuilder sb = new StringBuilder();
		HttpEntity entity = response.getEntity();
		try {
			if (entity != null) {
				InputStream instream = entity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(instream));

				String line = null;
				try {
					while ((line = reader.readLine()) != null) {
						sb.append(line);
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					instream.close();
				}
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String resultString = sb.toString();
		return resultString;
	}
	
	private static class RequestThread extends Thread {

		private DefaultHttpClient httpClient;

		public RequestThread() {
			setDaemon(true);
		}

		@Override
		public void run() {
			try {
				while (true) {
					if (requestStack.size() == 0) {
						synchronized (requestStack) {
							requestStack.wait();
						}
					}
					if (requestStack.size() != 0) {
						RequestData requestData;
						synchronized (requestStack) {
							requestData = requestStack.get(0);
						}
						if (httpClient == null) {
							httpClient = new DefaultHttpClient();
						}
						HttpRequestBase request = null;
						if (requestData.requestMethod == RequestData.GET_METHOD) {
							request = new HttpGet(requestData.requestStr);
						} else if (requestData.requestMethod == RequestData.POST_METHOD) {
							request = new HttpPost(requestData.requestStr);
							((HttpPost) request).setEntity(requestData.params);
							((HttpPost) request).setHeader(Constants.COOKIE_NAME, userId);
							((HttpPost) request).setHeader(Constants.COOKIE_NAME, deviceId);
						}
						Log.i(TAG, "request = " + requestData.requestStr);
						
						HttpResponse response = null;
						int statusCode = -1;
						String responseStr = null;
						try {
							Log.i(TAG, "request = " + requestData.requestStr);

							response = httpClient.execute(request);
							statusCode = response.getStatusLine().getStatusCode();
							
							Log.i(TAG, "statusCode = " + statusCode);
							
							if (statusCode >= 500) {
								throw new IOException("statusCode: " + statusCode);
							}
							
							if (requestData.command.equals(SIGN_IN)) {
								userId = response.getFirstHeader(Constants.COOKIE_NAME).getValue();
								deviceId = response.getLastHeader(Constants.COOKIE_NAME).getValue(); 
							}
							
							responseStr = convertResponseToString(response);
							Log.i(TAG, "response = " + responseStr);
							
							JSONObject jsonObject = null;
							try {
								jsonObject = new JSONObject(responseStr);
							} catch (JSONException e) {
								JSONArray jsonArray = new JSONArray(responseStr);
								jsonObject = new JSONObject();
								jsonObject.put("values", jsonArray);
							}
							if (requestData.requestObserver != null) {
								requestData.requestObserver.onSuccess(jsonObject);
								requestData.requestObserver.onSuccess(responseStr);
							}

							synchronized (requestStack) {
								requestStack.remove(requestData);
							}
						} catch (ClientProtocolException e) {
							e.printStackTrace();
							synchronized (requestStack) {
								requestStack.remove(requestData);
							}
						} catch (IOException e) {
							e.printStackTrace();
							synchronized (requestStack) {
								requestStack.remove(requestData);
							}
						} catch (JSONException e) {
							e.printStackTrace();
							synchronized (requestStack) {
								requestStack.remove(requestData);
							}
						}
					}
					if (Thread.interrupted())
						break;
				}
			} catch (InterruptedException e) {
				Log.d(TAG, "api is interapted " + e);
			}
		}
	}

	public static class RequestData {
		public final static int POST_METHOD = 1;
		public final static int GET_METHOD = 2;
		
		public RequestObserver requestObserver;
		public int requestMethod = GET_METHOD;
		public String requestStr;
		public String command;
		public HttpEntity params;
		
		public void setRequestObserver(RequestObserver requestObserver){
			 this.requestObserver = requestObserver;
		}
	}

	public static interface RequestObserver {
		public void onSuccess(String response); 
		
		public void onSuccess(JSONObject response) throws JSONException;

		public void onError(String response, Exception e);
	}
	
}
