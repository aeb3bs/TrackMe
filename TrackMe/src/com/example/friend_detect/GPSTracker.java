package com.example.friend_detect;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class GPSTracker extends Service implements LocationListener{
	Location mLocation;
	HashMap<String, Integer>hm=new HashMap<String,Integer>();
	@Override
	  public int onStartCommand(Intent intent, int flags, int startId){
		
		LocationManager mLocationManager;
		mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, this);
		return Service.START_NOT_STICKY;
	  }
	public GPSTracker() {
		super();
		Log.d("","constructor was called.");
		
		Log.d("","onHandleIntent was called.");
	}
	
	@Override
	public void onLocationChanged(Location arg0) {
		Log.d("","location being updated...");
		mLocation = arg0;
		Log.d("","pushing location to the amazon cloud...");
		//CloudManager cManager = new CloudManager();
		//cManager.pushLocation(arg0);
		CloudManager.pushLocation(arg0);
		Log.d("","pulling user ids from friends list...");
		Log.d("","pulling the locations of nearby friends...");
		MainActivity.myAccount.setLat(arg0.getLatitude());
		MainActivity.myAccount.setLon(arg0.getLongitude());
		try {
			getNearbyTrackees();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void getNearbyTrackees() throws InterruptedException, ExecutionException
	{
		ArrayList<User>users=CloudManager.getTrackees();
		for(User user: users)
		{
			float miles = MainActivity.getDistance((float)MainActivity.myAccount.getLat(), (float)MainActivity.myAccount.getLon(), (float)user.getLat(), (float)user.getLon());
			String name=MainActivity.getName(user.getPhoneNumber());
			if(miles<1)
			{
				if(hm.get(user.getPhoneNumber()) == null || hm.get(user.getPhoneNumber())==0)
				{
					sendNotification(name+" is nearby!", name+" is approximately " + miles + " miles away.");
					hm.put(user.getPhoneNumber(), 180);
				}
				else
				{
					hm.put(user.getPhoneNumber(),hm.get(user.getPhoneNumber())-1);
				}
			}
		}
	}
	public void sendNotification(String title, String msg)
	{
    	NotificationCompat.Builder mBuilder =
    	        new NotificationCompat.Builder(this)
    	        .setSmallIcon(R.drawable.trackme)
    	        .setContentTitle(title)
    	        .setContentText(msg);
    	// Creates an explicit intent for an Activity in your app
    	Intent resultIntent = new Intent(this, MainActivity.class);

    	// The stack builder object will contain an artificial back stack for the
    	// started Activity.
    	// This ensures that navigating backward from the Activity leads out of
    	// your application to the Home screen.
    	TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
    	// Adds the back stack for the Intent (but not the Intent itself)
    	stackBuilder.addParentStack(MainActivity.class);
    	// Adds the Intent that starts the Activity to the top of the stack
    	stackBuilder.addNextIntent(resultIntent);
    	PendingIntent resultPendingIntent =
    	        stackBuilder.getPendingIntent(
    	            0,
    	            PendingIntent.FLAG_UPDATE_CURRENT
    	        );
    	mBuilder.setContentIntent(resultPendingIntent);
    	NotificationManager mNotificationManager =
    	    (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
    	// mId allows you to update the notification later on.
    	mNotificationManager.notify(0, mBuilder.build());
	}
	@Override
	public void onProviderDisabled(String arg0) {
		//  Auto-generated method stub
		
	}
	@Override
	public void onProviderEnabled(String arg0) {
		//  Auto-generated method stub
		
	}
	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		//  Auto-generated method stub
		
	}
	@Override
	public IBinder onBind(Intent intent) {
		//  Auto-generated method stub
		return null;
	}
}
