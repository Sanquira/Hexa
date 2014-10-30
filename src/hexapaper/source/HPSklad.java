package hexapaper.source;

import hexapaper.hexapaper;
import hexapaper.Listeners.HPListenery;
import hexapaper.Listeners.HPListenery.HraciPlochaListener;
import hexapaper.entity.FreeSpace;
import hexapaper.entity.HPEntity;
import hexapaper.gui.Gprvky;
import hexapaper.gui.HraciPlocha;
import hexapaper.gui.HPRightMenu;
import hexapaper.network.server.HexaClient;

import java.awt.Color;
import java.awt.Component;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;

import core.LangFile;
import core.Location;

public class HPSklad {

	private static HPSklad instance = null;

	public Gprvky prvky;
	public HraciPlocha hraciPlocha;
	public HPRightMenu RMenu;

	public int RADIUS = 25;
	public int gridSl = 0;
	public int gridRa = 0;

	public JScrollPane scroll;
	public Location LocDontCare = new Location(RADIUS, RADIUS, 0);
	public HPEntity insertedEntity;
	public JMenu GameMenu;
	public JMenu ExportMenu;
	public JLabel connected;
	public JLabel position;

	public ArrayList<HPEntity> souradky;
	public ArrayList<HPEntity> databazePostav = new ArrayList<>();
	public ArrayList<HPEntity> databazeArtefaktu = new ArrayList<>();
	public ArrayList<Component> serverbanned = new ArrayList<>();

	public boolean hidePlayerColor = false;
	public boolean hideNPCColor = false;
	public boolean repeatableInsert = false;
	public boolean canEvent = false;
	public boolean isConnected = false;
	public boolean isPJ = false;
	public boolean insertingEntity = false;
	public boolean banned = false;

	public HexaClient client;
	public LangFile str;

	public final String VERSION = "v0.2a";
	public String lastName = "Player";

	public void send(Object o, String header) throws IOException {
		if (isConnected) {
			client.send(o, header);
		}
	}

	public void reload() {
		initLoad(souradky);
	}

	protected HPSklad() {
	}

	public void colorJMenu() {
		Color color = Color.DARK_GRAY;
		if (banned) {
			color = Color.RED;
		}
		for (Component item : serverbanned) {
			item.setForeground(color);
			item.repaint();
		}
	}

	public void init() {
		str = new LangFile(HPStrings.class);
		str.loadLang();

		hraciPlocha = new HraciPlocha();

		prvky = new Gprvky();
		RMenu = new HPRightMenu();
	}

	public static HPSklad getInstance() {
		if (instance == null) {
			instance = new HPSklad();
		}
		return instance;
	}

	public void setupInserting(HPEntity insert, boolean repeat) {
		if (insert == null) {
			insertedEntity = null;
			insertingEntity = false;
			repeatableInsert = false;
		} else {
			insertedEntity = insert;
			insertingEntity = true;
			repeatableInsert = repeat;
		}
	}

	public void initLoad(ArrayList<HPEntity> souradky) {
		hraciPlocha = new HraciPlocha();
		for (int i = 0; i < souradky.size(); i++) {
			if (souradky.get(i) instanceof FreeSpace) {
			} else {
				// System.out.println(i);
				hraciPlocha.insertEntity(i, souradky.get(i), true);
			}
		}
		hexapaper.HPfrm.repaint();
		HPListenery lis = new HPListenery();
		hraciPlocha.addMouseListener(lis.new HraciPlochaListener());
		hraciPlocha.addMouseMotionListener(lis.new HraciPlochaListener());
		scroll.setViewportView(hraciPlocha);
		scroll.getViewport().addChangeListener(lis.new ScrollListener());
		odblokujListenery();
	}

	public void odblokujListenery() {
		canEvent = true;
	}

	public void updatePosition(double x1, double y1) {
		double r = Math.cos(Math.toRadians(30)) * RADIUS;
		position.setText(str.get("Posititon") + Math.round(((x1 / RADIUS) - 1) * (2 / 3.) + 1) + "," + Math.round(((y1/r)-((y1/r)+1)%2-1)/2));
		position.repaint();
	}

	public void updateConnect() {
		if (isConnected && isPJ) {
			connected.setForeground(Color.BLUE);
			banned = false;
		}
		if (isConnected && !isPJ) {
			connected.setForeground(Color.GREEN);
			banned = true;
		}
		if (!isConnected) {
			connected.setForeground(Color.RED);
			banned = false;
		}
		connected.setText(str.get("ConnectLabel") + "{" + isConnected + "," + isPJ + "}");
		colorJMenu();
	}

	public static class prvekkNN implements Cloneable {
		private double x1, y1, vzd;
		private int idx;

		public prvekkNN(int index, double xp, double yp, double dist) {
			x1 = xp;
			y1 = yp;
			vzd = dist;
			idx = index;
		}

		public double getX1() {
			return x1;
		}

		public void setX1(double x1) {
			this.x1 = x1;
		}

		public double getY1() {
			return y1;
		}

		public void setY1(double y1) {
			this.y1 = y1;
		}

		public double getVzd() {
			return vzd;
		}

		public void setVzd(double vzd) {
			this.vzd = vzd;
		}

		public int getIdx() {
			return idx;
		}

		public void setIdx(int idx) {
			this.idx = idx;
		}

		public Object clone() throws CloneNotSupportedException {
			return super.clone();
		}

		public String toString() {
			return idx + ", " + x1 + ", " + y1 + ", " + vzd + "; ";
		}
	}

	public static class PropPair implements Cloneable, Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 2280106317511129808L;
		public String name;
		public String value;

		public PropPair(String name, String value) {
			this.name = name;
			this.value = value;
		}

		@Override
		public String toString() {
			return name + ": " + value;
		}

		@Override
		public PropPair clone() throws CloneNotSupportedException {
			return (PropPair) super.clone();
		}

	}

}