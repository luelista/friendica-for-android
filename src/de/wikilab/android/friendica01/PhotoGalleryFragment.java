package de.wikilab.android.friendica01;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

public class PhotoGalleryFragment extends ContentFragment {
	GridView list;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		myView = inflater.inflate(R.layout.photogalleryinner, container, false);
		list = (GridView) myView.findViewById(R.id.gridview);
		
		((TextView) myView.findViewById(R.id.fake_data_notice)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((TextView) myView.findViewById(R.id.fake_data_notice)).setVisibility(View.GONE);
			}
		});
		
		
		return myView;
	}
	
	protected void onNavigate(String target) {
		if (target != null && target.equals("mywall")) {
			((FragmentParentListener)getActivity()).OnFragmentMessage("Set Header Text", getString(R.string.mm_mywall), null);
			//loadWall();
		} else if (target != null && target.equals("notifications")) {
			((FragmentParentListener)getActivity()).OnFragmentMessage("Set Header Text", getString(R.string.mm_notifications), null);
			//loadNotifications();
		} else {
			((FragmentParentListener)getActivity()).OnFragmentMessage("Set Header Text", getString(R.string.mm_myphotoalbums), null);
			loadExampleGallery();
		}
	}
	
	public void loadExampleGallery() {
		final TwAjax t = new TwAjax(getActivity(), true, false);
		t.getUrlContent("http://www.panoramio.com/map/get_panoramas.php?set=4832719&from=0&to=20&minx=-180&miny=-90&maxx=180&maxy=90&size=small", new Runnable() {
			@Override
			public void run() {
				try {
					JSONArray j = (JSONArray) ((JSONObject)t.getJsonResult()).getJSONArray("photos");
					PhotoGalleryAdapter.Pic[] picArray = new PhotoGalleryAdapter.Pic[j.length()];
					
					for(int i = 0; i < j.length(); i++) {
						JSONObject d = j.getJSONObject(i);
						picArray[i] = new PhotoGalleryAdapter.Pic(d.getString("photo_file_url"), d.getString("photo_title"), null, null);
					}
					
					//ListView lvw = (ListView) findViewById(R.id.listview);
					
					list.setAdapter(new PhotoGalleryAdapter(getActivity(), R.layout.photogallery_item, picArray));
					
				} catch (Exception e) {

					//ListView lvw = (ListView) findViewById(R.id.listview);
					
					list.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_2, android.R.id.text2, new String[]{"Error: ", e.getMessage(), t.getResult()}));
					
					e.printStackTrace();
				}
			}
		});
		
	}
	

	
	
}
