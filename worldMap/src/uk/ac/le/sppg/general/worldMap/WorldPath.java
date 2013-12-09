/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.le.sppg.general.worldMap;

import java.awt.geom.GeneralPath;

/**
 *
 * @author nigel
 */
public class WorldPath {
    GeneralPath path;
    boolean coastLine;
    
    WorldPath(GeneralPath path, boolean coast) {
        this.path = path;
        this.coastLine = coast;
    }
    
    public GeneralPath getPath() {
        return path;
    }
    
    public boolean isCoastline() {
        return coastLine;
    }
}
