package de.wikilab.android.friendica01;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.WrapperListAdapter;

import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class MessageViewFragment extends ContentFragment {
	private static final String TAG="Friendica/MessageViewFragment";
	
	PullToRefreshListView reflvw;
	ListView list;
	
	String refreshTarget;
	
	final int ITEMS_PER_PAGE = 20;
	int curLoadPage = 1;
	boolean loadFinished = false;
	
	HashSet<Long> containedIds = new HashSet<Long>();
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		myView = inflater.inflate(R.layout.msg_listviewinner, container, false);
		reflvw = (PullToRefreshListView) myView.findViewById(R.id.listview);
		list = reflvw.getRefreshableView();
		list.setDividerHeight(0);
		
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
		
		list.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				
				Intent inte = new Intent(getActivity(), MessageDetailActivity.class);
				try {
					inte.putExtra("message-uri", ((JSONObject)getListAdapter().getItem(arg2 - 1)).getString("uri"));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				getActivity().startActivity(inte);
				return true;
			}
			
		});
		
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> self, View view, int position, long id) {
				/*if (refreshTarget.equals("notifications")) {
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
				}*/
			}
		});
		
		return myView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (refreshTarget != null && loadFinished) {
			navigate(refreshTarget);
		}
	}
	
	protected void onNavigate(String target) {
		if (curLoadPage == 1) reflvw.setRefreshing();
		refreshTarget = target;
		loadFinished = false;
		
		SendMessage("Loading Animation", Integer.valueOf(View.VISIBLE), null);
		if (target != null && target.startsWith("msg:conversation:")) {
			SendMessage("Set Header Text", "Loading...", null);
			loadMessages(target.substring(17));
		} else {
			SendMessage("Set Header Text", getString(R.string.mm_directmessages), null);
			loadMessages(null);
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
		if (curLoadPage == 1) {
			if (j.length() == 0) {
				reflvw.setVisibility(View.GONE);
				((TextView) myView.findViewById(R.id.lblInfo)).setVisibility(View.VISIBLE);
			}
			ArrayList<JSONObject> jsonObjectArray = new ArrayList<JSONObject>(j.length());
			containedIds.clear();
			for(int i = 0; i < j.length(); i++) {
				JSONObject jj = j.getJSONObject(i);
				jsonObjectArray.add(jj);
				containedIds.add(jj.getLong("id"));
			}
			list.setAdapter(new MessageListAdapter(getActivity(), jsonObjectArray));
		} else {
			PostListAdapter oldContent = getPostListAdapter();
			for(int i = 0; i < j.length(); i++) {
				JSONObject jj = j.getJSONObject(i);
				if (containedIds.contains(jj.getLong("id"))) continue;
				oldContent.add(jj);
				containedIds.add(jj.getLong("id"));
			}
			oldContent.notifyDataSetChanged();
			Toast.makeText(getActivity(), "Done loading more messages - scroll down :)", Toast.LENGTH_SHORT).show();
		}
		loadFinished = true;
	}
	

	public void loadMessages(String parentUri) {
		final TwAjax t = new TwAjax(getActivity(), true, true);
		String target = "all?";
		if (parentUri != null) target= "conversation?uri=" + URLEncoder.encode(parentUri) + "&";
		String url = Max.getServer(getActivity()) + "/api/direct_messages/" + target + "getUserObjects=false&getText=plain&count=" + String.valueOf(ITEMS_PER_PAGE) + "&page=" + String.valueOf(curLoadPage);
		t.getUrlContent(url, new Runnable() {
			@Override public void run() {
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
	
	
	
}
