package de.wikilab.android.friendica01;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class HtmlStringArrayAdapter extends ArrayAdapter<String> {

	private static final String TAG = "friendica01.MainScreenListAdapter";

	public int selectedItemIndex = -1;
	
	int tvRid;
	
	public void setSelectedItemIndex(int idx) {
		selectedItemIndex = idx;
		notifyDataSetChanged();
	}
	
	public HtmlStringArrayAdapter(Context context, int resource,
			int textViewResourceId, List<String> objects) {
		super(context, resource, textViewResourceId, objects);
		tvRid = textViewResourceId;
	}



	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View myView = super.getView(position, convertView, parent);
		
		String item = (String) getItem(position);
		((TextView)myView.findViewById(tvRid)).setText(Html.fromHtml(item));
		
		if (selectedItemIndex == position) {
			myView.setBackgroundResource(R.drawable.background_unread);
		}else {
			myView.setBackgroundResource(R.drawable.background_read);
		}
		
		return myView;
	}
	
	
}
