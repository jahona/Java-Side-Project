package dot;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import dot.Setting;

public class Map extends JFrame{
	/**
	* 
	*/
	private static final long serialVersionUID = 1L;
	/**
	 * 필요한 필드 선언
	 */
	
	protected BufferedImage bufferedImage = null;
	
	// 컴포넌트 생성
	ImageIcon icon = new ImageIcon("C:\\Users\\jahona\\Desktop\\img1.jpg");
	protected Image image = Toolkit.getDefaultToolkit().createImage("C:\\Users\\jahona\\Desktop\\img1.jpg");
	/**
	 * 화면구성 생성자
	 * @return 
	 */
	
	public Map() {
		// 패널설정
		super(Setting.gameTitle);
				
		bufferedImage = new BufferedImage(Setting.MAX_X, Setting.MAX_Y, BufferedImage.TYPE_INT_RGB);
		
		// jFrame 셋팅
		setSize(Setting.MAX_X, Setting.MAX_Y);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // x버튼 활성

		setVisible(true);
	}
	
	public static void main(String[] args) {
		new Map();
	
	}
}
