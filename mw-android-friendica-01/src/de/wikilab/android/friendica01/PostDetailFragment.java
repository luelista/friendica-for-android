package de.wikilab.android.friendica01;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

public class PostDetailFragment extends ContentFragment {

	ListView list;
	ProgressBar progbar;

	int conversationId;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		myView = inflater.inflate(R.layout.pl_listviewinner, container, false);
		list = (ListView) myView.findViewById(R.id.listview);
		progbar = (ProgressBar) myView.findViewById(R.id.progressbar);

		return myView;
	}

	protected void onNavigate(String target) {
		if (myView != null) {
			list.setVisibility(View.GONE);
			progbar.setVisibility(View.VISIBLE);
		}
		if (target != null && target.startsWith("conversation:")) {
			conversationId = Integer.parseInt(target.substring(13));
			
			((FragmentParentListener)getActivity()).OnFragmentMessage("Set Header Text", "Post Details ("+String.valueOf(conversationId)+")", null);
			loadConversation();
		}
	}

	public void hideProgBar() {

		list.setVisibility(View.VISIBLE);
		progbar.setVisibility(View.GONE);
	}

	public void loadConversation() {
		final TwAjax t = new TwAjax(getActivity(), true, true);
		t.getUrlContent(Max.getServer(getActivity()) + "/api/statuses/home_timeline.json", new Runnable() {
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
					list.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.pl_error_listitem, android.R.id.text1, new String[]{"Error: "+ e.getMessage(), Max.Hexdump(t.getResult().getBytes())}));
					e.printStackTrace();
				}
				hideProgBar();
			}
		});

	}


}
