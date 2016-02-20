package se.kth.nylun.kvarnspel.components;

import java.util.Arrays;
import java.util.LinkedList;

import se.kth.nylun.kvarnspel.R;
import se.kth.nylun.kvarnspel.R.drawable;
import se.kth.nylun.kvarnspel.R.string;
import se.kth.nylun.kvarnspel.model.NineMenMorrisRules;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.animation.TranslateAnimation;

public class BoardView extends SurfaceView implements SurfaceHolder.Callback{
	
	private float[][] points;
	private float[] lastPoint;
	
	//Load strings
	private String[] messages;
	private int currentMessage = 0;
	
	private boolean surfaceExists;
	private boolean gameOver = false;
	private int remove = 0; //4 = red, 5 = blue
	
	private NineMenMorrisRules gameLogic;
	private Drawable board;
	private LinkedList<GamePiece> redPieces;
	private LinkedList<GamePiece> bluePieces;
	private GamePiece movePiece;
	private int pieceSize;

	private Paint bgPaint;
	private Paint textPaint;
	private Resources res;
	private GameState gameState;
	
	private Drawable[] sprites;
	
	private SurfaceHolder holder;
	private GraphicsThread graphicsThread;
	
	public BoardView(Context context) {
		super(context);
		surfaceExists = false;
		
		res = context.getResources();
		setWillNotDraw(false);
		holder = getHolder();
		holder.addCallback(this);
		
		//Load sprites
		sprites = new Drawable[]{res.getDrawable(R.drawable.morris_red),
								res.getDrawable(R.drawable.morris_blue),
								res.getDrawable(R.drawable.morris_orange),
								res.getDrawable(R.drawable.morris_black),
								res.getDrawable(R.drawable.morris_green)};

		//Load strings
		messages = new String[]{"",
				res.getString(R.string.red_turn),
				res.getString(R.string.blue_turn),
				res.getString(R.string.red_removes),
				res.getString(R.string.blue_removes),
				res.getString(R.string.red_win),
				res.getString(R.string.blue_win)}; 
		
		//Init gameboard
		board = (Drawable) res.getDrawable(R.drawable.morris_board);
		
		//Init paints
		bgPaint = new Paint();
		bgPaint.setColor(Color.WHITE);
		textPaint = new Paint();
		textPaint.setTextSize(40);
		textPaint.setColor(Color.BLACK);
		
		resume();
		
	}
	
	public BoardView(Context context, GameState gameState){
		this(context);
		
		if(gameState != null)
			this.gameState = gameState;
		
	}
	
	public void newGame(){
		gameLogic = new NineMenMorrisRules();
		lastPoint = new float[2];
		
		//Init pieces
		redPieces = new LinkedList<GamePiece>();
		bluePieces = new LinkedList<GamePiece>();
		currentMessage = 1;
		populateBoard();
		gameOver = false;
		
		setFocusable(true);
		requestFocus();
	}
	
	public void newGame(GameState gs){
		
		gameLogic = new NineMenMorrisRules(gs);
		lastPoint = new float[2];
		//points = new float[25][2];
		
		//Init sticky points
		updateStickyPoints();
		
		//Init pieces
		redPieces = new LinkedList<GamePiece>();
		bluePieces = new LinkedList<GamePiece>();
		populateBoard();
		
		int[][] pieces = gs.getPieces();
		int red = 0;
		int blue = 0;
		
		Log.i("Kvarnspel", Arrays.toString(pieces[0]));
		Log.i("Kvarnspel", Arrays.toString(pieces[1]));
		
		for(int i=0;i<pieces[0].length;i++){
			
			if(pieces[0][i] > 0){
				redPieces.get(red).setPosition(points[pieces[0][i]], false);
				redPieces.get(red).setPos(pieces[0][i]);
				red++;
			} else if(pieces[0][i] == 0){
				redPieces.removeLast();
			}
		}
		for(int j=0;j<pieces[1].length;j++){
			
			if(pieces[1][j] > 0){
				bluePieces.get(blue).setPosition(points[pieces[1][j]], false);
				bluePieces.get(blue).setPos(pieces[1][j]);
				blue++;
			} else if(pieces[1][j] == 0){
				bluePieces.removeLast();
			} 
		}
		
		remove = gs.getRemove();
		currentMessage = gs.getCurrentMessage();
		gameOver = gs.isGameOver();

		setFocusable(true);
		requestFocus();
	}
	
