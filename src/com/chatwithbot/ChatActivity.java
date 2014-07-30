package com.chatwithbot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;

import com.chatbot.classes.Bot;
import com.chatbot.classes.Controller;
import com.chatbot.classes.Message;
import com.chatbot.classes.MessageArrayAdapter;
import com.chatbot.classes.MessageModel;
import com.chatbot.classes.PullToRefreshListView;
import com.chatbot.classes.Util;
import com.fpt.lib.asr.Languages;
import com.fpt.lib.asr.Result;
import com.fpt.lib.asr.SpeakToText;
import com.fpt.lib.asr.SpeakToTextListener;
import com.fpt.robot.RobotException;
import com.fpt.robot.app.RobotActivity;
import com.fpt.robot.leds.RobotLedEmotion;
import com.fpt.robot.motion.RobotGesture;
import com.fpt.robot.tts.RobotTextToSpeech;

public class ChatActivity extends RobotActivity implements SpeakToTextListener {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		initView();
		startFunction();
		if (getRobot() == null) {
			scan();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.about:
			Util.alert("Một sản phẩm của team BK-TBG - Đại học Bách Khoa Hà Nội");
			return true;
		case R.id.clear:
			messages.clear();
			messageArrayAdapter.notifyDataSetChanged();
			return true;
		case R.id.clearHistory:
			messages.clear();
			messageArrayAdapter.notifyDataSetChanged();
			if (messageModel.deleteAllMessages(bot.id)) {
				Util.alert("Xóa lịch sử thành công!");
			}

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	PullToRefreshListView listViewMessages;
	ImageView buttonSend;
	EditText editTextMessageInput;
	ImageView imageViewVoiceMessageInput;
	MessageModel messageModel;

	Bot bot;
	ArrayList<Message> messages = new ArrayList<Message>();
	MessageArrayAdapter messageArrayAdapter;

	String selectedLanguage = "en-US";
	boolean vietnameseSupported = true;

	String[] gestureList;

	SpeakToText stt;

	void initView() {
		stt = new SpeakToText(Languages.VIETNAMESE, this);

		listViewMessages = (PullToRefreshListView) this
				.findViewById(R.id.listViewMessages);
		// listViewMessages = (PullToRefreshListView)getListView();
		buttonSend = (ImageView) this.findViewById(R.id.buttonSend);
		editTextMessageInput = (EditText) this
				.findViewById(R.id.editTextMessageInput);
		imageViewVoiceMessageInput = (ImageView) this
				.findViewById(R.id.imageViewVoiceMessageInput);

		buttonSend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String text = editTextMessageInput.getText().toString();
				text = text.trim();
				if (text.length() == 0)
					return;
				editTextMessageInput.setText("");
				bot.sendMessage(text);
				Util.focusView(editTextMessageInput);
			}

		});

		imageViewVoiceMessageInput.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (getRobot() == null) {
					scan();
					return;
				} else {
					if (gestureList == null) {
						try {
							gestureList = RobotGesture
									.getGestureList(getRobot());
						} catch (RobotException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				new Thread(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						stt.recognize(4000, 5000);
					}
				}).start();
			}

		});

		listViewMessages
				.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
					@Override
					public void onRefresh() {
						new GetDataTask().execute();
					}
				});

		messageArrayAdapter = new MessageArrayAdapter(this, 0, messages);
		listViewMessages.setAdapter(messageArrayAdapter);
	}

	void startFunction() {
		Controller.share().setCurrentActivity(this);

		setBot(Controller.share().getCurrentBot());

		Intent detailsIntent = new Intent(
				RecognizerIntent.ACTION_GET_LANGUAGE_DETAILS);
		sendOrderedBroadcast(detailsIntent, null, new LanguageDetailsChecker(),
				null, Activity.RESULT_OK, null, null);

		messageModel = new MessageModel(this);
		messageModel.open();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		bot.removeAllListener();
		Thread[] threads = new Thread[Thread.activeCount()];
		Thread.enumerate(threads);
		for (Thread t : threads) {
			if (t.isAlive()) {
				t.stop();
			}
		}
	}

	void setBot(Bot _bot) {
		bot = _bot;

		this.setTitle(bot.name);

		bot.listen(new Bot.Listener() {
			@Override
			public void onReceivedMessage(String content) {
				// TODO Auto-generated method stub
				final Message message = new Message(content,
						Message.MessageOwner.CHATBOT, bot.id);
				messages.add(message);
				messageModel.createMessage(message); // save to db

				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						Util.hideProgressDialog();
						messageArrayAdapter.notifyDataSetChanged();
						if (getRobot() == null) {
							scan();
							return;
						}
						try {
							if (gestureList == null) {
								gestureList = RobotGesture
										.getGestureList(getRobot());
							}
							executeGesture();
							RobotTextToSpeech.say(getRobot(), message.content,
									RobotTextToSpeech.ROBOT_TTS_LANG_VI);
						} catch (RobotException e) {
							e.printStackTrace();
						}
						listViewMessagesScrollToBottom();
					}

					private void executeGesture() {
						// TODO Auto-generated method stub

						new Thread(new Runnable() {
							@Override
							public synchronized void run() {
								try {
									String gestureStr = "";
									if (message.content.toLowerCase().contains(
											"không nói cho tức")) {
										gestureStr = "No";
									}else if (message.content.toLowerCase().contains("nói lại được không")
											|| message.content.toLowerCase().contains("hơ hơ")
											|| message.content.toLowerCase().contains("hì hì")
											|| message.content.toLowerCase().contains("à")
											|| message.content.toLowerCase().contains("mình chẳng hiểu bạn nói")){
										gestureStr = "Annoyed";
									} else {
										Random r = new Random();
										gestureStr = gestureList[r
												.nextInt(gestureList.length)];
									}

									RobotGesture.runGesture(getRobot(),
											gestureStr);
									RobotLedEmotion.startEmotion(getRobot(),
											gestureStr);
								} catch (RobotException e) {
									e.printStackTrace();
								}
							}
						}).start();

					}

				});
			}

			@Override
			public void onSentMessage(String content) {
				// TODO Auto-generated method stub
				Message message = new Message(content,
						Message.MessageOwner.USER, bot.id);
				messages.add(message);
				messageModel.createMessage(message); // save to db

				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						messageArrayAdapter.notifyDataSetChanged();
						listViewMessagesScrollToBottom();
						new Handler().postDelayed(new Runnable() {
							@Override
							public void run() {
								if (!bot.isSendingMessage)
									return;
								Util.showProgressDialog(ChatActivity.this,
										"Bạn vui lòng chờ tôi một lát",
										"Tôi sẽ trả lời bạn nhanh nhất có thể");
							}
						}, 2000);
					}
				});
			}
		});
	}

	void listViewMessagesScrollToBottom() {
		listViewMessages.post(new Runnable() {
			@Override
			public void run() {
				// Select the last row so it will scroll into view...
				listViewMessages.setSelection(messageArrayAdapter.getCount() - 1);
			}
		});
	}

	public class LanguageDetailsChecker extends BroadcastReceiver {
		private List<String> supportedLanguages;

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle results = getResultExtras(true);
			if (results.containsKey(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES)) {
				supportedLanguages = results
						.getStringArrayList(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES);

				vietnameseSupported = supportedLanguages.contains("vi-VN");

				if (vietnameseSupported) {
					String[] arr = new String[] { "Vietnamese", "vietnamese",
							"VN", "vn", "vi-VN" };
					if (Arrays.asList(arr).contains(bot.language)) {
						selectedLanguage = "vi-VN";
						Util.log("Vietnamese supported");
					} else {
						Util.log("Please use English to communicate with robot");
					}
				} else {
					Util.log("Vietnamese voice not supported!");
				}
			}
		}
	}

	public class GetDataTask extends AsyncTask<Void, Void, String[]> {
		@Override
		protected String[] doInBackground(Void... voids) {
			return new String[0];
		}

		@Override
		protected void onPostExecute(String[] result) {
			long id = 0;
			if (messages.size() > 0) {
				id = messages.get(0).id;
			}
			List<Message> loadedMessages = messageModel.getAllMessagesBeforeId(
					id, bot.id, 10);
			int count = loadedMessages.size();
			if (count == 0) {
				// no more message
				listViewMessages.onRefreshComplete();
				return;
			} else {
				// has messages
				for (int i = 0; i < count; i++) {
					messages.add(0, loadedMessages.get(i));
					// loadedMessages.get(i).log();
				}
				messageArrayAdapter.notifyDataSetChanged();
				listViewMessages.onRefreshComplete();
			}

			super.onPostExecute(result);
		}
	}

	private ProgressDialog progressDialog = null;

	protected void showProgress(final String message) {
		// Log.d(TAG, "showProgress('" +message+ "')");
		runOnUiThread(new Runnable() {
			public void run() {
				if (progressDialog == null) {
					progressDialog = new ProgressDialog(ChatActivity.this);
				}
				// no title
				if (message != null) {
					progressDialog.setMessage(message);
				}
				progressDialog.setIndeterminate(true);
				progressDialog.setCancelable(true);
				progressDialog.show();
			}
		});
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	protected void cancelProgress() {
		// Log.d(TAG, "cancelProgress()");
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (progressDialog != null) {
					// progressDialog.cancel();
					progressDialog.dismiss();
				}
			}
		});
	}

	private void setText(final String text) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				showProgress(text);
			}
		});
	}

	@Override
	public void onWaiting() {
		showProgress("Waiting sound...");
	}

	@Override
	public void onRecording() {
		showProgress("Recording...");
	}

	@Override
	public void onError(Exception ex) {
		bot.sendMessage("on error:" + ex.getMessage());
		cancelProgress();
	}

	@Override
	public void onTimeout() {
		setText("on timeout \n");
		cancelProgress();
	}

	@Override
	public void onProcessing() {
		showProgress("Processing...");
	}

	@Override
	public void onResult(Result result) {
		final String str = result.result[0].alternative[0].transcript;
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				bot.sendMessage(str);
			}
		});
		cancelProgress();
	}

	@Override
	public void onStopped() {
		setText("On stopped");
		cancelProgress();

	}
}
