package uk.ac.le.sppg.coords;
import java.io.*;

/**
 * A class to implement a simple 3-D cartesian vector.
 * <p>
 * The <code>CartesianVector</code> is immutable.
 * 
 * @author Nigel Wade
 */
public class CartesianVector implements Serializable {

    private static final long serialVersionUID = 0x5253505047000002L;
    /**
     * the X component
     */
    public final double x;
    /**
     * the Y component
     */
    public final double y;
    /**
     * the Z component
     */
    public final double z;

    /**
     * the magnitude of the vector, calculated in the constructor.
     */
    public final double magnitude;

    /**
     * creates a vector with the given x, y, z components.
     * @param x
     * the X component value.
     * @param y
     * the Y component value.
     * @param z
     * the Z component value
     */
    public CartesianVector(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;

        this.magnitude = Math.sqrt(x * x + y * y + z * z);
    }

    /**
     * calculates the angle in degrees between this and another 
     * <code>CartesianVector</code>
     * @param v
     * the <code>CartesianVector</code> to calculate the angle from.
     * @return
     * the angle in degrees between this vector and <code>v</code>.
     */
    public double angle(CartesianVector v) {

        double cosAngle = (x * v.x + y * v.y + z * v.z) / this.magnitude / v.magnitude;

        return Math.toDegrees(Math.acos(cosAngle));

    }

    /**
     * calculates the vector sum of this and another 
     * <code>CartesianVector</code>
     * @param v
     * the <code>CartesianVector</code> to sum with this.
     * @return
     * the <code>CartesionVector</code> sum of this and <code>v</code>.
     */
    public CartesianVector sum(CartesianVector v) {

        return new CartesianVector(v.x + x, v.y + y, v.z + z);
    }

    /**
     * calculates the vector different between this and another 
     * <code>CartesianVector</code>
     * @param v
     * the <code>CartesianVector</code> to subtract from this.
     * @return
     * the <code>CartesionVector</code> difference between this and <code>v</code>.
     */
    public CartesianVector difference(CartesianVector v) {

        return new CartesianVector(x - v.x, y - v.y, z - v.z);

    }

    /**
     * rotates, or transforms, this vector with the 
     * <code>TransformMatrix</code> matrix.
     * 
     * @param matrix
     * the transformation to perform on this vector
     * @return
     * the transformed vector.
     */
    public CartesianVector rotate(TransformMatrix matrix) {

        double newx = matrix.x[0] * x + matrix.x[1] * y + matrix.x[2] * z;
        double newy = matrix.y[0] * x + matrix.y[1] * y + matrix.y[2] * z;
        double newz = matrix.z[0] * x + matrix.z[1] * y + matrix.z[2] * z;

        return new CartesianVector(newx, newy, newz);
    }

    /**
     * creates a new <code>CartesionVector</code> which is the 
     * normalized version of this vector.
     * @return
     * a new, normalized, <code>CartesianVector</code>
     */
    public CartesianVector normalize() {

        //if ( mag < 1.0e-100 ) 
        //    return result;

        double newx = x / magnitude;
        double newy = y / magnitude;
        double newz = z / magnitude;

        return new CartesianVector(newx, newy, newz);
    }

    /**
     * Converts this vector to polar coordinates. 
     * @return
     * A new <code>PolarVector</code> representation of this vector.
     */
    public PolarVector toPolar() {
        double r = magnitude;
        double theta = Math.acos(z / r);
        double phi = Math.asin(y / (r * Math.sin(theta)));

        return new PolarVector(r, theta, phi);
    }

}
