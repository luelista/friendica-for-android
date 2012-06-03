package de.wikilab.android.friendica01;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.TextView;

public class Max {


	public static final String DATA_DIR = "/sdcard/Android/data/de.wikilab.android.friendica01";
	public static final String IMG_CACHE_DIR = DATA_DIR + "/cache/imgs";
	
	public static void initDataDirs() {
		new File(DATA_DIR).mkdirs();
		new File(IMG_CACHE_DIR).mkdirs();
		
	}
	
	//warum muss das in Android so kompliziert sein???
	public static void resizeImage(String pathOfInputImage, String pathOfOutputImage, int dstWidth, int dstHeight) {
		try
		{
			int inWidth = 0;
			int inHeight = 0;

			InputStream in = new FileInputStream(pathOfInputImage);

			// decode image size (decode metadata only, not the whole image)
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(in, null, options);
			in.close();
			in = null;

			// save width and height
			inWidth = options.outWidth;
			inHeight = options.outHeight;

			// decode full image pre-resized
			in = new FileInputStream(pathOfInputImage);
			options = new BitmapFactory.Options();
			// calc rought re-size (this is no exact resize)
			options.inSampleSize = Math.max(inWidth/dstWidth, inHeight/dstHeight);
			// decode full image
			Bitmap roughBitmap = BitmapFactory.decodeStream(in, null, options);

			// calc exact destination size
			Matrix m = new Matrix();
			RectF inRect = new RectF(0, 0, roughBitmap.getWidth(), roughBitmap.getHeight());
			RectF outRect = new RectF(0, 0, dstWidth, dstHeight);
			m.setRectToRect(inRect, outRect, Matrix.ScaleToFit.CENTER);
			float[] values = new float[9];
			m.getValues(values);

			// resize bitmap
			Bitmap resizedBitmap = Bitmap.createScaledBitmap(roughBitmap, (int) (roughBitmap.getWidth() * values[0]), (int) (roughBitmap.getHeight() * values[4]), true);

			// save image
			try
			{
				FileOutputStream out = new FileOutputStream(pathOfOutputImage);
				resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
			}
			catch (Exception e)
			{
				Log.e("Image", e.getMessage(), e);
			}
		}
		catch (IOException e)
		{
			Log.e("Image", e.getMessage(), e);
		}
	}



	/** 
	 * Copy the content of the input stream into the output stream, using a temporary 
	 * byte array buffer whose size is defined by {@link #IO_BUFFER_SIZE}. 
	 * 
	 * @param in The input stream to copy from. 
	 * @param out The output stream to copy to. 
	 * 
	 * @throws IOException If any error occurs during the copy. 
	 */  
	public static void copy(InputStream in, OutputStream out) throws IOException {  
		final int IO_BUFFER_SIZE = 4 * 1024;  
		byte[] b = new byte[IO_BUFFER_SIZE];  
		int read;
		while ((read = in.read(b)) != -1) {  
			out.write(b, 0, read);
		}
	} 


	public static String getBaseName(String fileSpec) {
		if (fileSpec == null) return null;
		int pos = fileSpec.lastIndexOf("/");
		if (pos == -1) return fileSpec; else return fileSpec.substring(pos+1);
	}
	public static String getBaseNameWithoutExtension(String fileSpec) {
		if (fileSpec == null) return null;
		return getBaseName(fileSpec).replaceFirst("[.][^.]+$", "");
	}
	public static String getFileExtension(String fileSpec) {
		if (fileSpec == null) return null;
		int pos = fileSpec.lastIndexOf(".");
		if (pos == -1) return fileSpec; else return fileSpec.substring(pos+1);
	}
	public static String getRealPathFromURI(Context ctx, Uri contentUri) {
		Log.i("FileTypes", "URI = "+contentUri + ", scheme = "+contentUri.getScheme());
		if (contentUri.getScheme().equals("file")) {
			return contentUri.getPath();
		} else {
			String[] proj = { MediaStore.Images.Media.DATA };
			Cursor cursor = ctx.getContentResolver().query(contentUri, proj, null, null, null);
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			String res= cursor.getString(column_index);
			cursor.close();
			return res;
		}
	}




