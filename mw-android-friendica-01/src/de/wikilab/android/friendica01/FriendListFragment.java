package de.wikilab.android.friendica01;

import java.io.File;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class FriendListFragment extends ContentFragment implements FragmentParentListener {
	private static final String TAG="Friendica/FriendListFragment";
	
	GridView list;
	ProgressBar progbar;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		myView = inflater.inflate(R.layout.friendlistinner, container, false);
		list = (GridView) myView.findViewById(R.id.gridview);
		progbar = (ProgressBar) myView.findViewById(R.id.progressbar);
		
		((TextView) myView.findViewById(R.id.notice_bar)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				loadFriendList(true);
			}
		});
		
		
		return myView;
	}
	
	public void onNavigate(String target) {
		if (myView != null) {
			list.setVisibility(View.GONE);
			progbar.setVisibility(View.VISIBLE);
		}
		((FragmentParentListener)getActivity()).OnFragmentMessage("Set Header Text", getString(R.string.mm_friends), null);
		loadFriendList(false);
	}

	public void hideProgBar() {
		list.setVisibility(View.VISIBLE);
		progbar.setVisibility(View.GONE);
	}
	
	public void loadFriendList(boolean forceReload) {
		File cacheFile = new File(Max.DATA_DIR, "friendlist_cache.json");
		if (cacheFile.isFile() && !forceReload) {
			((TextView) myView.findViewById(R.id.notice_bar)).setText("Loaded from cache. Tap to refresh from server.");
			displayFriendlist();
			return;
		}
		((TextView) myView.findViewById(R.id.notice_bar)).setText("Loading...");
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		
		final TwAjax t = new TwAjax(getActivity(), true, false);
		t.urlDownloadToFile(Max.getServer(getActivity()) + "/api/statuses/friends.json", cacheFile.getAbsolutePath(), new Runnable() {
			@Override
			public void run() {
				displayFriendlist();
				((TextView) myView.findViewById(R.id.notice_bar)).setText("Loaded from server. Tap to refresh from server.");
			}
		});
		
	}
	
	void displayFriendlist() {
		String in = "<empty>";
		
		try {
			in = Max.readFile(new File(Max.DATA_DIR, "friendlist_cache.json").getAbsolutePath());
			
			JSONArray j = (JSONArray)new JSONTokener(in).nextValue();
			PhotoGalleryAdapter.Pic[] picArray = new PhotoGalleryAdapter.Pic[j.length()];
			
			for(int i = 0; i < j.length(); i++) {
				JSONObject d = j.getJSONObject(i);
				picArray[i] = new PhotoGalleryAdapter.Pic(
						d.getString("profile_image_url").replace("-6", "-4"), 
						Max.IMG_CACHE_DIR + "/friend_pi_" + d.getString("id") + "_.jpg", 
						d.getString("name"), d.getString("id"), d
						);
			}
			
			//ListView lvw = (ListView) findViewById(R.id.listview);
			
			PhotoGalleryAdapter pga = new PhotoGalleryAdapter(getActivity(), R.layout.friendlist_item, picArray);
			list.setAdapter(pga);
	
			pga.clickListener = new View.OnClickListener() {
				@Override public void onClick(View arg1) {
					Log.i("FriendListFragment", "click!");
					PhotoGalleryAdapter.Pic p = (PhotoGalleryAdapter.Pic) arg1.getTag();
					
					String userId = p.data1;
					Toast.makeText(getActivity(), "userId "+userId, Toast.LENGTH_SHORT).show();
					
					Intent i = new Intent(getActivity(), UserProfileActivity.class);
					i.putExtra("userId", userId);
					startActivity(i);
				}
			};
			
		} catch (Exception e) {
			list.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_2, android.R.id.text2, new String[]{"Error: ", e.getMessage(), in}));
			e.printStackTrace();
		}
		hideProgBar();
	}

	@Override
	public void OnFragmentMessage(String message, Object arg1, Object arg2) {
		Log.e("FLF->OnFragmentMessage", message);
		
		if (message.equals("On Click")) {
			PhotoGalleryAdapter.Pic p = (PhotoGalleryAdapter.Pic) arg1;
			Toast.makeText(getActivity(), p.caption, Toast.LENGTH_SHORT).show();
		}
		
	}
	

	
	
}
