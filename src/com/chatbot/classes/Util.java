package com.chatbot.classes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.chatwithbot.R;

public class Util {
static String LOG_TAG = "BKTBG";
	
	public static void log(String message)
	{
		Log.d(LOG_TAG, message);
	}

	public static void focusView(View view)
	{
		if(view==null)
			return;
		if(view.requestFocus()) {
		    Controller.share().getCurrentActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		}
	}
	
	public static void alert(String message, String title)
	{
		AlertDialog alertDialog = new AlertDialog.Builder(Controller.share().getCurrentActivity()).create();
		alertDialog.setTitle(title);
		alertDialog.setMessage(message);
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
			// here you can add functions
			}
		});
		alertDialog.setIcon(R.drawable.ic_launcher);
		alertDialog.show();
	}
	
	public static void alert(String message)
	{
		alert(message,"Information");
	}
	
	public static void toast(Context context, String message)
	{
		Toast.makeText(context, message, Toast.LENGTH_LONG).show();
		Util.log(message);
	}
	
	// LOAD DIALOG
	
	private static ProgressDialog progressDialog;
	
	public static void showProgressDialog(Context context, String message)
	{
		showProgressDialog(context,message,"Waiting...");
	}
	
	public static void showProgressDialog(Context context, String message, String title)
	{
		progressDialog = ProgressDialog.show(context, title, message, true);
	}

    public static ProgressDialog getProgressDialog()
    {
        return progressDialog;
    }
	
	public static void hideProgressDialog()
	{
        if(progressDialog==null)
            return;
		progressDialog.dismiss();
        progressDialog = null;
	}
	
	/*public static void confirm(String message, String title)
	{
		
	}
	
	public static void confirm(String message)
	{
		alert(message,"Confirm");
	}*/
	
	//
	
	public static void makeHttpRequest(final String urlString, final HttpRequestListener httpRequestListener)
	{
		Thread thread = new Thread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				URL url = null;
				try {
					url = new URL(urlString);
				} catch (MalformedURLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					httpRequestListener.onError(e1.getMessage());
					return;
				}
		        URLConnection urlConnection = null;
				try {
					urlConnection = url.openConnection();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					httpRequestListener.onError(e1.getMessage());
					return;
				}
		        BufferedReader in = null;
				try {
					in = new BufferedReader(
					                        new InputStreamReader(
					                        urlConnection.getInputStream()));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					httpRequestListener.onError(e.getMessage());
					return;
				}
				String lines = "";
		        String inputLine;

		        try {
					while ((inputLine = in.readLine()) != null) 
					{
					    //System.out.println(inputLine);
						lines += inputLine;
					}
					httpRequestListener.onSuccess(lines);
					Util.log(lines);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					httpRequestListener.onError(e.getMessage());
					return;
				}
		        try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					//httpRequestListener.onError(e.getMessage());
					//return;
				}
			}
			
		});
		thread.start();
	}
	
	public static abstract class HttpRequestListener
	{
		public abstract void onSuccess(String s);
		public abstract void onError(String error);
	}

    public static long getCurrentUnixTimestamp()
    {
        long unixTime = System.currentTimeMillis() / 1000L;
        return unixTime;
    }
}
