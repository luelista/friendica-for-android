package de.wikilab.android.friendica01.adapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import de.wikilab.android.friendica01.Max;
import de.wikilab.android.friendica01.R;
import de.wikilab.android.friendica01.TwAjax;
import de.wikilab.android.friendica01.R.drawable;
import de.wikilab.android.friendica01.R.id;
import de.wikilab.android.friendica01.R.layout;
import de.wikilab.android.friendica01.activity.UserProfileActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PostListAdapter extends ArrayAdapter<JSONObject> {
	private static final String TAG="Friendica/PostListAdapter";

	public boolean isPostDetails = false;

	interface OnUsernameClickListener {
		void OnUsernameClick(ViewHolder viewHolder);
	}
	public OnUsernameClickListener onUsernameClick;
	
	public static class ViewHolder {
		int Type, position;
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
	
	private void navigateUserProfile(int position) {
		try {
			Intent inte = new Intent(getContext(), UserProfileActivity.class);
			inte.putExtra("userId", String.valueOf(((JSONObject) getItem(position)).getJSONObject("user").getInt("id")));
			getContext().startActivity(inte);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private OnClickListener postPictureOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent inte = new Intent(Intent.ACTION_VIEW);
			inte.setDataAndType(Uri.parse("file://" + v.getTag()), "image/jpeg");
			getContext().startActivity(inte);
		}
	};
	
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
				
				H.picture[0] = (ImageView) convertView.findViewById(R.id.picture1); H.picture[0].setOnClickListener(postPictureOnClickListener);
				H.picture[1] = (ImageView) convertView.findViewById(R.id.picture2); H.picture[1].setOnClickListener(postPictureOnClickListener);
				H.picture[2] = (ImageView) convertView.findViewById(R.id.picture3); H.picture[2].setOnClickListener(postPictureOnClickListener);
				
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
			
			if (isPostDetails && H.Type != 3) {
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
			
			if (H.userName != null) {
				OnClickListener clk = new OnClickListener() {
					@Override public void onClick(View v) {
						navigateUserProfile(H.position);
					}
				};
				H.userName.setOnClickListener(clk);
				H.profileImage.setOnClickListener(clk);
			}
			
		} else {
			H = (ViewHolder) convertView.getTag();
		}
		
		JSONObject post = (JSONObject) getItem(position);
		H.position = position;
		
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
			//filtered_html = filtered_html.replaceAll("<img[^>]+src=[\"']([^>\"']+)[\"'][^>]*>", "<a href='$1'>Bild: $1</a>");
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
			
		} catch (Exception e) {
			H.htmlContent.setText("Invalid Dataset!");
		}
		
		return convertView;
	}
	
	private void downloadPics(final ViewHolder H, Spannable htmlSpannable) {
		int pos = 0;
		for (int i = 0; i <= 2; i++) H.picture[i].setVisibility(View.GONE);
		
		for (ImageSpan img : htmlSpannable.getSpans(0, htmlSpannable.length(), ImageSpan.class)) {
			htmlSpannable.removeSpan(img);
			
			if (pos > 2) break;
			
			final TwAjax pidl = new TwAjax(getContext(), true, false);
			pidl.ignoreSSLCerts = true;
			final String piurl = img.getSource();
			final int targetImg = pos;

			if(piurl.startsWith("data:image")) {
				Log.i(TAG, "TRY Extracting embedded post Img: " + piurl);
				final int imgStart = piurl.indexOf("base64,") + 7; // SHOULD CHECK FOR FAILURE TO FIND base64,
				final String encodedImg = piurl.substring(imgStart);
				final int imgHash = encodedImg.hashCode();
				final String imgHashString = Integer.toString(imgHash);
				
				final File pifile = new File(Max.IMG_CACHE_DIR + "/pi_" + Max.cleanFilename(imgHashString));
				H.picture[targetImg].setTag(pifile.getAbsolutePath());
				if (pifile.isFile()) {
					Log.i(TAG, "OK  Load cached embedded post Img: " + imgHashString);
					H.picture[targetImg].setImageDrawable(new BitmapDrawable(pifile.getAbsolutePath()));
					H.picture[targetImg].setVisibility(View.VISIBLE);
				}
				else {
					Log.i(TAG, "OK  Decoding embedded post Img: " + Integer.toString(imgHash));
					final byte[] imgAsBytes = Base64.decode(encodedImg.getBytes(), Base64.DEFAULT);
					try{
						FileOutputStream pifileOut = new FileOutputStream(pifile.getAbsolutePath());
						pifileOut.write(imgAsBytes);
						pifileOut.close();
					}
					catch(IOException e) {
						e.printStackTrace();
					}

					//H.picture[targetImg].setImageDrawable(new BitmapDrawable(BitmapFactory.decodeByteArray(imgAsBytes, 0, imgAsBytes.length)));
					H.picture[targetImg].setImageDrawable(new BitmapDrawable(pifile.getAbsolutePath()));
					H.picture[targetImg].setVisibility(View.VISIBLE);
				}
			}
			else {
				Log.i(TAG, "TRY Downloading post Img: " + piurl);
				final File pifile = new File(Max.IMG_CACHE_DIR + "/pi_" + Max.cleanFilename(piurl));
				H.picture[targetImg].setTag(pifile.getAbsolutePath());
				if (pifile.isFile()) {
					Log.i(TAG, "OK  Load cached post Img: " + piurl);
					BitmapDrawable bmp = new BitmapDrawable(pifile.getAbsolutePath());
					if (bmp.getBitmap() != null && bmp.getBitmap().getWidth()>30) { //minWidth 30px to remove facebook's ugly icons
						H.picture[targetImg].setImageDrawable(bmp);
						H.picture[targetImg].setVisibility(View.VISIBLE);
					}
				} else {
					pidl.urlDownloadToFile(piurl, pifile.getAbsolutePath(), new Runnable() {
						@Override
						public void run() {
							Log.i(TAG, "OK  Download post Img: " + piurl);
							BitmapDrawable bmp = new BitmapDrawable(pifile.getAbsolutePath());
							if (bmp.getBitmap() != null && bmp.getBitmap().getWidth()>30) { //minWidth 30px to remove facebook's ugly icons
								H.picture[targetImg].setImageDrawable(bmp);
								H.picture[targetImg].setVisibility(View.VISIBLE);
							}
						}
					});
				}
			}

			pos++;
			
		}
		
	}
	
	
}
