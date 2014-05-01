package com.cmpe.snaptext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import android.os.AsyncTask;
import android.util.Log;

public class SendMsgToServer extends AsyncTask<Void,Void,Void>{
	String msg;
	String receiverNumber;
	String senderNumber;
	String type;
	
	public SendMsgToServer(String msg, String receiverNumber,String senderNumber,String type) {
		this.msg = msg;
		this.senderNumber = senderNumber;
		this.receiverNumber = receiverNumber;
		this.type = type;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected Void doInBackground(Void... arg0) {
        try {
			URL url = new URL("http://snaptext.foamsnet.com:5000/send_message?message="+URLEncoder.encode(msg)
							+"&sender_number="+senderNumber+"&receiver_number=" +receiverNumber+"&message_type="+type);
			URLConnection connection;
			connection = url.openConnection();
			connection.connect();			
			// download the file
	        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	        Log.i("TAG",br.readLine());
		} catch (IOException e) {
			e.printStackTrace();
		}
        return null;
	}
}

