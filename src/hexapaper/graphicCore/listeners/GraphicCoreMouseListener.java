package hexapaper.graphicCore.listeners;

import hexapaper.graphicCore.Canvas;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.SwingUtilities;

import mathLibrary.vector.Vector2D;

public class GraphicCoreMouseListener implements MouseListener, MouseMotionListener, MouseWheelListener {

	private Canvas canvas;
	private Vector2D pressPoint;

	public GraphicCoreMouseListener(Canvas canvas) {
		this.canvas = canvas;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		canvas.addZoom(-e.getPreciseWheelRotation());
		canvas.repaint();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (SwingUtilities.isMiddleMouseButton(e)) {
			Vector2D dragPoint = new Vector2D(e.getX(), e.getY());
			canvas.addPosition(pressPoint.add(dragPoint.mul(-1)));
			pressPoint = dragPoint;
			canvas.repaint();
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (SwingUtilities.isMiddleMouseButton(e)) {
			canvas.getPopupMenu().show(canvas, e.getX(), e.getY());
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		pressPoint = new Vector2D(e.getX(), e.getY());
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

}
