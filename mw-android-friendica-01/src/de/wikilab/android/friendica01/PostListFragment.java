
package de.wikilab.android.friendica01;

import java.util.ArrayList;
import java.util.HashSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.WrapperListAdapter;

import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class PostListFragment extends ContentFragment {
	private static final String TAG="Friendica/PostListFragment";
	
	PullToRefreshListView reflvw;
	ListView list;
	ListAdapter ad;
	
	String refreshTarget;
	
	final int ITEMS_PER_PAGE = 20;
	int curLoadPage = 1;
	boolean loadFinished = false;
	
	HashSet<Long> containedIds = new HashSet<Long>();
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		myView = inflater.inflate(R.layout.pl_listviewinner, container, false);
		reflvw = (PullToRefreshListView) myView.findViewById(R.id.listview);
		list = reflvw.getRefreshableView();
		
		Log.d(TAG,  "==> onCreateView ");
		
		reflvw.setOnRefreshListener(new OnRefreshListener() {
		    @Override
		    public void onRefresh() {
		    	if (loadFinished) {
			    	curLoadPage = 1;
			        onNavigate(refreshTarget);
		    	}
		    }
		});
		
		reflvw.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {
			@Override
			public void onLastItemVisible() {
				if (loadFinished && getPostListAdapter() != null) {
					Toast.makeText(getActivity(), "Loading more items...", Toast.LENGTH_SHORT).show();
					curLoadPage ++;
					onNavigate(refreshTarget);
				} else {
					Log.i(TAG, "OnLastItemVisibleListener -- skip! lf="+loadFinished+" ad:"+list.getAdapter().getClass().toString());
				}
			}
		});
		
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> self, View view, int position, long id) {
				if (refreshTarget.equals("notifications")) {
					SendMessage("Loading Animation", Integer.valueOf(View.VISIBLE), null);
					final Notification n = ((Notification.NotificationsListAdapter)getListAdapter()).getItem(position-1);
					n.resolveTarget(getActivity(), new Runnable() {
						@Override public void run() {
							SendMessage("Loading Animation", Integer.valueOf(View.INVISIBLE), null);
							if (n.targetComponent != null && n.targetComponent.equals("conversation:")) {
								SendMessage("Navigate Conversation", String.valueOf(n.targetData), null);
							} else {
								Max.alert(getActivity(), "Unable to navigate to notification target<br><br><a href='" + n.targetUrl + "'>" + n.targetUrl + "</a>", "Not implemented");
							}
						}
					});
				} else {
					SendMessage("Navigate Conversation", String.valueOf(id), null);
				}
			}
		});

		if (ad != null && getPostListAdapter() == null) {
			//navigate(refreshTarget);
			list.setAdapter(ad);
		}
		
		if (savedInstanceState != null && savedInstanceState.containsKey("listviewState")) {
			list.onRestoreInstanceState(savedInstanceState.getParcelable("listviewState"));
		}
		
		return myView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putParcelable("listviewState", list.onSaveInstanceState());
		super.onSaveInstanceState(outState);
	}
	
	
	
	@Override
	public void onStart() {
		super.onStart();

		Log.d(TAG,  "==> onStart ");
	}
	
	protected void onNavigate(String target) {
		/*if (myView != null) {
			list.setVisibility(View.GONE);
			progbar.setVisibility(View.VISIBLE);
		}*/
		if (curLoadPage == 1) reflvw.setRefreshing();
		refreshTarget = target;
		loadFinished = false;
		
		SendMessage("Loading Animation", Integer.valueOf(View.VISIBLE), null);
		if (target != null && target.equals("mywall")) {
			SendMessage("Set Header Text", getString(R.string.mm_mywall), null);
			loadWall(null);
		} else if (target != null && target.startsWith("userwall:")) {
			SendMessage("Set Header Text", getString(R.string.mm_mywall), null);
			loadWall(target.substring(9));
		} else if (target != null && target.equals("notifications")) {
			SendMessage("Set Header Text", getString(R.string.mm_notifications), null);
			loadNotifications();
		} else {
			SendMessage("Set Header Text", getString(R.string.mm_timeline), null);
			loadTimeline();
		}
	}
	
	public void hideProgBar() {
		/*reflvw.setAddStatesFromChildren(addsStates)
		list.setVisibility(View.VISIBLE);
		progbar.setVisibility(View.GONE);*/
		if (curLoadPage == 1) reflvw.onRefreshComplete();

		SendMessage("Loading Animation", Integer.valueOf(View.INVISIBLE), null);
		
	}

	private PostListAdapter getPostListAdapter() {
		Adapter a = getListAdapter();
		if (a instanceof PostListAdapter) return (PostListAdapter)a;
		return null;
	}

	private Adapter getListAdapter() {
		Adapter a = list.getAdapter();
		if (a instanceof WrapperListAdapter) a = ((WrapperListAdapter)a).getWrappedAdapter();
		return a;
	}
	
	private void setItems(JSONArray j) throws JSONException {
		if (curLoadPage == 1 || getPostListAdapter() == null) {
			ArrayList<JSONObject> jsonObjectArray = new ArrayList<JSONObject>(j.length());
			containedIds.clear();
			for(int i = 0; i < j.length(); i++) {
				JSONObject jj = j.getJSONObject(i);
				jsonObjectArray.add(jj);
				containedIds.add(jj.getLong("id"));
			}
			ad = new PostListAdapter(getActivity(), jsonObjectArray);
			list.setAdapter(ad);
		} else {
			PostListAdapter oldContent = getPostListAdapter();
			for(int i = 0; i < j.length(); i++) {
				JSONObject jj = j.getJSONObject(i);
				if (containedIds.contains(jj.getLong("id"))) continue;
				oldContent.add(jj);
				containedIds.add(jj.getLong("id"));
			}
			oldContent.notifyDataSetChanged();
			Toast.makeText(getActivity(), "Done loading more items - scroll down :)", Toast.LENGTH_SHORT).show();
		}
		loadFinished = true;
	}
	
	public void loadTimeline() {
		final TwAjax t = new TwAjax(getActivity(), true, true);
		int ipp = ITEMS_PER_PAGE;
		int cp = curLoadPage;
		if (getPostListAdapter() == null) {
			ipp = ITEMS_PER_PAGE * curLoadPage;
			cp = 1;
		}
		t.getUrlContent(Max.getServer(getActivity()) + "/api/statuses/home_timeline.json?count=" + String.valueOf(ipp) + "&page=" + String.valueOf(cp), new Runnable() {
			@Override public void run() {
				try {
					JSONArray j = (JSONArray) t.getJsonResult();

					setItems(j);
					
				} catch (Exception e) {
					list.setAdapter(new ArrayAdapter<String>(
						getActivity(), R.layout.pl_error_listitem, android.R.id.text1, 
							new String[]{
								t.getURL(),
								"Error: "+ e.getMessage(), 
								Max.getStackTrace(e), 
								Max.Hexdump(t.getResult().getBytes())
							}
					));
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
			@Override public void run() {
				try {
					JSONArray j = (JSONArray) t.getJsonResult();

					setItems(j);
					
				} catch (Exception e) {
					if (list != null)list.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.pl_error_listitem, android.R.id.text1, new String[]{"Error: "+ e.getMessage(), Max.Hexdump(t.getResult().getBytes())}));
					e.printStackTrace();
				}
				hideProgBar();
			}
		});
		
	}
	
	

	void loadNotifications() {
		final TwAjax t = new TwAjax(getActivity(), true, true);
		t.getUrlXmlDocument(Max.getServer(getActivity()) + "/ping", new Runnable() {
			@Override public void run() {
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
					ad = new Notification.NotificationsListAdapter(getActivity(), notifs);
					list.setAdapter(ad);
					
				} catch (Exception e) {
					if (list != null) list.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.pl_error_listitem, android.R.id.text1, new String[]{"Error: "+ e.getMessage(), Max.Hexdump(t.getResult().getBytes())}));
					e.printStackTrace();
				}
				hideProgBar();
			}
		});
		
	}
	
	
	
}
