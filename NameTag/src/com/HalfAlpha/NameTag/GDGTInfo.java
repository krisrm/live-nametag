package com.HalfAlpha.NameTag;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import android.os.AsyncTask;
import android.util.Log;

public class GDGTInfo {

	protected static final String T = "NAMETAG";
	public String username;
	public String name;
	public int RP;
	public int followers;
	public int following;
	public int answers;
	public int comments;
	public int questions;
	public int reviews;
	public Exception error;
	private class GetInfoTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {

			try {
				
				String userURL = "http://talbot.cs.ualberta.ca:8080/GDGTInfo/info?u="
						+ username;
				Log.d(T,"'"+userURL+"'");
				URL url = new URL(
						userURL);
				URLConnection conn = url.openConnection();
				// Get the response
				BufferedReader rd = new BufferedReader(new InputStreamReader(
						conn.getInputStream()));
				try {
					name = rd.readLine();
					RP = Integer.parseInt(rd.readLine());
					followers = Integer.parseInt(rd.readLine());
					following = Integer.parseInt(rd.readLine());
					answers = Integer.parseInt(rd.readLine());
					comments = Integer.parseInt(rd.readLine());
					questions = Integer.parseInt(rd.readLine());
					reviews = Integer.parseInt(rd.readLine());
				} catch (Exception e) {
					Log.e(T, "Failed to parse user data");
					error = e;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

	}

	public GDGTInfo(String username) {
		this.username = username.trim();
		if (this.username.isEmpty()){
			this.username= "tgd";
		}
	}

	public void fillFromNetwork(final Runnable finished) {
		error = null;
		new GetInfoTask() {
			protected void onPostExecute(Void result) {
				finished.run();
				
			}
		}.execute(new Void[0]);

	}

	@Override
	public String toString() {
		String r ="";
		r +="Name: " + name+ "\n";
		r +="RP: " + RP+ "\n";
		r +="Followers: " + followers + "\n";
		r +="Following: " + following+ "\n";
		r +="Answers: " + answers+ "\n";
		r +="Questions: " + questions+ "\n";
		r +="Comments: " + comments+ "\n";
		r +="Reviews: " + reviews+ "\n";
		
		return r;
	}
	
	/*
	 * Formats a string for the Arduino Display. 
	 * 0 = clearScreen
	 * 1 = name
	 * 2 = RP
	 * 3 = followers
	 * 4 = following
	 */
	public String toLcdString(int display){
		String output = new String();
		if (display == 0){
			output = "                                ";
		}
		
		if (name == null || username == null)
			return "";
		
		if (display == 1){
			int len = 27 - name.length();
			output += "User:";
			for (; len > 0; len--){
				output += " ";
			}
			output += name;
		}
		if (display == 2){
			String RPstring = Integer.toString(RP);
			int len = 29 - RPstring.length();
			output += "RP:";
			for (; len > 0; len--){
				output += " ";
			}
			output += RPstring;
		}
		if (display == 3){
			String FollowersString = Integer.toString(followers);
			int len = 22 - FollowersString.length();
			output += "Followers:";
			for (; len > 0; len--){
				output +=" ";
			}
			output += FollowersString;
		}
		if (display == 4){
			String FollowingString = Integer.toString(following);
			int len = 22 - FollowingString.length();
			output += "Following:";
			for (; len > 0; len--){
				output+= " ";
			}
			output += FollowingString;
		}
		
		Log.d(T,"'"+output+"'");
		if (output.length() != 32){
			Log.e(T, "Travis probably sucks at math");
		}
		return output;
		
	}
	

}
