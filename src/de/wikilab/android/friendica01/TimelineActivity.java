package de.wikilab.android.friendica01;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ListView;

public class TimelineActivity extends Activity {
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.tl_listview);
		loadTimeline();
	}
	
	void loadTimeline() {

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String server = prefs.getString("login_server", null);
		
		final TwAjax t = new TwAjax(this, true, true);
		t.getUrlContent("http://" + server + "/api/statuses/home_timeline.json", new Runnable() {
			@Override
			public void run() {
				try {
					JSONArray j = (JSONArray) t.getJsonResult();
					JSONObject[] jsonObjectArray = new JSONObject[j.length()];
					
					for(int i = 0; i < j.length(); i++) {
						jsonObjectArray[i] = j.getJSONObject(i);
					}
					
					ListView lvw = (ListView) findViewById(R.id.listview);
					
					lvw.setAdapter(new PostListAdapter(TimelineActivity.this, jsonObjectArray));
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
	}
	
	
	
	
}
