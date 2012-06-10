package de.wikilab.android.friendica01;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.TextView;

public class HomeActivity extends FragmentActivity implements FragmentParentListener, LoginListener {

	WelcomeFragment frag_welcome = new WelcomeFragment();
	PostListFragment frag_posts = new PostListFragment();
	WritePostFragment frag_writepost = new WritePostFragment();
	PhotoGalleryFragment frag_photos = new PhotoGalleryFragment();
	
	
	String currentMMItem = null;
	
	public boolean isMultiCol() {

		//
		View viewer = (View) findViewById(R.id.view_fragment_container);
	    return (viewer != null);
		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.homeactivity);
		
		Max.initDataDirs();
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String userName = prefs.getString("login_user", null);
		if (userName == null || userName.length() < 1) {
			Max.showLoginForm(this, null);
		} else {
			Max.tryLogin(this);
		}

		if (isMultiCol()) {
			if (savedInstanceState == null) {
				FragmentTransaction t = getSupportFragmentManager().beginTransaction();
				t.add(R.id.view_fragment_container, frag_welcome);
				t.commit();
			} else {
				currentMMItem = savedInstanceState.getString("currentMMItem");
				if (currentMMItem != null) navigate(currentMMItem);
			}
			
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString("currentMMItem", currentMMItem);
		super.onSaveInstanceState(outState);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
	}
	
	@Override
	public void OnFragmentMessage(String message, Object arg1, Object arg2) {

		if (message.equals("Set Header Text")) {
			setHeadertext((String) arg1);
		}
		
		if (message.equals("Navigate Main Menu")) {
			navigate((String) arg1);
		}
	}
	
	void navigate(String arg1) {
		currentMMItem = arg1;
		
		if (arg1.equals("Timeline")) {
			navigatePostList("timeline");
		}
		
		if (arg1.equals("Notifications")) {
			navigatePostList("notifications");
		}

		if (arg1.equals("My Wall")) {
			navigatePostList("mywall");
		}
		
		if (arg1.equals("Update My Status")) {
			navigateStatusUpdate();
		}

		if (arg1.equals("My Photo Albums")) {
			navigatePhotoGallery("myalbums");
		}
		
		if (arg1.equals("Preferences")) {
			startActivity(new Intent(HomeActivity.this, PreferencesActivity.class));
		}
		
		if (arg1.equals("Log Out")) {
			SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(HomeActivity.this).edit();
			//prefs.putString("login_server", null); //keep server and user ...
			//prefs.putString("login_user", null);
			prefs.putString("login_password", null); //...only remove password
			prefs.commit();
			
			finish();
		}
		
	}
	
	void setHeadertext(String ht) {
		TextView subheading = (TextView) findViewById(R.id.header_text);
		
		if (subheading != null) subheading.setText(ht);
	}

	private void navigatePostList(String listTarget) {
	    if (! isMultiCol()) {
	        Intent showContent = new Intent(getApplicationContext(), PostListActivity.class);
	        showContent.putExtra("target", listTarget);
	        startActivity(showContent);
	    } else {
	    	Fragment viewerFragment = (Fragment) getSupportFragmentManager().findFragmentById(R.id.view_fragment_container);
	    	if (viewerFragment != frag_posts) {
	    		FragmentTransaction t = getSupportFragmentManager().beginTransaction();
	    		t.replace(R.id.view_fragment_container, frag_posts);
	    		t.commit();
	    	}
    		frag_posts.navigateList(listTarget);
	    }
	}

	private void navigatePhotoGallery(String galleryTarget) {
	    if (! isMultiCol()) {
	        Intent showContent = new Intent(getApplicationContext(), PhotoGalleryActivity.class);
	        showContent.putExtra("target", galleryTarget);
	        startActivity(showContent);
	    } else {
	    	Fragment viewerFragment = (Fragment) getSupportFragmentManager().findFragmentById(R.id.view_fragment_container);
	    	if (viewerFragment != frag_photos) {
	    		FragmentTransaction t = getSupportFragmentManager().beginTransaction();
	    		t.replace(R.id.view_fragment_container, frag_photos);
	    		t.commit();
	    	}
    		frag_photos.navigateList(galleryTarget);
	    }
	}
	

	private void navigateStatusUpdate() {
	    if (! isMultiCol()) {
	        Intent showContent = new Intent(getApplicationContext(), WritePostActivity.class);
	        //showContent.putExtra("target", listTarget);
	        startActivity(showContent);
	    } else {
	    	Fragment viewerFragment = (Fragment) getSupportFragmentManager().findFragmentById(R.id.view_fragment_container);
	    	if (viewerFragment != frag_writepost) {
	    		FragmentTransaction t = getSupportFragmentManager().beginTransaction();
	    		t.replace(R.id.view_fragment_container, frag_writepost);
	    		t.commit();
	    	}
    		//frag_posts.navigateList(listTarget);
	    }
	}
	
	
	@Override
	public void OnLogin() {
		LoginListener target = (LoginListener) getSupportFragmentManager().findFragmentById(R.id.menu_fragment);
		target.OnLogin();
		
	}
	
}
