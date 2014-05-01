package com.cmpe.snaptext;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class SendMsgActivity extends Activity implements OnClickListener{
	
	private Button btn_send,btn_pic;
	private EditText et_message;
	private String receiverNumber;
	private MessageAdapter adapter;
	private ListView listview;
	private String senderNumber;
	private SharedPreferences sharedPrefereces;
	File photo;
	//Camera
    private static int TAKE_PICTURE_FROM_BACK_CAMERA = 1;
    private int i=1;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_send_msg);
		getActionBar().setTitle(getIntent().getStringExtra("name"));
		receiverNumber = getIntent().getStringExtra("phone");
		btn_send = (Button) findViewById(R.id.btn_send);
		btn_pic = (Button) findViewById(R.id.btn_pic);
		listview = (ListView) findViewById(R.id.messageHistoryList);
		et_message = (EditText) findViewById(R.id.et_message);
		btn_send.setOnClickListener(this);
		btn_pic.setOnClickListener(this);
		sharedPrefereces = getSharedPreferences(RegisterDevice.MyPREFERENCES, Context.MODE_PRIVATE);
		populateMessages();
		
		// BroadcastReceiver to receive notifications
		BroadcastReceiver notifier = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (!intent.getStringExtra("sender").equalsIgnoreCase(receiverNumber))
					return;
				// TODO: type might be incorrect. verify it.
				adapter.add(new Message("", true, intent.getStringExtra("msg"), intent.getStringExtra("type")));
                adapter.notifyDataSetChanged();
                listview.setSelection(adapter.getCount() - 1);
			}
		};
		//TODO : Call Unregister for this
		// Tie the broadcast receiver to GCM receiver
		IntentFilter notifyFilter = new IntentFilter(GcmBroadcastReceiver.NOTIFIER_INTENT);
        registerReceiver(notifier, notifyFilter);
	}

	
	
	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.btn_pic){
			takePhoto(v);
		}
		if(v.getId() == R.id.btn_send){
			String msg = et_message.getText().toString();
			senderNumber = sharedPrefereces.getString("phoneNumber"," ");
			ContactsDB.addContactMessage(this, receiverNumber, msg, false,"text");
			new SendMsgToServer(msg,receiverNumber,senderNumber,"text").execute();
			adapter.add(new Message("", false, msg, "text"));
			adapter.notifyDataSetChanged();
			listview.setSelection(adapter.getCount() - 1);
			et_message.setText("");
		}
	}
	
	public void send(){
		Log.i("photo000000000",photo.getAbsolutePath());
		String origFile = photo.getAbsolutePath();
		Bitmap bitmap = BitmapFactory.decodeFile(origFile);
		File file = new File(origFile);
		boolean deleted = file.delete();
		try {
			bitmap.compress(CompressFormat.JPEG, 50, new FileOutputStream(new File(origFile)));
			bitmap.recycle();
		} catch (Exception e) {
			e.printStackTrace();
		}
		String msg = photo.getAbsolutePath();
		senderNumber = sharedPrefereces.getString("phoneNumber"," ");
		ContactsDB.addContactMessage(this, receiverNumber,photo.getAbsolutePath(), false, "image");
		new SendImageToServer(msg,receiverNumber,senderNumber).execute();
		adapter.add(new Message("", false, msg, "image"));
		adapter.notifyDataSetChanged();
		listview.setSelection(adapter.getCount() - 1);
		et_message.setText("");
	}
	
	private void populateMessages() {
		ContactsDB.init(this);
		Cursor cursor = ContactsDB.getContactMessages(receiverNumber);
		adapter = new MessageAdapter(this, R.layout.listview_row);
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			String msg = cursor.getString(2);
			Log.e("## type:", msg != null ? msg : "null");
			do {
				adapter.add(new Message("", cursor.getInt(1) == 1, cursor.getString(0), cursor.getString(2)));
			} while (cursor.moveToNext());
		}
		listview.setAdapter(adapter);
		listview.setSelection(adapter.getCount() - 1);
		ContactsDB.deactivate();
	}
	
	public void takePhoto(View v) {
	       
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        // storing in the external storage public directory 
        photo = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "pic"+i+".jpg");
        i++;
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
        imageUri = Uri.fromFile(photo);
        // start the camera methods returns in OnActivityResult
        startActivityForResult(intent, TAKE_PICTURE_FROM_BACK_CAMERA);
    }
	
    // override the original activity result function
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
        
        case 1:
           
            if(resultCode == Activity.RESULT_OK) {           
               send();
            }
        }
    }
 
}

class MessageAdapter extends ArrayAdapter<Message> {
	
	private ArrayList<Message> messages;
	
	public MessageAdapter(Context context, int resource) {
		super(context, resource);
		messages = new ArrayList<Message>();
	}
	
	public int getCount() {
		return messages.size();
	}
	
	public Message getItem(int index) {
		return messages.get(index);
	}
	
	public void add(Message message) {
		messages.add(message);	
	}

	@SuppressWarnings("deprecation")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		if (row == null) {
			LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(R.layout.listview_row, parent, false);
		}
		Message message = messages.get(position);
		TextView tv = (TextView)row.findViewById(R.id.tv_message);
		ImageView iv = (ImageView)row.findViewById(R.id.iv_message);
		if (message.type == null || message.type.equalsIgnoreCase("text")) {
			tv.setBackgroundResource(message.received ? R.drawable.bubble_yellow : R.drawable.bubble_green);
			tv.setText(message.message);
			tv.setVisibility(View.VISIBLE);
			iv.setVisibility(View.GONE);
		} else if(message.type.equalsIgnoreCase("image")) {
			Log.e("###", "im here " + message.message);
			Bitmap bitmap = BitmapFactory.decodeFile(message.message);
			int destHeight = (int) ( bitmap.getHeight() * (256.0 / bitmap.getWidth()) );
			Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 256, destHeight, true);
			bitmap.recycle();
			iv.setImageBitmap(scaled);
			iv.setVisibility(View.VISIBLE);
			tv.setVisibility(View.GONE);
		}
		((LinearLayout)row.findViewById(R.id.wrapper)).setGravity(message.received ? Gravity.LEFT : Gravity.RIGHT);
		return row;
	}
}


