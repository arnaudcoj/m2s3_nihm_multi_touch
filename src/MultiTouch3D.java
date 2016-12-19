/**
 *
 * @author <a href="mailto:gery.casiez@univ-lille1.fr">Gery Casiez</a>
 * @version
 */

import javafx.application.Application;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.geometry.Rectangle2D;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.PickResult;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class MultiTouch3D extends Application{
	BlobQueue bqueue;
	PickResult res;
	Point3D boxContact = null;
	boolean secondFingerInside = false;
	GraphicsContext gc;
	
	double sceneWidth = 800;
	double sceneHeight = 800;
	
	boolean fullscreen = false;
	boolean touchscreen = false;
	
	String debugInfo = "";

	public void start(Stage stage) {
        if (!Platform.isSupported(ConditionalFeature.SCENE3D)) {
            throw new RuntimeException("*** ERROR: SCENE3D is not supported on your machine !!");
        }
        
        if (fullscreen) {
	        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
	        sceneWidth = primaryScreenBounds.getWidth();
	        sceneHeight = primaryScreenBounds.getHeight();
        }
        
		Canvas canvas = new Canvas(sceneWidth,sceneHeight);
		canvas.setMouseTransparent(true);
		Group rootCanvas = new Group(canvas);
		gc = canvas.getGraphicsContext2D();
        bqueue = new BlobQueue(gc);
		
		// Create a Box
        Box box = new Box(200, 200, 200);
        box.setTranslateX(sceneWidth/2);
        box.setTranslateY(sceneHeight/2);
        box.setTranslateZ(100);
        box.setRotationAxis(new Point3D(1,1,0));
        box.setRotate(45);
        box.setId("box");
        
        // Set the material for the box
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(Color.rgb(191, 155, 58));
        material.setSpecularColor(Color.rgb(160, 142, 93));
        box.setMaterial(material);
        
        // Set up a light
        PointLight pointLight = new PointLight(Color.WHITE);
        pointLight.setTranslateX(400);
        pointLight.setTranslateY(400);
        pointLight.setTranslateZ(-300);

        AmbientLight ambientLight = new AmbientLight(Color.rgb(150, 150, 150));
        
        Group group3Dscene = new Group(box, ambientLight, pointLight);
		Group wholeGroup = new Group(group3Dscene, rootCanvas);
		
		Scene scene = new Scene(wholeGroup, sceneWidth, sceneHeight);
        PerspectiveCamera camera = new PerspectiveCamera();
        camera.setFarClip(1000);
		scene.setCamera(camera);
            		
		scene.setOnMousePressed(e -> {
			boolean finger0pressed = bqueue.checkId(0);
			boolean finger1pressed = bqueue.checkId(1);
			
			if (!touchscreen) {
				Point2D p = new Point2D(e.getX(), e.getY());
				if (e.isSecondaryButtonDown()) 
					bqueue.remove(p);
				else 
		    		bqueue.add(p);
			}

			finger0pressed = !finger0pressed && bqueue.checkId(0);
			finger1pressed = !finger1pressed && bqueue.checkId(1);
			
            res = e.getPickResult();	
            
            //when we put the first finger on the cube we register the contact point on the box
            if(finger0pressed && e.getPickResult().getIntersectedNode() != null) {
				Point3D p_w = box.localToParent(res.getIntersectedPoint());	
				boxContact = new Point3D(box.getTranslateX() - p_w.getX(), box.getTranslateY() - p_w.getY(), box.getTranslateZ() - p_w.getZ());
            }
            
            //check if the pressed button is nb 1
            if(finger1pressed) {
            	secondFingerInside = e.getPickResult().getIntersectedNode() != null;
            }
            
            System.out.println(res);
            if (res.getIntersectedNode() != null && res.getIntersectedNode().getId() != null) {
            	System.out.println("Le pointeur est sur le cube");
            } else {
            	res = null;
            }

            e.consume(); 
            redrawMyCanvas();
        });
		
		scene.setOnMouseDragged(e -> {
			if (!touchscreen) {
				Point2D oldp0 = bqueue.checkId(0) ? bqueue.getPoint(0) : null;
				Point2D oldp1 = bqueue.checkId(1) ? bqueue.getPoint(1) : null;
				
				Point2D p = new Point2D(e.getX(), e.getY());
	    		bqueue.update(p);
	    		
				Point2D p0 = bqueue.checkId(0) ? bqueue.getPoint(0) : null;
				Point2D p1 = bqueue.checkId(1) ? bqueue.getPoint(1) : null;
	    		
				//one finger on cube
				if(p0 != null) {
		    		//ok only if the screen is on the plane XY
		    		box.setTranslateX(p0.getX() + boxContact.getX());
		    		box.setTranslateY(p0.getY() + boxContact.getY());
				}
				
				if(p1 != null) {
					if(secondFingerInside) {
						Quaternion qold = new Quaternion(box.getRotationAxis(), Math.toRadians(box.getRotate()));
						Point2D delta = new Point2D(p1.getX() - oldp1.getX(), p1.getY() - oldp1.getY());
						
						double t = Math.atan(delta.magnitude() / (box.getWidth() / 2d));
						Quaternion q = new Quaternion(new Point3D(-delta.getY(), delta.getX(), 0d), -t);
						
						Quaternion qresult = q.multiply(qold);
						
						box.setRotationAxis(qresult.getAxis());
						box.setRotate(Math.toDegrees(qresult.getAngle()));
					} else {
						double dy = p1.getY() - oldp1.getY();
						box.setTranslateZ(box.getTranslateZ() - dy);
					}
				}
				
			}
        	

			redrawMyCanvas();
        });
        
		scene.setOnMouseReleased(e -> {
    		if(!bqueue.checkId(0)) {
    			boxContact = null;
    		}
        });
		
		scene.setOnTouchPressed(e -> {
			System.out.println("Touch pressed " + e);
		});
		
		scene.setOnTouchMoved(e -> {
			System.out.println("Touch moved " + e);
		});
		
		scene.setOnTouchReleased(e -> {
			System.out.println("Touch released " + e);
		});
        		
		stage.setScene(scene);
		stage.setTitle("Université Lille 1 - M2 IVI - NIHM - 3D MultiTouch interaction - G. Casiez");
		stage.show();
	}
	
	void redrawMyCanvas() {
		gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
		gc.fillText(debugInfo, 10, 20);
		
		if (!touchscreen) bqueue.draw();
	}
	
	public static void main(String[] args) {
		Application.launch(args);
	}
}
