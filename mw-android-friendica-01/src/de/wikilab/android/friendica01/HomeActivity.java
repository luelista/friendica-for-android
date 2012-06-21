package de.wikilab.android.friendica01;

import java.io.File;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class HomeActivity extends FragmentActivity implements FragmentParentListener, LoginListener {
	private static final String TAG="Friendica/HomeActivity";
	

	public static final int RQ_SELECT_PHOTO = 44;
	public static final int RQ_TAKE_PHOTO = 55;
	
	public File takePhotoTarget;
	
	WelcomeFragment frag_welcome = new WelcomeFragment();
	PostListFragment frag_posts = new PostListFragment();
	WritePostFragment frag_writepost = new WritePostFragment();
	PhotoGalleryFragment frag_photos = new PhotoGalleryFragment();
	FriendListFragment frag_friendlist = new FriendListFragment();
	PostDetailFragment frag_postdetail = new PostDetailFragment();
	//PreferenceFragment frag_preferences = new PreferenceFragment();
	
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
		
		//ViewServer.get(this).addWindow(this);
		
		Log.i(TAG, "Should check for updates?");
		
		if (prefs.getBoolean("updateChecker", true)) {
			Log.i(TAG, "Checking for updates...");
			final TwAjax updateChecker = new TwAjax(this, true, false);
			updateChecker.getUrlContent("http://friendica-for-android.wiki-lab.net/docs/update.txt", new Runnable() {
				@Override
				public void run() {
					String res = updateChecker.getResult();
					if (res != null && res.startsWith("UPDATE=")) {
						try {
							int version = Integer.parseInt(res.substring(7));
							int currentVersion = getPackageManager().getPackageInfo(getPackageName(), 0 ).versionCode;
							Log.i(TAG, "UpdateCheck onlineVersion="+version+" currentVersion="+currentVersion);
							if (version > currentVersion) {
								Max.alert(HomeActivity.this, "Open the app's website to download the newest version:<br><a href='https://github.com/max-weller/friendica-for-android/downloads'>https://github.com/max-weller/friendica-for-android/downloads</a><br><br>(Go to Preferences to disable update check)", "Update available!");
							}
						} catch (NameNotFoundException e) {
							e.printStackTrace();
							Log.e(TAG, "UpdateCheck failed! (2)");
						}
					} else {
						Log.e(TAG, "UpdateCheck failed!");
					}
				}
			});
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
		if (message.equals("Loading Animation")) {
			((ProgressBar) findViewById(R.id.glob_progressbar)).setVisibility(((Integer)arg1).intValue());
		}
		if (message.equals("Navigate Conversation")) {
			navigateConversation((String) arg1);
		}
	}
	

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	switch(requestCode) {
    	case RQ_SELECT_PHOTO:
    		if (resultCode == RESULT_OK) {
    			Intent in = new Intent(HomeActivity.this, FriendicaImgUploadActivity.class);
    			in.putExtra(Intent.EXTRA_STREAM, data.getData());
    			startActivity(in);
    		}
    		break;
    	case RQ_TAKE_PHOTO:
    		if (resultCode == RESULT_OK) {
    			//Log.e("INTENT=",data==null?"NULL":"not null");
    			Intent in = new Intent(HomeActivity.this, FriendicaImgUploadActivity.class);
    			in.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(takePhotoTarget));
    			//in.putExtra(Intent.EXTRA_STREAM, data.getData());
    			startActivity(in);
    		}
    		break;
    	default:
    		super.onActivityResult(requestCode, resultCode, data);
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

		if (arg1.equals("Friends")) {
			navigateFriendList();
		}

		if (arg1.equals("My Photo Albums")) {
			navigatePhotoGallery("myalbums");
		}

		if (arg1.equals("Take Photo And Upload")) {
			Intent in = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			takePhotoTarget = Max.getTempFile();
			in.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(takePhotoTarget));
			startActivityForResult(in, RQ_TAKE_PHOTO);
		}
		if (arg1.equals("Select Photo And Upload")) {
			Intent in = new Intent(Intent.ACTION_PICK);
			in.setType("image/*");
			startActivityForResult(in, RQ_SELECT_PHOTO);
		}
		
		if (arg1.equals("Preferences")) {
			navigatePreferences();
		}
		
		if (arg1.equals("Log Out")) {
			SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(HomeActivity.this).edit();
			//prefs.putString("login_server", null); //keep server and user ...
			prefs.putString("login_user", null);
			prefs.putString("login_password", null); //...only remove password
			prefs.commit();
			
			finish();
		}
		
	}
	
	void setHeadertext(String ht) {
		TextView subheading = (TextView) findViewById(R.id.header_text);
		
		if (subheading != null) subheading.setText(ht);
	}

	private void navigateFriendList() {
	    if (! isMultiCol()) {
	        Intent showContent = new Intent(getApplicationContext(), GenericContentActivity.class);
	        showContent.putExtra("target", "friendlist");
	        startActivity(showContent);
	    } else {
	    	Fragment viewerFragment = (Fragment) getSupportFragmentManager().findFragmentById(R.id.view_fragment_container);
	    	if (viewerFragment != frag_friendlist) {
	    		FragmentTransaction t = getSupportFragmentManager().beginTransaction();
	    		t.replace(R.id.view_fragment_container, frag_friendlist);
	    		t.commit();
	    	}
	    	frag_friendlist.navigate("friendlist");
	    }
	}

	private void navigatePostList(String listTarget) {
	    if (! isMultiCol()) {
	        Intent showContent = new Intent(getApplicationContext(), GenericContentActivity.class);
	        showContent.putExtra("target", listTarget);
	        startActivity(showContent);
	    } else {
	    	Fragment viewerFragment = (Fragment) getSupportFragmentManager().findFragmentById(R.id.view_fragment_container);
	    	if (viewerFragment != frag_posts) {
	    		FragmentTransaction t = getSupportFragmentManager().beginTransaction();
	    		t.replace(R.id.view_fragment_container, frag_posts);
	    		t.commit();
	    	}
    		frag_posts.navigate(listTarget);
	    }
	}

	private void navigateConversation(String conversationId) {
	    if (! isMultiCol()) {
	        Intent showContent = new Intent(getApplicationContext(), GenericContentActivity.class);
	        showContent.putExtra("target", "conversation:" + conversationId);
	        startActivity(showContent);
	    } else {
	    	Fragment viewerFragment = (Fragment) getSupportFragmentManager().findFragmentById(R.id.view_fragment_container);
	    	if (viewerFragment != frag_postdetail) {
	    		FragmentTransaction t = getSupportFragmentManager().beginTransaction();
	    		t.addToBackStack("");
	    		t.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
	    		t.replace(R.id.view_fragment_container, frag_postdetail);
	    		t.commit();
	    	}
	    	frag_postdetail.navigate("conversation:" + conversationId);
	    }
	}

	private void navigatePhotoGallery(String galleryTarget) {
	    if (! isMultiCol()) {
	        Intent showContent = new Intent(getApplicationContext(), GenericContentActivity.class);
	        showContent.putExtra("target", galleryTarget);
	        startActivity(showContent);
	    } else {
	    	Fragment viewerFragment = (Fragment) getSupportFragmentManager().findFragmentById(R.id.view_fragment_container);
	    	if (viewerFragment != frag_photos) {
	    		FragmentTransaction t = getSupportFragmentManager().beginTransaction();
	    		t.replace(R.id.view_fragment_container, frag_photos);
	    		t.commit();
	    	}
    		frag_photos.navigate(galleryTarget);
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
	

	private void navigatePreferences() {
	    /*if (! isMultiCol()) {*/
	        Intent showContent = new Intent(getApplicationContext(), PreferencesActivity.class);
	        //showContent.putExtra("target", listTarget);
	        startActivity(showContent);
	    /*} else {
	    	Fragment viewerFragment = (Fragment) getSupportFragmentManager().findFragmentById(R.id.view_fragment_container);
	    	if (viewerFragment != frag_preferences) {
	    		FragmentTransaction t = getSupportFragmentManager().beginTransaction();
	    		t.replace(R.id.view_fragment_container, frag_preferences);
	    		t.commit();
	    	}
    		frag_preferences.navigate("");
	    }*/
	}
	
	public void navigatePostDetailId(String postId) {
		Fragment viewerFragment = (Fragment) getSupportFragmentManager().findFragmentById(R.id.view_fragment_container);
    	if (viewerFragment != frag_postdetail) {
    		FragmentTransaction t = getSupportFragmentManager().beginTransaction();
    		t.replace(R.id.view_fragment_container, frag_postdetail);
    		t.commit();
    	}
    	frag_postdetail.navigate("post:" + postId);
	}
	
	@Override
	public void OnLogin() {
		LoginListener target = (LoginListener) getSupportFragmentManager().findFragmentById(R.id.menu_fragment);
		target.OnLogin();
		
	}
	
}
