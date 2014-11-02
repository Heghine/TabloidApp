package com.science.inc.tabloid;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;

import com.science.inc.tabloid.backend.API;
import com.science.inc.tabloid.backend.API.RequestObserver;
import com.science.inc.tabloid.data.UserData;
import com.science.inc.tabloid.manager.LocalDbManager;
import com.science.inc.tabloid.manager.PreferenceManager;
import com.science.inc.tabloid.manager.TabloidAppManager;
import com.science.inc.tabloid.util.Constants;

public class MainActivity extends Activity {

	private static Twitter twitter;
	private static RequestToken requestToken;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);
		
		PreferenceManager.getInstance().init(getApplicationContext());
		LocalDbManager.getInstance().init(getApplicationContext());
		TabloidAppManager.getInstance().mainActivity = this;
		
		((Button) findViewById(R.id.twitter_login_btn)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				loginToTwitter();
			}
		});
		
		if (PreferenceManager.getInstance().getTwitterLogin()) {
			hideSignInButton();
			signInUser();
		} else {
			showSignInButton();
		}
	}
	
	@SuppressLint("SetJavaScriptEnabled")
	public void loginToTwitter() {
		if (!PreferenceManager.getInstance().getTwitterLogin()) {
			TwitterFactory factory = new TwitterFactory();
	        twitter = factory.getInstance();
	        twitter.setOAuthConsumer(Constants.TWITTER_CONSUMER_KEY, Constants.TWITTER_CONSUMER_SECRET);
	        
	        new AsyncTask<Void, Void, Void>() {
	            @Override
	            protected Void doInBackground(Void... params) {
	                try {
	                	requestToken = twitter.getOAuthRequestToken();
	                } catch (TwitterException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
						// possibly internet connection problem
					}
	                return null;
	            }
	
	            @Override
	            protected void onPostExecute(Void result) {
	                super.onPostExecute(result);
	                if (requestToken != null) {
		                String url = requestToken.getAuthenticationURL();
		                
		                final Dialog twitterLoginDialog = new Dialog(MainActivity.this);
		                twitterLoginDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		                
		                WebView webView = new WebView(MainActivity.this);
		                webView.getSettings().setJavaScriptEnabled(true);
		        		webView.loadUrl(url);
		        		webView.setWebViewClient(new WebViewClient() {
		        			
		        			@Override
		        	        public void onPageFinished(WebView view, String url) {
		        				super.onPageFinished(view, url);
		        				if (url.contains(Constants.URL_TWITTER_OAUTH_VERIFIER)) {
		        					Uri uri = Uri.parse(url);
		        					String verifier = uri.getQueryParameter(Constants.URL_TWITTER_OAUTH_VERIFIER);
		        					twitterLoginDialog.dismiss();
		        					MainActivity.this.hideSignInButton();
		        					MainActivity.this.getTwitterId(verifier);
		        				}
		        	        }
		        		});
		        		
		        		
		                twitterLoginDialog.setContentView(webView);
		                twitterLoginDialog.show();
	                }
	            }
	        }.execute();
		} else {
			signInUser();
		}
	}
	
	public void signInUser() {
		final String twitterId = PreferenceManager.getInstance().getTwitterId();
		API.signInUser("5489664", twitterId, new RequestObserver() {
			
			@Override
			public void onSuccess(JSONObject response) throws JSONException {
				if (response.optInt("errorCode", 0) == 0) {
					int tagsCount = response.optInt("tags", 0);
					TabloidAppManager.getInstance().userData = new UserData(twitterId, tagsCount);
					if (tagsCount == 0) {
						if (PreferenceManager.getInstance().isLocalDbInitialized()) {
							startChooseTagsActivity();
						} else {
							getAllTags();
						}
					} else {
						// load user tags
					}
				}
			}
			
			@Override
			public void onError(String response, Exception e) {
				
			}

			@Override
			public void onSuccess(String response) {
				
			}
		});
	}
	
	public void getTwitterId(final String verifier) {
		new AsyncTask<Void, Void, AccessToken>() {

			@Override
			protected AccessToken doInBackground(Void... params) {
				AccessToken accessToken = null;
				try {
					accessToken = twitter.getOAuthAccessToken(requestToken, verifier);
				} catch (TwitterException e) {
					e.printStackTrace();
				}
				return accessToken;
			}

			@Override
			protected void onPostExecute(AccessToken accessToken) {
				super.onPostExecute(accessToken);

				PreferenceManager.getInstance().setTwitterKeyOauthToken(accessToken.getToken());
				PreferenceManager.getInstance().setTwitterKeyOauthSecret(accessToken.getTokenSecret());
				PreferenceManager.getInstance().setTwitterLogin(true);
				PreferenceManager.getInstance().setTwitterId(String.valueOf(accessToken.getUserId()));

				signInUser();
			}

		}.execute();
	}

	public void getAllTags() {
		API.getAllTags(new RequestObserver() {
			
			@Override
			public void onSuccess(JSONObject response) throws JSONException {
				if (!response.has("errorCode")) {
					JSONArray tagsJsonArray = response.getJSONArray("values");
					for (int i = 0; i < tagsJsonArray.length(); i++) {
						Log.d("heghine", "tag = " + tagsJsonArray.getJSONObject(i).getString("name"));
						LocalDbManager.getInstance().insertTag(tagsJsonArray.getJSONObject(i).getString("serverId"), tagsJsonArray.getJSONObject(i).getString("name"));
					}
					PreferenceManager.getInstance().setLocalDbInitialized(true);
					startChooseTagsActivity();
				}
			}
			
			@Override
			public void onError(String response, Exception e) {
				
			}

			@Override
			public void onSuccess(String response) {
				
			}
		});
	}
	
	public void startChooseTagsActivity() {
		MainActivity.this.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				Intent chooseTagsActivity = new Intent(MainActivity.this, ChooseTagsActivity.class);
				startActivity(chooseTagsActivity);
			}
		});
	}
	
	private void hideSignInButton() {
		((LinearLayout) findViewById(R.id.twitter_login_container)).setVisibility(View.GONE);
	}
	
	private void showSignInButton() {
		((LinearLayout) findViewById(R.id.twitter_login_container)).setVisibility(View.VISIBLE);
	}
}
