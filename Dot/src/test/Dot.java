package test;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import test.Setting;


public class Dot extends Map implements KeyListener, Runnable {
	private int mx, my;
	private int wx, wy;
	private int mstep;
	private int msleep;
	private int Cnt, Ant;
	private double mtime;
	private boolean left = false, right = false, up = false, down = false;
	private boolean play = true, finish = false;

	private ArrayList<Enemy> EnemyList = null;
	
	public void draw() {
		// TODO Auto-generated method stub
		
		Graphics gs = bufferedImage.getGraphics();
		gs.setColor(Color.black);
		gs.fillRect(0, 0, Setting.MAX_X, Setting.MAX_Y);
		gs.setColor(Color.white);
		gs.drawString("Enemy 객체수 : " + EnemyList.size(), 850, 50);
		gs.drawString("게임시작 : Enter", 850, 90);
		gs.drawString("시간 : " + String.format("%.2f", mtime), 850, 130);

		if(finish) {
			gs.drawString("G A M E     O V E R", 400, 325);
		}
		
		gs.setColor(Color.white);
		//gs.drawImage(image, mx, my, wx, wy, null);
		gs.fillRect(mx, my, wx, wy);
		
		
		for(int i = 0; i < EnemyList.size(); i++) {
			Enemy e = (Enemy)EnemyList.get(i);
			if(e.dy > 3) {
				gs.setColor(Color.CYAN);	
			}
			else {
				gs.setColor(Color.white);
			}
			gs.fillRect(e.x, e.y, e.w, e.h);
			if(e.y > Setting.MAX_Y || e.x > Setting.MAX_X || e.x < 0 || e.y <0) 
				EnemyList.remove(i);
			e.moveEn();
		}	
		
		Graphics ge = this.getGraphics();
		ge.drawImage(bufferedImage, 0, 0, Setting.MAX_X, Setting.MAX_Y, this);
	}
	 
	
	public Dot() {
		// TODO Auto-generated constructor stub
		mx = 500;
		my = 300;
		wx = 10;
		wy = 10;
		msleep = 30;
		mstep = 5;
		mtime=0;
		Cnt=0;
		Ant=0;
		EnemyList = new ArrayList<>();
		addKeyListener(this);
	}

	public void keyControl() {
		if(0 < mx) {
			if(left) mx -= mstep;
		}
		if(Setting.MAX_X > mx+wx) {
			if(right) mx += mstep;
		}
		if(0 < my) {
			if(up) my -= mstep;
		}
		if(Setting.MAX_Y > my + wy) {
			if(down) my += mstep;
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		int keycode = e.getKeyCode();
		switch (keycode) {
		case KeyEvent.VK_UP:
			up=false;
			break;
		case KeyEvent.VK_DOWN:
			down=false;
			break;
		case KeyEvent.VK_LEFT:
			left=false;
			break;
		case KeyEvent.VK_RIGHT:
			right=false;
			break;
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		// super.keyPressed(e);
		int keycode = e.getKeyCode();

		switch (keycode) {
		case KeyEvent.VK_UP:
			up=true;
			break;
		case KeyEvent.VK_DOWN:
			down=true;
			break;
		case KeyEvent.VK_LEFT:
			left=true;
			break;
		case KeyEvent.VK_RIGHT:
			right=true;
			break;
		case KeyEvent.VK_ENTER:
			play=true;
			finish = false;
			reset();
			break;
		}
		System.out.println(mx + ", " + my);
	}

	public void reset() {
		EnemyList.clear();
		mx = 500;
		my = 300;
		mtime=0;
		Cnt=0;
	}
	
	@Override
	public void run() {	
		//키보드 이벤트 추가
		enCreate();
		try {
			do {	
				Thread.sleep(msleep);
				mtime+=0.01;
				if(play) {
					keyControl();
					crashChk();
					if(Cnt>1000) {
						enCreate();
						Cnt=0;
					}
					if(Ant>10) {
						Ant=0;
						//draw();
					}
					Cnt+=10;
					//Ant+=10;
					draw();
				}
				
			} while (true);

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void enCreate() {
		for(int i = 0; i < 10; i++) {
			int rx = (int)(1000*Math.random());
			int ry = 10;
			
			EnemyList.add(new Enemy(rx, ry));
			//EnemyList.add(new Enemy(ry, rx));
			//en = new Enemy(Setting.MAX_X-rx, Setting.MAX_Y-ry);
			//EnemyList.add(en);
			//en = new Enemy(Setting.MAX_Y-ry, Setting.MAX_X-rx);
			//EnemyList.add(en);
		}
	}

	public void crashChk() {
		Graphics g = this.getGraphics();
		Polygon p = null;
		
		for(int i = 0; i < EnemyList.size(); i++) {
			Enemy e = (Enemy)EnemyList.get(i);
			int[] xpoints = {mx, (mx + wx), (mx + wx), mx};
			int[] ypoints = {my, my, (my + wy), (my + wy)};
			p = new Polygon(xpoints, ypoints, 4);
			if(p.intersects((double)e.x, (double)e.y, (double)e.w, (double)e.h)) {
				EnemyList.remove(i);
				play = false;
				finish = true;
			}
		}
	}
	
	class Enemy {
		int x;
		int y;
		int dy;			// 기울기  y = ax
		int dx;		// x좌표에서 왼쪽 혹은 오른쪽
		int w = 5;
		int h = 5;
		
		public Enemy(int x, int y) {
			this.x = x;
			this.y = y;
			if(mx>this.x)
				dx=1;
			else if(mx<this.x)
				dx=-1;
			else
				dx=0;
			
			if(my>this.y)
				dy = (int)(5*Math.random());
			else if(my<this.y)
				dy = -(int)(5*Math.random());
			else {
				dy=0;
			}
		}
		
		public void moveEn() {
			x+=dx;
			y+=dy;
		} 
	}
	
	public static void main(String[] args) {
		Runnable runnable = new Dot();
		Thread thread = new Thread(runnable);
		thread.start();		
	}
}
