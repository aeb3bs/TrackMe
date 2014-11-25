package com.example.friend_detect;
import java.util.HashMap;
import java.util.Map;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class GPSTracker extends Service implements LocationListener{
	Location mLocation;
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
		
	}
	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}
