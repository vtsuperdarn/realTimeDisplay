/*
 * SiteList.java
 *
 * Created on 20 November 2007, 15:08
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.ac.le.sppg.coords;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import uk.ac.le.sppg.coords.GeneralSite;

/**
 *
 * @author nigel
 */
public class SiteList {
    
    // hash map to hold the site details
    private Map<String,Site> siteList = new HashMap<String,Site>();
    private static final Site unknownSite = new GeneralSite("unknown","unknown","unknown",-1,0.0,0.0,0.0);
    
    // hash map to cross-reference the site id with the site name
    private Map<Integer,String> siteNumber = new HashMap<Integer,String>();

    /**
     * Return a Site by looking up the name in the Site
     */
    public Site get(String compactSiteName) {
        Site site = siteList.get(compactSiteName);
        if ( site == null ) {
          site = getById(-1);
          if ( site == null ) {
            site = unknownSite;
          }
        }
        return site;
    }
    
    public Site getById(int id) {
        String name = siteNumber.get(new Integer(id));
        Site site;
        if (name != null) {
            site = siteList.get(name);
        } else {
          site = getById(-1);
          if ( site == null ) {
            site = unknownSite;
          }
        }
        return site;
    }

    /**
     * finds the definition of a <code>Site</code> from its full name.
     * @param name
     * the full name of the site to search for
     * @return
     * the <code>Site</code> if it's found, <code>null</code>
     * otherwise.
     */
    public Site getByName(String name) {
        for(Site site:siteList.values()) {
            if ( site.getName().equals(name))
                return site;
        }

        Site  site = getById(-1);
        if ( site == null ) {
          site = unknownSite;
        }
        return site;
    }
    /**
     * finds the definition of a <code>Site</code> from its short name.
     * @param name
     * the short name of the site to search for
     * @return
     * the <code>Site</code> if it's found, <code>null</code>
     * otherwise.
     */
    public Site getByShortName(String name) {
        for(Site site:siteList.values()) {
            if ( site.getShortName().equals(name)) 
                return site;
        }
        
        Site  site = getById(-1);
        if ( site == null ) {
          site = unknownSite;
        }
        return site;
    }

    
    /**
     * finds the definition of a <code>Site</code> from its single character identifier.
     * @param identifier
     * the single character identifier of the site to search for
     * @return
     * the <code>Site</code> if it's found, <code>null</code>
     * otherwise.
     */
    public Site getByIdentifier(String identifier) {
        for(Site site:siteList.values()) {
            if ( site.getIdentifier().equals(identifier)) 
                return site;
        }
        
        Site  site = getById(-1);
        if ( site == null ) {
          site = unknownSite;
        }
        return site;
    }

    public void addSite(Site site) {
        siteList.put(site.getCompactName(), site);
        siteNumber.put(new Integer(site.getStationId()), site.getCompactName());
    }
    
    public Set<String> getNames() {
        return siteList.keySet();
    }
    
    public Collection<Site> getSites() {
        return siteList.values();
    }
    
    public Set<Integer> getIds() {
        return siteNumber.keySet();
    }
}
