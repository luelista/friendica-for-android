package de.wikilab.android.friendica01.fragment;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshListView;

import de.wikilab.android.friendica01.Max;
import de.wikilab.android.friendica01.R;
import de.wikilab.android.friendica01.TwAjax;
import de.wikilab.android.friendica01.R.id;
import de.wikilab.android.friendica01.R.layout;
import de.wikilab.android.friendica01.adapter.PostListAdapter;

public class PostDetailFragment extends ContentFragment {
	private static final String TAG="Friendica/PostDetailFragment";
	

	PullToRefreshListView reflvw;
	ListView list;

	String refreshTarget;
	

	String conversationId;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		myView = inflater.inflate(R.layout.pd_listviewinner, container, false);
		reflvw = (PullToRefreshListView) myView.findViewById(R.id.listview);
		list = reflvw.getRefreshableView();
		
		((Button) myView.findViewById(R.id.btn_upload)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sendComment();
			}
		});
		
		return myView;
	}
	
	protected void sendComment() {
		SendMessage("Loading Animation", Integer.valueOf(View.VISIBLE), null);
		final TwAjax t = new TwAjax(getActivity(), true, true);
		t.addPostData("status", ((EditText) myView.findViewById(R.id.maintb)).getText().toString());
		t.addPostData("source", "<a href='http://friendica-for-android.wiki-lab.net'>Friendica for Android</a>");
		t.addPostData("in_reply_to_status_id", conversationId);
		((Button) myView.findViewById(R.id.btn_upload)).setEnabled(false);
		t.postData(Max.getServer(getActivity()) + "/api/statuses/update", new Runnable() {
			@Override
			public void run() {
				((EditText) myView.findViewById(R.id.maintb)).setText("");
				((Button) myView.findViewById(R.id.btn_upload)).setEnabled(true);
				loadComments();
			}
		});
	}

	protected void onNavigate(String target) {
		if (myView != null) {
			
		}
		SendMessage("Loading Animation", Integer.valueOf(View.VISIBLE), null);
		if (target != null && target.startsWith("conversation:")) {
			conversationId = target.substring(13);
			
			SendMessage("Set Header Text", "Post Details ("+String.valueOf(conversationId)+")", null);
			loadInitialPost();
		}
	}

	public void hideProgBar() {
		SendMessage("Loading Animation", Integer.valueOf(View.INVISIBLE), null);
	}

	public void loadInitialPost() {
		final TwAjax t = new TwAjax(getActivity(), true, true);
		t.getUrlContent(Max.getServer(getActivity()) + "/api/statuses/show/" + conversationId, new Runnable() {
			@Override
			public void run() {
				try {
					ArrayList<JSONObject> jsonObjectArray = new ArrayList<JSONObject>();
					JSONObject jj = (JSONObject)t.getJsonResult();
					
					jsonObjectArray.add(jj);
					
					//ListView lvw = (ListView) findViewById(R.id.listview);
					
					list.setAdapter(new PostListAdapter(getActivity(), jsonObjectArray));
					
					if (jj.has("statusnet_conversation_id") && jj.getString("statusnet_conversation_id").equals("0") == false) {
						conversationId = jj.getString("statusnet_conversation_id");
					}
					
					loadComments();
					
					
				} catch (Exception e) {
					list.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.pl_error_listitem, android.R.id.text1, new String[]{"Error: "+ e.getMessage(), Max.Hexdump(t.getResult().getBytes())}));
					e.printStackTrace();
				}
			}
		});

	}


	public void loadComments() {
		final TwAjax t = new TwAjax(getActivity(), true, true);
		t.getUrlContent(Max.getServer(getActivity()) + "/api/statuses/show/" + conversationId + "?conversation=true", new Runnable() {
			@Override
			public void run() {
				try {
					ArrayList<JSONObject> jsonObjectArray = new ArrayList<JSONObject>();
					Object obj = t.getJsonResult();
					if (obj instanceof JSONArray) {
						JSONArray j = (JSONArray)obj;
						
						for(int i = 0; i < j.length(); i++)	jsonObjectArray.add(j.getJSONObject(i));
						
						//ListView lvw = (ListView) findViewById(R.id.listview);
						PostListAdapter pla = new PostListAdapter(getActivity(), jsonObjectArray);
						pla.isPostDetails=true;
						list.setAdapter(pla);
					} else {
						Max.alert(getActivity(), "Sorry, your Friendica server doesn't support conversation view!<br><br>Refer to this page for more information: <a href='http://friendica-for-android.wiki-lab.net/notes#conv-view-note'>http://friendica-for-android.wiki-lab.net/notes#conv-view-note</a>");
					}
					
				} catch (Exception e) {
					e.printStackTrace();
					if (list != null) list.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.pl_error_listitem, android.R.id.text1, new String[]{"Error: "+ e.getMessage(), Max.Hexdump(t.getResult().getBytes())}));
				}
				hideProgBar();
			}
		});

	}


}
