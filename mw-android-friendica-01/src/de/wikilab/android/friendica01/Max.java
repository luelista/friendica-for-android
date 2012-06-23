package de.wikilab.android.friendica01;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class Max {
	private static final String TAG="Friendica/Max";
	


	public static final String DATA_DIR = "/sdcard/Android/data/de.wikilab.android.friendica01";
	public static final String IMG_CACHE_DIR = DATA_DIR + "/cache/imgs";
	
	public static PendingIntent piTimerNotifications;
	
	public static void initDataDirs() {
		new File(DATA_DIR).mkdirs();
		new File(IMG_CACHE_DIR).mkdirs();
		
	}
	
    public static String Hexdump(byte[] data) {
    	StringBuffer b = new StringBuffer();
        char[] parts = new char[17];
        int partsloc = 0;
        for (int i = 0; i < data.length; i++) {
            int d = ((int) data[i]) & 0xff;
            if (d == 0) {
                parts[partsloc++] = '.';
            } else if (d < 32 || d >= 127) {
                parts[partsloc++] = '?';
            } else {
                parts[partsloc++] = (char) d;
            }
            if (i % 16 == 0) {
                int start = Integer.toHexString(data.length).length();
                int end = Integer.toHexString(i).length();

                for (int j = start; j > end; j--) {
                    b.append("0");
                }
                b.append(Integer.toHexString(i) + ": ");
            }
            if (d < 16) {
            	b.append("0" + Integer.toHexString(d));
            } else {
            	b.append(Integer.toHexString(d));
            }
            if ((i & 15) == 15 || i == data.length - 1) {
            	b.append("      " + new String(parts) + "\n");
                partsloc = 0;
            } else if ((i & 7) == 7) {
            	b.append("  ");
                parts[partsloc++] = ' ';
            } else if ((i & 1) == 1) {
            	b.append(" ");
            }
        }
        b.append("\n");
        return b.toString();
    }
	
    public static void runTimer(Context c) {
    	Log.i("Friendica", "try runTimer");
    	if (piTimerNotifications != null) return;
    	AlarmManager a = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
    	piTimerNotifications = PendingIntent.getService(c, 1, new Intent(c, NotificationCheckerService.class), 0);
    	a.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 0, AlarmManager.INTERVAL_FIFTEEN_MINUTES, piTimerNotifications);
    	Toast.makeText(c, "Friendica: Notif. check timer run", Toast.LENGTH_SHORT).show();
    	Log.i("Friendica", "done runTimer");
    }
    
    public static void cancelTimer(Context c) {
    	Log.i("Friendica", "try cancelTimer");
    	if (piTimerNotifications == null) return;
    	AlarmManager a = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
    	a.cancel(piTimerNotifications);
    	piTimerNotifications = null;
    	Toast.makeText(c, "Friendica: Notif. check timer cancel", Toast.LENGTH_SHORT).show();
    	Log.i("Friendica", "done cancelTimer");
    }

    public static String readFile(String path) throws IOException {
    	FileInputStream stream = new FileInputStream(new File(path));
    	try {
    		FileChannel fc = stream.getChannel();
    		MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
    		/* Instead of using default, pass in a decoder. */
    		return Charset.defaultCharset().decode(bb).toString();
    	}
    	finally {
    		stream.close();
    	}
    }
    
    public static String getServer(Context ctx) {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		return  prefs.getString("login_protocol", null) + "://" + prefs.getString("login_server", null);
    }
    

    public static File getTempFile() {
		return new File(Max.IMG_CACHE_DIR, "imgUploadTemp_" + System.currentTimeMillis() + ".jpg");
	}
    
    /**
	 * 
	 * @param ctx     MUST IMPLEMENT LoginListener !!!
	 * @param errmes
	 */
	public static void tryLogin(final Activity ctx) {
		final ProgressDialog pd = new ProgressDialog(ctx);
		pd.setMessage("Logging in...");
		pd.show();		

		String server = Max.getServer(ctx);
		
		final TwAjax t = new TwAjax(ctx, true, true);
		t.getUrlContent(server+"/api/account/verify_credentials", new Runnable() {
			@Override
			public void run() {
				pd.dismiss();
				try {
					if (t.isSuccess()) {
						if (t.getHttpCode() == 200) {
							JSONObject r = (JSONObject) t.getJsonResult();
							String name = r.getString("name");
							((TextView)ctx.findViewById(R.id.selected_clipboard)).setText(name);
							((LoginListener) ctx).OnLogin();
							
							
							final TwAjax profileImgDl = new TwAjax();
							final String targetFs = IMG_CACHE_DIR+"/my_profile_pic_"+r.getString("id")+".jpg";
							if (new File(targetFs).isFile()) {
								((ImageView)ctx.findViewById(R.id.profile_image)).setImageURI(Uri.parse("file://"+targetFs));
							} else {
								profileImgDl.urlDownloadToFile(r.getString("profile_image_url"), targetFs, new Runnable() {
									@Override
									public void run() {
										((ImageView)ctx.findViewById(R.id.profile_image)).setImageURI(Uri.parse("file://"+targetFs));
									}
								});
							}
							
						} else {
							showLoginForm(ctx, "Error:"+t.getResult());
						}
					} else {
						
						showLoginForm(ctx, "ERR:"+t.getError().toString());
					}
					
				} catch(Exception ex) {
					showLoginForm(ctx, "ERR2:"+t.getResult()+ex.toString());
					
				}
			}
		});
	}
	
	/**
	 * 
	 * @param ctx     MUST IMPLEMENT LoginListener !!!
	 * @param errmes
	 */
	public static void showLoginForm(final Activity ctx, String errmes) {
		View myView = ctx.getLayoutInflater().inflate(R.layout.loginscreen, null, false);
		final AlertDialog alert = new AlertDialog.Builder(ctx)
		.setTitle("Login to Friendica")
		.setView(myView)
		.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				ctx.finish();
			}
		})
		.show();

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		String protocol = prefs.getString("login_protocol", null);
		String server = prefs.getString("login_server", null);
		String userName = prefs.getString("login_user", null);
		
		if (errmes != null) {
			((TextView)myView.findViewById(R.id.lblInfo)).setText(errmes);
		}
		
		final Spinner selProtocol = (Spinner)myView.findViewById(R.id.selProtocol);
		selProtocol.setSelection(selProtocol.equals("https") ? 1 : 0);            //HACK !!!
		
		final EditText edtServer = (EditText)myView.findViewById(R.id.edtServer);
		edtServer.setText(server);
		
		final EditText edtUser = (EditText)myView.findViewById(R.id.edtUser);
		edtUser.setText(userName);

		final EditText edtPassword = (EditText)myView.findViewById(R.id.edtPassword);
		
		((TextView)myView.findViewById(R.id.proxy_settings)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ctx.startActivity(new Intent(ctx, PreferencesActivity.class));
			}
		});
		
		((Button)myView.findViewById(R.id.button1)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(ctx).edit();
				prefs.putString("login_protocol", selProtocol.getSelectedItem().toString());
				prefs.putString("login_server", edtServer.getText().toString());
				prefs.putString("login_user", edtUser.getText().toString());
				prefs.putString("login_password", edtPassword.getText().toString());
				prefs.commit();
				
				alert.dismiss();
				
				tryLogin(ctx);
			}
		});
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

	private static class ImageLoadTask extends AsyncTask<Void, Object, Void> {

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

				if (cachedFile.isFile()) {
					Drawable d = new BitmapDrawable(resources, cachedFile.getAbsolutePath());
					
					// we use publishProgress to run some code on the
					// UI thread to actually show the image
					// -> onProgressUpdate()
					publishProgress(img, d);
				}

			}

			return null;

		}

		@Override
		protected void onProgressUpdate(Object... values) {

			// save ImageSpan to a local variable just for convenience
			ImageSpan img = (ImageSpan) values[0];
			Drawable d = (Drawable) values[1];

			// now we get the File object again. so remeber to always return
			// the same file for the same ImageSpan object
			File cache = getImageFile(img);

			// if the file exists, show it
			//if (cache.isFile()) {

				Log.d(TAG, "File OK");
				
				// first we need to get a Drawable object
				//Drawable d = new BitmapDrawable(resources, cache.getAbsolutePath());
				
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
			//} else {
			//	Log.e(TAG, "File NOT FOUND = Download error");
				
			//}
		}

		private File getImageFile(ImageSpan img) {
			return new File(IMG_CACHE_DIR + "/url_" + Max.cleanFilename(img.getSource()));
		}

	}

	public static String cleanFilename(String url) {
		return url.replaceAll("[^a-z0-9.-]", "_");
		
	}
	
	public static Matcher regeximatch(String pattern, String string) {
		return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(string);
	}

	public static void alert(Context ctx, String text) {
		alert(ctx, text, null, "OK");
	}
	public static void alert(Context ctx, String text, String title) {
		alert(ctx, text, title, "OK");
	}
	public static void alert(Context ctx, String text, String title, String okButtonText) {
		AlertDialog ad = new AlertDialog.Builder(ctx).create();  
	    ad.setCancelable(false); // This blocks the 'BACK' button  
	    ad.setMessage(Html.fromHtml(text));
	    ad.setTitle(title);
	    ad.setButton(okButtonText, new DialogInterface.OnClickListener() {  
	        @Override  
	        public void onClick(DialogInterface dialog, int which) {  
	            dialog.dismiss();                      
	        }  
	    });  
	    ad.show();
	    ((TextView)ad.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
	}
	

}