	public static void setHtmlWithImages(TextView t, String html) {
		// first parse the html
		// replace getHtmlCode() with whatever generates/fetches your html
		Spanned spanned = Html.fromHtml(html);
		Spannable htmlSpannable;
		
		// we need a SpannableStringBuilder for later use
		if (spanned instanceof SpannableStringBuilder) {
			// for now Html.fromHtml() returns a SpannableStringBuiler
			// so we can just cast it
			htmlSpannable = (SpannableStringBuilder) spanned;
		} else {
			// but we have a fallback just in case this will change later
			// or a custom subclass of Html is used
			htmlSpannable = new SpannableStringBuilder(spanned);
		}

		// now we can call setText() on the next view.
		// this won't show any images yet
		t.setText(htmlSpannable);

		// next we start a AsyncTask that loads the images
		new ImageLoadTask(t.getContext(), htmlSpannable, t).execute();
		
	}

	private static class ImageLoadTask extends AsyncTask<Void, ImageSpan, Void> {

		private static final String TAG = "friendica01.Max.ImageLoadTask";
		
		DisplayMetrics metrics;
		Spannable htmlSpannable;
		Resources resources;
		TextView htmlTextView;
		TwAjax t;
		
		public ImageLoadTask(Context c, Spannable htmlSpannablep, TextView htmlTextViewp) {
			htmlSpannable = htmlSpannablep;
			htmlTextView = htmlTextViewp;
			
			// we need this to properly scale the images later
			//c.getWindowManager().getDefaultDisplay().getMetrics(metrics);
			resources = c.getResources();
			metrics = resources.getDisplayMetrics();
			t= new TwAjax(c, true, false);
		}
		
		@Override
		protected void onPreExecute() {
		}

		@Override
		protected Void doInBackground(Void... params) {

			// iterate over all images found in the html
			for (ImageSpan img : htmlSpannable.getSpans(0,
					htmlSpannable.length(), ImageSpan.class)) {

				Log.d(TAG, "Loading: "+img.getSource());
				
				File cachedFile = getImageFile(img);
				if (!cachedFile.isFile()) {
					t.urlDownloadToFile(img.getSource(), cachedFile.getAbsolutePath(), null);
					Log.d(TAG, "Download done");
				}

				// we use publishProgress to run some code on the
				// UI thread to actually show the image
				// -> onProgressUpdate()
				publishProgress(img);

			}

			return null;

		}

		@Override
		protected void onProgressUpdate(ImageSpan... values) {

			// save ImageSpan to a local variable just for convenience
			ImageSpan img = values[0];

			// now we get the File object again. so remeber to always return
			// the same file for the same ImageSpan object
			File cache = getImageFile(img);

			// if the file exists, show it
			if (cache.isFile()) {

				Log.d(TAG, "File OK");
				
				// first we need to get a Drawable object
				Drawable d = new BitmapDrawable(resources, cache.getAbsolutePath());
				
				// next we do some scaling
				int width, height;
				int originalWidthScaled = (int) (d.getIntrinsicWidth() * metrics.density);
				int originalHeightScaled = (int) (d.getIntrinsicHeight() * metrics.density);
				if (originalWidthScaled > metrics.widthPixels) {
					height = d.getIntrinsicHeight() * metrics.widthPixels
							/ d.getIntrinsicWidth();
					width = metrics.widthPixels;
				} else {
					height = originalHeightScaled;
					width = originalWidthScaled;
				}

				// it's important to call setBounds otherwise the image will
				// have a size of 0px * 0px and won't show at all
				d.setBounds(0, 0, width, height);

				// now we create a new ImageSpan
				ImageSpan newImg = new ImageSpan(d, img.getSource());

				// find the position of the old ImageSpan
				int start = htmlSpannable.getSpanStart(img);
				int end = htmlSpannable.getSpanEnd(img);

				// remove the old ImageSpan
				htmlSpannable.removeSpan(img);

				// add the new ImageSpan
				htmlSpannable.setSpan(newImg, start, end,
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

				//finally we have to update the TextView with our
				// updates Spannable to display the image
				htmlTextView.setText(htmlSpannable);
			} else {
				Log.e(TAG, "File NOT FOUND = Download error");
				
			}
		}

		private File getImageFile(ImageSpan img) {
			return new File(IMG_CACHE_DIR + "/url_" + Max.cleanFilename(img.getSource()));
		}

	}

	public static String cleanFilename(String url) {
		return url.replaceAll("[^a-z0-9.-]", "_");
		
	}
	

}
