package com.example.friend_detect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.AttributeValueUpdate;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;

public class CloudManager {
	private static BasicAWSCredentials credentials = new BasicAWSCredentials("AKIAJIPVGDJJUBINMZAQ", "pZ0bHTCdvFWVXQIKsyEjADQ5EVqBYQhz65MbDuQ4");
	public CloudManager(){}
	//pushes the location of this device to the database
	public static void pushLocation(Location arg0)
	{
		pushLocation pid0 = new pushLocation();
		Object[]params = {arg0,credentials};
		pid0.execute(params);
	}
	//downloads user info from database that corresponds with deviceId
	public static void signIn(String deviceId, String phoneNumber) throws InterruptedException, ExecutionException
	{
		signIn pid0 = new signIn();
		Object[]params = {deviceId,phoneNumber,credentials};
		User myUser = pid0.execute(params).get();
		MainActivity.myAccount = myUser;
	}
	//sends request to other device 
	public static void sendRequest(String phoneNumber) throws InterruptedException, ExecutionException
	{
		sendRequest pid0 = new sendRequest();
		getTrackingId gti = new getTrackingId();
		String trackingIdTracker = gti.execute(phoneNumber,credentials).get();
		Object[]params = {credentials, trackingIdTracker};
		pid0.execute(params);
	}
	//accepts request of other device to begin tracking
	public static void approveRequest(String phoneNumber) throws InterruptedException, ExecutionException
	{
		approveRequest pid0 = new approveRequest();
		getTrackingId gti = new getTrackingId();
		String trackingIdTrackee = gti.execute(phoneNumber,credentials).get();
		Object[]params = {trackingIdTrackee,credentials};
		pid0.execute(params);
	}
	//returns all devices that are being tracked by this device
	public static ArrayList<User> getTrackees() throws InterruptedException, ExecutionException
	{
		Object[]params={credentials};
		getTrackees pid0 = new getTrackees();
		ArrayList<User>users = pid0.execute(params).get();
		return users;
	}
	public static ArrayList<User> getRequests() throws InterruptedException, ExecutionException
	{
		Object[]params={credentials};
		getRequests pid0 = new getRequests();
		return pid0.execute(params).get();
	}

