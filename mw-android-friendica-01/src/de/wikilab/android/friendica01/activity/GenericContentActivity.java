package de.wikilab.android.friendica01.activity;
/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
import de.wikilab.android.friendica01.FragmentParentListener;
import de.wikilab.android.friendica01.R;
import de.wikilab.android.friendica01.R.id;
import de.wikilab.android.friendica01.R.layout;
import de.wikilab.android.friendica01.fragment.ContentFragment;
import de.wikilab.android.friendica01.fragment.FriendListFragment;
import de.wikilab.android.friendica01.fragment.MessageViewFragment;
import de.wikilab.android.friendica01.fragment.PhotoGalleryFragment;
import de.wikilab.android.friendica01.fragment.PostDetailFragment;
import de.wikilab.android.friendica01.fragment.PostListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class GenericContentActivity extends FragmentActivity implements FragmentParentListener {
	private static final String TAG="Friendica/GenericContentActivity";
	
	TextView header_text;
	ContentFragment frag;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.genericcontentactivity);
		
		TextView header_logo = (TextView) findViewById(R.id.header_logo);
		header_logo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		header_text = (TextView) findViewById(R.id.header_text);
		header_text.setText("...GenericContentActivity...");
		
		loadFragment();
	}
	
	@Override
	public void onAttachFragment(Fragment fragment) {
		onNavigate((ContentFragment) fragment);
	}
	
	@Override
	public void OnFragmentMessage(String message, Object arg1, Object arg2) {
		if (message.equals("Set Header Text")) {
			setHeadertext((String) arg1);
		}
		if (message.equals("Loading Animation")) {
			((ProgressBar) findViewById(R.id.glob_progressbar)).setVisibility(((Integer)arg1).intValue());
		}
		if (message.equals("Navigate Conversation")) {
			Intent in = new Intent(this, GenericContentActivity.class);
			in.putExtra("target", "conversation:" + arg1);
			startActivity(in);
		}
	}
	void setHeadertext(String ht) {
		TextView txtht = (TextView) findViewById(R.id.header_text);
		txtht.setText(ht);
	}

	void loadFragment() {
		String target = null;
		
		if (getIntent() != null && getIntent().getStringExtra("target") != null) {
			target = getIntent().getStringExtra("target");

			if (target.equals("timeline") || target.equals("notifications") || target.equals("mywall")) {
				FragmentTransaction t = getSupportFragmentManager().beginTransaction();
				t.add(R.id.content_fragment, new PostListFragment());
				t.commit();
			}
			if (target.equals("myalbums")) {
				FragmentTransaction t = getSupportFragmentManager().beginTransaction();
				t.add(R.id.content_fragment, new PhotoGalleryFragment());
				t.commit();
			}
			if (target.equals("friendlist")) {
				FragmentTransaction t = getSupportFragmentManager().beginTransaction();
				t.add(R.id.content_fragment, new FriendListFragment());
				t.commit();
			}
			if (target.startsWith("conversation:")) {
				FragmentTransaction t = getSupportFragmentManager().beginTransaction();
				t.add(R.id.content_fragment, new PostDetailFragment());
				t.commit();
			}
			if (target.startsWith("msg:")) {
				FragmentTransaction t = getSupportFragmentManager().beginTransaction();
				t.add(R.id.content_fragment, new MessageViewFragment());
				t.commit();
			}
		}

	}

	void onNavigate(ContentFragment frag) {
		String target = null;
		
		if (getIntent() != null && getIntent().getStringExtra("target") != null) {
			target = getIntent().getStringExtra("target");
		}
		
		frag.navigate(target);
	}

}
