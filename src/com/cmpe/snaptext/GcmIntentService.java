package com.cmpe.snaptext;

import java.net.URL;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;
import android.util.Log;

public class GcmIntentService extends IntentService{
	
	public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;
    private String TAG = "GcmIntentService";

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String msg = intent.getStringExtra("message");
        String senderNumber = intent.getStringExtra("sender");
        String type = intent.getStringExtra("type");
        if(type.equals("image")){
        	//Download Image
        	Log.i("image","image");
        	try{
        		Log.i("image1","image");
        		new DownloadFile(msg,this,senderNumber,intent).execute(); 
        		Log.i("image2","image");
        	}catch(Exception e){
        		e.printStackTrace();
        	}
        }else{
        	Log.i("not image","not image");
	        ContactsDB.addContactMessage(this, senderNumber, msg, true,"text");
	        String senderName = ContactsDB.getContactName(this, senderNumber);
		    // Post notification of received message.
		    sendNotification(msg,senderName,senderNumber);
		    Log.i(TAG, senderName + msg);
	        GcmBroadcastReceiver.completeWakefulIntent(intent);
        }
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg, String from , String fromNumber) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);
        
        Intent intent = new Intent(this, SendMsgActivity.class);
        intent.putExtra("name", from);
        intent.putExtra("phone", fromNumber);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
        .setSmallIcon(R.drawable.ic_launcher)
        .setContentTitle(from)
        .setLargeIcon(decodeImage(msg))
        .setStyle(new NotificationCompat.BigTextStyle()
        .bigText(msg))
        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
    
    private static Bitmap decodeImage(String image_data) {
    	// Decode the encoded string into largeIcon
    	Bitmap largeIcon = null;
    	if ((image_data != null) && (!image_data.equals(""))) {
    		byte[] decodedImage = Base64.decode(image_data, Base64.DEFAULT);
    		if (decodedImage != null) {
    			largeIcon = BitmapFactory.decodeByteArray(decodedImage
    					, 0
    					, decodedImage.length);
    		}
    	}
    	return largeIcon;
    }
   
}