	public void tryRemovePiece(GamePiece piece){
		
		if( remove == NineMenMorrisRules.BLUE_MARKER && 
				gameLogic.remove(piece.getPos(), NineMenMorrisRules.BLUE_MARKER)){
			
			bluePieces.remove(movePiece);
			movePiece = null;
			remove = 0;
			currentMessage = 2;
			if(gameLogic.win(NineMenMorrisRules.BLUE_MARKER)){
				currentMessage = 5;
				gameOver = true;
			}

		}else if( remove == NineMenMorrisRules.RED_MARKER &&
				gameLogic.remove(piece.getPos(), NineMenMorrisRules.RED_MARKER)){
			
			redPieces.remove(piece);
			movePiece = null;
			remove = 0;
			currentMessage = 1;
			if(gameLogic.win(NineMenMorrisRules.RED_MARKER)){
				currentMessage = 6;
				gameOver = true;
			}

		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent me) {
		
		if(gameOver)
			return false;
		
		if(me.getAction() == MotionEvent.ACTION_DOWN){

			//If pieces are to be removed and a piece is touched
			if(remove != 0 && (movePiece = getPieceNear(me.getX(),me.getY())) != null ){
				
				Log.i("Kvarnspel",Integer.toString(movePiece.getColor()));
				if( movePiece.getPos() != -1 )
					tryRemovePiece(movePiece);
			
			//Normally
			}else{
				//Check if there's a piece nearby
				movePiece = getPieceNear(me.getX(),me.getY());
				
				if(movePiece != null){
					lastPoint[0] = movePiece.getX();
					lastPoint[1] = movePiece.getY();
				}
			}
			
		} else if(me.getAction() == MotionEvent.ACTION_MOVE){
			
			if(movePiece != null){
				movePiece.setPosition(me.getX(), me.getY());
				
			}
			
		} else if(me.getAction() == MotionEvent.ACTION_UP){
			if(movePiece != null){
				
				//Check if piece is dropped near a point on the board
				int to = getPointNear(me.getX(),me.getY());
				int from = movePiece.getPos();
				
				//If dropped near a legal position
				if(to != -1 && gameLogic.legalMove(to, from, movePiece.getColor())){
					movePiece.setPosition(points[to], false);
					movePiece.setPos(to);
					
					//If move set up three in a row
					if(gameLogic.remove(to)){
						
						if(movePiece.getColor() == 1){
							currentMessage = 4;
							remove = NineMenMorrisRules.RED_MARKER;
						}else {
							currentMessage = 3;
							remove = NineMenMorrisRules.BLUE_MARKER;
							}

					//Else, just next turn
					}else{
						if(currentMessage == 1)
							currentMessage = 2;
						else
							currentMessage = 1;
					}
					
				}else{	//Move piece back to last point
					
					movePiece.setPosition(lastPoint,true);
					
				}

				movePiece = null;
			}
			
		}
		
		return true;
	}
	
	private GamePiece getPieceNear(float x, float y){
		GamePiece p = null;
		if(gameLogic.getTurn() == NineMenMorrisRules.RED_MOVES ){
			for(int i=0;i<redPieces.size();i++){
				if( redPieces.get(i).intersects(x, y)){
					p = redPieces.get(i);
					break;
				}
			}
		} else if(gameLogic.getTurn() == NineMenMorrisRules.BLUE_MOVES){
			
			for(int i=0;i<bluePieces.size();i++){
				if( bluePieces.get(i).intersects(x, y)){
					p = bluePieces.get(i);
					break;
				}
			}
		}
		return p;
	}
	
	private int getPointNear(float x, float y){
		int point = -1;
		if(movePiece == null)
			return point;
		for(int i=1;i<25;i++){
			if(movePiece.intersects(points[i][0], points[i][1])){
				movePiece.setPosition(points[i][0],points[i][1]);
				point = i;
				break;
			}
		}
		return point;
	}
	
	public void resume(){
		if(graphicsThread == null){
			graphicsThread = new GraphicsThread(this, 30);
			
			if(surfaceExists)
				graphicsThread.start();
		}
			
	}
	
	public void pause(){
		if (graphicsThread != null) {
			graphicsThread.requestExitAndWait();
			graphicsThread = null;
		}
	}
	
	public void draw(){
		
		Canvas canvas = holder.lockCanvas();
		{
			canvas.drawPaint(bgPaint);
			
			board.draw(canvas);
			
			canvas.drawText(messages[currentMessage], 0, getHeight()-20, textPaint);
			
			for(int i=0;i<redPieces.size();i++){
				GamePiece p = redPieces.get(i);			
				redPieces.get(i).draw(canvas);		
			}
			for(int i=0;i<bluePieces.size();i++){
				bluePieces.get(i).draw(canvas);
			}

		}
		holder.unlockCanvasAndPost(canvas);
		
	}
	
	private void populateBoard(){
		pieceSize = getWidth()/8;
		
		for(int i=0;i<9;i++){
			
			//Get game piece colours
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getContext());
			int p1 = Integer.parseInt(prefs.getString("p1col", "1"));
			int p2 = Integer.parseInt(prefs.getString("p2col", "2"));
			
			redPieces.addLast(new GamePiece(getWidth()/20+i*pieceSize/4,getWidth()+10, pieceSize, sprites[p1],gameLogic.RED_MOVES));
			bluePieces.addLast(new GamePiece(getWidth()*5/6-i*pieceSize/4,getWidth()+10,pieceSize, sprites[p2],gameLogic.BLUE_MOVES));
		}
		rescalePieces(getWidth()/8);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		
		//Update bounds for board
		board.setBounds(new Rect(0,0,getWidth(),getWidth()));
			
		updateStickyPoints();
		
		//Rescale game pieces
		rescalePieces(getWidth()/8);
			
	}
	
