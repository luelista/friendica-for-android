package de.wikilab.android.friendica01;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class GenericContentActivity extends FragmentActivity implements FragmentParentListener {
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
