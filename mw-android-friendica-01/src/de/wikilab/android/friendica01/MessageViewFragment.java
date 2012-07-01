package de.wikilab.android.friendica01;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
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
import android.widget.Button;
import android.widget.EditText;
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
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int position, long arg3) {
				
				new AlertDialog.Builder(getActivity())
				.setTitle("Message actions...")
				.setItems(new CharSequence[] {"View Details", "View Conversation", "Reply", "Delete"}, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch(which) {
						case 0:
							try {
								Intent inte = new Intent(getActivity(), MessageDetailActivity.class);
								inte.putExtra("message-uri", ((JSONObject)getListAdapter().getItem(position - 1)).getString("uri"));
								getActivity().startActivity(inte);
							} catch (JSONException e) {
								e.printStackTrace();
							}
							break;
						case 1:
						case 2:
							try {
								//show conv. view which contains reply box
								Intent inte = new Intent(getActivity(), GenericContentActivity.class);
								inte.putExtra("target", "msg:conversation:" + ((JSONObject)getListAdapter().getItem(position - 1)).getString("parent"));
								getActivity().startActivity(inte);
							} catch (JSONException e) {
								e.printStackTrace();
							}
							break;
						case 3:
							//missing api
							Toast.makeText(getActivity(), "Sorry, Friendica API doesn't support this yet...", Toast.LENGTH_LONG).show();
							break;
						}
						dialog.dismiss();
					}
				})
				.show();
				
				return true;
			}
			
		});
		
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> self, View view, int position, long id) {
				
			}
		});
		
		((Button) myView.findViewById(R.id.btn_upload)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendReplyMessage();
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
	
	protected void sendReplyMessage() {
		if (refreshTarget == null ||!refreshTarget.startsWith("msg:conversation:")) return;
		
		String replyId = refreshTarget.substring(17);
		
		String message = ((EditText) myView.findViewById(R.id.maintb)).getText().toString();
		
		
		
	}
	
	protected void onNavigate(String target) {
		if (curLoadPage == 1) reflvw.setRefreshing();
		refreshTarget = target;
		loadFinished = false;
		
		SendMessage("Loading Animation", Integer.valueOf(View.VISIBLE), null);
		if (target != null && target.startsWith("msg:conversation:")) {
			SendMessage("Set Header Text", "Loading...", null);
			loadMessages(target.substring(17));
			myView.findViewById(R.id.top_control_bar).setVisibility(View.GONE);
		} else {
			SendMessage("Set Header Text", getString(R.string.mm_directmessages), null);
			loadMessages(null);
			myView.findViewById(R.id.comment_box).setVisibility(View.GONE);
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
