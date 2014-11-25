package com.example.friend_detect;

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
		Object[]params = {phoneNumber, credentials, trackingIdTracker};
		pid0.execute(params);
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
		key.put("deviceId", new AttributeValue().withS(""+MainActivity.myAccount.getDeviceId()));
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
	//Note that there might be two deviceIds that map to the same phone number. (i.e. what if someone changes phones).
	//If we were to release this application we would take most recent user account determined with the time field,
	//and delete all the older user accounts.
	@Override
	protected Void doInBackground(Object... params) {
		String phoneNumber = (String)params[0];
		BasicAWSCredentials credentials = (BasicAWSCredentials) params[1];
		String trackingIdTrackee = MainActivity.myAccount.getTrackId();
		String trackingIdTracker = (String) params[2];
		AmazonDynamoDBClient client = new AmazonDynamoDBClient(credentials);
		Map<String, Condition>scanfilter = new HashMap<String, Condition>();
		Condition condition = new Condition()
		.withComparisonOperator(ComparisonOperator.EQ.toString())
		.withAttributeValueList(new AttributeValue().withS(trackingIdTracker));
		scanfilter.put("trackingIdTracker", condition);
		ScanRequest scanRequest = new ScanRequest();
		scanRequest.setScanFilter(scanfilter);
		scanRequest.setTableName("USERS");
		ScanResult sr = client.scan(scanRequest);
		if(sr.getItems().size()==0)
		{
			PutItemRequest pr = new PutItemRequest();
			HashMap<String,AttributeValue>item=new HashMap<String,AttributeValue>();
			item.put("trackingIdTracker", new AttributeValue().withS(trackingIdTracker));
			//item.put("trackingIdTrackeePending", new AttributeValue().withM());
			pr.withTableName("USER_REQUESTS");
			pr.withItem(item);
			PutItemResult send = client.putItem(pr);
		}
		else
		{
			Map<String, AttributeValue> key = new HashMap<String, AttributeValue>();
			key.put("trackingIdTracker", new AttributeValue().withS(""+MainActivity.myAccount.getDeviceId()));
			Map<String, AttributeValueUpdate> item = new HashMap<String, AttributeValueUpdate>();
			item.put("time", new AttributeValueUpdate().withValue(new AttributeValue(""+System.currentTimeMillis())));
			UpdateItemRequest updateRequest = new UpdateItemRequest();
			updateRequest.setKey(key);
			updateRequest.withTableName("USER_REQUESTS");
			updateRequest.withAttributeUpdates(item);
			client.updateItem(updateRequest);
		}
		return null;
	}
}
class approveRequest extends AsyncTask<Object,Void,Boolean>
{
	@Override
	protected Boolean doInBackground(Object... params) {
		Location arg0 = (Location) params[0];
		BasicAWSCredentials credentials = (BasicAWSCredentials) params[1];
		AmazonDynamoDBClient client = new AmazonDynamoDBClient(credentials);
		Map<String, AttributeValue> key = new HashMap<String, AttributeValue>();
		key.put("trackingIdTracker", new AttributeValue().withS(""+MainActivity.myAccount.getDeviceId()));
		Map<String, AttributeValueUpdate> item = new HashMap<String, AttributeValueUpdate>();
		item.put("lat", new AttributeValueUpdate().withValue(new AttributeValue(""+arg0.getLatitude())));
		item.put("lon", new AttributeValueUpdate().withValue(new AttributeValue(""+arg0.getLongitude())));
		item.put("time", new AttributeValueUpdate().withValue(new AttributeValue(""+System.currentTimeMillis())));
		UpdateItemRequest updateRequest = new UpdateItemRequest();
		updateRequest.setKey(key);
		updateRequest.withTableName("USER_REQUESTS");
		updateRequest.withAttributeUpdates(item);
		client.updateItem(updateRequest);
		return true;
	}
}
