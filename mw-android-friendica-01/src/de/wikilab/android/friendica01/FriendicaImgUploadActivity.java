package de.wikilab.android.friendica01;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

public class FriendicaImgUploadActivity extends Activity implements LoginListener {
	private static final String TAG="Friendica/FriendicaImgUploadActivity";

	public final static int RQ_SELECT_CLIPBOARD = 1;
	
	String uploadCbName = "";
	int uploadCbId = 0;
	
	String fileExt;
	Uri fileToUpload;
	boolean deleteAfterUpload;
	
	String textToUpload;
	boolean uploadTextMode;
	
	public static Bitmap loadResizedBitmap( String filename, int width, int height, boolean exact ) {
	    Bitmap bitmap = null;
	    BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile( filename, options );
	    if ( options.outHeight > 0 && options.outWidth > 0 ) {
	        options.inJustDecodeBounds = false;
	        options.inSampleSize = 2;
	        while (    options.outWidth  / options.inSampleSize > width
	                && options.outHeight / options.inSampleSize > height ) {
	            options.inSampleSize++;
	        }
	        options.inSampleSize--;

	        bitmap = BitmapFactory.decodeFile( filename, options );
	        if ( bitmap != null && exact ) {
	            bitmap = Bitmap.createScaledBitmap( bitmap, width, height, false );
	        }
	    }
	    return bitmap;
	}

	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        setContentView(R.layout.uploadfile);
        
        
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String userName = prefs.getString("login_user", null);
		if (userName == null || userName.length() < 1) {
			Max.showLoginForm(this, null);
		} else {
			Max.tryLogin(this);
		}
        
        /*
        View btn_select_clipboard = (View) findViewById(R.id.btn_select_clipboard);
        btn_select_clipboard.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				startActivityForResult(new Intent(UploadFile.this, ClipboardSelector.class), RQ_SELECT_CLIPBOARD);
			}
		});
        */
        
		//TextView t = new TextView(UploadFile.this);
        //EditText txtFilename = (EditText) findViewById(R.id.txt_filename);
        
        EditText t = (EditText) findViewById(R.id.maintb);
		t.setText("File Uploader\n\nERR: Intent did not contain file!\n\nPress menu button for debug info !!!\n\n");

		View btn_upload = (View) findViewById(R.id.btn_upload);
		btn_upload.setEnabled(false);
		
		//this.setContentView(t);
		
		Intent callingIntent = getIntent();
		if (callingIntent != null) {
			if (callingIntent.hasExtra(Intent.EXTRA_STREAM)) {
				fileToUpload = (Uri) callingIntent.getParcelableExtra(Intent.EXTRA_STREAM);
				String fileSpec = Max.getRealPathFromURI(FriendicaImgUploadActivity.this, fileToUpload);
				
				ImageView gallerypic =((ImageView)findViewById(R.id.preview));
				Drawable toRecycle= gallerypic.getDrawable();
				if (toRecycle != null) {
				    ((BitmapDrawable)gallerypic.getDrawable()).getBitmap().recycle();
				}
				
				//gallerypic.setImageURI(Uri.parse("file://"+fileSpec));
				gallerypic.setImageBitmap(loadResizedBitmap(fileSpec, 500, 300, false));
				
				//txtFilename.setText(Max.getBaseName(fileSpec));
				
				t.setText("Andfrnd Uploader Beta\n\n[b]URI:[/b] " + fileToUpload.toString() + "\n[b]File name:[/b] " + fileSpec);
				
				deleteAfterUpload = false;
				
				// restore data after failed upload:
				if (callingIntent.hasExtra(FileUploadService.EXTRA_DESCTEXT)) {
					t.setText(callingIntent.getStringExtra(FileUploadService.EXTRA_DESCTEXT));
				}
				
				uploadTextMode = false;
				btn_upload.setEnabled(true);
			}
		}
		
		
		btn_upload.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				EditText txtDesc = (EditText) findViewById(R.id.maintb);
				
				Intent uploadIntent = new Intent(getApplicationContext(), FileUploadService.class);
				Bundle b = new Bundle();
				//b.putInt(FileUploadService.EXTRA_CLIPBOARDID, uploadCbId);
				//b.putString("clipboardName", uploadCbName);
				//b.putBoolean(FileUploadService.EXTRA_DELETE, deleteAfterUpload);
				//b.putString(FileUploadService.EXTRA_FILENAME, txtFilename.getText().toString());
				b.putString(FileUploadService.EXTRA_DESCTEXT, txtDesc.getText().toString());
				/*
				if (uploadTextMode == true) {
					try {
						String fileName = "textUploadTemp_" + System.currentTimeMillis() + ".txt";
						FileOutputStream fos = openFileOutput(fileName, Activity.MODE_WORLD_READABLE);
						BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
						bw.write(textToUpload);
						bw.close();
						b.putParcelable(Intent.EXTRA_STREAM, Uri.parse("file://" + getFilesDir().getAbsolutePath() + "/" + fileName));
					} catch (IOException e) { Log.e("UploadFile", "unable to write temp file !!! this should never happen !!!"); return; }
					
				} else {*/
					b.putParcelable(Intent.EXTRA_STREAM, fileToUpload);
				/*}*/
				uploadIntent.putExtras(b);

				Log.i("Andfrnd/UploadFile", "before startService");
				startService(uploadIntent);
				Log.i("Andfrnd/UploadFile", "after startService");
				
				finish();
				
				
			}
		});
		
		//
	}
	

	@Override
	public void OnLogin() {

	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.uploadfile_menu, menu);
        return true;
    }
    private String getTypeName(Object o) {
    	if (o == null) return "<null>";
    	Class type = o.getClass();
    	if (type == null) return "<unknown>"; else return type.getCanonicalName();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.view_debug:
        	Intent callingIntent = getIntent();
    		if (callingIntent != null) {
    			Bundle e = callingIntent.getExtras();
    			String[] val = new String[e.keySet().size()];
    			String[] val2 = new String[e.keySet().size()];
    			int i=0;
    			for(String key : e.keySet()) {
    				val[i] = key+": "+String.valueOf(e.get(key));
    				val2[i++] = getTypeName(e.get(key))+" "+key+":\n"+String.valueOf(e.get(key));
    			}
    			final String[] values = val2;
    			
    			new AlertDialog.Builder(FriendicaImgUploadActivity.this)
    			.setItems(val, new OnClickListener() {
    				@Override
    				public void onClick(DialogInterface dialog, int which) {
    					new AlertDialog.Builder(FriendicaImgUploadActivity.this)
    					.setMessage(values[which])
    					.show();
    				}
    			})
    			.setTitle("Debug Info [File]")
    			.show();
    			
    		}
            return true;

        default:
            return super.onOptionsItemSelected(item);
        }
    }
    

}