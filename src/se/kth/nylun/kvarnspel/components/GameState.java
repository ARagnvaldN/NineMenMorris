package se.kth.nylun.kvarnspel.components;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

import android.text.format.Time;
import android.util.Log;

public class GameState implements Serializable{
	private int turn;
	private int[] board;
	private int redmarker;
	private int bluemarker;
	private int remove;
	private int currentMessage;
	private boolean gameOver;
	private int[][] pieces;
	private String date;
	
	public GameState(int turn, int[] board, int redmarker,
					 int bluemarker, int remove, int message,
					 boolean gameOver, int[][] pieces ){
		
		this.turn = turn;
		this.board = board;
		this.redmarker = redmarker;
		this.bluemarker = bluemarker;
		this.remove = remove;
		this.currentMessage = message;
		this.gameOver = gameOver;
		this.pieces = pieces;
		
		date = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
		
	}
	
	public String getTime(){
		return date;
	}
	
	public int[][] getPieces() {
		return pieces;
	}


	public int getTurn() {
		return turn;
	}

	public int[] getBoard() {
		return board;
	}

	public int getRedmarker() {
		return redmarker;
	}

	public int getBluemarker() {
		return bluemarker;
	}

	public int getRemove() {
		return remove;
	}

	public int getCurrentMessage() {
		return currentMessage;
	}

	public boolean isGameOver() {
		return gameOver;
	}

	public GameState(byte[] state){
		ByteArrayInputStream stream = new ByteArrayInputStream(state);
		ObjectInput in = null;
		try{
			in = new ObjectInputStream(stream);
			GameState gs = (GameState) in.readObject();
			
		} catch(IOException e){
			Log.e("Kvarnspel", e.toString());
			
		} catch (ClassNotFoundException e) {
			Log.e("Kvarnspel", e.toString());
		}
	}
	
	public byte[] getBytes(){
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		ObjectOutputStream out = null;
		try{
			out = new ObjectOutputStream(stream);
			out.writeObject(this);
			byte[] bytes = stream.toByteArray();
			return bytes;
			
		} catch(IOException e){
			Log.e("Kvarnspel",e.toString());
		}
		
		return null;
	}

}
