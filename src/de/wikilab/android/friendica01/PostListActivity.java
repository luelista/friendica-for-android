package de.wikilab.android.friendica01;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class PostListActivity extends FragmentActivity implements FragmentParentListener {
	TextView header_text;
	PostListFragment frag;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.pl_listview);
		
		TextView header_logo = (TextView) findViewById(R.id.header_logo);
		header_logo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		header_text = (TextView) findViewById(R.id.header_text);
		header_text.setText("My Profile Wall");
		
		frag = (PostListFragment) getSupportFragmentManager ().findFragmentById(R.id.pl_fragment);
		
		onNavigate();
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
	
	void onNavigate() {
		String target = null;
		
		if (getIntent() != null && getIntent().getStringExtra("target") != null) {
			target = getIntent().getStringExtra("target");
		}
		
		frag.navigateList(target);
	}


	
	
	
}
