package com.cmpe.snaptext;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

public class DownloadFile extends AsyncTask<Void, Void, Void>{

	private URL url;
	private String msg;
	private String senderNumber;
	GcmIntentService gis;
    private String TAG = "DownloadFile";
    private Intent intent;
    public static final String NOTIFIER_INTENT = "com.cmpe.snaptext.intent.action.NOTIFY";
	
	public DownloadFile(String msg,GcmIntentService gis,String senderNumber,Intent intent ) {
		this.msg = msg;
		this.gis = gis;
		this.senderNumber = senderNumber;
		this.intent = intent;
	}

	@Override
	protected Void doInBackground(Void... params) {
	
			URLConnection connection;
			try {
				URL url = new URL(msg);
				connection = url.openConnection();
				connection.connect();			
				String[] str = url.toString().split("/");
				Log.i("Activity Name ---- File Name","" +"---" +  str[str.length - 1]);
				// download the file
		        InputStream input = new BufferedInputStream(url.openStream());
		        String msg1 = Environment.getExternalStorageDirectory() + "/" + str[str.length - 1];
		        Log.i(TAG,msg1);
		        OutputStream output = new FileOutputStream(Environment.getExternalStorageDirectory() + "/" + str[str.length - 1]);
		        Log.i("output",output.toString());
				byte data[] = new byte[1024];
	            int count;
	            while ((count = input.read(data)) != -1) {
	                output.write(data, 0, count);
	            }
	            output.flush();
	            output.close();
	            input.close();
	            
	            ContactsDB.addContactMessage(gis, senderNumber, msg1, true,"image");
		        String senderName = ContactsDB.getContactName(gis, senderNumber);
			    // Post notification of received message.
		        
			    //sendNotification(msg,senderName,senderNumber);
			    Log.i(TAG, senderName + msg);
			    Intent notifier = new Intent(NOTIFIER_INTENT);
		        notifier.putExtra("sender", senderNumber);
		        notifier.putExtra("msg", msg1);
		        notifier.putExtra("type", "image");
		        gis.sendBroadcast(notifier);
		        //GcmBroadcastReceiver.completeWakefulIntent(intent);
			} catch (IOException e) {
				e.printStackTrace();
			}
	 return null;       
	}
}