package com.chatbot.classes;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.chatwithbot.R;

public class BotArrayAdapter extends ArrayAdapter<Bot> {

	private final Context context;
	private final List<Bot> bots;
	
	public BotArrayAdapter(Context _context, int resource, List<Bot> _objects) {
		super(_context, R.layout.list_item_bot, _objects);
		this.context = _context;
		this.bots = _objects;
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
		View rowView = inflater.inflate(R.layout.list_item_bot, parent, false);
		TextView textViewName = (TextView) rowView.findViewById(R.id.textViewName);
		TextView textViewLanguage = (TextView)rowView.findViewById(R.id.textViewLanguage);
		
		// Change icon based on name
		Bot bot = bots.get(position);
		textViewName.setText(bot.name);
		textViewLanguage.setText(bot.language);
		
		return rowView;
	}


}
