package com.science.inc.tabloid;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.science.inc.tabloid.backend.API;
import com.science.inc.tabloid.backend.API.RequestObserver;
import com.science.inc.tabloid.data.TagData;
import com.science.inc.tabloid.manager.TabloidAppManager;
import com.science.inc.tabloid.util.Constants;
import com.science.inc.tabloid.view.RoundImage;

public class ChooseTagsActivity extends Activity {
	
	private HashMap<String, RoundImage> images = new HashMap<String, RoundImage>();
	private ImageAdapter gridViewAdapter;
	private boolean hasChoosingDone;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		final ArrayList<TagData> tagsToChoose = getTagsList(0);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_choose_tags);
		
		GridView gridView = (GridView) findViewById(R.id.choose_tags_grid_view);
		gridViewAdapter = new ImageAdapter(this, tagsToChoose);
	    gridView.setAdapter(gridViewAdapter);
	    gridView.setOnItemClickListener(new OnItemClickListener() {
	        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	            if (TabloidAppManager.getInstance().userData.userTags.get(position) != null) {
	            	TabloidAppManager.getInstance().userData.userTags.remove(position);
	            	v.setBackgroundResource(R.drawable.image_view_bg_not_choosen);
	            } else {
	            	v.setBackgroundResource(R.drawable.image_view_bg_choosen);
	            	TabloidAppManager.getInstance().userData.userTags.put(position, tagsToChoose.get(position));
	            }
	        }
	    });
	    
	    ((Button) findViewById(R.id.okay_btn)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (!hasChoosingDone) {
					hasChoosingDone = true;
					resetGridView();
					ArrayList<TagData> tagsToChoose = getTagsList(6);
					gridViewAdapter.tags = tagsToChoose;
					gridViewAdapter.notifyDataSetChanged();
				} else {
					addUserTags();
				}
			}
		});
	}
	
	public void addUserTags() {
		JSONArray tagsJsonArr = new JSONArray(Arrays.asList(TabloidAppManager.getInstance().getAllTags()));
		JSONObject tagsObj = new JSONObject();
		try {
			tagsObj.put("userTags", tagsJsonArr);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		API.addUserTags(tagsObj, new RequestObserver() {
			
			@Override
			public void onSuccess(JSONObject response) throws JSONException {
				TabloidAppManager.getInstance().mainActivity.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						startFeedActivity();
					}
				});
			}
			
			@Override
			public void onSuccess(String response) {
				Log.d("heghine", response);
			}
			
			@Override
			public void onError(String response, Exception e) {
				
			}
		});
	}
	
	private void startFeedActivity() {
		Intent chooseTagsActivity = new Intent(ChooseTagsActivity.this, FeedActivity.class);
		startActivity(chooseTagsActivity);
	}
	
	public void resetGridView() {
		GridView gridView = (GridView) findViewById(R.id.choose_tags_grid_view);
		for (int i = 0; i < Constants.CHOOSE_TAG_COUNT; i++) {
			gridView.getChildAt(i).setBackgroundResource(R.drawable.image_view_bg_not_choosen);
			
//			set background no image
//			gridView.getChildAt(i).findViewById(R.id.choose_tag_img)
		}
		
		((TextView) findViewById(R.id.title_txt)).setText(getResources().getString(R.string.what_about_these));
	}
	
	public ArrayList<TagData> getTagsList(int startIndex) {
		ArrayList<TagData> tagsToChoose = new ArrayList<TagData>();
		for (int i = startIndex; i < startIndex + Constants.CHOOSE_TAG_COUNT; i++) {
			tagsToChoose.add(TabloidAppManager.getInstance().getAllTags().get(i));
		}
		
		return tagsToChoose;
	}
	
	public class ImageAdapter extends BaseAdapter {
	    private Context context;
	    public ArrayList<TagData> tags;
	    
	    public ImageAdapter(Context c, ArrayList<TagData> t) {
	        context = c;
	        tags = t;
	    }

	    public int getCount() {
	        return tags.size();
	    }

	    public TagData getItem(int position) {
	        return tags.get(position);
	    }

	    public long getItemId(int position) {
	        return 0;
	    }

	    public View getView(int position, View convertView, ViewGroup parent) {
	    	ImageView imageView;
	    	if (convertView == null) {
				LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				imageView = (ImageView) li.inflate(R.layout.item_choose_tag, parent, false);
			} else {
				imageView = (ImageView) convertView;
			}
	    	TagData tag = getItem(position);
	    	if (images.get(tag.serverId) == null) {
		    	String url = Constants.IMAGE_URL + tag.serverId + ".jpg";
		    	(new TagImageLoader(imageView, tag.serverId)).execute(url);
	    	} else {
	    		imageView.setImageDrawable(images.get(tag.serverId));
	    	}
	    	
	        return imageView;
	    }
	}
	
	public class TagImageLoader extends AsyncTask<String, Void, Bitmap> {
		
		private ImageView imageView;
		private String serverId;
		
		public TagImageLoader(ImageView imageView, String serverId) {
			this.imageView = imageView;
			this.serverId = serverId;
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			String url = params[0];
			try {
		        InputStream is = (InputStream) new URL(url).getContent();
		        Bitmap d = BitmapFactory.decodeStream(is);
		        is.close();
		        return d;
		    } catch (Exception e) {
		        return null;
		    }
		}
		
		@Override
		protected void onPostExecute(Bitmap result) {
			if (result != null) {
				RoundImage roundedImage = new RoundImage(result);
				imageView.setImageDrawable(roundedImage);
				images.put(serverId, roundedImage);
			} else {
				// set default image
			}
		}
	}

}
