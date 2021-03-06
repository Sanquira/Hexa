package hexapaper.gui;

import hexapaper.entity.Artefact;
import hexapaper.entity.FreeSpace;
import hexapaper.entity.HPEntity;
import hexapaper.entity.Postava;
import hexapaper.entity.Wall;
import hexapaper.source.BPolygon;
import hexapaper.source.HPSklad;
import hexapaper.source.HPSklad.prvekkNN;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import javax.swing.JPanel;

import core.Grids;
import core.Location;

public class HraciPlocha extends JPanel
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	HPSklad sk = HPSklad.getInstance();
	HPEntity cursor = null;
	int oldIdx = -1;

	int fontSize = (int) Math.round(sk.c.RADIUS * 0.75);

	public HraciPlocha()
	{
		init(0, 0, 0);
	}

	public HraciPlocha(int gridSl, int gridRa, int RADIUS)
	{
		init(gridSl, gridRa, RADIUS);
	}

	public void init(int gridSl, int gridRa, int RADIUS)
	{
		setPreferredSize(new Dimension((int) Math.round((Math.round(gridSl / 2.0) * 2 + ((int) gridSl / 2.5) + 2) * RADIUS),
				(int) Math.round(Math.cos(Math.toRadians(30)) * RADIUS * 2 * (gridRa + 0.5))));
		setBackground(Color.black);
		sk.souradky = genGrid(gridSl, gridRa);
	}

	private ArrayList<HPEntity> genGrid(int sloupcu, int radku)
	{
		ArrayList<HPEntity> grid = new ArrayList<HPEntity>();
		int[][] souradky = Grids.gridHexa(sloupcu, radku, sk.c.RADIUS);
		for (int i = 0; i < souradky.length; i++)
		{
			souradky[i][0] += sk.c.RADIUS;
			souradky[i][1] += Math.round(sk.c.RADIUS * Math.cos(Math.toRadians(30)));
			grid.add(new FreeSpace(new Location(souradky[i][0], souradky[i][1], 0)));
		}
		return grid;
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		g.setFont(getFont().deriveFont((float) fontSize).deriveFont(Font.BOLD));
		FontMetrics fm = g.getFontMetrics();
		Graphics2D g2 = (Graphics2D) g;
		g2.setStroke(new BasicStroke(2));
		for (HPEntity ent : sk.souradky)
		{
			if (sk.scroll.getViewport().getViewRect().contains(ent.loc.getPoint()))
			{
				g2.setColor(ent.background);
				g2.fillPolygon(new Gprvky().emptyHexagon(ent.loc));
				g2.setColor(Color.black);
				g2.drawString(ent.tag, Math.round(ent.loc.getX() - (fm.getStringBounds(ent.tag, g).getWidth() / 2)),
						Math.round(ent.loc.getY() + (fm.getStringBounds(ent.tag, g).getHeight() / 3)));
				fm.getStringBounds(ent.tag, g).getWidth();
				for (BPolygon poly : ent.prvek)
				{
					if (!poly.isFilled)
					{
						g2.drawPolygon(poly);
					} else
					{
						g2.fillPolygon(poly);
					}
				}
			}
		}
		if (cursor != null)
		{
			g2.setColor(cursor.background);
			if (!sk.isConnected && !sk.isPJ)
			{
				g2.fillPolygon(new Gprvky().emptyHexagon(cursor.loc));
				g2.setColor(Color.black);
				g2.drawString(cursor.tag, Math.round(cursor.loc.getX() - (fm.getStringBounds(cursor.tag, g).getWidth() / 2)),
						Math.round(cursor.loc.getY() + (fm.getStringBounds(cursor.tag, g).getHeight() / 3)));
				fm.getStringBounds(cursor.tag, g).getWidth();
			}
			for (BPolygon poly : cursor.prvek)
			{
				if (!poly.isFilled)
				{
					g2.drawPolygon(poly);
				} else
				{
					g2.fillPolygon(poly);
				}
			}
		}
	}

	public int countDir(ArrayList<prvekkNN> prvky)
	{
		int[] u = new int[2];
		u[0] = (int) Math.signum(prvky.get(1).getX1() - prvky.get(0).getX1());
		u[1] = (int) Math.signum(prvky.get(1).getY1() - prvky.get(0).getY1());

		if (u[1] == 1)
		{
			switch (u[0])
			{
			case -1:
				return 4;
			case 0:
				return 3;
			case 1:
				return 2;
			}
		}
		if (u[1] == -1)
		{
			switch (u[0])
			{
			case -1:
				return 5;
			case 0:
				return 0;
			case 1:
				return 1;
			}
		}

		System.err.println("HraciPlocha.countDir(nejblizsi prvky) Chyba je ve vektoru. Vysel nula i kdyz nemel.");
		System.exit(-1);
		return -1;
	}

	public void rotateEntity(ArrayList<prvekkNN> idx)
	{
		int smer = countDir(idx);
		for (HPEntity ent : sk.souradky)
		{
			if (ent.loc.getX() == idx.get(0).getX1() && ent.loc.getY() == idx.get(0).getY1() && ent.isRotateable)
			{
				if ((sk.isConnected && sk.isPJ) || !sk.isConnected)
				{
					ent.loc.setDir(smer);
					ent.recreateGraphics();
					Integer[] obj = { ent.loc.getX(), ent.loc.getY(), ent.loc.getDir() };
					sk.send(obj, "rotateEnt", true);
					repaint();
					return;
				}
				System.out.println("No permission: Rotate");
				break;
			}
		}
	}

	public boolean insertEntity(Integer idx, HPEntity type, Boolean hard)
	{
		if (type == null)
		{
			return false;
		}

		HPEntity ent = sk.souradky.get(idx);
		if (ent.isColidable || hard)
		{
			Location loc = ent.loc;
			type.loc.setX(loc.getX());
			type.loc.setY(loc.getY());
			if (sk.colorAdd)
			{
				// System.out.println("barvím");
				type.setBcg(sk.color);
			}
			type.recreateGraphics();
			sk.souradky.set(idx, CloneEntity(type));
			repaint();
			if (sk.isConnected && sk.isPJ && type != null)
			{
				Object[] o = { idx, CloneEntity(type)};
				sk.client.send(o, "insertEnt");
			}
			if (!sk.repeatableInsert)
			{
				type = null;
				sk.setupInserting(null, false);
			}
			return true;

		} else
		{
			if (oldIdx != -1)
			{
				insertEntity(oldIdx, type, true);
				oldIdx = -1;
				return false;
			}
		}
		return false;

	}

	public void drawCursor(int x, int y)
	{
		if (sk.insertingEntity)
		{
			cursor = CloneEntity(sk.insertedEntity);
			cursor.setBcg(Color.white);
			cursor.loc = new Location(x, y, cursor.loc.getDir());
			// cursor.background=Color.BLACK;
			cursor.recreateGraphics();
			repaint();
		} else
		{
			cursor = null;
			repaint();
		}
	}

	public void saveEntity(int idx)
	{
		// System.out.println(sk.souradky.get(idx));
		if (sk.souradky.get(idx) instanceof FreeSpace)
		{
			sk.setupInserting(null, false);
			return;
		}
		HPEntity ent = CloneEntity(sk.souradky.get(idx));
		sk.setupInserting(ent, false);
		Location loc = ent.loc;
		oldIdx = idx;
		drawCursor(loc.getX(), loc.getY());

	}

	public void releaseEntity(int idx)
	{
		if (insertEntity(idx, sk.insertedEntity, false) && idx != oldIdx)
		{
			insertEntity(oldIdx, new FreeSpace(sk.souradky.get(oldIdx).loc), true);
		}
		drawCursor(-1, -1);
	}

	private HPEntity CloneEntity(HPEntity entity)
	{
		if (entity instanceof Artefact)
			return new Artefact((Artefact) entity);
		if (entity instanceof Postava)
			return new Postava((Postava) entity);
		if (entity instanceof FreeSpace)
			return new FreeSpace(entity.loc);
		if (entity instanceof Wall)
			return new Wall(entity.loc);
		return null;
	}

}
