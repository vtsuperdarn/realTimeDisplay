package uk.ac.le.sppg.coords;

import java.io.Serializable;


/**
 * The SiteList class holds a set of pre-defined sites which can be
 * accessed either by name or index number.
 * <p>
 * A <code>Site</code> contains its location in <code>Geographic</code>
 * and <code>Geocentric</code> coordinates, its name, abbreviated name,
 * and a station id. SuperDARN sites are sub-classed from <code>Site</code>
 * as <code>SuperDarnSite</code> and additionally have values for
 * boresite, beam separation and receiver rise time.
 * <p>
 * <code>Site.stationId</code> for EISCAT sites is the
 * NCAR number, and for SuperDARN sites is the station id number.
 * <p>
 * The class statically pre-defines the EISCAT and SuperDARN sites plus
 * a few other sites of interest.
 * Other sites can be added to the list explicitely by the
 * <code>addSite</code> and <code>addSuperDarnSite</code> methods,
 * and implicitely by the
 * <code>Topocenctric(double, double, double, String, Geographic)</code>
 * constructor.
 *
 *
 * @author Nigel Wade
 */
public class GeneralSiteList 
        extends SiteList 
        implements Serializable {
    
    private static final long serialVersionUID = 0x5253505047000006L;

    private static final GeneralSiteList list;
    
    private GeneralSiteList() {}
    
    public static GeneralSiteList getList() {
        return list;
    }
    
    static {
        list = new GeneralSiteList();
        list.addSite(new GeneralSite("unknown","unknown","unknown",-1,0.0,0.0,0.0));
        list.addSite(new GeneralSite("Tromso", "eiscat-t", "t", 72, 69.585, 19.22, 86.3));
        list.addSite(new GeneralSite("Svalbard", "eiscat-l", "l", 95, 78.09, 16.03, 438.0));
        list.addSite(new GeneralSite("Kiruna","eiscat-k", "k", 71, 67.861, 20.44, 417.6));
        list.addSite(new GeneralSite("Sodankyla", "eiscat-s", "s", 73, 67.364, 26.63, 197.0));
        list.addSite(new GeneralSite("Millstone_Hill", "mhl", "m", 30, 42.6, -71.5, 0.0));
        list.addSite(new GeneralSite("Sondrestrom", "snd", "d", 80, 67.0, -51.0, 0.0));
    }
}
