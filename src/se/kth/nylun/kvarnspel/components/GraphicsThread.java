package se.kth.nylun.kvarnspel.components;

import android.util.Log;

public class GraphicsThread extends Thread{

	private boolean running = false;
	private BoardView boardView;
	private long sleepTime;
	
	public GraphicsThread(BoardView view, long sleepTime){
		this.sleepTime = sleepTime;
		this.boardView = view;
		running = true;
	}
	
	protected synchronized void setRunning(boolean b) {
		running = b;
	}

	protected synchronized boolean isRunning() {
		return running;
	}
	
	@Override
	public void run() {

		while(running){
			boardView.draw();
			
			//Sleep
			try{
				Thread.sleep(sleepTime);
			}catch(InterruptedException e){
				Log.e("Kvarnspel",e.toString());
			}
			
		}
		
	}
	
	public void requestExitAndWait(){
		running = false;
		try{
			join();
		} catch(InterruptedException e){
			Log.e("Kvarnspel",e.toString());
		}
	}


}
