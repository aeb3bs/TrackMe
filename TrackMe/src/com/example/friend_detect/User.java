package com.example.friend_detect;

public class User {
private String deviceId;
private String trackId;
private double lat;
private double lon;
private String phoneNumber;
	public User(String deviceId, String trackId, double lat, double lon, String phoneNumber){
		this.deviceId = deviceId;
		this.trackId = trackId;
		this.lat = lat;
		this.lon = lon;
		this.phoneNumber = phoneNumber;
	}
	public User(){
		deviceId = "";
		trackId = "";
		lat = -5;
		lon = -5;
		phoneNumber = "";
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	public String getDeviceId() {
		return deviceId;
	}
	public String getTrackId() {
		return trackId;
	}
	public double getLat() {
		return lat;
	}
	public double getLon() {
		return lon;
	}
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setTrackId(String trackId) {
		this.trackId = trackId;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	public void setLon(double lon) {
		this.lon = lon;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
}