	private void updateStickyPoints(){
		
		//Update sticky points
				float x0 = 0;
				float dx = (getWidth() - 2*x0)/7;
				float x1 = dx + x0;
				float x2 = 2*dx + x0;
				float x3 = 3*dx + x0;
				float x4 = 4*dx + x0;
				float x5 = 5*dx + x0;
				float x6 = 6*dx + x0;
				
				points = new float[25][2];
				points[0][0] = 	points[0][1] = 0;
				
				points[1][0] = 	points[1][1] = x2;
				points[2][0] = 	points[2][1] = x1;
				points[3][0] = points[3][1] = x0;
				
				points[4][0] = x3;	points[4][1] = x2;
				points[5][0] = x3;	points[5][1] = x1;
				points[6][0] = x3; 	points[6][1] = x0;
				
				points[7][0] = x4;	points[7][1] = x2;
				points[8][0] = x5;	points[8][1] = x1;
				points[9][0] = x6; 	points[9][1] = x0
						;
				points[10][0] = x4;	points[10][1] = x3;
				points[11][0] = x5;	points[11][1] = x3;
				points[12][0] = x6; points[12][1] = x3;
				
				points[13][0] = points[13][1] = x4;
				points[14][0] =	points[14][1] = x5;
				points[15][0] = points[15][1] = x6;
				
				points[16][0] = x3;	points[16][1] = x4;
				points[17][0] = x3;	points[17][1] = x5;
				points[18][0] = x3; points[18][1] = x6;
				
				points[19][0] = x2;	points[19][1] = x4;
				points[20][0] = x1;	points[20][1] = x5;
				points[21][0] = x0; points[21][1] = x6;
				
				points[22][0] = x2;	points[22][1] = x3;
				points[23][0] = x1;	points[23][1] = x3;
				points[24][0] = x0; points[24][1] = x3;
	}
	
	private void rescalePieces(int size){
		for(int i=0;i<9;i++){
			if( redPieces.size() > i ){
				GamePiece p = redPieces.get(i);
				p.setSize(size);
				if(p.getPos() != -1)
					p.setPosition(points[p.getPos()], false);
			}
			if( bluePieces.size() > i ){
				GamePiece p = redPieces.get(i);
				p.setSize(size);
				if(p.getPos() != -1)
					p.setPosition(points[p.getPos()], false);

			}
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		
		Log.i("Kvarnspel",Boolean.toString(gameState!=null));
		
		if(gameState != null)
			newGame(gameState);
		else
			newGame();
		
		surfaceExists = true;
		if(graphicsThread != null){
			graphicsThread.start();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		surfaceExists = false;
		pause();
		
	}
	
	public GameState getInstanceState(){
		
		int[][] pieces = new int[2][9];
		
		int i = 0;
		for(GamePiece rp : redPieces){
			if(rp.getPos() != -1)
				pieces[0][i] = rp.getPos();
			else
				pieces[0][i] = -1;
			i++;
		}
		i=0;
		for(GamePiece bp : bluePieces){
			if(bp.getPos() != -1)
				pieces[1][i] = bp.getPos();
			else
				pieces[1][i] = -1;
			i++;
		}
		
		GameState gs = new GameState(gameLogic.getTurn(), 
									gameLogic.getGameplan(), 
									gameLogic.getRedMarkers(), 
									gameLogic.getBlueMarkers(),
									remove,
									currentMessage,
									gameOver,
									pieces);
		
		return gs;
	}

}
