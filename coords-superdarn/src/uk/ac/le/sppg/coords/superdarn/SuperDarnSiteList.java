/*
 * SuperDarnSiteList.java
 *
 * Created on 21 September 2007, 14:34
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.ac.le.sppg.coords.superdarn;

import uk.ac.le.sppg.coords.SiteList;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
        
/**
 *
 * @author nigel
 */
public class SuperDarnSiteList 
        extends SiteList
        implements Serializable {
        
    private static final long serialVersionUID = 0x5253505047000046L;

    private static final SuperDarnSiteList list;
    
    private SuperDarnSiteList() {}
    
    public static SuperDarnSiteList getList() {
        return list;
    }
    
    public SuperDarnSite get(String compactSiteName) {
        return (SuperDarnSite) super.get(compactSiteName);
    }
    
    public SuperDarnSite getByName(String siteName) {
        return (SuperDarnSite) super.getByName(siteName);
    }
    
    public SuperDarnSite getById(int id) {
        return (SuperDarnSite) super.getById(id);
    }

    /**
     * finds the definition of a <code>Site</code> from its short name.
     * @param name
     * the short name of the site to search for
     * @return
     * the <code>Site</code> if it's found, <code>null</code>
     * otherwise.
     */
    public SuperDarnSite getByShortName(String name) {
        return (SuperDarnSite) super.getByShortName(name);
    }
    
    public SuperDarnSite getByIdentifier(String name) {
        return (SuperDarnSite) super.getByIdentifier(name);
    }

    static {
        list = new SuperDarnSiteList();
        list.addSite(new SuperDarnSite("unknown", "unknown", "unknown", -1, 0.0, 0.0, 0.0, 0.0, 0.0,0.0,0));
        
//System.out.println("in SuperDarnSiteList static initializer");
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT0"));
        
        Pattern p = Pattern.compile("\\s*(\\d+)\\s+([-]*\\d+)\\s+\"([^\"]+)\"\\s+\"([^\"]+)\"\\s+\"([^\"]+)\"\\s+\"([^\"]+)\"\\s+\"([^\"]+)\"\\s*$");
//        Pattern p2 = Pattern.compile("\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+([-+]*\\d+(\\.\\d*)?)\\s+([-+]*\\d+(\\.\\d*)?)\\s+([-+]*\\d+(\\.\\d*)?)\\s+([-+]*\\d+(\\.\\d*)?)\\s+([-+]*\\d+(\\.\\d*)?)\\s+([-+]*\\d+(\\.\\d*)?)\\s+([-+]*\\d+(\\.\\d*)?)\\s+([-+]*\\d+(\\.\\d*)?)\\s+([-+]*\\d+(\\.\\d*)?)\\s+([-+]*\\d+(\\.\\d*)?)\\s+([-+]*\\d+(\\.\\d*)?)\\s+([-+]*\\d+(\\.\\d*)?)\\s+([-+]*\\d+(\\.\\d*)?)\\s+.*");
        Pattern p2 = Pattern.compile("\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+([-+]*\\d+(\\.\\d*)?)\\s+([-+]*\\d+(\\.\\d*)?)\\s+([-+]*\\d+(\\.\\d*)?)\\s+([-+]*\\d+(\\.\\d*)?)\\s+([-+]*\\d+(\\.\\d*)?)\\s+([-+]*\\d+(\\.\\d*)?)\\s+([-+]*\\d+(\\.\\d*)?)\\s+([-+]*\\d+(\\.\\d*)?)\\s+([-+]*\\d+(\\.\\d*)?)\\s+([-+]*\\d+(\\.\\d*)?)\\s+([-+]*\\d+(\\.\\d*)?)\\s+([-+]*\\d+(\\.\\d*)?)\\s+([-+]*\\d+(\\.\\d*)?)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+).*");

        InputStream in = SuperDarnSiteList.class.getResourceAsStream("/tables/superdarn/radar.dat");
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        
        String line;
        Matcher m;
        try {
            while((line = reader.readLine()) != null) {
                if ( line.length() > 0 && ! line.matches("\\s+")) {
                    m = p.matcher(line);
//System.out.println("radar.dat line: "+line);
                    if ( m.matches() ) {
                        int status = Integer.parseInt(m.group(2));
                        if ( status >= 0 ) {
                            // parse the line from the radar table
                            int id = Integer.parseInt(m.group(1));
// create a SuperDarnSite from the data
                            String name = m.group(3);
                            String shortName = m.group(6);
                            String hardwareFile = new String("/tables/superdarn/hdw/"+m.group(5));
                            String identifier = m.group(7);
                            
                            InputStream in2 = SuperDarnSiteList.class.getResourceAsStream(hardwareFile);
                            BufferedReader r = new BufferedReader(new InputStreamReader(in2));
                            
                            String line2;
                            
//System.out.println("reading site file: "+hardwareFile);
                            SuperDarnSite site = null;
                            while((line2 = r.readLine()) != null) {
//System.out.println("site line: "+line2);
                                m = p2.matcher(line2);
                                if ( m.matches() ) {
//System.out.println("matched");
                                    int year = Integer.parseInt(m.group(2));
                                    int yearSecs = Integer.parseInt(m.group(3));
                                    double lat = Double.parseDouble(m.group(4));
                                    double lon = Double.parseDouble(m.group(6));
                                    double alt = Double.parseDouble(m.group(8));
                                    double boreSite = Double.parseDouble(m.group(10));
                                    double beamSep = Double.parseDouble(m.group(12));
                                    double recRise = Double.parseDouble(m.group(28));
                                    int maxBeams = Integer.parseInt(m.group(32));
                                    
                                    cal.clear();
                                    cal.set(Calendar.YEAR, year);
                                    cal.set(Calendar.DAY_OF_YEAR, yearSecs/86400);
                                    
                                    Date date = cal.getTime();
                                            
                                    if ( site == null ) {
                                        if ( shortName.equals("pyk") ) {
                                            site = new SuperDarnBandSite(name, shortName, identifier, id,
                                                date, lat, lon, alt, boreSite, beamSep, recRise, maxBeams, FrequencyBand.icelandBands);
                                        }
                                        else if ( shortName.equals("han") ) {
                                            site = new SuperDarnBandSite(name, shortName, identifier, id,
                                                date, lat, lon, alt, boreSite, beamSep, recRise, maxBeams, FrequencyBand.finlandBands);
                                        }
                                        else {
                                                site = new SuperDarnSite(name, shortName, identifier, id,
                                                date, lat, lon, alt, boreSite, beamSep, recRise, maxBeams);
                                        }
//System.out.println("add site: "+site.getName());

                                        list.addSite(site);
                                    }
                                    else {
                                        site.addDetails(date, lat, lon, alt, boreSite, beamSep, recRise, maxBeams);
                                    }
                                }
                            }
                        }
                            
                            
                    }
                    else {
                        throw new Error("Failed to parse line in radar.dat: "+line);
                    }
                }
            }
        }
        catch(IOException e) {
            throw new Error("IO error parsing radar.dat");
        }
    }
        


}
