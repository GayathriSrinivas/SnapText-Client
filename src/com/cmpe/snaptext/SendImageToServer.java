package com.cmpe.snaptext;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class SendImageToServer extends AsyncTask<Void, Void, Void> {


	private String TAG = getClass().getName();
	private String fileName ;
	String receiverNumber;
	String senderNumber;
	
	public SendImageToServer(String fileName,String receiverNumber,String senderNumber){
		this.fileName = fileName;
		this.receiverNumber = receiverNumber;
		this.senderNumber = senderNumber;
		//this.fileName = "/storage/emulated/0/gals.jpg";
	}
	
	@Override
	protected Void doInBackground(Void... params) {
        //Looper.prepare(); //For Preparing Message Pool for the child Thread
        HttpClient client = new DefaultHttpClient();
        //HttpConnectionParams.setConnectionTimeout(client.getParams(), 1000000); //Timeout Limit
        HttpResponse response;
        try {
        	String url = new String("http://snaptext.foamsnet.com:5000/image");
            HttpPost post = new HttpPost(url);
            Log.e("h###", fileName);
            FileEntity reqEntity = new FileEntity(new File(fileName), "image/jpeg");
            post.setEntity(reqEntity);
            response = client.execute(post);
            StringBuilder sb= new StringBuilder();
            /*Checking response */
            if(response!=null){
                InputStream in = response.getEntity().getContent(); //Get the data in the entity
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                
                String line = br.readLine();
                while(line != null){
                	Log.i(TAG,line);
                	sb.append(line);
                	line = br.readLine();
                }
                
            
                JSONObject data = new JSONObject(sb.toString());
                Log.i("Data",sb.toString());
    			new SendMsgToServer(data.getString("fileName"),receiverNumber,senderNumber,"image").execute();
            }else
	           Log.i(TAG,"Response is NULL ...but SENT !!!");
        } catch(Exception e) {
            e.printStackTrace();
        }
       Log.i(TAG, "Background task over");
	  return null;
	}
	

}
