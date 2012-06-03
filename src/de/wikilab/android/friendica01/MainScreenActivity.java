package de.wikilab.android.friendica01;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainScreenActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Max.initDataDirs();
		
		setContentView(R.layout.mainscreen);
		
		ArrayList<String> mainList = new ArrayList<String>();
		mainList.add("Timeline");
		mainList.add("Your Latest Posts");
		mainList.add("Your Photo Albums");
		mainList.add("Friends");
		mainList.add("Take Photo And Upload");
		mainList.add("Select Photo And Upload");
		
		
		ListView lvw = (ListView) findViewById(R.id.listview);
		lvw.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, mainList));
		
		lvw.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int index,
					long arg3) {
				if (index == 0) {
					//Timeline
					
					startActivity(new Intent(MainScreenActivity.this, TimelineActivity.class));
				}
			}
		});
	}
	
}
