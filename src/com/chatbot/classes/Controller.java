package com.chatbot.classes;

import android.app.Activity;

public class Controller {

		// Gioi han mot bien Controller duy nhat
		// Cach su dung Controller.share()
		private static Controller _shared = null;
		public static Controller share()
		{
			if(_shared==null)
			{
				_shared = new Controller();
			}
			return _shared;
		}
		private Controller(){}
		
		public static String BOT_MESSAGE = "BOT_MESSAGE";
		
		// get va set activity hien tai
		private Activity _currentActivity = null;
		public void setCurrentActivity(Activity _activity)
		{
			_currentActivity = _activity;
		}
		public Activity getCurrentActivity()
		{
			return _currentActivity;
		}
		
		// get va set bot
		
		private Bot _currentBot = null;
		public void setCurrentBot(Bot bot)
		{
			_currentBot = bot;
		}
		public Bot getCurrentBot()
		{
			return _currentBot;
		}
		
		
}
