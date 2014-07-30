package com.chatbot.classes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;

public class Bot {
	public String id;
	public String name, language;
	
	//
	
	public static String ACCESS_TOKEN = "b0e58933-67f3-42f3-9ee1-2806be87e1b6";
	
	public static void fetchAll(final FetchAllListener fetchAllListener)
	{
    	
    	Util.makeHttpRequest("http://tech.fpt.com.vn/AIML/api/bots?token="+ACCESS_TOKEN, new Util.HttpRequestListener() {
			@Override
			public void onSuccess(String s) {
				// TODO Auto-generated method stub
				try {
					JSONObject jsonResult = new JSONObject(s);
					String result = jsonResult.getString("result");
					if(result.trim().endsWith("success"))
					{
						ArrayList<Bot> bots = new ArrayList<Bot>();
						JSONArray jsonBots = jsonResult.getJSONArray("bots");
						//Util.log("length: "+jsonBots.length());
						for(int i=0; i<jsonBots.length();i++)
						{
							JSONObject jsonBot = jsonBots.getJSONObject(i);
							Bot bot = new Bot();
							bot.id = jsonBot.getString("id");
							bot.name = jsonBot.getString("name");
							bot.language = jsonBot.getString("language");
							bots.add(bot);
						}
						
						fetchAllListener.onFetchCompleted(bots);
					}
					else
					{
						Util.log("fail");
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Util.log(e.getMessage());
                    fetchAllListener.onDataError();
				}
				
			}
			@Override
			public void onError(String error) {
				// TODO Auto-generated method stub
				Util.log(error);
                fetchAllListener.onFetchError();
				
			}
		});
	}
	
	public static abstract class FetchAllListener
	{
		public abstract void onFetchCompleted(ArrayList<Bot> bots);
        public abstract void onFetchError();
        public abstract void onDataError();
	}
	
	//
	
	public Bot()
	{
		
	}
	
	public Bot(String _id, String _name, String _language)
	{
		this.id = _id;
		this.name = _name;
		this.language = _language;
	}

    public boolean isSendingMessage = false;

	public void sendMessage(String message)
	{
		for(Listener listener : listeners)
		{
			listener.onSentMessage(message);
		}

        isSendingMessage = true;
		
		Util.makeHttpRequest("http://tech.fpt.com.vn/AIML/api/bots/"+id+"/chat?request="+URLEncoder.encode(message)+"&token="+ACCESS_TOKEN, new Util.HttpRequestListener(){

			@Override
			public void onSuccess(String s) {
				// TODO Auto-generated method stub
				JSONObject jsonResult;
				try {
					jsonResult = new JSONObject(s);
					String result = jsonResult.getString("status");
					if(result.trim().endsWith("success"))
					{
						String response = jsonResult.getString("response");
						for(Listener listener : listeners)
						{
							listener.onReceivedMessage(response);
						}
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

                isSendingMessage = false;
			}

			@Override
			public void onError(String error) {
				// TODO Auto-generated method stub
				Util.log(error);
                isSendingMessage = false;
                String response = "Xin lỗi, tôi đang bận chút việc :(";
                for(Listener listener : listeners)
                {
                    listener.onReceivedMessage(response);
                }
			}
			
		});
	}
	
	// Listeners
	
	ArrayList<Listener> listeners = new ArrayList<Listener>();
	
	public void listen(Listener listener)
	{
        listeners.add(listener);
	}

    public void removeListener(Listener listener){
        listeners.remove(listener);
    }

    public void removeAllListener()
    {
        listeners.clear();
    }
	
	public static abstract class Listener
	{
		public abstract void onReceivedMessage(String message);
		public abstract void onSentMessage(String message);
	}
}

