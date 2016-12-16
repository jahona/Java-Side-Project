package test;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Stack;

import javax.naming.InitialContext;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class MazeGame extends JFrame implements Runnable, KeyListener, MouseListener{
	private static final int MAZE_SIZE_X = 12;			// maze x축 크기
	private static final int MAZE_SIZE_Y = 10;			// maze y축 크기
	private static final int MAZE_PADDING_X = 2;
	private static final int MAZE_PADDING_Y = 6;

	private static final int EMPTY = 6;
	private static final int BLOCK = 7;
	private static final int ENTRYPOINT = 8;
	private static final int ENDPOINT = 9;
	private static final int PATHPOINT = 10;
	private static final int CHECKEDPOINT = 11;

	private static final String SUB_1 = "게임 시작 : Enter";
	private static final String SUB_2 = "다시 시작 : Enter";
	private static final String SUB_3=  "             ";

	private int m_st_x, m_st_y;		// 미로찾기 주체의 좌표
	private int m_end_x, m_end_y;	// 도착점 좌표	
	private boolean m_play, m_finish, m_sucess;
	private int[][] m_map = new int[MAZE_SIZE_Y][MAZE_SIZE_X];			//map 2차원 배열
	private int[][] m_path = new int[MAZE_SIZE_Y][MAZE_SIZE_X];			//시작점과 도착점까지의 최단거리 경로를 기록
	private int[][] m_dist = new int[MAZE_SIZE_Y][MAZE_SIZE_X];			//각 지점까지의 최단거리를 기록
	private int m_chooseblock;		// 맵 생성시 선택한 블록들
	private boolean m_checkEntrypoint = false;			// 맵 생성시 시작 지점을 생성했는지 확인
	private boolean m_checkEndpoint = false;			// 맵 생성시 도착 지점을 생성했는지 확인

	private int m_dy[] = {0, 1, 0, -1};
	private int m_dx[] = {1, 0, -1, 0};

	//더블 버퍼링을 하기 위한 이미지 화면을 나타내는 변수와 그 이미지의 그래픽을 제어하기 위한 함수
	private Image offScreenImage;
	private Graphics offScreen;

	Stack<Integer> m_stack_x = new Stack<Integer>();
	Stack<Integer> m_stack_y = new Stack<Integer>();
	Stack<Integer> m_stack_path = new Stack<Integer>();
	Stack<Integer> m_stack_path2 = new Stack<Integer>();

	Toolkit toolkit = Toolkit.getDefaultToolkit();
	Image title = toolkit.getImage(".\\img\\title.gif"); //이미지 불러오기
	Image block = toolkit.getImage(".\\img\\block.jpg"); //이미지 불러오기
	Image empty = toolkit.getImage(".\\img\\empty.jpg"); //이미지 불러오기
	Image entry = toolkit.getImage(".\\img\\entry.jpg"); //이미지 불러오기
	Image end = toolkit.getImage(".\\img\\end.jpg"); //이미지 불러오기

	private MazeGame() {
		//init();
		//mapMaking();

		this.addKeyListener(this);
		this.addMouseListener(this);
		this.setLayout(null);

		setTitle("자동 미로 찾기 게임");
		setSize(700, 600);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		// 마우스 좌표와 미로 맵 클릭시 좌표 싱크율을  -30을 함으로서 맞춤
		int xpos = e.getX()-30;
		int ypos = e.getY()-30;

		// 게임중이 아닐때만 맵을 클릭해서 이벤트를 동작시킬 수 있다
		if(!m_play) check_maze_area(ypos, xpos);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		int key = e.getKeyCode();

		if(!m_play) {
			switch(key){   
			case KeyEvent.VK_1:
				//벽
				m_chooseblock = BLOCK;
				break;
			case KeyEvent.VK_2:
				//시작점
				m_chooseblock = ENTRYPOINT;
				break;
			case KeyEvent.VK_3:
				//종료지점
				m_chooseblock = ENDPOINT;
				break;
			case KeyEvent.VK_ENTER:
				m_play = !m_play;
				break;
			}
			repaint();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	private void inputBlock(int y, int x) {
		if(m_map[y][x] == EMPTY) {
			m_map[y][x] = BLOCK;
		}
		else if(m_map[y][x] == BLOCK) {
			m_map[y][x] = EMPTY;
		}
	}

	private void inputEntrypoint(int y, int x) {
		if(m_checkEntrypoint == false && m_map[y][x] == EMPTY) {
			// 맵 시작시 움직일 처음 시작 좌표를 지정
			m_st_x = x;
			m_st_y = y;
			m_map[y][x] = ENTRYPOINT;

			m_checkEntrypoint = true;
			return;
		}

		//엔트리포인트가 이미 한번 찍혔을 경우
		//그 시작 지점을 다시 한번 클릭시 엔트리 포인트를 삭제
		if(m_map[y][x]==ENTRYPOINT) {
			m_map[y][x] = EMPTY;
			m_checkEntrypoint = false;
		}
		else if(m_map[y][x]==EMPTY){
			JOptionPane.showMessageDialog(null, "시작점은 한개입니다. 명심하세요");
		}

	}

	private void inputEndpoint(int y, int x) {
		if(m_checkEndpoint==false && m_map[y][x] == EMPTY) {
			m_end_y = y;
			m_end_x = x;
			m_map[y][x] = ENDPOINT;

			m_checkEndpoint = true;
			return;
		}

		//엔드포인트가 이미 한번 찍혔을 경우
		//그 시작 지점을 다시 한번 클릭시 엔트리 포인트를 삭제
		if(m_map[y][x]==ENDPOINT) {
			m_map[y][x]=EMPTY;
			m_checkEndpoint = false;
		}
		else if(m_map[y][x]==EMPTY){
			JOptionPane.showMessageDialog(null, "도착점은 한개입니다. 명심하세요");
		}
	}


	//클릭한 곳에 선택한 블록을 설정
	private void check_maze_area(int y, int x) {
		for(int i=0 ; i<MAZE_SIZE_Y ; i++) {
			for(int j=0 ; j<MAZE_SIZE_X ; j++) {
				if((j+MAZE_PADDING_X-1)*30<=x && x<(j+MAZE_PADDING_X)*30 
						&& (i+MAZE_PADDING_Y-1)*30<=y && (i+MAZE_PADDING_Y)*30>y) {
					if(m_chooseblock==BLOCK) {
						inputBlock(i, j);
					}
					else if(m_chooseblock==ENTRYPOINT) {
						inputEntrypoint(i, j);
					}
					else if(m_chooseblock==ENDPOINT) {
						inputEndpoint(i, j);
					}		
					repaint();
					return;
				}
			}
		}
	}

	public void gameWait() {
		while(!gameStatus()) {
			// 약간의 sleep을 줌으로써 start의 값을 변경 후 판별한 시간을 준다.
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	// 현재 좌표가 유효한지 검사
	public boolean isValidLoc(int r, int c) {
		if(r<0 || c<0 || r>=MAZE_SIZE_Y || c>=MAZE_SIZE_X) 
			return false;
		else 
			return (m_map[r][c] == EMPTY || m_map[r][c] == ENDPOINT);
	}

	void gamePlay() {
		m_play = true;
	}

	void gameStop() {
		m_play = false;
	}

	boolean gameStatus() {
		return m_play;
	}

	public void mapReset() {
		for(int i=0 ; i<MAZE_SIZE_Y ; i++) {
			for(int j=0 ; j<MAZE_SIZE_X ; j++) {
				m_map[i][j] = EMPTY;
			}
		}
	}

	public void distReset() {
		for(int i=0 ; i<MAZE_SIZE_Y ; i++) {
			for(int j=0 ; j<MAZE_SIZE_X ; j++) {
				m_dist[i][j] = Integer.MAX_VALUE;
			}
		}
		m_dist[m_st_y][m_st_x] = 0;
	}

	public void pathReset() {
		for(int i=0 ; i<MAZE_SIZE_Y ; i++) {
			for(int j=0 ; j<MAZE_SIZE_X ; j++) {
				m_path[i][j] = 0;
			}
		}
	}

	//최단 경로를 얻는다
	public boolean checkShortdist() {
		// 맵 만들기		
		Integer min = Integer.MAX_VALUE;
		int y, x;
		int i;
		int direction;

		//시잦검을 받는다.
		m_stack_x.push(m_st_x);
		m_stack_y.push(m_st_y);
		m_stack_path.push(-1);

		while(!m_stack_x.empty()) {
			//스택에 있던  x, y좌표를 받는다.
			x = m_stack_x.peek();
			y = m_stack_y.peek();
			direction = m_stack_path.peek();
			m_stack_x.pop();
			m_stack_y.pop();
			m_stack_path.pop();

			m_path[y][x] = direction;

			if(m_map[y][x] == ENDPOINT) {
				m_sucess = true;
				if(min > m_dist[y][x])
					min = m_dist[y][x];
			}

			for(i=0 ; i<4 ; i++) {
				if(isValidLoc(y+m_dy[i], x+m_dx[i]) && (m_dist[y+m_dy[i]][x+m_dx[i]] >= m_dist[y][x]+1)) {
					m_stack_y.push(y+m_dy[i]);
					m_stack_x.push(x+m_dx[i]);	
					m_dist[y+m_dy[i]][x+m_dx[i]] = m_dist[y][x] + 1;
					m_stack_path.push(i);
				}
			}
		}

		if(m_sucess) {
			x = m_end_x;
			y = m_end_y;
			while(m_path[y][x]!=-1) {
				m_stack_path2.push(m_path[y][x]);
				direction = m_path[y][x];
				y-=m_dy[direction];
				x-=m_dx[direction];
			}

			return true;
		}
		return false;
	}

	@Override
	public void update(Graphics g) {
		// TODO Auto-generated method stub
		paint(g);
	}

	public void doublePainting(Graphics g, int w, int h) {
		g.drawImage(title,0,45,this); //이미지 입력

		g.setColor(Color.black);
		if(!m_play && !m_finish) {
			g.drawString(SUB_3, 300, 530);
			g.drawString(SUB_1, 300, 530);
		}
		else if(m_finish) {
			g.drawString(SUB_3, 300, 530);
			g.drawString(SUB_2, 300, 530);	
		}
		g.drawString("1번 : 벽돌", 440, 260);
		g.drawString("2번 : 시작점", 440, 320);
		g.drawString("3번 : 도각점", 440, 380);

		g.setColor(Color.black);
		g.drawImage(block, 535, 245, this); //이미지 입력
		g.drawImage(entry, 535, 305, this); //이미지 입력
		g.drawImage(end, 535, 365, this); //이미지 입력

		if(m_chooseblock == EMPTY)
			g.drawRect(535, 450, 30, 30);
		else if(m_chooseblock == BLOCK) {
			g.drawImage(block, 535, 450, this); //이미지 입력
		}
		else if(m_chooseblock == ENTRYPOINT) {
			g.drawImage(entry, 535, 450, this); //이미지 입력
		}
		else if(m_chooseblock == ENDPOINT) {
			g.drawImage(end, 535, 450, this); //이미지 입력
		}


		for(int i=0 ; i<MAZE_SIZE_Y ; i++) {
			for(int j=0 ; j<MAZE_SIZE_X ; j++) {
				if(m_map[i][j] == PATHPOINT && m_play==true) {
					g.drawImage(entry,(j+MAZE_PADDING_X) * 30, (i+MAZE_PADDING_Y) * 30, this); //이미지 입력
				}
				else if(m_map[i][j] == BLOCK) {
					g.drawImage(block,(j+MAZE_PADDING_X) * 30, (i+MAZE_PADDING_Y) * 30, this); //이미지 입력
				}
				else if(m_map[i][j] == EMPTY || m_map[i][j] == CHECKEDPOINT) {
					g.drawImage(empty,(j+MAZE_PADDING_X) * 30, (i+MAZE_PADDING_Y) * 30, this); //이미지 입력
				}
				else if(m_map[i][j] == ENDPOINT) {
					g.setColor(Color.red);
					g.fillRect((j+MAZE_PADDING_X) * 30, (i+MAZE_PADDING_Y) * 30, 30, 30);
					g.drawImage(end,(j+MAZE_PADDING_X) * 30, (i+MAZE_PADDING_Y) * 30, this); //이미지 입력
				}
				else if(m_map[i][j] == ENTRYPOINT) {
					g.drawImage(entry,(j+MAZE_PADDING_X) * 30, (i+MAZE_PADDING_Y) * 30, this); //이미지 입력
				}
			}
		}
	}

	/*
	 * 더블 버퍼링 기법을 이용하여 그림 이동간의 화면 끊김 현상을 줄임
	 */
	@Override
	public void paint(Graphics g) {
		// TODO Auto-generated method stub
		int w = this.getSize().width;
		int h = this.getSize().height;

		if(offScreen == null) {
			try {
				offScreenImage = createImage(w,h);
				offScreen = offScreenImage.getGraphics();
			} catch(Exception e) {
				offScreen = null;
			}
		}

		if(offScreen!=null) {
			doublePainting(offScreen, w, h);
			g.drawImage(offScreenImage, 0, 0, this);
		}
		else {
			g.drawImage(offScreenImage, 0, 0, this);
		}

	}

	public void mapMaking() {
		init();
		mapReset();
		pathReset();
		distReset();
		gameWait();
	}

	public void init() {
		m_play = false;
		m_finish = false;
		m_sucess = false;
		m_checkEntrypoint = false;
		m_checkEndpoint = false;
		m_chooseblock = EMPTY;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		int x, y;
		Integer direction;

		while(true) {
			init();
			mapMaking();
			repaint();
			if(!checkShortdist()) {
				JOptionPane.showMessageDialog(null, "도착점으로 갈 수 있는 경우의 수가 없습니다");
			}
			else {
				x = m_st_x;
				y = m_st_y;

				m_map[y][x] = PATHPOINT;

				while(!m_stack_path2.empty()) {
					repaint();
					direction = m_stack_path2.peek();
					m_stack_path2.pop();
					m_map[y][x]=EMPTY;
					y+=m_dy[direction];
					x+=m_dx[direction];
					m_map[y][x]=PATHPOINT;

					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				JOptionPane.showMessageDialog(null, "결과가 나왔습니다");
			}
			repaint();
		}
	}

	public static void main(String[] args) {		
		//Maze 게임 생성
		MazeGame maze = new MazeGame();
		Thread thread = new Thread(maze);

		maze.repaint();
		thread.start();
	}

}
