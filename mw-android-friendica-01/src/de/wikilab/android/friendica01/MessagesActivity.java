/*package de.wikilab.android.friendica01;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import com.handmark.pulltorefresh.library.PullToRefreshListView;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MessagesActivity extends FragmentActivity implements FragmentParentListener {
	private static final String TAG="Friendica/MessagesActivity";
	
	PullToRefreshListView reflvw;
	ListView lvw;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		setContentView(R.layout.messages);
		
		reflvw = (PullToRefreshListView) findViewById(R.id.listview);
		lvw = reflvw.getRefreshableView();
		
		final TwAjax t = new TwAjax(this, true, true);
		t.getUrlContent(Max.getServer(this) + "/api/direct_messages/all?getText=true", new Runnable() {
			@Override public void run() {
				try {
					JSONArray j = (JSONArray) t.getJsonResult();
					ArrayList<String> list = new ArrayList<String>();
					
					for (int i = 0; i < j.length(); i++) {
						String title = j.getJSONObject(i).getString("text");
						if (title.indexOf("\n") > 0) title = title.substring(0, title.indexOf("\n"));
						list.add("<u>" + j.getJSONObject(i).getString("sender_screen_name") + "</u> -> <u>" + j.getJSONObject(i).getString("recipient_screen_name") + "</u><br>" + title);
					}
					
					lvw.setAdapter(new MessageViewAdapter(MessagesActivity.this, android.R.layout.simple_list_item_2, android.R.id.text1, list));
					
					
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	@Override
	public void onAttachFragment(Fragment fragment) {
		super.onAttachFragment(fragment);
		
//		if (fragment instanceof PostListFragment) ((ContentFragment)fragment).navigate("messages");
		
	}

	@Override
	public void OnFragmentMessage(String message, Object arg1, Object arg2) {
		
		if (message.equals("Show Direct Message")) {
			Intent in = new Intent(this, GenericContentActivity.class);
			in.putExtra("target", "message:" + arg1);
			startActivity(in);
		}
	}
	
}
*/