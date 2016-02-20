package se.kth.nylun.kvarnspel.components;

import java.util.ArrayList;
import java.util.List;

import se.kth.nylun.kvarnspel.R;
import se.kth.nylun.kvarnspel.R.id;
import se.kth.nylun.kvarnspel.R.layout;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class GameStateAdapter extends ArrayAdapter<GameState> {

	ArrayList<GameState> gameStates;
	
	public GameStateAdapter(Context context, int textViewResourceId,
			List<GameState> objects) {
		super(context, textViewResourceId, objects);
		
		this.gameStates = (ArrayList) objects;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		
		View view = convertView;
		
		if(view == null){	//Inflate the view
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view = inflater.inflate(R.layout.game_state_list_item,null);
		
		}
		
		GameState gs = gameStates.get(position);
		
		if (gs != null) {

			TextView time = (TextView) view.findViewById(R.id.time_text);

			if (time != null)
				time.setText(gs.getTime());
			
		}
	
		return view;
	}
}
