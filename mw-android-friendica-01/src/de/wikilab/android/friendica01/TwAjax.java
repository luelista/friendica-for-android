package de.wikilab.android.friendica01;
/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONTokener;
import org.w3c.dom.Document;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

public class TwAjax extends Thread {
	private static final String TAG="Friendica/TwAjax";
	
	
	{
		java.util.logging.Logger.getLogger("org.apache.http.wire").setLevel(java.util.logging.Level.FINEST);
		//java.util.logging.Logger.getLogger("org.apache.http.headers").setLevel(java.util.logging.Level.FINEST);

		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
		System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
		System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", "debug");
		System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "debug");
		System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.headers", "debug");

	}
	
	private Runnable myCallback;
	private Handler myHandler;
	private String myMethod;
	private List<NameValuePair> myPostData;
	private String myUrl;
	private String myResult;
	private int myHttpStatus;
	private Exception myError;
	private boolean success;
	private String twSession;
	private List<PostFile> myPostFiles;
	private boolean convertToBitmap = false;
	private boolean convertToXml = false;
	private String downloadToFile = null;
	private String fetchHeader = null;
	public Header[] fetchHeaderResult = null;
	private Bitmap myBmpResult;
	private String myHttpAuthUser, myHttpAuthPass;
	private Document myXmlDocument;
	private String myProxyIp, myProxyUsername, myProxyPassword;
	private int myProxyPort;
	private static final BasicCookieStore cookieStoreManager = new BasicCookieStore();
	public boolean ignoreSSLCerts = false;
	
	public class IgnoreCertsSSLSocketFactory extends SSLSocketFactory {
	    SSLContext sslContext = SSLContext.getInstance("TLS");

	    public IgnoreCertsSSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
	        super(truststore);

	        TrustManager tm = new X509TrustManager() {
	            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	            }

	            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	            }

	            public X509Certificate[] getAcceptedIssuers() {
	                return null;
	            }
	        };

	        sslContext.init(null, new TrustManager[] { tm }, null);
	    }

	    @Override
	    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
	        return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
	    }

	    @Override
	    public Socket createSocket() throws IOException {
	        return sslContext.getSocketFactory().createSocket();
	    }
	}

	
	public static class PostFile {
		public static final int MAX_BUFFER_SIZE = 1*1024*1024;
		private String fieldName,remoteFilename;
		private File file;
		public PostFile(String postFieldName, String attachmentFileName, String localFileName) {
			fieldName=postFieldName; remoteFilename=attachmentFileName;
			file=new File(localFileName);			
		}
		public PostFile(String postFieldName, String attachmentFileName, File localFile) {
			fieldName=postFieldName; remoteFilename=attachmentFileName;
			file=localFile;			
		}
		public void writeToStream(DataOutputStream outputStream, String boundary) throws IOException {
			outputStream.writeBytes("--" + boundary + "\r\n");
			outputStream.writeBytes("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + remoteFilename +"\"" + "\r\n");
			outputStream.writeBytes("\r\n");

			FileInputStream fileInputStream = new FileInputStream(file);
			int bytesAvailable = fileInputStream.available();
			int bufferSize = Math.min(bytesAvailable, MAX_BUFFER_SIZE);
			byte[] buffer = new byte[bufferSize];

			int bytesRead = fileInputStream.read(buffer, 0, bufferSize); // Read file

			while (bytesRead > 0) {
				outputStream.write(buffer, 0, bufferSize);
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, MAX_BUFFER_SIZE);
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			}

			outputStream.writeBytes("\r\n");
			fileInputStream.close();
		}
	}
	
	public void addPostFile(PostFile file) {
		if (myPostFiles == null) myPostFiles = new ArrayList<PostFile>();
		myPostFiles.add(file);
	}
	
	/*
	HttpURLConnection connection = null;
	DataOutputStream outputStream = null;
	DataInputStream inputStream = null;

	String pathToOurFile = "/data/file_to_send.mp3";
	String urlServer = "http://192.168.1.1/handle_upload.php";
	String lineEnd = "\r\n";
	String twoHyphens = "--";
	String boundary =  "*****";

	int bytesRead, bytesAvailable, bufferSize;
	byte[] buffer;
	int maxBufferSize = 1*1024*1024;

	try
	{
	FileInputStream fileInputStream = new FileInputStream(new File(pathToOurFile) );

	URL url = new URL(urlServer);
	connection = (HttpURLConnection) url.openConnection();

	// Allow Inputs & Outputs
	connection.setDoInput(true);
	connection.setDoOutput(true);
	connection.setUseCaches(false);

	// Enable POST method
	connection.setRequestMethod("POST");

	connection.setRequestProperty("Connection", "Keep-Alive");
	connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);

	outputStream = new DataOutputStream( connection.getOutputStream() );
	outputStream.writeBytes(twoHyphens + boundary + lineEnd);
	outputStream.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + pathToOurFile +"\"" + lineEnd);
	outputStream.writeBytes(lineEnd);

	bytesAvailable = fileInputStream.available();
	bufferSize = Math.min(bytesAvailable, maxBufferSize);
	buffer = new byte[bufferSize];

	// Read file
	bytesRead = fileInputStream.read(buffer, 0, bufferSize);

	while (bytesRead > 0)
	{
	outputStream.write(buffer, 0, bufferSize);
	bytesAvailable = fileInputStream.available();
	bufferSize = Math.min(bytesAvailable, maxBufferSize);
	bytesRead = fileInputStream.read(buffer, 0, bufferSize);
	}

	outputStream.writeBytes(lineEnd);
	outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

	// Responses from the server (code and message)
	serverResponseCode = connection.getResponseCode();
	serverResponseMessage = connection.getResponseMessage();

	fileInputStream.close();
	outputStream.flush();
	outputStream.close();
	}
	catch (Exception ex)
	{
	//Exception handling
	}
	*/
	
	public void updateProxySettings(Context ctx) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		myProxyIp = prefs.getString("proxy_host", null);
		if (myProxyIp == null || myProxyIp.equals("")) {
			myProxyIp = null;
			
			ProxySelector defaultProxySelector = ProxySelector.getDefault();
	        List<Proxy> proxyList = defaultProxySelector.select(URI.create("http://frnd.tk"));
	        //Log.i("TwAjax", "proxyCount="+proxyList.size()+"|"+((InetSocketAddress)proxyList.get(0).address()).getHostName());
	        if (proxyList.size() == 0 || proxyList.get(0).address() == null) {
	        	return;
			}
			myProxyIp = ((InetSocketAddress)proxyList.get(0).address()).getHostName();
			myProxyPort = ((InetSocketAddress)proxyList.get(0).address()).getPort();
		} else {
			myProxyPort = Integer.valueOf(prefs.getString("proxy_port", null));
		}
		
		//for(String key:prefs.getAll().keySet()) {
		//Log.w("PREF:",key+"="+prefs.getAll().get(key).toString());	
		//}
		myProxyUsername = prefs.getString("proxy_user", null);
		myProxyPassword = prefs.getString("proxy_password", null);
		Log.i("TwAjax", "PROXY SETTINGS:");
		Log.i("TwAjax", "Host=" + myProxyIp);
		Log.i("TwAjax", "Port=" + myProxyPort);
		Log.i("TwAjax", "User=" + myProxyUsername);
		
	}
	
	public TwAjax() {
	}
	public TwAjax(String sessionID) {
		twSession = sessionID;
	}
	public TwAjax(Context ctx, boolean updateProxySettings, boolean initializeLoginData) {
		if (initializeLoginData) this.initializeLoginData(ctx);
		if (updateProxySettings) this.updateProxySettings(ctx);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		if(prefs.getBoolean("ssl_unsafe", false) == true) ignoreSSLCerts = true;
		
	}
	
	public String getURL() {
		return myUrl;
	}
	public void setURL(String newUrl) {
		myUrl = newUrl;
	}
	public String getMethod() {
		return myMethod;
	}
	public void setMethod(String newMethod) {
		myMethod = newMethod;
	}
	public List<NameValuePair> getPostData() {
		return myPostData;
	}
	public void setPostData(List<NameValuePair> newPostData) {
		myPostData = newPostData;
	}
	public void addPostData(String key, String value) {
		if (myPostData == null) myPostData = new ArrayList<NameValuePair>();
		myPostData.add(new BasicNameValuePair(key, value));
	}
	public boolean initializeLoginData(Context ctx) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		String userName = prefs.getString("login_user", null);
		if (userName == null || userName.length() < 1) return false;
		this.myHttpAuthUser = userName;
		this.myHttpAuthPass = prefs.getString("login_password", null);
		return true;
	}
	public int getHttpCode() {
		return myHttpStatus;
	}
	public boolean isSuccess() {
		return success;
	}
	public Exception getError() {
		return myError;
	}
	public Object getJsonResult() {
		if (!success) return null;
		try {
			JSONTokener jt = new JSONTokener(myResult);
			return jt.nextValue();
		} catch (JSONException ex) {
			//server returned malformed data
			return null;
		}
	}
	public String getResult() {
		return myResult;
	}
	public Bitmap getBitmapResult() {
		return myBmpResult;
	}
	public Document getXmlDocumentResult() {
		return myXmlDocument;
	}
	
	public void run() {
        try {
			if (myPostFiles == null) {
				runDefault();
			} else {
				runFileUpload();
			}
        } catch (Exception e) {
        	success=false;
        	myError = e;
        }
        if (myHandler != null && myCallback != null) myHandler.post(myCallback);
	}
	
    private void setHttpClientProxy(DefaultHttpClient httpclient) {
    	if (myProxyIp == null) return;
    	
        httpclient.getCredentialsProvider().setCredentials(  
                new AuthScope(myProxyIp, myProxyPort),  
                new UsernamePasswordCredentials(  
                        myProxyUsername, myProxyPassword));  

       HttpHost proxy = new HttpHost(myProxyIp, myProxyPort);  

       httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);  


    }  
	
    public DefaultHttpClient getNewHttpClient() {
        if (ignoreSSLCerts) {
    	try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            SSLSocketFactory sf = new IgnoreCertsSSLSocketFactory(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

            return new DefaultHttpClient(ccm, params);
        } catch (Exception e) {
            return new DefaultHttpClient();
        }
        } else {
            return new DefaultHttpClient();
        }
    }

    
	private void runDefault() throws IOException {
		Log.v("TwAjax", "runDefault URL="+myUrl);
		
		// Create a new HttpClient and Get/Post Header
		DefaultHttpClient httpclient = getNewHttpClient();
        setHttpClientProxy(httpclient);
        httpclient.getParams().setParameter("http.protocol.content-charset", "UTF-8");
        //final HttpParams params = new BasicHttpParams();
        HttpClientParams.setRedirecting(httpclient.getParams(), false);

        HttpRequestBase m;
        if (myMethod == "POST") {
	        m = new HttpPost(myUrl);
	        ((HttpPost)m).setEntity(new UrlEncodedFormEntity(myPostData, "utf-8"));
        } else {
        	m = new HttpGet(myUrl);
        }
        m.addHeader("Host", m.getURI().getHost());
        if (twSession != null) m.addHeader("Cookie", "twnetSID=" + twSession);
        httpclient.setCookieStore(cookieStoreManager);
        
		//generate auth header if user/pass are provided to this class
		if (this.myHttpAuthUser != null) {
			m.addHeader("Authorization", "Basic "+Base64.encodeToString((this.myHttpAuthUser+":"+this.myHttpAuthPass).getBytes(), Base64.NO_WRAP));
		}
        // Execute HTTP Get/Post Request
        HttpResponse response = httpclient.execute(m);
        //InputStream is = response.getEntity().getContent();
        myHttpStatus = response.getStatusLine().getStatusCode();
        if (this.fetchHeader != null) {
        	this.fetchHeaderResult = response.getHeaders(this.fetchHeader);
        	Header[] h = response.getAllHeaders();
        	for(Header hh : h) Log.d(TAG, "Header "+hh.getName()+"="+hh.getValue());
        	
        } else if (this.downloadToFile != null) {
        	Log.v("TwAjax", "runDefault downloadToFile="+downloadToFile);
            // download the file
            InputStream input = new BufferedInputStream(response.getEntity().getContent());
            OutputStream output = new FileOutputStream(downloadToFile);

            byte data[] = new byte[1024];

            long total = 0; int count;
            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....
                //publishProgress((int)(total*100/lenghtOfFile));
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();
        } else if (this.convertToBitmap) {
        	myBmpResult = BitmapFactory.decodeStream(response.getEntity().getContent());
        } else if (this.convertToXml) {
        	try {
				myXmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(response.getEntity().getContent());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
        	
        } else {
            myResult = EntityUtils.toString(response.getEntity(), "UTF-8");
        }
        //BufferedInputStream bis = new BufferedInputStream(is);
        //ByteArrayBuffer baf = new ByteArrayBuffer(50);

        //int current = 0;
        //while((current = bis.read()) != -1){
        //    baf.append((byte)current);
        //}

        //myResult = new String(baf.toByteArray(), "utf-8");
        success=true;
	}
	private void runFileUpload() throws IOException {

		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary =  "d1934afa-f2e4-449b-99be-8be6ebfec594";
		Log.i("Andfrnd/TwAjax", "URL="+getURL());
		URL url = new URL(getURL());
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		
		// Allow Inputs & Outputs
		connection.setDoInput(true);
		connection.setDoOutput(true);
		connection.setUseCaches(false);

		// Enable POST method
		connection.setRequestMethod("POST");

		//generate auth header if user/pass are provided to this class
		if (this.myHttpAuthUser != null) {
			connection.setRequestProperty("Authorization", "Basic "+Base64.encodeToString((this.myHttpAuthUser+":"+this.myHttpAuthPass).getBytes(), Base64.NO_WRAP));
		}
		//Log.i("Andfrnd","-->"+connection.getRequestProperty("Authorization")+"<--");
		
		connection.setRequestProperty("Host", url.getHost());
		connection.setRequestProperty("Connection", "Keep-Alive");
		connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);

		DataOutputStream outputStream = new DataOutputStream( connection.getOutputStream() );
		for(NameValuePair nvp : myPostData) {
			outputStream.writeBytes(twoHyphens + boundary + lineEnd);
			outputStream.writeBytes("Content-Disposition: form-data; name=\""+nvp.getName()+"\"" + lineEnd);
			outputStream.writeBytes(lineEnd+nvp.getValue()+lineEnd);
		}
		for(PostFile pf : myPostFiles) {
			pf.writeToStream(outputStream, boundary);
		}
		outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

		// Responses from the server (code and message)
		myHttpStatus = connection.getResponseCode();

		outputStream.flush();
		outputStream.close();
		
		if (myHttpStatus < 400) {
			myResult = convertStreamToString(connection.getInputStream());
		} else {
			myResult = convertStreamToString(connection.getErrorStream());
		}
		
        success=true;
	}
    public String convertStreamToString(InputStream is) throws IOException {
		/*
		 * To convert the InputStream to String we use the
		 * Reader.read(char[] buffer) method. We iterate until the
		 * Reader return -1 which means there's no more data to
		 * read. We use the StringWriter class to produce the string.
		 */
		if (is != null) {
			StringWriter writer = new StringWriter();
		
		    char[] buffer = new char[1024];
		    try {
		    	BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		        int n;
		        while ((n = reader.read(buffer)) != -1) {
		            writer.write(buffer, 0, n);
		        }
		    } finally {
		        is.close();
		    }
		    return writer.toString();
		} else {        
		    return "";
		}
	}
	public void fetchUrlHeader(String method, String url, String headerFieldToFetch, Runnable callback) {
		this.myMethod = method;
		this.myUrl = url;
		this.myCallback = callback;
		this.fetchHeader = headerFieldToFetch;
		if (callback != null) {
			this.myHandler = new Handler();
			this.start();
		} else {
			this.run();
		}
	}
	public void getUrlContent(String url, Runnable callback) {
		this.myMethod = "GET";
		this.myUrl = url;
		this.myCallback = callback;
		if (callback != null) {
			this.myHandler = new Handler();
			this.start();
		} else {
			this.run();
		}
	}
	public void getUrlBitmap(String url, Runnable callback) {
		this.myMethod = "GET";
		this.myUrl = url;
		this.myCallback = callback;
		this.convertToBitmap = true;
		if (callback != null) {
			this.myHandler = new Handler();
			this.start();
		} else {
			this.run();
		}
	}
	public void getUrlXmlDocument(String url, Runnable callback) {
		this.myMethod = "GET";
		this.myUrl = url;
		this.myCallback = callback;
		this.convertToXml = true;
		if (callback != null) {
			this.myHandler = new Handler();
			this.start();
		} else {
			this.run();
		}
	}
	public void urlDownloadToFile(String url, String targetFileSpec, Runnable callback) {
		this.myMethod = "GET";
		this.myUrl = url;
		this.myCallback = callback;
		this.downloadToFile = targetFileSpec;
		if (callback != null) {
			this.myHandler = new Handler();
			this.start();
		} else {
			this.run();
		}
	}
	public void uploadFile(String url, Runnable callback) {
		try {
			this.myMethod = "POST";
			this.myUrl = url;
			this.myCallback = callback;
			if (callback != null) { //async
				this.myHandler = new Handler();
				this.start();
			} else { //sync
				this.run();
			}
		} catch (Exception e) {
			success=false; myError=e; if (callback != null) callback.run();
		}
	}
	public void postData(String url, Runnable callback) {
		try {
			this.myMethod = "POST";
			this.myUrl = url;
			this.myCallback = callback;
			if (callback != null) { //async
				this.myHandler = new Handler();
				this.start();
			} else { //sync
				this.run();
			}
		} catch (Exception e) {
			success=false; myError=e; if (callback != null) callback.run();
		}
	}
	
}