	public static boolean phoneNumberExists(String phoneNumber) throws InterruptedException, ExecutionException
	{
		phoneNumberExists pid0 = new phoneNumberExists();
		Object[]params = {phoneNumber,credentials};
		return pid0.execute(params).get();
	}
	public static void filterContacts() throws InterruptedException, ExecutionException
	{
		filterContactExists pid0 = new filterContactExists();
		filterRequestExists pid1 = new filterRequestExists();
		Object[]params={credentials};
		HashSet<String>hs = new HashSet<String>();
		hs=pid0.execute(params).get();
		HashSet<String>phoneNumbers = new HashSet<String>();
		phoneNumbers=pid1.execute(params).get();
		for(int index=MainActivity.myContacts.size()-1;index>=0;index--)
		{
			if(!hs.contains(MainActivity.myContacts.get(index).phoneNumber))
			{
				MainActivity.myContacts.remove(index);
			}
		}
		for(int index=MainActivity.myContacts.size()-1;index>=0;index--)
		{
			if(phoneNumbers.contains(MainActivity.myContacts.get(index).phoneNumber))
			{
				MainActivity.myContacts.remove(index);
			}
		}
	}
}
//class getRequests extends AsyncTask<Object, Void, ArrayList<User>>
//{
//	@Override
//	protected ArrayList<User> doInBackground(Object... params) {
//		BasicAWSCredentials credentials = (BasicAWSCredentials) params[0];
//		AmazonDynamoDBClient client = new AmazonDynamoDBClient(credentials);
//		Map<String, Condition>scanfilter = new HashMap<String, Condition>();
//		Condition condition = new Condition()
//		.withComparisonOperator(ComparisonOperator.EQ.toString())
//		.withAttributeValueList(new AttributeValue().withS(MainActivity.myAccount.getTrackId()));
//		scanfilter.put("trackingIdTracker", condition);
//		ScanRequest scanRequest1 = new ScanRequest();
//		scanRequest1.setTableName("USER_REQUESTS");
//		ScanResult sr1 = client.scan(scanRequest1);
//		
//		return null;
//	}
//}
class filterContactExists extends AsyncTask<Object,Void, HashSet<String>>
{
	@Override
	protected HashSet<String> doInBackground(Object... params) {
		HashSet<String>phoneNumbers=new HashSet<String>();
		BasicAWSCredentials credentials = (BasicAWSCredentials) params[0];
		AmazonDynamoDBClient client = new AmazonDynamoDBClient(credentials);
		ScanRequest scanRequest = new ScanRequest();
		scanRequest.setTableName("USERS");
		ScanResult sr = client.scan(scanRequest);
		for(Map<String,AttributeValue>m:sr.getItems())
		{
			phoneNumbers.add(m.get("phoneNumber").getS());
		}
		return phoneNumbers;
	}
}
class filterRequestExists extends AsyncTask<Object,Void, HashSet<String>>
{
	@Override
	protected HashSet<String> doInBackground(Object... params) {
		HashSet<String>trackers=new HashSet<String>();
		HashSet<String>phoneNumbers=new HashSet<String>();
		BasicAWSCredentials credentials = (BasicAWSCredentials) params[0];
		AmazonDynamoDBClient client = new AmazonDynamoDBClient(credentials);
		Map<String, Condition>scanfilter = new HashMap<String, Condition>();
		Condition condition = new Condition()
		.withComparisonOperator(ComparisonOperator.EQ.toString())
		.withAttributeValueList(new AttributeValue().withS(MainActivity.myAccount.getTrackId()));
		scanfilter.put("trackingIdTrackee", condition);
		ScanRequest scanRequest1 = new ScanRequest();
		scanRequest1.setTableName("USER_REQUESTS");
		ScanResult sr1 = client.scan(scanRequest1);
		for(Map<String,AttributeValue>m:sr1.getItems())
		{
			trackers.add(m.get("trackingIdTracker").getS());
		}
		ScanRequest scanRequest2 = new ScanRequest();
		scanRequest2.setTableName("USERS");
		ScanResult sr2 = client.scan(scanRequest2);
		for(int index=sr2.getItems().size()-1;index>=0;index--)
		{
			Map<String,AttributeValue>m=sr2.getItems().get(index);
			if(trackers.contains(m.get("trackingId").getS()))
			{
				phoneNumbers.add(m.get("phoneNumber").getS());
			}
		}
		return phoneNumbers;
	}
}
//TODO This should not be a scan. Very inefficient and would make more sense to do some sort of getitemrequest
class phoneNumberExists extends AsyncTask<Object, Void, Boolean>
{
	@Override
	protected Boolean doInBackground(Object... params) {
		BasicAWSCredentials credentials = (BasicAWSCredentials) params[1];
		AmazonDynamoDBClient client = new AmazonDynamoDBClient(credentials);
		Map<String, Condition>scanfilter = new HashMap<String, Condition>();
		Condition condition = new Condition()
		.withComparisonOperator(ComparisonOperator.EQ.toString())
		.withAttributeValueList(new AttributeValue().withS((String) params[0]));
		scanfilter.put("phoneNumber", condition);
		ScanRequest scanRequest = new ScanRequest();
		scanRequest.setScanFilter(scanfilter);
		scanRequest.setTableName("USERS");
		ScanResult sr = client.scan(scanRequest);
		if(sr.getItems().size()>0)
			return true;
		else
			return false;
	}
}
class getTrackees extends AsyncTask<Object, Void, ArrayList<User>>
{
	@Override
	protected ArrayList<User> doInBackground(Object... params) {
		BasicAWSCredentials credentials = (BasicAWSCredentials) params[0];
		AmazonDynamoDBClient client = new AmazonDynamoDBClient(credentials);
		Map<String, Condition>scanfilter = new HashMap<String, Condition>();
		Condition condition1 = new Condition()
		.withComparisonOperator(ComparisonOperator.EQ.toString())
		.withAttributeValueList(new AttributeValue().withS(MainActivity.myAccount.getTrackId()));
		Condition condition2 = new Condition()
		.withComparisonOperator(ComparisonOperator.EQ.toString())
		.withAttributeValueList(new AttributeValue().withN("1"));
		scanfilter.put("trackingIdTracker", condition1);
		scanfilter.put("status",condition2);
		ScanRequest scanRequest = new ScanRequest();
		scanRequest.setScanFilter(scanfilter);
		scanRequest.setTableName("USER_REQUESTS");
		ScanResult sr = client.scan(scanRequest);
		ArrayList<String>linkingIds=new ArrayList<String>();
		for(Map<String,AttributeValue>m: sr.getItems())
		{
			linkingIds.add(m.get("trackingIdTrackee").getS());
		}
		ArrayList<User>users = new ArrayList<User>();
		for(String s:linkingIds)
		{
		Condition condition = new Condition()
		.withAttributeValueList(new AttributeValue().withS(s))
		.withComparisonOperator(ComparisonOperator.EQ.toString());
		scanfilter = new HashMap<String, Condition>();
		scanfilter.put("trackingId",condition);
		scanRequest = new ScanRequest();
		scanRequest.setScanFilter(scanfilter);
		scanRequest.setTableName("USERS");
		sr = client.scan(scanRequest);
		for(Map<String,AttributeValue>m:sr.getItems())
			{
			String s1=m.get("lat").getS();
			s1=s1.replace(",","");
			double lat = Double.parseDouble(s1);
			String s2=m.get("lon").getS();
			s2=s2.replace(",","");
			double lon = Double.parseDouble(s2);
			User temp = new User(m.get("deviceId").getS(),m.get("trackingId").getS(),lat,lon,m.get("phoneNumber").getS());
			users.add(temp);
			}
		}
		return users;
	}
	
}
class pushLocation extends AsyncTask<Object,Void,Boolean>
{
	@Override
	protected Boolean doInBackground(Object... params) {
		Location arg0 = (Location) params[0];
		BasicAWSCredentials credentials = (BasicAWSCredentials) params[1];
		AmazonDynamoDBClient client = new AmazonDynamoDBClient(credentials);
		Map<String, AttributeValue> key = new HashMap<String, AttributeValue>();
		key.put("deviceId", new AttributeValue().withS(MainActivity.myAccount.getDeviceId()));
		Map<String, AttributeValueUpdate> item = new HashMap<String, AttributeValueUpdate>();
		item.put("lat", new AttributeValueUpdate().withValue(new AttributeValue(""+arg0.getLatitude())));
		item.put("lon", new AttributeValueUpdate().withValue(new AttributeValue(""+arg0.getLongitude())));
		item.put("time", new AttributeValueUpdate().withValue(new AttributeValue(""+System.currentTimeMillis())));
		UpdateItemRequest updateRequest = new UpdateItemRequest();
		updateRequest.setKey(key);
		updateRequest.withTableName("USERS");
		updateRequest.withAttributeUpdates(item);
		client.updateItem(updateRequest);
		return true;
	}
}
//TODO Upon signing in, you should check if other user has same phone number as you. If so, keep most recent user, and delete all the others
class signIn extends AsyncTask<Object,Void,User>
{
	@Override
	protected User doInBackground(Object... params) {
		User myUser = new User();
		String phoneNumber = (String)params[1];
		String deviceId = (String)params[0];
		BasicAWSCredentials credentials = (BasicAWSCredentials) params[2];
		AmazonDynamoDBClient client = new AmazonDynamoDBClient(credentials);
		Map<String, AttributeValue>key=new HashMap<String, AttributeValue>();
//		AttributeValue av = new AttributeValue();
//		key.put("deviceId", new AttributeValue());
//		GetItemRequest getItemRequest = new GetItemRequest("USERS",key);
//		GetItemResult getItemResult = client.getItem(getItemRequest);
		Map<String, Condition>scanfilter = new HashMap<String, Condition>();
		Condition condition = new Condition()
		.withComparisonOperator(ComparisonOperator.EQ.toString())
		.withAttributeValueList(new AttributeValue().withS(deviceId));
		scanfilter.put("deviceId", condition);
		ScanRequest scanRequest = new ScanRequest();
		scanRequest.setScanFilter(scanfilter);
		scanRequest.setTableName("USERS");
		try{
		ScanResult scanResult = client.scan(scanRequest);
		//Log.d("",scanResult.toString());
		if(scanResult.getItems().size()>0){
			myUser.setTrackId(scanResult.getItems().get(0).get("trackingId").getS());
			myUser.setPhoneNumber(phoneNumber);
			myUser.setDeviceId(scanResult.getItems().get(0).get("deviceId").getS());
		}
		else{
			PutItemRequest pr = new PutItemRequest();
			HashMap<String,AttributeValue>item=new HashMap<String,AttributeValue>();
			item.put("deviceId", new AttributeValue().withS(deviceId));
			myUser.setDeviceId(deviceId);
			item.put("phoneNumber", new AttributeValue().withS(phoneNumber));
			myUser.setPhoneNumber(phoneNumber);
			item.put("lat", new AttributeValue().withN("0"));
			myUser.setLat(0);
			item.put("lon", new AttributeValue().withN("0"));
			myUser.setLon(0);
			item.put("time", new AttributeValue().withN(""+System.currentTimeMillis()));
			String trackingId = UUID.randomUUID().toString();
			myUser.setTrackId(trackingId);
			item.put("trackingId", new AttributeValue().withS(trackingId));
			pr.withTableName("USERS");
			pr.withItem(item);
			PutItemResult send = client.putItem(pr);
		}
		}
		catch(Exception e){
			Log.e("",e.toString());
		}
		
		return myUser;
	}
}
class getTrackingId extends AsyncTask<Object, Void, String>
{
	@Override
	protected String doInBackground(Object... params) {
		BasicAWSCredentials credentials = (BasicAWSCredentials) params[1];
		AmazonDynamoDBClient client = new AmazonDynamoDBClient(credentials);
		String phoneNumber = (String) params[0];
		Map<String, Condition>scanfilter = new HashMap<String, Condition>();
		Condition condition = new Condition()
		.withComparisonOperator(ComparisonOperator.EQ.toString())
		.withAttributeValueList(new AttributeValue().withS(phoneNumber));
		scanfilter.put("phoneNumber", condition);
		ScanRequest scanRequest = new ScanRequest();
		scanRequest.setScanFilter(scanfilter);
		scanRequest.setTableName("USERS");
		ScanResult sr = client.scan(scanRequest);
		String linkId = sr.getItems().get(0).get("trackingId").getS();
		return linkId;
	}	
}
class sendRequest extends AsyncTask<Object, Void, Void>
{
	@Override
	protected Void doInBackground(Object... params) {
		BasicAWSCredentials credentials = (BasicAWSCredentials) params[0];
		String trackingIdTrackee = MainActivity.myAccount.getTrackId();
		String trackingIdTracker=(String) params[1];
		AmazonDynamoDBClient client = new AmazonDynamoDBClient(credentials);
		Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
		item.put("trackingIdTracker", new AttributeValue().withS(""+trackingIdTracker));
		item.put("trackingIdTrackee", new AttributeValue().withS(""+trackingIdTrackee));
		item.put("status", new AttributeValue().withN("0"));
		PutItemRequest putRequest = new PutItemRequest();
		putRequest.withTableName("USER_REQUESTS");
		putRequest.withItem(item);
		client.putItem(putRequest);
		return null;
	}
}
class approveRequest extends AsyncTask<Object,Void,Boolean>
{
	@Override
	protected Boolean doInBackground(Object... params) {
		String trackingIdTrackee = (String) params[0];
		BasicAWSCredentials credentials = (BasicAWSCredentials) params[1];
		String trackingIdTracker = MainActivity.myAccount.getTrackId();
		AmazonDynamoDBClient client = new AmazonDynamoDBClient(credentials);
		Map<String, AttributeValue> key = new HashMap<String, AttributeValue>();
		key.put("trackingIdTracker", new AttributeValue().withS(trackingIdTracker));
		key.put("trackingIdTrackee", new AttributeValue().withS(trackingIdTrackee));
		Map<String, AttributeValueUpdate> item = new HashMap<String, AttributeValueUpdate>();
		item.put("status", new AttributeValueUpdate().withValue(new AttributeValue().withN("1")));
		UpdateItemRequest updateRequest = new UpdateItemRequest();
		updateRequest.setKey(key);
		updateRequest.withTableName("USER_REQUESTS");
		updateRequest.withAttributeUpdates(item);
		client.updateItem(updateRequest);
		return true;
	}
}
class getRequests extends AsyncTask<Object, Void, ArrayList<User>>
{
	@Override
	protected ArrayList<User> doInBackground(Object... params) {
		BasicAWSCredentials credentials = (BasicAWSCredentials) params[0];
		AmazonDynamoDBClient client = new AmazonDynamoDBClient(credentials);
		Map<String, Condition>scanfilter = new HashMap<String, Condition>();
		Condition condition1 = new Condition()
		.withComparisonOperator(ComparisonOperator.EQ.toString())
		.withAttributeValueList(new AttributeValue().withS(MainActivity.myAccount.getTrackId()));
		Log.d("","TEST:"+MainActivity.myAccount.getDeviceId());
		Condition condition2 = new Condition()
		.withComparisonOperator(ComparisonOperator.EQ.toString())
		.withAttributeValueList(new AttributeValue().withN("0"));
		scanfilter.put("trackingIdTracker", condition1);
		scanfilter.put("status",condition2);
		ScanRequest scanRequest = new ScanRequest();
		scanRequest.setScanFilter(scanfilter);
		scanRequest.setTableName("USER_REQUESTS");
		ScanResult sr = client.scan(scanRequest);
		HashSet<String>linkingIds=new HashSet<String>();
		for(Map<String,AttributeValue>m: sr.getItems())
		{
			linkingIds.add(m.get("trackingIdTrackee").getS());
		}
		ArrayList<User>users = new ArrayList<User>();
		scanRequest = new ScanRequest();
		for(String s:linkingIds)
		{
		Condition condition = new Condition()
		.withAttributeValueList(new AttributeValue().withS(s))
		.withComparisonOperator(ComparisonOperator.EQ.toString());
		scanRequest.addScanFilterEntry("trackingId", condition);
		}
		scanRequest.setTableName("USERS");
		sr = client.scan(scanRequest);
		for(Map<String,AttributeValue>m:sr.getItems())
		{
			if(linkingIds.contains(m.get("trackingId").getS())){
						User temp = new User(m.get("deviceId").getS(),m.get("trackingId").getS(),Double.parseDouble(m.get("lat").getS()),Double.parseDouble(m.get("lon").getS()),m.get("phoneNumber").getS());
						users.add(temp);}
		}
		return users;
	}
}
