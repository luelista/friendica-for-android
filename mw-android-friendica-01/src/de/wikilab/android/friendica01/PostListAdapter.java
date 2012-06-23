package de.wikilab.android.friendica01;

import java.io.File;
import java.sql.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PostListAdapter extends ArrayAdapter<JSONObject> {
	private static final String TAG="Friendica/PostListAdapter";

	public boolean isPostDetails = false;
	
	protected static class ViewHolder {
		int Type;
		ImageView profileImage;
		TextView userName, htmlContent, dateTime;
		ImageView[] picture = new ImageView[3];
	}
	
	
	public PostListAdapter(Context context, List<JSONObject> objects) {
		super(context, R.layout.pl_listitem, objects);
		
	}
	
	@Override
	public boolean hasStableIds() {
		return true;
	}
	@Override
	public long getItemId(int position) {
		try {
			return ((JSONObject) getItem(position)).getLong("id");
		} catch (JSONException e) {
			Log.e(TAG, "Item without ID!");
			return 0;
		}
	}
	
	@Override
	public int getItemViewType(int position) {
		try {
			JSONObject post = (JSONObject) getItem(position);
			
			if (post.getString("verb").equals("http://activitystrea.ms/schema/1.0/like")) {
				post.put("MW_TYPE", 3);
			} else if (post.has("in_reply_to_status_id") && post.getString("in_reply_to_status_id").equals("0") == false) {
				post.put("MW_TYPE", 2);
			} else if (post.getString("statusnet_html").contains("<img")) {
				post.put("MW_TYPE", 1);
			} else {
				post.put("MW_TYPE", 0);
			}
			
			return post.getInt("MW_TYPE");
		} catch (JSONException e) {
			return 0;
		}
	}
	
	@Override
	public int getViewTypeCount() {
		return 4;
	}
	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder H;
		if (convertView == null) {
			LayoutInflater inf = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			H = new ViewHolder();
			H.Type = getItemViewType(position);
			
			if (H.Type == 1) {
				convertView = inf.inflate(R.layout.pl_listitem_picture, null);
				H.userName = (TextView) convertView.findViewById(R.id.userName);
				H.htmlContent = (TextView) convertView.findViewById(R.id.htmlContent);
				H.profileImage = (ImageView) convertView.findViewById(R.id.profileImage);
				
				H.picture[0] = (ImageView) convertView.findViewById(R.id.picture1);
				H.picture[1] = (ImageView) convertView.findViewById(R.id.picture2);
				H.picture[2] = (ImageView) convertView.findViewById(R.id.picture3);
			} else if (H.Type == 2) {
				convertView = inf.inflate(R.layout.pl_listitem_comment, null);
				H.userName = (TextView) convertView.findViewById(R.id.userName);
				H.htmlContent = (TextView) convertView.findViewById(R.id.htmlContent);
				H.profileImage = (ImageView) convertView.findViewById(R.id.profileImage);

			} else if (H.Type == 3) {
				convertView = inf.inflate(R.layout.pl_listitem_like, null);
				H.htmlContent = (TextView) convertView.findViewById(R.id.htmlContent);
				
			} else {
				convertView = inf.inflate(R.layout.pl_listitem, null);
				H.userName = (TextView) convertView.findViewById(R.id.userName);
				H.htmlContent = (TextView) convertView.findViewById(R.id.htmlContent);
				H.profileImage = (ImageView) convertView.findViewById(R.id.profileImage);
			}
			
			if (isPostDetails) {
				if (H.Type <= 1) {
					H.userName.setTextSize(18); H.htmlContent.setTextSize(18);
				}
				
				View savedView = convertView;
				convertView = inf.inflate(R.layout.pd_listitemwrapper, null);
				((LinearLayout)convertView).addView(savedView, 0);
				H.htmlContent.setFocusable(true);
				H.htmlContent.setMovementMethod(LinkMovementMethod.getInstance());

				H.dateTime = (TextView) convertView.findViewById(R.id.date);
			}
			
			convertView.setTag(H);
		} else {
			H = (ViewHolder) convertView.getTag();
		}
		
		JSONObject post = (JSONObject) getItem(position);
		
		if (H.profileImage != null) {
			H.profileImage.setImageResource(R.drawable.ic_launcher);
			try {
				final String piurl = post.getJSONObject("user").getString("profile_image_url");
				Log.i(TAG, "TRY Download profile img: " + piurl);
				final TwAjax pidl = new TwAjax(getContext(), true, false);
				pidl.ignoreSSLCerts = true;
				
				//NEW: download cached
				final File pifile = new File(Max.IMG_CACHE_DIR + "/pi_" + Max.cleanFilename(piurl));
				if (pifile.isFile()) {
					Log.i(TAG, "OK  Load cached profile Img: " + piurl);
					//profileImage.setImageURI(Uri.parse("file://" + pifile.getAbsolutePath()));
					H.profileImage.setImageDrawable(new BitmapDrawable(pifile.getAbsolutePath()));
				} else {
					pidl.urlDownloadToFile(piurl, pifile.getAbsolutePath(), new Runnable() {
						@Override
						public void run() {
							Log.i(TAG, "OK  Download profile Img: " + piurl);
							H.profileImage.setImageDrawable(new BitmapDrawable(pifile.getAbsolutePath()));
						}
					});
				}
			} catch (JSONException e) {
			}
		}
		
		if (H.userName != null) {
			try {
				String appendix = "";
				if (H.Type ==  2 && !isPostDetails)  appendix = " replied to " + post.getString("in_reply_to_screen_name") + ":";
				H.userName.setText(post.getJSONObject("user").getString("name") + appendix);
			} catch (Exception e) {
				H.userName.setText("Invalid Dataset!");
			}
		}
		
		if (H.dateTime != null) {
			try {
				//H.dateTime.setText("on "+post.getString("published"));
				
				
				H.dateTime.setText(DateUtils.getRelativeDateTimeString(parent.getContext(), 
						java.util.Date.parse(post.getString("published")),
						DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_24HOUR));
				
			} catch (Exception e) {
				H.dateTime.setText("Invalid Dataset!");
			}
		}
		
		try {

			//Max.setHtmlWithImages(H.htmlContent, post.getString("statusnet_html"));
			String filtered_html = post.getString("statusnet_html");
			filtered_html = filtered_html.replaceAll("(<br[^>]*>|</?div[^>]*>|</?p>)", "  ");
			Spanned spanned = Html.fromHtml(filtered_html);
			Spannable htmlSpannable;
			if (spanned instanceof SpannableStringBuilder) {
				htmlSpannable = (SpannableStringBuilder) spanned;
			} else {
				htmlSpannable = new SpannableStringBuilder(spanned);
			}
			if (H.Type == 1 ) {
				downloadPics(H, htmlSpannable);
			}
			
			H.htmlContent.setText(htmlSpannable);
			
		} catch (JSONException e) {
			H.htmlContent.setText("Invalid Dataset!");
		}
		
		return convertView;
	}
	
	private void downloadPics(final ViewHolder H, Spannable htmlSpannable) {
		int pos = 0;
		for (int i = 0; i < 2; i++) H.picture[i].setVisibility(View.GONE);
		
		for (ImageSpan img : htmlSpannable.getSpans(0, htmlSpannable.length(), ImageSpan.class)) {
			htmlSpannable.removeSpan(img);
			
			if (pos > 2) break;
			
			final TwAjax pidl = new TwAjax(getContext(), true, false);
			pidl.ignoreSSLCerts = true;
			final String piurl = img.getSource();
			Log.i(TAG, "TRY Downloading post Img: " + piurl);
			final int targetImg = pos;
			final File pifile = new File(Max.IMG_CACHE_DIR + "/pi_" + Max.cleanFilename(piurl));
			if (pifile.isFile()) {
				Log.i(TAG, "OK  Load cached post Img: " + piurl);
				//profileImage.setImageURI(Uri.parse("file://" + pifile.getAbsolutePath()));
				H.picture[targetImg].setImageDrawable(new BitmapDrawable(pifile.getAbsolutePath()));
				H.picture[targetImg].setVisibility(View.VISIBLE);
			} else {
				pidl.urlDownloadToFile(piurl, pifile.getAbsolutePath(), new Runnable() {
					@Override
					public void run() {
						Log.i(TAG, "OK  Download post Img: " + piurl);
						H.picture[targetImg].setImageDrawable(new BitmapDrawable(pifile.getAbsolutePath()));
						H.picture[targetImg].setVisibility(View.VISIBLE);
					}
				});
			}
			pos++;
			
		}
		
	}
	
	
}
