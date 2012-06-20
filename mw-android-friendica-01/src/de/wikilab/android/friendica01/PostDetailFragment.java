package de.wikilab.android.friendica01;

import java.util.ArrayList;

import org.json.JSONObject;

import com.handmark.pulltorefresh.library.PullToRefreshListView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

public class PostDetailFragment extends ContentFragment {
	private static final String TAG="Friendica/PostDetailFragment";
	

	PullToRefreshListView reflvw;
	ListView list;

	String refreshTarget;
	

	int conversationId;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		myView = inflater.inflate(R.layout.pl_listviewinner, container, false);
		reflvw = (PullToRefreshListView) myView.findViewById(R.id.listview);
		list = reflvw.getRefreshableView();
		
		return myView;
	}

	protected void onNavigate(String target) {
		if (myView != null) {
			
		}
		((FragmentParentListener)getActivity()).OnFragmentMessage("Loading Animation", Integer.valueOf(View.VISIBLE), null);
		if (target != null && target.startsWith("conversation:")) {
			conversationId = Integer.parseInt(target.substring(13));
			
			((FragmentParentListener)getActivity()).OnFragmentMessage("Set Header Text", "Post Details ("+String.valueOf(conversationId)+")", null);
			loadConversation();
		}
	}

	public void hideProgBar() {

		((FragmentParentListener)getActivity()).OnFragmentMessage("Loading Animation", Integer.valueOf(View.INVISIBLE), null);
	}

	public void loadConversation() {
		final TwAjax t = new TwAjax(getActivity(), true, true);
		t.getUrlContent(Max.getServer(getActivity()) + "/api/statuses/show/" + conversationId, new Runnable() {
			@Override
			public void run() {
				try {
					ArrayList<JSONObject> jsonObjectArray = new ArrayList<JSONObject>();
					
					jsonObjectArray.add((JSONObject)t.getJsonResult());
					
					//ListView lvw = (ListView) findViewById(R.id.listview);

					list.setAdapter(new PostListAdapter(getActivity(), jsonObjectArray));
					
				} catch (Exception e) {
					list.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.pl_error_listitem, android.R.id.text1, new String[]{"Error: "+ e.getMessage(), Max.Hexdump(t.getResult().getBytes())}));
					e.printStackTrace();
				}
				hideProgBar();
			}
		});

	}


}
