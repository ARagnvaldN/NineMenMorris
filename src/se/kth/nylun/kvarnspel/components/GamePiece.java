package se.kth.nylun.kvarnspel.components;

import java.io.Serializable;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

public class GamePiece implements Serializable {
	private float x,y;
	private int pos;
	private final int width_height = 60;
	private final int TOUCH_LIMIT = 5;
	private int size;
	private int color;
	private Drawable sprite;
	private boolean isMoving;
	
	public GamePiece(float x, float y, int size, Drawable sprite, int color){
		this.x = x;
		this.y = y;
		this.size = size;
		this.color = color;
		this.sprite = sprite;
		this.pos = -1;
		isMoving = false;
	}
	
	public void setPosition(float x, float y){
		this.x = x-size;
		this.y = y-size;
		
	}
	
	public void setPosition(float[] xy, boolean exact){
		if(exact)
			setPosition(xy[0],xy[1]);
		
		this.x = xy[0];
		this.y = xy[1];
	}
	
	public void setPosition(float x, float y, boolean exact){
		if(!exact)
			setPosition(x,y);
		
		this.x = x;
		this.y = y;
		
	}
	
	public boolean isMoving(){
		return isMoving;
	}
	
	public int getPos(){
		return pos;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}
	
	public int getColor(){
		return color;
	}
	
	private Rect getSpriteBounds(){
		return new Rect((int)x, (int)y, (int)x+size,(int)y+size);
	}
	
	public boolean intersects(float x, float y){
		return getSpriteBounds().intersect((int)x-TOUCH_LIMIT, (int)y-TOUCH_LIMIT, (int)x+TOUCH_LIMIT, (int)y+TOUCH_LIMIT);
		
	}
	
	public void draw(Canvas canvas){
		
		//Create bounds and draw the sprite
		sprite.setBounds(getSpriteBounds());
		sprite.draw(canvas);
		
	}

	public void setPos(int to) {
		pos = to;
		
	}
	
	@Override
	public boolean equals(Object o){
		if(o == this)
			return true;
		
		return false;
	}

	public void setSize(int size) {
		this.size = size;
		
	}

}
