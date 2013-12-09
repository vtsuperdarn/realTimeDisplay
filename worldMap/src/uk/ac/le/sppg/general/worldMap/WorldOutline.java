/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.le.sppg.general.worldMap;

import uk.ac.le.sppg.coords.GeographicOutline;

/**
 *
 * @author nigel
 */
public  class WorldOutline extends GeographicOutline {
    private boolean coastline;

    public WorldOutline(int maxSize, boolean coastline) {
        super(maxSize);
        this.coastline = coastline;
    }

    protected boolean isCoastline() {
        return coastline;
    }

}
