package com.example.friend_detect;

import java.util.ArrayList;
import java.util.HashMap;
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
	
	public static void pushLocation(Location arg0)
	{
		pushLocation pid0 = new pushLocation();
		Object[]params = {arg0,credentials};
		pid0.execute(params);
	}
	public static void signIn(String deviceId, String phoneNumber) throws InterruptedException, ExecutionException
	{
		signIn pid0 = new signIn();
		Object[]params = {deviceId,phoneNumber,credentials};
		User myUser = pid0.execute(params).get();
		MainActivity.myAccount = myUser;
	}
	public static void sendRequest(String phoneNumber) throws InterruptedException, ExecutionException
	{
		sendRequest pid0 = new sendRequest();
		getTrackingId gti = new getTrackingId();
		String trackingIdTracker = gti.execute(phoneNumber,credentials).get();
		Object[]params = {credentials, trackingIdTracker};
		pid0.execute(params);
	}
	public static void approveRequest(String phoneNumber) throws InterruptedException, ExecutionException
	{
		approveRequest pid0 = new approveRequest();
		getTrackingId gti = new getTrackingId();
		String trackingIdTrackee = gti.execute(phoneNumber,credentials).get();
		Object[]params = {trackingIdTrackee,credentials};
		pid0.execute(params);
	}
	public static ArrayList<User> getTrackees() throws InterruptedException, ExecutionException
	{
		Object[]params={credentials};
		getTrackees pid0 = new getTrackees();
		return pid0.execute(params).get();
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
			User temp = new User(m.get("deviceId").getS(),m.get("trackingId").getS(),Double.parseDouble(m.get("lat").getN()),Double.parseDouble(m.get("lon").getN()),m.get("phoneNumber").getS());
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
		String trackingIdTracker = (String) params[1];
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
		Condition condition2 = new Condition()
		.withComparisonOperator(ComparisonOperator.EQ.toString())
		.withAttributeValueList(new AttributeValue().withN("0"));
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
			User temp = new User(m.get("deviceId").getS(),m.get("trackingId").getS(),Double.parseDouble(m.get("lat").getN()),Double.parseDouble(m.get("lon").getN()),m.get("phoneNumber").getS());
			users.add(temp);
			}
		}
		return users;
	}
	
}
