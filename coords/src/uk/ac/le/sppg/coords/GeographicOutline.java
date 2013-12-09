/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.le.sppg.coords;

import java.util.ArrayList;

/**
 * This class represents the outline of some Geographical feature.
 * It is an ArrayList of Geographic points.
 * 
 * @author Nigel Wade
 */
public class GeographicOutline {
    ArrayList<Geographic> path;
    
    public GeographicOutline(int initialSize) {
        path = new ArrayList<Geographic>( initialSize );
    }
    
    public void add(Geographic newPoint) {
        path.add(newPoint);
    }
    public ArrayList<Geographic> getPath() { return path; }
}
