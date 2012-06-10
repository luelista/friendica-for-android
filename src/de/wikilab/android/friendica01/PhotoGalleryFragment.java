package de.wikilab.android.friendica01;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;

public class PhotoGalleryFragment extends Fragment {
	GridView list;
	View myView;
	
	String navigateOrder = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		myView = inflater.inflate(R.layout.photogalleryinner, container, false);
		list = (GridView) myView.findViewById(R.id.gridview);
		return myView;
	}
	/*
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.tl_listview);
		
		TextView header_logo = (TextView) findViewById(R.id.header_logo);
		header_logo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		loadTimeline();
	}
*/
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (navigateOrder != null) navigateList(navigateOrder); navigateOrder = null;
	}
	
	public void navigateList(String target) {
		if (!isAdded()) {
			navigateOrder = target;
			return;
		}
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
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		String server = prefs.getString("login_server", null);
		
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
					
					list.setAdapter(new PhotoGalleryAdapter(getActivity(), picArray));
					
				} catch (Exception e) {

					//ListView lvw = (ListView) findViewById(R.id.listview);
					
					list.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_2, android.R.id.text2, new String[]{"Error: ", e.getMessage(), t.getResult()}));
					
					e.printStackTrace();
				}
			}
		});
		
	}
	

	
	
}
