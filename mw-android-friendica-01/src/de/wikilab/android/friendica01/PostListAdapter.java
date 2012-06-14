package de.wikilab.android.friendica01;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class PostListAdapter extends ArrayAdapter<JSONObject> {

	private static final String TAG = "friendica01.PostListAdapter";
	
	public PostListAdapter(Context context, JSONObject[] objects) {
		super(context, R.layout.pl_listitem, objects);
		
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inf = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			convertView = inf.inflate(R.layout.pl_listitem, null);
		}
		
		JSONObject post = (JSONObject) getItem(position);

		final ImageView profileImage = (ImageView) convertView.findViewById(R.id.profileImage);
		profileImage.setImageResource(R.drawable.ic_launcher);
		try {
			String piurl = post.getJSONObject("user").getString("profile_image_url");
			Log.i(TAG, "Going to download profile img: " + piurl);
			final TwAjax pidl = new TwAjax(getContext(), true, false);
			
			//OLD: uncached, download every time, annoying when scrolling fast!
			/*pidl.getUrlBitmap(piurl, new Runnable() {
				@Override
				public void run() {
					
					profileImage.setImageBitmap(pidl.getBitmapResult());
				}
			});*/
			
			//NEW: download cached
			final File pifile = new File(Max.IMG_CACHE_DIR + "/pi_" + Max.cleanFilename(piurl));
			if (pifile.isFile()) {
				profileImage.setImageURI(Uri.parse("file://" + pifile.getAbsolutePath()));
			} else {
				pidl.urlDownloadToFile(piurl, pifile.getAbsolutePath(), new Runnable() {
					@Override
					public void run() {
						profileImage.setImageURI(Uri.parse("file://" + pifile.getAbsolutePath()));
					}
				});
			}
		} catch (JSONException e) {
		}

		TextView userName = (TextView) convertView.findViewById(R.id.userName);
		try {
			userName.setText(post.getJSONObject("user").getString("name"));
		} catch (JSONException e) {
			userName.setText("Invalid Dataset!");
		}

		TextView htmlContent = (TextView) convertView.findViewById(R.id.htmlContent);
		try {
			//htmlContent.setText(Html.fromHtml(post.getString("statusnet_html")));
			Max.setHtmlWithImages(htmlContent, post.getString("statusnet_html"));
		} catch (JSONException e) {
			htmlContent.setText("Invalid Dataset!");
		}
		
		return convertView;
	}
	
	
}
