package com.example.friend_detect;

import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends Activity {
	public static User myAccount;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		myAccount = new User();
		getDeviceId();
		signIn();
		startGPSTracking();
		sendRequest();
	}

	public void sendRequest() {
		try {
			CloudManager.sendRequest("5712947610");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void startGPSTracking() {
		Intent mServiceIntent = new Intent(this, GPSTracker.class);
		this.startService(mServiceIntent);
	}

	public void getDeviceId() {
		Context mAppContext = getBaseContext();
		TelephonyManager tMgr = (TelephonyManager) mAppContext
				.getSystemService(Context.TELEPHONY_SERVICE);
		try {
			myAccount.setDeviceId(tMgr.getDeviceId());
			myAccount.setPhoneNumber("5712947610");// tMgr.getLine1Number());
			Log.d("DEVICE ID:", tMgr.getDeviceId());
			// Log.d("PHONE NUMBER:",tMgr.getLine1Number());
		} catch (Exception e) {
			Log.e("",
					"This is most likely an emulator because phone number or device id not found. We will be giving a simulation device id.");
			myAccount.setDeviceId("1234");
			myAccount.setPhoneNumber("571-294-7610");
		}
	}

	public void signIn() {
		try {
			CloudManager.signIn(myAccount.getDeviceId(),
					myAccount.getPhoneNumber());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ContentResolver cr = getContentResolver();
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
				null, null, null);
		if (cur.getCount() > 0) {
			while (cur.moveToNext()) {
				String id = cur.getString(cur
						.getColumnIndex(ContactsContract.Contacts._ID));
				String name = cur
						.getString(cur
								.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				Log.d("", "CONTACT:" + name);
				Log.d("", "ID:" + id);
				// if (Integer.parseInt(cur.getString(
				// cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)))
				// > 0) {
				// Cursor pCur = cr.query(
				// ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
				// null,
				// ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
				// new String[]{id}, null);
				// while (pCur.moveToNext()) {
				// String phoneNo =
				// pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
				// }
				// pCur.close();
			}
		}
		// }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
