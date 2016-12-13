/**
 * BlobQueue.java
 *
 * @author <a href="mailto:gery.casiez@univ-lille1.fr">Gery Casiez</a>
 * @version
 */

import java.util.ConcurrentModificationException;
import java.util.HashMap;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;


public class BlobQueue {
	int size = 30;
	HashMap<Integer, Point2D> cursors;
	GraphicsContext gc;
	
	private int getNewID() {
		int i = 0;
		while (cursors.keySet().contains(i)) {
			i++;
		}
		return i;
	}
	
	public BlobQueue(GraphicsContext graphicsContext) {
		cursors=new HashMap<Integer,Point2D>();
		gc = graphicsContext;
	}
	
	public void add(int id, Point2D p) {
		cursors.put(id,p);
	}
	
	public void update(int id, Point2D p) {
		cursors.remove(id);
		cursors.put(id,p);
	}
	
	public void remove(int id) {
		cursors.remove(id);
	}
	
	public int getNbFingers() {
		return cursors.keySet().size();
	}
	
	public void draw() {
		try {
			for(int i : cursors.keySet()) {
				Point2D p = cursors.get(i);
				gc.setFill(Color.PINK);
				gc.fillOval(p.getX() - size/2, p.getY() - size/2, size, size);
				
				gc.setStroke(Color.BLACK);
				gc.strokeOval(p.getX() - size/2, p.getY() - size/2, size, size);
								
				// Text with id
				gc.setFill(Color.BLACK);
				gc.fillText("" + i, p.getX()-3, p.getY()+4);
	
			}
		}
		catch (ConcurrentModificationException e) {
			System.out.println("Exception ConcurrentModificationException to fix!");
		}
	}
		
	public boolean checkId(int id) {
		return (cursors.keySet().contains(id));
	}

	public void add(Point2D p) {
		for(int i : cursors.keySet()) {
			Point2D p2 = cursors.get(i);
			if ((p2.subtract(p)).magnitude() < size/2.0) {
				update(i,p);
				return;
			}
		}
		add(getNewID(), p);	
	}

	public void update(Point2D p) {
		for(int i : cursors.keySet()) {
			Point2D p2 = cursors.get(i);
			if ((p2.subtract(p)).magnitude() < size/2.0) {
				update(i,p);
				return;
			}
		}
	}

	public void remove(Point2D p) {
		for(int i : cursors.keySet()) {
			Point2D p2 = cursors.get(i);
			if ((p2.subtract(p)).magnitude() < size/2.0) {
				remove(i);
				return;
			}
		}
	}	
	
	public Point2D getPoint(int id) {
		return cursors.get(id);
	}
}
