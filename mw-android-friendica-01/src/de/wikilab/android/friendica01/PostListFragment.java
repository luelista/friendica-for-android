package de.wikilab.android.friendica01;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class PostListFragment extends ContentFragment {
	private static final String TAG="Friendica/PostListFragment";
	
	PullToRefreshListView reflvw;
	ListView list;
	ProgressBar progbar;
	
	String refreshTarget;
	
	final int ITEMS_PER_PAGE = 20;
	int curLoadPage = 0;
	boolean loadFinished = false;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		myView = inflater.inflate(R.layout.pl_listviewinner, container, false);
		reflvw = (PullToRefreshListView) myView.findViewById(R.id.listview);
		list = reflvw.getRefreshableView();
		
		progbar = (ProgressBar) myView.findViewById(R.id.progressbar);
		
		reflvw.setOnRefreshListener(new OnRefreshListener() {
		    @Override
		    public void onRefresh() {
		    	if (loadFinished) {
			    	curLoadPage = 0;
			        onNavigate(refreshTarget);
		    	}
		    }
		});
		
		reflvw.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {
			@Override
			public void onLastItemVisible() {
				if (loadFinished && list.getAdapter() instanceof PostListAdapter) {
					Toast.makeText(getActivity(), "OnLastItemVisibleListener -- loading", Toast.LENGTH_SHORT).show();
					curLoadPage ++;
					onNavigate(refreshTarget);
				} else {
					Log.i(TAG, "OnLastItemVisibleListener -- skip! lf="+loadFinished+" ad:"+list.getAdapter().getClass().toString());
				}
			}
		});
		
		return myView;
	}
	
	protected void onNavigate(String target) {
		/*if (myView != null) {
			list.setVisibility(View.GONE);
			progbar.setVisibility(View.VISIBLE);
		}*/
		reflvw.setRefreshing();
		refreshTarget = target;
		loadFinished = false;
		if (target != null && target.equals("mywall")) {
			((FragmentParentListener)getActivity()).OnFragmentMessage("Set Header Text", getString(R.string.mm_mywall), null);
			loadWall(null);
		} else if (target != null && target.startsWith("userwall:")) {
			((FragmentParentListener)getActivity()).OnFragmentMessage("Set Header Text", getString(R.string.mm_mywall), null);
			loadWall(target.substring(9));
		} else if (target != null && target.equals("notifications")) {
			((FragmentParentListener)getActivity()).OnFragmentMessage("Set Header Text", getString(R.string.mm_notifications), null);
			loadNotifications();
		} else {
			((FragmentParentListener)getActivity()).OnFragmentMessage("Set Header Text", getString(R.string.mm_timeline), null);
			loadTimeline();
		}
	}
	
	public void hideProgBar() {
		/*reflvw.setAddStatesFromChildren(addsStates)
		list.setVisibility(View.VISIBLE);
		progbar.setVisibility(View.GONE);*/
		reflvw.onRefreshComplete();
		
	}
	
	private void setItems(JSONArray j) throws JSONException {
		if (curLoadPage == 0) {
			JSONObject[] jsonObjectArray;
			jsonObjectArray = new JSONObject[j.length()];
			for(int i = 0; i < j.length(); i++) {
				jsonObjectArray[i] = j.getJSONObject(i);
			}
			list.setAdapter(new PostListAdapter(getActivity(), jsonObjectArray));
		} else {
			PostListAdapter oldContent = (PostListAdapter)list.getAdapter();
			for(int i = 0; i < j.length(); i++) {
				oldContent.add(j.getJSONObject(i));
			}
			oldContent.notifyDataSetChanged();
		}
		loadFinished = true;
	}
	
	public void loadTimeline() {
		final TwAjax t = new TwAjax(getActivity(), true, true);
		t.getUrlContent(Max.getServer(getActivity()) + "/api/statuses/home_timeline.json?count=" + String.valueOf(ITEMS_PER_PAGE) + "&page=" + String.valueOf(curLoadPage), new Runnable() {
			@Override
			public void run() {
				try {
					JSONArray j = (JSONArray) t.getJsonResult();

					setItems(j);
					
				} catch (Exception e) {
					list.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.pl_error_listitem, android.R.id.text1, new String[]{"Error: "+ e.getMessage(), Max.Hexdump(t.getResult().getBytes())}));
					e.printStackTrace();
				}
				hideProgBar();
			}
		});
		
	}
	

	public void loadWall(String userId) {
		final TwAjax t = new TwAjax(getActivity(), true, true);
		String url = Max.getServer(getActivity()) + "/api/statuses/user_timeline.json?count=" + String.valueOf(ITEMS_PER_PAGE) + "&page=" + String.valueOf(curLoadPage);
		if (userId != null) url += "&user_id=" + userId;
		t.getUrlContent(url, new Runnable() {
			@Override
			public void run() {
				try {
					JSONArray j = (JSONArray) t.getJsonResult();

					setItems(j);
					
				} catch (Exception e) {
					list.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.pl_error_listitem, android.R.id.text1, new String[]{"Error: "+ e.getMessage(), Max.Hexdump(t.getResult().getBytes())}));
					e.printStackTrace();
				}
				hideProgBar();
			}
		});
		
	}
	
	

	void loadNotifications() {
		final TwAjax t = new TwAjax(getActivity(), true, true);
		t.getUrlXmlDocument(Max.getServer(getActivity()) + "/ping", new Runnable() {
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
					list.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.pl_error_listitem, android.R.id.text1, new String[]{"Error: "+ e.getMessage(), Max.Hexdump(t.getResult().getBytes())}));
					e.printStackTrace();
				}
				hideProgBar();
			}
		});
		
	}
	
	
	
}
