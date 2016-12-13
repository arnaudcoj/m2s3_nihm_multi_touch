
/**
 *
 * @author <a href="mailto:gery.casiez@univ-lille1.fr">Gery Casiez</a>
 * @version
 */

import javafx.geometry.Point3D;

public class Quaternion {
	private double x, y, z, w;

    public Quaternion(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }
    
    // Given an axis and angle, compute quaternion.
    public Quaternion(Point3D a, double phi)
    {
    	double omega = 0.5f * phi;
    	double s = Math.sin(omega);

    	Point3D b = a.normalize();
    	x = s * b.getX();
    	y = s * b.getY();
    	z = s * b.getZ();

    	w = Math.cos(omega);
    }
    
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public double getW() {
        return w;
    }
    
    public double norm() {
    	return Math.sqrt(x * x + y * y + z * z + w * w);
    }

    public Quaternion normalize() {
        double length = norm();
        return new Quaternion(x / length, y / length, z / length, w / length);
    }

    public Quaternion conjugate() {
        return new Quaternion(-x, -y, -z, w);
    }
    
    public Quaternion multiply(Quaternion other) {
        double _x = (x * other.w) + (w * other.x) + (y * other.z) - (z * other.y);
        double _y = (y * other.w) + (w * other.y) + (z * other.x) - (x * other.z);
        double _z = (z * other.w) + (w * other.z) + (x * other.y) - (y * other.x);
        double _w = (w * other.w) - (x * other.x) - (y * other.y) - (z * other.z);

        return new Quaternion(_x, _y, _z, _w);
    }
    
    //Given a quaternion, gives the axis and angle
    public Point3D getAxis()
    {
    	/* Normalisation du quaternion */
    	Quaternion q = this.normalize();

    	/* Rotation angle */
    	double cos_a = q.getW();
    	double sin_a = Math.sqrt(1 - cos_a * cos_a);
    	if ( Math.abs( sin_a ) < 0.0005 ) sin_a = 1;
    	
    	return new Point3D(q.getX()/sin_a, q.getY()/sin_a, q.getZ()/sin_a);
    }
    
    public Quaternion invert()
    {
    	double	norm = norm();

    	if (norm > 0) return conjugate().multiply(new Quaternion(0,0,0,1.0/norm));
    	
    	return new Quaternion(0.0, 0.0, 0.0, 0.0);
    }

    
    public double getAngle() {
    	/* Normalisation du quaternion */
    	Quaternion q = this.normalize();

    	/* Rotation angle */
    	double cos_a = q.getW();
    	return Math.acos(cos_a) * 2;    	
    }
}
