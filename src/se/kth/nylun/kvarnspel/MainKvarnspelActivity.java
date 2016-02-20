package se.kth.nylun.kvarnspel;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import se.kth.nylun.kvarnspel.components.BoardView;
import se.kth.nylun.kvarnspel.components.GameState;
import se.kth.nylun.kvarnspel.components.GameStateAdapter;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

public class MainKvarnspelActivity extends Activity {

	private BoardView boardView;
	private Button newGameButton;
	private Button resumeButton;
	private ListView savedGamesList;
	private GameStateAdapter adapter;
	
	private MenuItem newGameMenu;
	private MenuItem saveGameMenu;
	
	private GameState currentGameState;
	private ArrayList<GameState> savedGameStates;
	
	private final String CURRENT_GAME_FILE = "current";
	private final String SAVE_GAME_FILE = "saves";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main_kvarnspel);
		
		newGameButton = (Button) findViewById(R.id.new_game_btn);
		resumeButton = (Button) findViewById(R.id.resume_btn);
		savedGamesList = (ListView) findViewById(R.id.listView);
		
		//Load games
		loadCurrent();
		loadGames();
		
		//Create list of save games, if there are none
		if(savedGameStates == null)
			savedGameStates = new ArrayList<GameState>();
		
		//Set listeners
		OnClickListener l = new ButtonListener();
		newGameButton.setOnClickListener(l);
		resumeButton.setOnClickListener(l);
		
		adapter  = 	new GameStateAdapter(this, R.layout.game_state_list_item, savedGameStates);
		savedGamesList.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		
		OnItemClickListener il = new ListListener();
		savedGamesList.setOnItemClickListener(il);

	}	
	
	
	private void loadGames() {
		
		FileInputStream in = null;
		try {
		  	in = this.openFileInput(SAVE_GAME_FILE);
		    ObjectInputStream ois = new ObjectInputStream(in);
		    
		    savedGameStates = (ArrayList<GameState>) ois.readObject();
		    
		    in.close();

		} catch (IOException e) {
			Log.e("Kvarnspel",e.toString());
				
		} catch (ClassNotFoundException e) {
			Log.e("Kvarnspel",e.toString());
		}
	}
	
	@Override
	protected void onStart(){
		super.onStart();
		
		//Get game piece colours
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String p1 = prefs.getString("p1col", "red");
		String p2 = prefs.getString("p2col", "blue");
		
	}

	@Override
	protected void onResume(){
		super.onResume();
		
		if(boardView != null)
			boardView.resume();
	}

	@Override
	protected void onPause(){
		super.onPause();
		
		if(boardView != null)
			boardView.pause();
	}
	
	@Override
	protected void onStop(){
		super.onStop();
		
		//Save current game in current file
		if(boardView != null)
			saveCurrent();
		saveGames();
		
	}
	
	private void saveGames() {
		FileOutputStream out = null;
		
		try {
			//Open a stream to a file
			out = openFileOutput(SAVE_GAME_FILE, Context.MODE_PRIVATE);
			ObjectOutputStream oos = new ObjectOutputStream(out);
			oos.writeObject(savedGameStates);
			
			out.close();
			Log.i("Kvarnspel","SAVED");
		} catch (Exception e) {
		  Log.e("MainExchangeActivity",e.toString());
		  
		}
		
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem mi){
		
		switch  ( mi.getItemId() ) {
		
		case R.id.menu_new_game:
			
			boardView.newGame();
			break;
			
		case R.id.menu_save_game:
			
			saveThisGame();
			break;
			
		case R.id.menu_settings:
		
			Intent intent = new Intent(this, KvarnspelPreferenceActivity.class);
			startActivity(intent);
			break;
		
		}
		
		return true;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		getMenuInflater().inflate(R.menu.main_kvarnspel, menu);
		
		newGameMenu = menu.findItem(R.id.menu_new_game);
		saveGameMenu = menu.findItem(R.id.menu_save_game);
		
		newGameMenu.setVisible(false);
		saveGameMenu.setVisible(false);
		
		return true;
	}
	
private void loadCurrent(){
	
		try {
		  	FileInputStream in = this.openFileInput(CURRENT_GAME_FILE);
		    ObjectInputStream ois = new ObjectInputStream(in);
		    
		    currentGameState = (GameState) ois.readObject();
		    
		    in.close();
		    Log.i("Kvarnspel","Loading");
		} catch (IOException e) {
				Log.e("Kvarnspel",e.toString());
				
		} catch (ClassNotFoundException e) {
			Log.e("Kvarnspel",e.toString());
		}
		
	}

	private void saveThisGame(){
		GameState gs = boardView.getInstanceState();
		savedGameStates.add(gs);

		boardView.pause();
		boardView = null;
		this.finish();
	}


	private void saveCurrent(){
		FileOutputStream out = null;
		
		try {
			//Open a stream to a file
			out = openFileOutput(CURRENT_GAME_FILE, Context.MODE_PRIVATE);
			ObjectOutputStream oos = new ObjectOutputStream(out);
			oos.writeObject(boardView.getInstanceState());
			
			out.close();
			Log.i("Kvarnspel","SAVED");
		} catch (Exception e) {
		  Log.e("saveCurrent() ErrorLog: ",e.toString());
		  
		}
	}
	
	private class ButtonListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			
			if(v == newGameButton)
				startNewGame();
			
			else if(v == resumeButton)
				resumeGame(currentGameState);

			
		}
		
	}
	
	private class ListListener implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			
			resumeGame(savedGameStates.get(arg2));
			savedGameStates.remove(arg2);
			
		}
		
		
	}

	public void startNewGame() {
		
		newGameMenu.setVisible(true);
		saveGameMenu.setVisible(true);
		
		boardView = new BoardView(this);
		setContentView(boardView);
		
	}


	public void resumeGame(GameState gs) {
		
		newGameMenu.setVisible(true);
		saveGameMenu.setVisible(true);
		
		boardView = new BoardView(this, gs);
		setContentView(boardView);
		
	}

}
