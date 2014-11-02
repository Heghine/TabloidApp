package com.science.inc.tabloid.manager;

import java.util.ArrayList;
import com.science.inc.tabloid.MainActivity;
import com.science.inc.tabloid.data.TagData;
import com.science.inc.tabloid.data.UserData;

public class TabloidAppManager {
	
	private static TabloidAppManager instance;
	
	private TabloidAppManager() {
		
	}
	
	public static TabloidAppManager getInstance() {
		if (instance == null) {
			instance = new TabloidAppManager();
		}
		
		return instance;
	}
	
	public UserData userData;
	
	public MainActivity mainActivity;
	
	private ArrayList<TagData> allTags = new ArrayList<TagData>();

	public ArrayList<TagData> getAllTags() {
		if (allTags.isEmpty()) {
			allTags = LocalDbManager.getInstance().getAllTags();
		}
		return allTags;
	}

	public void setAllTags(ArrayList<TagData> allTags) {
		this.allTags = allTags;
	}
}
