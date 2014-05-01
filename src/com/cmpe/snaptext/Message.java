package com.cmpe.snaptext;

import android.util.Log;

public class Message {
	
	public String phoneNumber;
	public boolean received;
	public String message;
	public String type;
	
	public void print() {
		Log.e("## Message: ", message);
		Log.e("## type: ", type);
	}
	
	public Message(String phoneNumber, boolean received, String message, String type) {
		this.phoneNumber = phoneNumber;
		this.received = received;
		this.message = message;
		this.type = type;
	} 

}
