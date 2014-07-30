package com.chatbot.classes;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.chatwithbot.R;

public class MessageArrayAdapter extends ArrayAdapter<Message> {

	private final List<Message> messages;
	private final Context context;

	public MessageArrayAdapter(Context _context, int resource, List<Message> objects) {
		super(_context, resource, objects);
		context = _context;
		messages = objects;
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		Message message = messages.get(position);
		
		View rowView = null;
		if(message.owner==Message.MessageOwner.CHATBOT)
		{
			rowView = inflater.inflate(R.layout.list_item_message_to, parent, false);
			TextView textViewInfo = (TextView)rowView.findViewById(R.id.textViewInfo);
			textViewInfo.setText(Controller.share().getCurrentBot().name);
		}
		else if(message.owner==Message.MessageOwner.USER)
		{
			rowView = inflater.inflate(R.layout.list_item_message_from, parent, false);
		}
		
		TextView textViewContent = (TextView) rowView.findViewById(R.id.textViewContent);
		textViewContent.setText(message.content);
		
		return rowView;
	}

    public boolean areAllItemsEnabled() {
        return false;
    }

    public boolean isEnabled(int position) {
        return false;
    }

}
