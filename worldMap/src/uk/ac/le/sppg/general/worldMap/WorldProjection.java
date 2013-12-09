/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.le.sppg.general.worldMap;

import uk.ac.le.sppg.coords.proj.Projection;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;

/**
 *
 * @author nigel
 */
public class WorldProjection {
    
    private Projection projection = null;
    
    private ArrayList<WorldPath> coasts = null;
    private ArrayList<GeneralPath> meridians = null;
    private ArrayList<GeneralPath> parallels = null;
    
    protected WorldProjection(World world, int minLength, Projection proj) {
        this.projection = proj;
        
        coasts = world.projectCoastlines( proj, minLength );
        meridians = world.projectMeridians(proj, minLength);
        parallels = world.projectParallels(proj, minLength);

    }
    
    /**
     * Fills the land and lakes with the requested colours.
     * @param g2
     * the graphics context into which the coastlines will be drawn.
     * This should have had a suitable {@link AffineTransform} applied,
     * or be appropriately scaled.
     * @param landColour
     * colour with which to fill the land
     * @param lakeColour
     * colour with which to fill the lakes
     * @return
     * <code>false</code> if the outlines have not been completely loaded or a
     * projection is currently in progress.
     */
    public boolean fillAll( Graphics2D g2, Color landColour, Color lakeColour ) {
        
        Color c = g2.getColor();        
        
        for ( WorldPath path: coasts ) {
            
            if ( path.isCoastline() )
                g2.setColor( landColour );
            else
                g2.setColor( lakeColour );
            
            g2.fill( path.getPath() );
        }
        
        g2.setColor( c );
        
        return true;
    }
    
    /**
     * Fills the land the requested colour.
     * @param g2
     * the graphics context into which the coastlines will be drawn.
     * This should have had a suitable {@link AffineTransform} applied,
     * or be appropriately scaled.
     * @param landColour
     * colour with which to fill the land
     * @return
     * <code>false</code> if the outlines have not been completely loaded or a
     * projection is currently in progress.
     */
    public synchronized boolean fillContinents( Graphics2D g2, Color landColour ) {
        
        Color c = g2.getColor();
        
        g2.setColor( landColour );
        
        for ( WorldPath path: coasts ) {
            
            if ( path.isCoastline() )
                g2.fill( path.getPath() );
        }
        
        g2.setColor( c );
        
        return true;
    }
    
    /**
     * Fills the lakes with the requested colour.
     * @param g2
     * the graphics context into which the coastlines will be drawn.
     * This should have had a suitable {@link AffineTransform} applied,
     * or be appropriately scaled.
     * @param lakeColour
     * colour with which to fill the lakes
     * @return
     * <code>false</code> if the outlines have not been completely loaded or a
     * projection is currently in progress.
     */
    public synchronized boolean fillLakes( Graphics2D g2, Color lakeColour ) {
        
        Color c = g2.getColor();
        
        g2.setColor( lakeColour );
        
        for ( WorldPath path: coasts ) {
            
            if ( ! path.isCoastline() )
                g2.fill( path.getPath() );
        }
        
        g2.setColor( c );
        
        return true;
    }
    
    /**
     * Draws the projected coastlines in the specified colour into the
     * <code>Graphics2D</code> context.
     * @param g2
     * the graphics context into which the coastlines will be drawn.
     * This should have had a suitable {@link AffineTransform} applied,
     * or be appropriately scaled..
     * @param colour
     * the colour in which the coastlines should be drawn.
     * @return
     * <code>false</code> if the outlines have not been completely loaded or a
     * projection is currently in progress.
     */
    public synchronized boolean drawCoastlines( Graphics2D g2, Color colour ) {
        Color c = g2.getColor();
        //System.out.println( g2.getTransform() );
        g2.setColor( colour );
        
        for ( WorldPath path: coasts ) {
            
            if ( path.isCoastline() )
                g2.draw( path.getPath() );
        }
        
        g2.setColor( c );
        
        return true;
    }
    
    /**
     * Draws the projected lakes in the specified colour into the
     * <code>Graphics2D</code> context.
     * @param g2
     * the graphics context into which the lakes will be drawn.
     * This should have had a suitable {@link AffineTransform} applied,
     * or be appropriately scaled.
     * @param colour
     * the colour in which the lakes should be drawn.
     * @return
     * <code>false</code> if the outlines have not been completely loaded or a
     * projection is currently in progress.
     */
    public synchronized boolean drawLakes( Graphics2D g2, Color colour ) {
        
        Color c = g2.getColor();
        
        g2.setColor( colour );
        
        for ( WorldPath path: coasts ) {
            
            if ( ! path.isCoastline() )
                g2.draw( path.getPath() );
        }
        
        g2.setColor( c );
        
        return true;
    }
    
    /**
     * Draws the meridional and zonal grid lines.
     * These are created at 15 degree intervals.
     * @param g2
     * the graphics context into which the gridlines will be drawn.
     * This should have had a suitable {@link AffineTransform} applied,
     * or be appropriately scaled.
     * @param colour
     * the colour in which the lakes should be drawn.
     * @return
     * <code>false</code> if the outlines have not been completely loaded or a
     * projection is currently in progress.
     */
    public synchronized boolean drawGridlines( Graphics2D g2, Color colour ) {
        
        Color c = g2.getColor();
        
        g2.setColor( colour );
        
        for ( GeneralPath path: meridians ) {
            g2.draw( path );
        }

        for ( GeneralPath path: parallels ) {
            g2.draw( path );
        }
                
        g2.setColor( c );
        
        return true;
    }
    
    /**
     * Gets the geographic outlines of coasts and lakes.
     * Each coast and lake is stored as a separate entry in an <code>ArrayList</code>.
     * Each outline is a {@link #OutlineGeo}.
     * @return
     * The <code>ArrayList</code> containing the geographic outlines.
     * @throws WorldMapLoadingException
     * if the outlines are currently being loaded.
     */
    public ArrayList<WorldPath> getCoastlines() {             
        return coasts;
    }
    
}
