/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.le.sppg.superdarn.fitDataBeans;

/**
 *
 * @author nigel
 */
public class FitDataValue
        extends Number {

    private static final long serialVersionUID = 0x5253505047000045L;

    FitDataValue(double init) {
        value = init;
    }
    public double value;

    public String toString() {
        return String.valueOf(value);
    }

    public double doubleValue() {
        return value;
    }

    public float floatValue() {
        return (float) value;
    }

    public int intValue() {
        return (int) value;
    }

    public long longValue() {
        return (long) value;
    }

}
