package com.science.inc.tabloid;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.TextView;
import android.widget.Toast;

import com.science.inc.tabloid.data.TagData;
import com.science.inc.tabloid.manager.TabloidAppManager;

public class FeedActivity extends Activity {

	private AutocompleteCustomArrayAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_feed);

		adapter = new AutocompleteCustomArrayAdapter(this, TabloidAppManager.getInstance().getAllTags());

		AutoCompleteTextView searchAutoComplete = ((AutoCompleteTextView) findViewById(R.id.search_auto_complete));
		searchAutoComplete.setAdapter(adapter);
		searchAutoComplete.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				TagData tag = adapter.suggestions.get(position);
				TabloidAppManager.getInstance().userData.userTags.put(position, tag);
				Toast t = Toast.makeText(getApplicationContext(), getResources().getString(R.string.successfully_added), Toast.LENGTH_LONG);
				t.setGravity(Gravity.CENTER, 0, 0);
				t.show();
			}
		});
	}

	public class AutocompleteCustomArrayAdapter extends ArrayAdapter<TagData> {

		private Context context;
		private ArrayList<TagData> items;
		private ArrayList<TagData> itemsAll;
		public ArrayList<TagData> suggestions;

		public AutocompleteCustomArrayAdapter(Context mContext, ArrayList<TagData> tags) {
			super(mContext, R.layout.item_auto_complete, tags);
			this.context = mContext;
			this.items = tags;
			this.itemsAll = (ArrayList<TagData>) tags.clone();
			this.suggestions = new ArrayList<TagData>();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.item_auto_complete, parent, false);
			}
			TagData tag = getItem(position);

			TextView textViewItem = (TextView) convertView.findViewById(R.id.suggested_tag_txt);
			textViewItem.setText(tag.name);

			return convertView;
		}

		@Override
		public Filter getFilter() {
			return nameFilter;
		}

		Filter nameFilter = new Filter() {
			@Override
			public String convertResultToString(Object resultValue) {
				String str = ((TagData) (resultValue)).name;
				return str;
			}

			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				if (constraint != null) {
					suggestions.clear();
					for (TagData tag : itemsAll) {
						String tagName = tag.name;
						String[] names = tagName.split(" ");
						for (int i = 0; i < names.length; i++) {
							if (names[i].toLowerCase().startsWith(constraint.toString().toLowerCase())) {
								suggestions.add(tag);
								break;
							}
						}
						
					}
					FilterResults filterResults = new FilterResults();
					filterResults.values = suggestions;
					filterResults.count = suggestions.size();
					return filterResults;
				} else {
					return new FilterResults();
				}
			}

			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				ArrayList<TagData> suggestedTags = (ArrayList<TagData>) results.values;
				if (results != null && results.count > 0) {
					clear();
					for (TagData c : suggestedTags) {
						add(c);
					}
					notifyDataSetChanged();
				}
			}

		};

	}
}
