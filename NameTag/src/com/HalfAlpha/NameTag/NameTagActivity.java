package com.HalfAlpha.NameTag;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class NameTagActivity extends Activity {
	
	Handler h;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        final EditText gdgtUser = (EditText) findViewById(R.id.gdgtUser);
        final TextView gdgtData = (TextView) findViewById(R.id.gdgtData);
        final Button refreshButton = (Button) findViewById(R.id.refreshButton);
        
        this.h = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                // process incoming messages here
                switch (msg.what) {
                    case 0:
                    	gdgtData.append((String) msg.obj);
                    	gdgtData.append("\n");
                    	break;
                }
                super.handleMessage(msg);
            }
        };


        refreshButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
            	try	{
            		gdgtData.setText("");
                // Perform action on click
                	URL url = new URL("http://talbot.cs.ualberta.ca:8080/GDGTInfo/info?u=" + gdgtUser.getText().toString());
                    URLConnection conn = url.openConnection();
                    // Get the response
                    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String line = "";
                    while ((line = rd.readLine()) != null) {
                		Message lmsg;
                        lmsg = new Message();
                        lmsg.obj = line;
                        lmsg.what = 0;
                        NameTagActivity.this.h.sendMessage(lmsg);
                    }

            	}
            	catch (Exception e)	{
            	}
            }
        });        


    }
}