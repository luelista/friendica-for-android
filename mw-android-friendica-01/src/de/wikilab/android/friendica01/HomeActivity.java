package de.wikilab.android.friendica01;
/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gcm.GCMRegistrar;

public class HomeActivity extends FragmentActivity implements FragmentParentListener, LoginListener {
	private static final String TAG="Friendica/HomeActivity";

	public final static String SENDER_ID = "179387721673";

	public static final int RQ_SELECT_PHOTO = 44;
	public static final int RQ_TAKE_PHOTO = 55;

	public File takePhotoTarget;

	boolean isLargeMode = false;
	
	/*
	WelcomeFragment frag_welcome = new WelcomeFragment();
	PostListFragment frag_posts = new PostListFragment();
	WritePostFragment frag_writepost = new WritePostFragment();
	PhotoGalleryFragment frag_photos = new PhotoGalleryFragment();
	FriendListFragment frag_friendlist = new FriendListFragment();
	PostDetailFragment frag_postdetail = new PostDetailFragment();
	MessageViewFragment frag_messages = new MessageViewFragment();
	//PreferenceFragment frag_preferences = new PreferenceFragment();
	 */
	
	String currentMMItem = null;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.homeactivity);

		Max.initDataDirs();

		Log.d(TAG, "screenLayout="+getResources().getConfiguration().screenLayout);
		if (Max.isLarge(getResources().getConfiguration())) {
			// on a large screen device ...
			isLargeMode = true;
		}
	
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String userName = prefs.getString("login_user", null);
		if (userName == null || userName.length() < 1) {
			Max.showLoginForm(this, "Please enter account data");
		} else {
			Max.tryLogin(this);
			
			if (savedInstanceState == null) {
				navigate("Timeline");
			} else {
				currentMMItem = savedInstanceState.getString("currentMMItem");
				if (currentMMItem != null) navigate(currentMMItem);
			}
			try {
                GCMRegistrar.checkDevice(this);
                GCMRegistrar.checkManifest(this);
                final String regId = GCMRegistrar.getRegistrationId(this);
                if (regId.equals("")) {
                  Log.v(TAG, "Registering for GCM");
                  GCMRegistrar.register(this, SENDER_ID);
                } else {
                  Log.v(TAG, "Already registered");
                }

            } catch(Exception e) {
                Log.e(TAG, "Google Cloud Messaging not supported - please install Google Apps package!");
                Log.e(TAG, e.toString());
                Log.e(TAG, "Continuing without GCM. Push notifications won't work.");

            }
		}

	
		View toggle = findViewById(R.id.toggle_left_bar);
		if (toggle != null) toggle.setOnClickListener(toggleMenuBarHandler);
		//toggle = findViewById(R.id.toggle_left_bar2);
		//if (toggle != null) toggle.setOnClickListener(toggleMenuBarHandler);
		toggle = findViewById(R.id.left_bar_header);
		if (toggle != null) toggle.setOnClickListener(toggleMenuBarHandler);

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
								Max.alert(HomeActivity.this, "<a href='http://friendica-for-android.wiki-lab.net/update-landing/'>Click here to load the update!</a><br><br>(Go to Preferences to disable update check)", "Update available!");
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

	OnClickListener toggleMenuBarHandler = new OnClickListener() {
		@Override
		public void onClick(View v) {
			toggleMenuBarVisible();
		}
	};
	
	protected void toggleMenuBarVisible() {
		View leftBar = findViewById(R.id.left_bar);
		setMenuBarVisible(leftBar.getVisibility() == View.GONE);
	}
	
	protected void setMenuBarVisible(boolean v) {
		View leftBar = findViewById(R.id.left_bar);
		if (v) {
			Animation anim1 = AnimationUtils.loadAnimation(HomeActivity.this, android.R.anim.slide_in_left);
	        anim1.setInterpolator((new AccelerateDecelerateInterpolator()));
	        //anim1.setFillAfter(true);
	        leftBar.setAnimation(anim1);

			leftBar.setVisibility(View.VISIBLE);
		} else {
			Animation anim1 = AnimationUtils.loadAnimation(HomeActivity.this, R.anim.slide_out_left);
	        anim1.setInterpolator((new AccelerateDecelerateInterpolator()));
	        anim1.setFillAfter(true);
	        leftBar.setAnimation(anim1);

			leftBar.setVisibility(View.GONE);
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

	void onNavMainFragment()  {
		if (!isLargeMode) {
			View leftBar = findViewById(R.id.left_bar);
				leftBar.setVisibility(View.GONE);
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
		if (arg1.equals("Messages")) {
			//Intent in = new Intent(HomeActivity.this, MessagesActivity.class);
			//startActivity(in);
			navigateMessages("msg:all");
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

	private void navigateMainFragment(ContentFragment newFragment, String target) {
		onNavMainFragment();
		FragmentTransaction t = getSupportFragmentManager().beginTransaction();
		Bundle b = new Bundle();
		b.putString("target", target);
		newFragment.setArguments(b);
		t.replace(R.id.view_fragment_container, newFragment);
		//t.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		t.setCustomAnimations(R.anim.slide_in_right, android.R.anim.slide_out_right);
		t.addToBackStack(null);
		t.commit();
		newFragment.navigate(target);
	}
	
	private void navigateFriendList() {
		navigateMainFragment(new FriendListFragment(), "friendlist");
	}

	private void navigatePostList(String listTarget) {
		navigateMainFragment(new PostListFragment(), listTarget);
	}

	private void navigateConversation(String conversationId) {
		navigateMainFragment(new PostDetailFragment(), "conversation:" + conversationId);
	}

	private void navigatePhotoGallery(String galleryTarget) {
		navigateMainFragment(new PhotoGalleryFragment(), galleryTarget);
	}

	private void navigateMessages(String target) {
		navigateMainFragment(new MessageViewFragment(), target);
	}


	private void navigateStatusUpdate() {
		navigateMainFragment(new WritePostFragment(), "statusupdate");
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
	
	
	//never used???
	
/*
	public void navigatePostDetailId(String postId) {
		onNavMainFragment();
		Fragment viewerFragment = (Fragment) getSupportFragmentManager().findFragmentById(R.id.view_fragment_container);
		if (viewerFragment != frag_postdetail) {
			FragmentTransaction t = getSupportFragmentManager().beginTransaction();
			t.replace(R.id.view_fragment_container, frag_postdetail);
			t.commit();
		}
		frag_postdetail.navigate("post:" + postId);
	}
*/
	@Override
	public void OnLogin() {
		LoginListener target = (LoginListener) getSupportFragmentManager().findFragmentById(R.id.menu_fragment);
		target.OnLogin();

	}

	@Override
	public void onBackPressed() {
		if (!isLargeMode) {
			View leftBar = findViewById(R.id.left_bar);
			if (leftBar.getVisibility() != View.GONE) {
				leftBar.setVisibility(View.GONE);
				return;
			}
		}
		Fragment viewerFragment = (Fragment) getSupportFragmentManager().findFragmentById(R.id.view_fragment_container);
		if (viewerFragment instanceof ContentFragment) {
			if (((ContentFragment)viewerFragment).onBackPressed()) {
				return;
			}
		}
		super.onBackPressed();
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			toggleMenuBarVisible();
			return false;
		} else {
			return super.onKeyUp(keyCode, event);
		}
	}
	
}
