package com.example.friend_detect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.FragmentActivity;
import android.telephony.TelephonyManager;
import android.util.FloatMath;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
public class MainActivity extends FragmentActivity {
	public static ArrayList<Contact>myContacts = new ArrayList<Contact>();
	ActionBar.Tab tab1,tab2,tab3,tab4;
	public static User myAccount;
	public static android.support.v4.app.FragmentManager fragmentManager;
	Fragment fragmentTab1 = new FragmentTab1();
	Fragment fragmentTab2 = new FragmentTab2();
	Fragment fragmentTab3 = new FragmentTab3();
	Fragment fragmentTab4 = new FragmentTab4();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		fragmentManager = getSupportFragmentManager();
		
		myAccount = new User();
		myAccount.setDeviceId("000001");
		myAccount.setPhoneNumber("1111111111");
		signIn();
		
		
		Log.d("","TEST:"+myAccount.getTrackId());
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		startGPSTracking();
		
		tab1 = actionBar.newTab().setText("Map");
		tab2 = actionBar.newTab().setText("Nearby");
		tab3 = actionBar.newTab().setText("Incoming Requests");
		tab4 = actionBar.newTab().setText("Send Request");
		
		tab1.setTabListener(new Listener1<MapFragment>(this, "map", MapFragment.class));
        tab2.setTabListener(new Listener2<NearbyFragment>(this, "nearby", NearbyFragment.class));
        tab3.setTabListener(new Listener3<IncomingFragment>(this, "incoming", IncomingFragment.class));
        tab4.setTabListener(new Listener4<OutgoingFragment>(this, "outcoming", OutgoingFragment.class));
		
		actionBar.addTab(tab1);
		actionBar.addTab(tab2);
		actionBar.addTab(tab3);
		actionBar.addTab(tab4);
		
		try {
			getContacts(this);
			CloudManager.filterContacts();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
//		Intent intent = new Intent(this.getBaseContext(), NearbyActivity.class);
//		this.startActivity(intent);
//		getDeviceId();
//		signIn();
//		startGPSTracking();
//		sendRequest("7032814458");
//		approveRequest("7032814458");
//		ArrayList<User>trackees=getTrackees();
//		Log.d("","TEST:"+trackees);
//		 // Get ListView object from xml
//        listView.setOnItemClickListener(new OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view,
//               int position, long id) {
//              
//             // ListView Clicked item index
//             int itemPosition     = position;
//             
//             // ListView Clicked item value
//             String  itemValue    = (String) listView.getItemAtPosition(position);
//              
//              // Show Alert 
//              Toast.makeText(getApplicationContext(),
//                "Position :"+itemPosition+"  ListItem : " +itemValue , Toast.LENGTH_LONG)
//                .show();
//           
//            }
//
//       }); 
	}
	public ArrayList<User> getTrackees()
	{
		try {
			return CloudManager.getTrackees();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return null;
	}
	public void approveRequest(String phoneNumber){
		try {
			CloudManager.approveRequest(phoneNumber);
		} catch (InterruptedException e) {
			//  Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			//  Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void sendRequest(String phoneNumber) {
		try {
			CloudManager.sendRequest(phoneNumber);
		} catch (InterruptedException e) {
			//  Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			//  Auto-generated catch block
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
			Log.e("","Unable to get device id.");
		}
	}

	public void signIn() {
		try {
			CloudManager.signIn(myAccount.getDeviceId(),
					myAccount.getPhoneNumber());
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
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
	public static void getContacts(Activity x) throws InterruptedException, ExecutionException
	{
		myContacts = new ArrayList<Contact>();
		ContentResolver cr = x.getContentResolver();
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
				null, null, null);
		if (cur.getCount() > 0) {
			while (cur.moveToNext()) {
				String id = cur.getString(cur
						.getColumnIndex(ContactsContract.Contacts._ID));
				String name = cur
						.getString(cur
								.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				//String x = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.));
				Log.d("", "CONTACT:" + name);
				Log.d("", "ID:" + id);
				//Log.d("","CURRENT TESTING:"+x);
				if (Integer.parseInt(cur.getString(
		                   cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
		                Cursor pCur = cr.query(
		 		    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
		 		    null, 
		 		    ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?", 
		 		    new String[]{id}, null);
		 	        while (pCur.moveToNext()) {
		 		    String phoneNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA));
		 		    phoneNumber=phoneNumber.replace("-", "");
		 		    phoneNumber=phoneNumber.replace("(", "");
		 		    phoneNumber=phoneNumber.replace(")", "");
		 		    phoneNumber=phoneNumber.replace(" ", "");
		 		    Log.d("","###:"+phoneNumber);
		 		    Contact c = new Contact(id,name,phoneNumber);
		 		    myContacts.add(c);
		 	        } 
		 	        pCur.close();
		 	    }
			}
		}
	}
	public static float getDistance(float lat_a, float lng_a, float lat_b, float lng_b)
	{
		float pk = (float) (180/3.14169);

	    float a1 = lat_a / pk;
	    float a2 = lng_a / pk;
	    float b1 = lat_b / pk;
	    float b2 = lng_b / pk;

	    float t1 = FloatMath.cos(a1)*FloatMath.cos(a2)*FloatMath.cos(b1)*FloatMath.cos(b2);
	    float t2 = FloatMath.cos(a1)*FloatMath.sin(a2)*FloatMath.cos(b1)*FloatMath.sin(b2);
	    float t3 = FloatMath.sin(a1)*FloatMath.sin(b1);
	    float tt = (float) Math.acos(t1 + t2 + t3);
	    float miles = (float) ((float) 6366000*tt/1000*.62);
	    return miles;
	}
	public static String getName(String phoneNumber)
	{
		HashMap<String,String>hm=new HashMap<String,String>();
		for(Contact c:MainActivity.myContacts)
		{
			hm.put(c.phoneNumber,c.name);
		}
		if(hm.get(phoneNumber)!=null)
		{
			return hm.get(phoneNumber);
		}
		return phoneNumber;
	}
}

