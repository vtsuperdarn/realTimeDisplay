/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.le.sppg.superdarn.dataDisplay.fanWindow;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

/**
 *
 * @author nigel
 */
public class ViewpointNavigationAction 
extends AbstractAction {
    
    public enum Direction {
        NORTH, SOUTH, EAST, WEST, NORTH_EAST, NORTH_WEST, SOUTH_EAST, SOUTH_WEST,
        CENTRE, ZOOM_IN, ZOOM_OUT;
    }
    
    FanPlot parent;
    Direction dir;
    
    ViewpointNavigationAction(FanPlot parent, Direction dir, String text, ImageIcon icon, String description) {
        super(text, icon);
        this.parent= parent;
        putValue(SHORT_DESCRIPTION, description);
        this.dir = dir;
    }
    
    public void actionPerformed(ActionEvent e) {
        switch(this.dir) {
            case NORTH:
                parent.moveNorth();
                break;
            case SOUTH:
                parent.moveSouth();
                break;
            case EAST:
                parent.moveEast();
                break;
            case WEST:
                parent.moveWest();
                break;
                
            case NORTH_EAST:
                parent.moveNorthEast();
                break;
            case SOUTH_EAST:
                parent.moveSouthEast();
                break;

            case NORTH_WEST:
                parent.moveNorthWest();
                break;
            case SOUTH_WEST:
                parent.moveSouthWest();
                break;

            case CENTRE:
                parent.centreView();
                break;

            case ZOOM_IN:
                parent.zoomIn();
                break;
            case ZOOM_OUT:
                parent.zoomOut();
                break;
        }
    }
}
