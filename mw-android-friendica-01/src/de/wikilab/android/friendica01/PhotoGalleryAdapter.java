package de.wikilab.android.friendica01;

import java.io.File;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class PhotoGalleryAdapter extends BaseAdapter {
    private Context mContext;
    private Pic[] mPics ;
    private int viewId;
    
    public OnClickListener clickListener;
    
    public static class Pic {
    	public String url,cache,caption,data1; public Object data2;
    	public Pic(String purl, String pcache, String pcaption, String pdata1, Object pdata2) { url=purl; cache=pcache; caption=pcaption; data1=pdata1; data2=pdata2; }
    }
    
    public PhotoGalleryAdapter(Context c, int pviewId, Pic[] pics) {
        mContext = c; mPics = pics; viewId = pviewId;
    }

    public int getCount() {
        return mPics.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        final ImageView imageView;
        if (convertView == null) {  // if it's not recycled, initialize some attributes
        	LayoutInflater inf = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        	convertView = inf.inflate(viewId, null);
        	if (clickListener != null) {
	        	final View ccv = convertView;//.findViewById(R.id.profile_image);
	        	ccv.setOnClickListener(new OnClickListener() {
					@Override public void onClick(View v) {
						clickListener.onClick(ccv);
					}
				});
        	}
        	//imageView = new ImageView(mContext);
            //imageView.setLayoutParams(new GridView.LayoutParams(145, 145));
            //imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            //imageView.setPadding(8, 8, 8, 8);
        }
        imageView = (ImageView) convertView.findViewById(R.id.profile_image);
        
        ((TextView) convertView.findViewById(R.id.caption)).setText(mPics[position].caption);
        convertView.setTag(mPics[position]);
        
/*
		Drawable toRecycle= imageView.getDrawable();
		if (toRecycle != null) {
		    ((BitmapDrawable)imageView.getDrawable()).getBitmap().recycle();
		}
	*/	
        
		//NEW: download cached
		final File cachefile = new File(mPics[position].cache);
		if (cachefile.isFile()) {
			imageView.setImageURI(Uri.parse("file://" + cachefile.getAbsolutePath()));
		} else {
	        final TwAjax t = new TwAjax(mContext, true, false);
	        
			imageView.setImageBitmap(null);
			t.urlDownloadToFile(mPics[position].url, cachefile.getAbsolutePath(), new Runnable() {
				@Override
				public void run() {
					imageView.setImageURI(Uri.parse("file://" + cachefile.getAbsolutePath()));
				}
			});
		}
		
        return convertView;
    }

}