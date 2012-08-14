package com.HalfAlpha.NameTag;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.net.URL;
import java.net.URLConnection;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class GDGTInfo implements Serializable{

	private static final long serialVersionUID = 3624605201239664437L;
	
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
	transient private Activity activity;

	private boolean cached;
	private class GetInfoTask extends AsyncTask<Void, Void, Void> {
		private String FILE_PREFIX= "GDGT_";

		@Override
		protected Void doInBackground(Void... params) {

			try {
				
				String userURL = C.URL
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
					saveToFile();
				} catch (Exception e) {
					Log.e(T, "Failed to parse user data");
					error = e;
				}
			} catch (IOException e) {
				e.printStackTrace();
				Log.e(C.T,"no network");
				readFromFile();
			}
			return null;
		}

		private void saveToFile() {
			try {
				FileOutputStream os = activity.openFileOutput(FILE_PREFIX+username, Context.MODE_PRIVATE);
				ObjectOutputStream oos= new ObjectOutputStream(os);
				oos.writeObject(GDGTInfo.this);
				oos.close();
				os.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				//can't save the info
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void readFromFile() {
			try {
				FileInputStream is = activity.openFileInput(FILE_PREFIX+username);
				ObjectInputStream ois = new ObjectInputStream(is);
				GDGTInfo.this.fillFromObject((GDGTInfo) ois.readObject());
				ois.close();
				is.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (StreamCorruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			
		}

	}

	public GDGTInfo(){}
	
	public GDGTInfo(String username, Activity activity) {
		this.username = username.trim();
		this.activity = activity;
		if (this.username.isEmpty()){
			this.username= "tgd";
		}
	}

	public void fillFromObject(final GDGTInfo o){
		this.name = o.name;
		this.RP = o.RP;
		this.reviews = o.reviews;
		this.followers = o.followers;
		this.following = o.following;
		this.questions = o.questions;
		this.comments = o.comments;
		this.answers = o.answers;
		this.cached = true;
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
		if (cached){
			r +="(data from cache)\n";
		}
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
		String output = "";
		
		if (name == null || username == null)
			return "";
		
		if (display == 0){
			int len = 27 - name.length();
			output += "User:";
			for (; len > 0; len--){
				output += " ";
			}
			output += name;
		}
		if (display == 1){
			String RPstring = Integer.toString(RP);
			int len = 29 - RPstring.length();
			output += "RP:";
			for (; len > 0; len--){
				output += " ";
			}
			output += RPstring;
		}
		if (display == 2){
			String FollowersString = Integer.toString(followers);
			int len = 22 - FollowersString.length();
			output += "Followers:";
			for (; len > 0; len--){
				output +=" ";
			}
			output += FollowersString;
		}
		if (display == 3){
			String FollowingString = Integer.toString(following);
			int len = 22 - FollowingString.length();
			output += "Following:";
			for (; len > 0; len--){
				output+= " ";
			}
			output += FollowingString;
		}
		
		if (output.length() != 32){
			Log.e(T, "Travis probably sucks at math");
		}
		return output;
		
	}
	

}
