package com.chatwithbot;

import java.util.ArrayList;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.chatbot.classes.Bot;
import com.chatbot.classes.BotArrayAdapter;
import com.chatbot.classes.Controller;
import com.chatbot.classes.Util;
import com.fpt.robot.app.RobotActivity;

public class MainActivity extends RobotActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Controller.share().setCurrentActivity(this);
		initView();
		startFunction();
	}

	ListView listViewBots;
	Button buttonRefresh;

	ArrayList<Bot> bots = new ArrayList<Bot>();
	BotArrayAdapter botArrayAdapter;

	Activity self;

	void initView() {
		listViewBots = (ListView) this.findViewById(R.id.listViewBots);
		buttonRefresh = (Button) this.findViewById(R.id.buttonRefresh);

		listViewBots.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position,
					long id) {
				// TODO Auto-generated method stub
				if (position == -1)
					return;
				Bot bot = bots.get(position);
				Controller.share().setCurrentBot(bot);
				Intent intent = new Intent(self, ChatActivity.class);
				startActivity(intent);

			}

		});

		buttonRefresh.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				doRefresh();
			}

		});
	}

	void startFunction() {
		self = this;

		botArrayAdapter = new BotArrayAdapter(this, 0, bots);
		listViewBots.setAdapter(botArrayAdapter);

		doRefresh();
	}

	void doRefresh() {
		Util.showProgressDialog(this, "Fetching bots...");
		Util.getProgressDialog().setOnKeyListener(
				new DialogInterface.OnKeyListener() {
					@Override
					public boolean onKey(DialogInterface dialogInterface,
							int i, KeyEvent keyEvent) {
						if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK
								&& keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
							Util.hideProgressDialog();
						}
						return false;
					}
				});
		Bot.fetchAll(new Bot.FetchAllListener() {
			@Override
			public void onFetchCompleted(ArrayList<Bot> _bots) {
				// TODO Auto-generated method stub
				bots.clear();
				bots.addAll(_bots);
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						botArrayAdapter.notifyDataSetChanged();
						Util.hideProgressDialog();
					}

				});
			}

			@Override
			public void onFetchError() {
				Util.alert("Lỗi mạng, kiểm tra kết nối wifi hoặc 3G!");
				Util.hideProgressDialog();
			}

			@Override
			public void onDataError() {
				Util.alert("Lỗi dữ liệu từ server!");
				Util.hideProgressDialog();
			}
		});
	}
}
