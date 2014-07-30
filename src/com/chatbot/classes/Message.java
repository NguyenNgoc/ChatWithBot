package com.chatbot.classes;

public class Message {
	
	public enum MessageOwner {
		CHATBOT(1), USER(2);

        public static MessageOwner getOwner(int _value)
        {
            switch (_value)
            {
                case 1:
                    return CHATBOT;
                case 2:
                    return USER;
            }
            return null;
        }

        private final int value;
        private MessageOwner(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
	};
	
	public String content;
	public MessageOwner owner;
    public long timestamp;
    public long id = 0;
    public String bot_id = "";
	
	public Message(String _content, MessageOwner _owner, String _bot_id)
	{
		content = _content;
		owner = _owner;
        bot_id = _bot_id;
        timestamp = Util.getCurrentUnixTimestamp();
	}

    public Message(){

    }

    public void log()
    {
        Util.log("message: " + content);
    }
}
