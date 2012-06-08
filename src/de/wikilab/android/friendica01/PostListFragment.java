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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class PostListFragment extends Fragment {
	ListView list;
	
	String navigateOrder = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		list = (ListView) inflater.inflate(R.layout.pl_listviewinner, container);
		
		return list;
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
		if (!isInLayout()) {
			navigateOrder = target;
			return;
		}
		if (target != null && target.equals("mywall")) {
			((FragmentParentListener)getActivity()).OnFragmentMessage("Set Header Text", getString(R.string.mm_mywall), null);
			loadWall();
		} else if (target != null && target.equals("notifications")) {
			((FragmentParentListener)getActivity()).OnFragmentMessage("Set Header Text", getString(R.string.mm_notifications), null);
			loadNotifications();
		} else {
			((FragmentParentListener)getActivity()).OnFragmentMessage("Set Header Text", getString(R.string.mm_timeline), null);
			loadTimeline();
		}
	}
	
	public void loadTimeline() {

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		String server = prefs.getString("login_server", null);
		
		final TwAjax t = new TwAjax(getActivity(), true, true);
		t.getUrlContent("http://" + server + "/api/statuses/home_timeline.json", new Runnable() {
			@Override
			public void run() {
				try {
					JSONArray j = (JSONArray) t.getJsonResult();
					JSONObject[] jsonObjectArray = new JSONObject[j.length()];
					
					for(int i = 0; i < j.length(); i++) {
						jsonObjectArray[i] = j.getJSONObject(i);
					}
					
					//ListView lvw = (ListView) findViewById(R.id.listview);
					
					list.setAdapter(new PostListAdapter(getActivity(), jsonObjectArray));
					
				} catch (Exception e) {

					//ListView lvw = (ListView) findViewById(R.id.listview);
					
					list.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_2, android.R.id.text2, new String[]{"Error: ", e.getMessage(), t.getResult()}));
					
					e.printStackTrace();
				}
			}
		});
		
	}
	

	public void loadWall() {

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		String server = prefs.getString("login_server", null);
		
		final TwAjax t = new TwAjax(getActivity(), true, true);
		t.getUrlContent("http://" + server + "/api/statuses/user_timeline.json", new Runnable() {
			@Override
			public void run() {
				try {
					JSONArray j = (JSONArray) t.getJsonResult();
					JSONObject[] jsonObjectArray = new JSONObject[j.length()];
					
					for(int i = 0; i < j.length(); i++) {
						jsonObjectArray[i] = j.getJSONObject(i);
					}
					
					//ListView lvw = (ListView) findViewById(R.id.listview);
					
					list.setAdapter(new PostListAdapter(getActivity(), jsonObjectArray));
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
	}
	
	

	void loadNotifications() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		String server = prefs.getString("login_server", null);
		
		final TwAjax t = new TwAjax(getActivity(), true, true);
		t.getUrlXmlDocument("http://" + server + "/ping", new Runnable() {
			@Override
			public void run() {
				try {
					Document xd = t.getXmlDocumentResult();
					Node el = xd.getElementsByTagName("notif").item(0);
					ArrayList<Notification> notifs = new ArrayList<Notification>();
					
					for(int i = 0; i < el.getChildNodes().getLength(); i++) {
						if (el.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE) {
							notifs.add(Notification.fromXmlNode(el.getChildNodes().item(i)));
						}
					}
					
					//ListView lvw = (ListView) findViewById(R.id.listview);
					
					list.setAdapter(new Notification.NotificationsListAdapter(getActivity(), notifs));
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
	}
	
	
	
}
