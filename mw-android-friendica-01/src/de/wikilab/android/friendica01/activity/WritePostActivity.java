package de.wikilab.android.friendica01.activity;

import de.wikilab.android.friendica01.FragmentParentListener;
import de.wikilab.android.friendica01.R;
import de.wikilab.android.friendica01.R.id;
import de.wikilab.android.friendica01.R.layout;
import de.wikilab.android.friendica01.R.string;
import de.wikilab.android.friendica01.fragment.WritePostFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class WritePostActivity extends FragmentActivity implements FragmentParentListener {
	private static final String TAG="Friendica/WritePostActivity";
	
	TextView header_text;
	WritePostFragment frag;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.writepost);
		
		TextView header_logo = (TextView) findViewById(R.id.header_logo);
		header_logo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		header_text = (TextView) findViewById(R.id.header_text);
		header_text.setText(getString(R.string.mm_updatemystatus));
		
		frag = (WritePostFragment) getSupportFragmentManager ().findFragmentById(R.id.pl_fragment);
		
		if (getIntent() != null && getIntent().getAction() == Intent.ACTION_SEND) {
			frag.handleSendIntent(getIntent());
		}
		
	}
	

	@Override
	public void OnFragmentMessage(String message, Object arg1, Object arg2) {
		if (message.equals("Set Header Text")) {
			//setHeadertext((String) arg1);
		}
		if (message.equals("Finished")) {
			finish();
		}
	}
	

}
