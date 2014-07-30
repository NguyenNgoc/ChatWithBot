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
											"không nói cho tức")
											|| message.content
													.toLowerCase()
													.contains(
															"không nên nói cộc lốc")
											|| message.content.toLowerCase()
													.contains("cấm nói tục")
											|| message.content.toLowerCase()
													.contains("chửi bậy")
											|| message.content
													.toLowerCase()
													.contains("chắc chắn không")
											|| message.content.toLowerCase()
													.contains("không yêu đâu")
											|| message.content.toLowerCase()
													.contains("đừng")
											|| message.content.toLowerCase()
													.contains("không thích")
											|| message.content.toLowerCase()
													.contains("không ngu")
											|| message.content.toLowerCase()
													.contains("tôi không nói")
											|| message.content.toLowerCase()
													.contains("không cho")) {
										gestureStr = "No";
									} else if (message.content.toLowerCase()
											.contains("hơ hơ")
											|| message.content.toLowerCase()
													.contains("hì hì")
											|| message.content.toLowerCase()
													.contains("hê hê")
											|| message.content.toLowerCase()
													.contains("à")) {
										gestureStr = "Embarrassed";
									} else if (message.content.toLowerCase()
											.contains("ha ha")) {
										gestureStr = "Laugh";
									} else if (message.content.toLowerCase()
											.contains("nói lại được không")
											|| message.content.toLowerCase()
													.contains("đúng không")
											|| message.content
													.toLowerCase()
													.contains(
															"bạn không thích hả")
											|| message.content.toLowerCase()
													.contains("suy nghĩ gì")
											|| message.content.toLowerCase()
													.contains("muốn ăn gì")
											|| message.content
													.toLowerCase()
													.contains(
															"không hiểu chỗ nào")
											|| message.content
													.toLowerCase()
													.contains(
															"không hiểu cái gì")
											|| message.content.toLowerCase()
													.contains("được không")) {
										gestureStr = "WhatSThis";
									} else if (message.content.toLowerCase()
											.contains("hình như chưa")
											|| message.content.toLowerCase()
													.contains(
															"hình như là chưa")
											|| message.content.toLowerCase()
													.contains("không có gì")
											|| message.content
													.toLowerCase()
													.contains(
															"không có ai để nói chuyện")
											|| message.content
													.toLowerCase()
													.contains(
															"không nói chuyện với tôi")
											|| message.content.toLowerCase()
													.contains("yêu làm chi")) {
										gestureStr = "Nothing";
									} else if (message.content.toLowerCase()
											.contains("nghe quen quen")
											|| message.content.toLowerCase()
													.contains(
															"chờ tôi vài phút")) {
										gestureStr = "Thinking";
									} else if (message.content.toLowerCase()
											.contains("tôi ngu")
											|| message.content.toLowerCase()
													.contains("đồ điên")
											|| message.content.toLowerCase()
													.contains("cút đi")) {
										gestureStr = "Angry";
									} else if (message.content.toLowerCase()
											.contains("sao lại chán")
											|| message.content.toLowerCase()
													.contains("sao lại bó tay")
											|| message.content
													.toLowerCase()
													.contains("sao bạn lại nói")
											|| message.content.toLowerCase()
													.contains("cám ơn gì")
											|| message.content.toLowerCase()
													.contains("tại sao")
											|| message.content.toLowerCase()
													.contains("sao lại thế")
											|| message.content.toLowerCase()
													.contains("làm gì")
											|| message.content.toLowerCase()
													.contains("thế nào")
											|| message.content
													.toLowerCase()
													.contains(
															"bạn cũng vui tính")
											|| message.content.toLowerCase()
													.contains("nếu không chê")
											|| message.content.toLowerCase()
													.contains("tôi luôn ở đây")
											|| message.content.toLowerCase()
													.contains("trước mặt bạn")
											|| message.content.toLowerCase()
													.contains("tôi vẫn ở đây")
											|| message.content.toLowerCase()
													.contains(
															"đối diện tôi đây")) {
										gestureStr = "Give";
									} else if (message.content.toLowerCase()
											.contains("khen thừa")
											|| message.content
													.toLowerCase()
													.contains(
															"nói điều ai cũng biết")
											|| message.content
													.toLowerCase()
													.contains("một thời bá đạo")
											|| message.content
													.toLowerCase()
													.contains(
															"câu nói thừa nhất")
											|| message.content.toLowerCase()
													.contains("mình giỏi quá")
											|| message.content.toLowerCase()
													.contains(
															"mình vui tính mà")
											|| message.content.toLowerCase()
													.contains("yêu mấy cô rồi")
											|| message.content.toLowerCase()
													.contains("tôi có thừa")
											|| message.content.toLowerCase()
													.contains("tên tôi đẹp")
											|| message.content.toLowerCase()
													.contains("bá đạo")) {
										gestureStr = "Proud";
									} else if (message.content.toLowerCase()
											.contains("chào bạn")
											|| message.content
													.toLowerCase()
													.contains(
															"bạn có khỏe không")
											|| message.content.toLowerCase()
													.contains("bạn tên gì")
											|| message.content
													.toLowerCase()
													.contains(
															"rất vui được làm quen với bạn")) {
										gestureStr = "HandMotionBehavior5";
									} else if (message.content.toLowerCase()
											.contains("có chứ")
											|| message.content.toLowerCase()
													.contains("thích chứ")
											|| message.content.toLowerCase()
													.contains("sẵn sàng")
											|| message.content.toLowerCase()
													.contains("chắc chắn")
											|| message.content.toLowerCase()
													.contains("tôi làm được")
											|| message.content.toLowerCase()
													.contains(
															"tôi quan tâm bạn")
											|| message.content.toLowerCase()
													.contains("tôi có duyên")
											|| message.content
													.toLowerCase()
													.contains(
															"chắc chắn là phải có rồi")
											|| message.content
													.toLowerCase()
													.contains(
															"tôi quên sao được")
											|| message.content.toLowerCase()
													.contains("hiển nhiên")
											|| message.content.toLowerCase()
													.contains("bạn sẽ ngủ")
											|| message.content.toLowerCase()
													.contains(
															"tôi cũng yêu bạn")
											|| message.content.toLowerCase()
													.contains("tôi là nam")) {
										gestureStr = "Sure";
									} else if (message.content.toLowerCase()
											.contains("bình thường")
											|| message.content.toLowerCase()
													.contains("tôi thích cười")
											|| message.content.toLowerCase()
													.contains("trời đẹp")) {
										gestureStr = "Optimistic";
									} else if (message.content.toLowerCase()
											.contains("nhớ mang theo ô nhé")
											|| message.content.toLowerCase()
													.contains("nước chanh nhé")) {
										gestureStr = "Take";
									} else if (message.content.toLowerCase()
											.contains("trời nóng thật")) {
										gestureStr = "Relieved";
									} else if (message.content.toLowerCase()
											.contains("kệ")
											|| message.content
													.toLowerCase()
													.contains(
															"cám ơn bạn đã quá khen")
											|| message.content
													.toLowerCase()
													.contains(
															"không có gì thật mà")
											|| message.content
													.toLowerCase()
													.contains(
															"buồn cười thì cười")
											|| message.content
													.toLowerCase()
													.contains(
															"tôi từng nói câu này")
											|| message.content.toLowerCase()
													.contains("mình nói rồi à")) {
										gestureStr = "Innocent";
									} else if (message.content.toLowerCase()
											.contains("thịt chim cánh cụt")
											|| message.content.toLowerCase()
													.contains("thịt cá voi")
											|| message.content.toLowerCase()
													.contains("bánh đa")
											|| message.content
													.toLowerCase()
													.contains(
															"vừa ăn vừa uống rượu")
											|| message.content.toLowerCase()
													.contains("thích lắm")) {
										gestureStr = "Excited";
									} else if (message.content
											.toLowerCase()
											.contains(
													"có duyên ắt phải gặp nhau")
											|| message.content.toLowerCase()
													.contains("tôi khỏe")
											|| message.content.toLowerCase()
													.contains("khỏe đừng hỏi")
											|| message.content.toLowerCase()
													.contains("mình khỏe")
											|| message.content
													.toLowerCase()
													.contains("tôi rất là khỏe")
											|| message.content
													.toLowerCase()
													.contains(
															"chuyện gì cũng được")
											|| message.content.toLowerCase()
													.contains("vớ vẩn")
											|| message.content.toLowerCase()
													.contains("sao mà ốm được")
											|| message.content.toLowerCase()
													.contains("là đúng rồi")
											|| message.content.toLowerCase()
													.contains("tin tôi đi")
											|| message.content
													.toLowerCase()
													.contains(
															"nhiệt tình ấy chứ")
											|| message.content
													.toLowerCase()
													.contains(
															"tại sao lại không")
											|| message.content.toLowerCase()
													.contains("tại sao không")
											|| message.content.toLowerCase()
													.contains("tất nhiên")
											|| message.content.toLowerCase()
													.contains("yên tâm")
											|| message.content
													.toLowerCase()
													.contains(
															"không biết nói dối")) {
										gestureStr = "Determined      ";
									} else if (message.content.toLowerCase()
											.contains("tôi không khỏe")
											|| message.content.toLowerCase()
													.contains("tôi không muốn")
											|| message.content
													.toLowerCase()
													.contains(
															"không hiểu vì sao")
											|| message.content.toLowerCase()
													.contains("chán lắm")) {
										gestureStr = "Amused";
									} else if (message.content.toLowerCase()
											.contains("cố lên")
											|| message.content.toLowerCase()
													.contains("với tôi đi")
											|| message.content
													.toLowerCase()
													.contains("hãy kể tôi nghe")
											|| message.content.toLowerCase()
													.contains("yêu đi")
											|| message.content.toLowerCase()
													.contains("cưới nhau đi")
											|| message.content.toLowerCase()
													.contains("ngủ đi")
											|| message.content.toLowerCase()
													.contains("ngủ thôi")
											|| message.content.toLowerCase()
													.contains("uống đi")
											|| message.content.toLowerCase()
													.contains("uống nước đi")
											|| message.content
													.toLowerCase()
													.contains(
															"uống cốc nước mát đi")
											|| message.content.toLowerCase()
													.contains("cứ tự nhiên")) {
										gestureStr = "ComeOn";
									} else if (message.content.toLowerCase()
											.contains("mua thuốc uống đi")
											|| message.content
													.toLowerCase()
													.contains("hãy đi ra ngoài")
											|| message.content.toLowerCase()
													.contains("lên giường")
											|| message.content.toLowerCase()
													.contains("lăn vào bếp")
											|| message.content.toLowerCase()
													.contains("ra quán trà đá")
											|| message.content
													.toLowerCase()
													.contains(
															"nói chuyện khác đi")
											|| message.content
													.toLowerCase()
													.contains(
															"kiếm chôc nào mát")
											|| message.content.toLowerCase()
													.contains("đi bác sĩ")
											|| message.content.toLowerCase()
													.contains("nghỉ ngơi đi")) {
										gestureStr = "Far";
									} else if (message.content.toLowerCase()
											.contains("tôi không biết")
											|| message.content
													.toLowerCase()
													.contains(
															"làm sao mà biết được")
											|| message.content.toLowerCase()
													.contains("quên rồi")
											|| message.content.toLowerCase()
													.contains("không nhớ")
											|| message.content
													.toLowerCase()
													.contains(
															"mình chẳng hiểu bạn nói")) {
										gestureStr = "IDontKnow";
									} else if (message.content
											.toLowerCase()
											.contains(
													"tức giận chỉ hại sức khỏe")
											|| message.content.toLowerCase()
													.contains("hạ hỏa")
											|| message.content
													.toLowerCase()
													.contains(
															"giải tỏa nỗi bực dọc")) {
										gestureStr = "CalmDown";
									} else if (message.content.toLowerCase()
											.contains("chán bạn quá")
											|| message.content.toLowerCase()
													.contains("chán thật")
											|| message.content.toLowerCase()
													.contains("chán đời")
											|| message.content
													.toLowerCase()
													.contains("số phận hẩm hiu")
											|| message.content.toLowerCase()
													.contains("vô duyên quá")
											|| message.content.toLowerCase()
													.contains("chán quá")) {
										gestureStr = "Disappointed";
									} else if (message.content.toLowerCase()
											.contains("chúc một ngày tốt đẹp")
											|| message.content.toLowerCase()
													.contains("tuyệt vời")
											|| message.content
													.toLowerCase()
													.contains(
															"bố tôi là những thiên tài")
											|| message.content
													.toLowerCase()
													.contains(
															"có sức khỏe là có tất cả")
											|| message.content.toLowerCase()
													.contains("vui tính")
											|| message.content.toLowerCase()
													.contains("dễ mến")
											|| message.content
													.toLowerCase()
													.contains(
															"những thứ tôi thích ăn")
											|| message.content
													.toLowerCase()
													.contains(
															"tôi thích ăn nhiều thứ")
											|| message.content.toLowerCase()
													.contains("vui nhất")
											|| message.content.toLowerCase()
													.contains(
															"ngày đấy may mắn")) {
										gestureStr = "Happy";
									} else if (message.content.toLowerCase()
											.contains("tôi không có mẹ")
											|| message.content
													.toLowerCase()
													.contains(
															"không muốn nói chuyện với tôi")
											|| message.content.toLowerCase()
													.contains("yêu đơn phương")
											|| message.content.toLowerCase()
													.contains("buồn quá")) {
										gestureStr = "Sad";
									} else if (message.content.toLowerCase()
											.contains("chỉ tại bọn mình")
											|| message.content.toLowerCase()
													.contains("có đi đâu đâu")
											|| message.content.toLowerCase()
													.contains("nhóm gồm có")
											|| message.content
													.toLowerCase()
													.contains(
															"tôi được như thế này là nhờ họ")
											|| message.content.toLowerCase()
													.contains("vì")) {
										gestureStr = "Explain";
									} else if (message.content.toLowerCase()
											.contains("chắc là")) {
										gestureStr = "Maybe";
									} else {
										Random r = new Random();
										int temp = r
												.nextInt(gestureList.length);
										gestureStr = gestureList[temp];
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
