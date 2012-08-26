package de.wikilab.android.friendica01;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
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

	PullToRefreshListView rlConv, rlMsg;
	ListView lstConv, lstMsg;
	
	String refreshTarget;

	final int ITEMS_PER_PAGE = 20;
	int curConvLoadPage = 1, curMsgLoadPage = 1;
	boolean convloadFinished = false, msgloadFinished = false;

	boolean isLargeMode = false;
	boolean isMessageVisible = false;
	String messageParentUri;

	HashSet<Long> convcontainedIds = new HashSet<Long>(), msgcontainedIds = new HashSet<Long>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		myView = inflater.inflate(R.layout.msg_view, container, false);
		rlConv = (PullToRefreshListView) myView.findViewById(R.id.conversations);
		rlMsg = (PullToRefreshListView) myView.findViewById(R.id.messages);
		lstConv = rlConv.getRefreshableView();
		lstMsg = rlMsg.getRefreshableView();
		lstMsg.setDividerHeight(0);

		Log.d(TAG, "screenLayout="+getResources().getConfiguration().screenLayout);
		if (Max.isLarge(getResources().getConfiguration())) {
			// on a large screen device ...
			isLargeMode = true;
		}

		rlConv.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				if (convloadFinished) {
					onNavigate(refreshTarget);
				}
			}
		});

		rlConv.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {
			@Override
			public void onLastItemVisible() {
				if (convloadFinished && getConvListAdapter() != null) {
					Toast.makeText(getActivity(), "Loading more items...", Toast.LENGTH_SHORT).show();
					curConvLoadPage ++;

					convloadFinished = false;

					SendMessage("Loading Animation", Integer.valueOf(View.VISIBLE), null);

					loadConversations();
				} else {
					Log.i(TAG, "OnLastItemVisibleListener -- skip! lf="+convloadFinished+" ad:"+lstConv.getAdapter().getClass().toString());
				}
			}
		});

		lstConv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> self, View view, int position, long id) {
				try {

					navigateMessage(((JSONObject)self.getAdapter().getItem(position)).getString("parent-uri"));
				} catch (JSONException e) {
					e.printStackTrace();
				}
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
	public boolean onBackPressed() {
		if (isMessageVisible) {
			setMessageVisible(false);
			return true;
		}
		return false;
	}
	
	private void setMessageVisible(boolean v) {
		if (!isLargeMode) {
			rlConv.setVisibility(v ? View.GONE : View.VISIBLE);
			myView.findViewById(R.id.right_bar).setVisibility(v ? View.VISIBLE : View.GONE);
			isMessageVisible = v;
		}
	}
	
	private void navigateMessage(String parentUri) {
		curMsgLoadPage = 1;
		rlMsg.setRefreshing();
		messageParentUri = parentUri;

		loadMessages();
		
		setMessageVisible(true);
		if (!isLargeMode) {
			lstMsg.setAdapter(null);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (refreshTarget != null && convloadFinished) {
			navigate(refreshTarget);
		}
	}

	protected void sendReplyMessage() {
		if (refreshTarget == null ||!refreshTarget.startsWith("msg:conversation:")) return;

		String replyId = refreshTarget.substring(17);

		String message = ((EditText) myView.findViewById(R.id.maintb)).getText().toString();



	}

	protected void onNavigate(String target) {
		rlConv.setRefreshing();
		SendMessage("Set Header Text", getString(R.string.mm_directmessages), null);
		curConvLoadPage = 1;
		
		refreshTarget = target;
		convloadFinished = false;

		SendMessage("Loading Animation", Integer.valueOf(View.VISIBLE), null);

		loadConversations();

	}

	public void hideProgBar() {
		/*reflvw.setAddStatesFromChildren(addsStates)
		list.setVisibility(View.VISIBLE);
		progbar.setVisibility(View.GONE);*/
		if (curConvLoadPage == 1) rlConv.onRefreshComplete();

		SendMessage("Loading Animation", Integer.valueOf(View.INVISIBLE), null);

	}

	private Adapter getConvListAdapter() {
		Adapter a = lstConv.getAdapter();
		if (a instanceof WrapperListAdapter) a = ((WrapperListAdapter)a).getWrappedAdapter();
		return a;
	}

	private Adapter getMsgListAdapter() {
		Adapter a = lstMsg.getAdapter();
		if (a instanceof WrapperListAdapter) a = ((WrapperListAdapter)a).getWrappedAdapter();
		return a;
	}

	private void setConvItems(JSONArray j) throws JSONException {
		if (curConvLoadPage == 1) {
			if (j.length() == 0) {
				rlConv.setVisibility(View.GONE);
				((TextView) myView.findViewById(R.id.lblInfo)).setVisibility(View.VISIBLE);
			}
			ArrayList<JSONObject> jsonObjectArray = new ArrayList<JSONObject>(j.length());
			convcontainedIds.clear();
			for(int i = 0; i < j.length(); i++) {
				JSONObject jj = j.getJSONObject(i);
				jsonObjectArray.add(jj);
				convcontainedIds.add(jj.getLong("id"));
			}
			lstConv.setAdapter(new MessageListAdapter(getActivity(), jsonObjectArray));
		} else {
			MessageListAdapter oldContent = (MessageListAdapter) getConvListAdapter();
			for(int i = 0; i < j.length(); i++) {
				JSONObject jj = j.getJSONObject(i);
				if (convcontainedIds.contains(jj.getLong("id"))) continue;
				oldContent.add(jj);
				convcontainedIds.add(jj.getLong("id"));
			}
			oldContent.notifyDataSetChanged();
			Toast.makeText(getActivity(), "Done loading more messages - scroll down :)", Toast.LENGTH_SHORT).show();
		}
		convloadFinished = true;
	}

	private void setMsgItems(JSONArray j) throws JSONException {
		if (curMsgLoadPage == 1) {
			if (j.length() == 0) {
				Toast.makeText(getActivity(), "No messages found.", Toast.LENGTH_LONG).show();
				return;
			}
			ArrayList<JSONObject> jsonObjectArray = new ArrayList<JSONObject>(j.length());
			msgcontainedIds.clear();
			for(int i = 0; i < j.length(); i++) {
				JSONObject jj = j.getJSONObject(i);
				jsonObjectArray.add(jj);
				msgcontainedIds.add(jj.getLong("id"));
			}
			lstMsg.setAdapter(new MessageContentAdapter(getActivity(), jsonObjectArray));
		} else {
			MessageContentAdapter oldContent = (MessageContentAdapter) getMsgListAdapter();
			for(int i = 0; i < j.length(); i++) {
				JSONObject jj = j.getJSONObject(i);
				if (msgcontainedIds.contains(jj.getLong("id"))) continue;
				oldContent.add(jj);
				msgcontainedIds.add(jj.getLong("id"));
			}
			oldContent.notifyDataSetChanged();
			Toast.makeText(getActivity(), "Done loading more messages - scroll down :)", Toast.LENGTH_SHORT).show();
		}
		msgloadFinished = true;
	}


	public void loadMessages() {
		SendMessage("Loading Animation", Integer.valueOf(View.VISIBLE), null);

		final TwAjax t = new TwAjax(getActivity(), true, true);
		String target= "conversation?uri=" + URLEncoder.encode(messageParentUri) + "&";
		String url = Max.getServer(getActivity()) + "/api/direct_messages/" + target + "getUserObjects=false&getText=plain&count=" + String.valueOf(ITEMS_PER_PAGE) + "&page=" + String.valueOf(curMsgLoadPage);
		t.getUrlContent(url, new Runnable() {
			@Override public void run() {
				try {
					JSONArray j = (JSONArray) t.getJsonResult();

					setMsgItems(j);

				} catch (Exception e) {
					lstMsg.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.pl_error_listitem, android.R.id.text1, new String[]{"Error: "+ e.getMessage(), Max.Hexdump(t.getResult().getBytes())}));
					e.printStackTrace();
				}
				if (curMsgLoadPage == 1) rlMsg.onRefreshComplete();

				SendMessage("Loading Animation", Integer.valueOf(View.INVISIBLE), null);

			}
		});

	}



	public void loadConversations() {
		final TwAjax t = new TwAjax(getActivity(), true, true);
		String url = Max.getServer(getActivity()) + "/api/direct_messages/conversations?count=" + String.valueOf(ITEMS_PER_PAGE) + "&page=" + String.valueOf(curConvLoadPage);
		t.getUrlContent(url, new Runnable() {
			@Override public void run() {
				try {
					JSONArray j = (JSONArray) t.getJsonResult();

					setConvItems(j);

				} catch (Exception e) {
					lstConv.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.pl_error_listitem, android.R.id.text1, new String[]{"Error: "+ e.getMessage(), Max.Hexdump(t.getResult().getBytes())}));
					e.printStackTrace();
				}
				hideProgBar();
			}
		});

	}



}
