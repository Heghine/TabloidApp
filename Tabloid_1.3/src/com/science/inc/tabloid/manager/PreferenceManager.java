package com.science.inc.tabloid.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


public class PreferenceManager {
	
	private static PreferenceManager instance;
	private SharedPreferences sharedPreferences;
	private Editor editor;
	
	public static final String NAME = "Tabloid";
	
	public static final String PREFERENCE_TWITTER_KEY_OAUTH_TOKEN = "twitter.key.oauth.token";
	public static final String PREFERENCE_TWITTER_KEY_OAUTH_SECRET = "twitter.key.oauth.secret";
	public static final String PREFERENCE_TWITTER_KEY_LOGIN = "twitter.key.login";
	public static final String PREFERENCE_TWITTER_ID = "twitter.id";
	public static final String PREFERENCE_IS_LOCAL_DB_INITIALIZED = "local.db.initialized";
	
	private PreferenceManager() {
		
	}
	
	public static PreferenceManager getInstance() {
		if (instance == null) {
			instance = new PreferenceManager();
		}
		return instance;
	}
	
	public void init(Context context) {
		sharedPreferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
		editor = sharedPreferences.edit();
	}
	
	public String getTwitterKeyOauthToken() {
		return sharedPreferences.getString(PREFERENCE_TWITTER_KEY_OAUTH_TOKEN, "");
	}
	
	public void setTwitterKeyOauthToken(String value) {
		editor.putString(PREFERENCE_TWITTER_KEY_OAUTH_TOKEN, value).commit();
	}
	
	public String getTwitterKeyOauthSecret() {
		return sharedPreferences.getString(PREFERENCE_TWITTER_KEY_OAUTH_SECRET, "");
	}
	
	public void setTwitterKeyOauthSecret(String value) {
		editor.putString(PREFERENCE_TWITTER_KEY_OAUTH_SECRET, value).commit();
	}
	
	public boolean getTwitterLogin() {
		return sharedPreferences.getBoolean(PREFERENCE_TWITTER_KEY_LOGIN, false);
	}
	
	public void setTwitterLogin(boolean value) {
		editor.putBoolean(PREFERENCE_TWITTER_KEY_LOGIN, value).commit();
	}
	
	public String getTwitterId() {
		return sharedPreferences.getString(PREFERENCE_TWITTER_ID, "");
	}
	
	public void setTwitterId(String value) {
		editor.putString(PREFERENCE_TWITTER_ID, value).commit();
	}
	
	public boolean isLocalDbInitialized() {
		return sharedPreferences.getBoolean(PREFERENCE_IS_LOCAL_DB_INITIALIZED, false);
	}
	
	public void setLocalDbInitialized(boolean value) {
		editor.putBoolean(PREFERENCE_IS_LOCAL_DB_INITIALIZED, value).commit();
	}
}
