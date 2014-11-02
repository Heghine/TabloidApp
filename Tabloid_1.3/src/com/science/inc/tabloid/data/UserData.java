package com.science.inc.tabloid.data;

import android.util.SparseArray;

public class UserData {
	
	public String userId;
	public String twitterId;
	public String deviceId;
	
	public int tagCount;
	public SparseArray<TagData> userTags = new SparseArray<TagData>();
	
	public UserData(String userId, String twitterId, String deviceId) {
		this.userId = userId;
		this.twitterId = twitterId;
		this.deviceId = deviceId;
	}
	
	public UserData(String twitterId, int tagCount) {
		this.twitterId = twitterId;
		this.tagCount = tagCount;
	}
}
