/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.le.sppg.superdarn.realTimeControl;

import uk.ac.le.sppg.coords.Geographic;
import uk.ac.le.sppg.coords.proj.Projection;
import uk.ac.le.sppg.coords.proj.Stereographic;
import java.io.IOException;
import java.net.MalformedURLException;
import uk.ac.le.sppg.superdarn.dataDisplay.PlotParameter;
import uk.ac.le.sppg.coords.superdarn.SuperDarnSite;
import uk.ac.le.sppg.coords.superdarn.SuperDarnSiteList;
import uk.ac.le.sppg.general.worldMap.OldWorld;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import uk.ac.le.sppg.superdarn.fitData.ChannelId;

/**
 *
 * @author nigel
 */
public class ProcessArgs {

    TimeZone myUTC = new SimpleTimeZone(0,"myUTC");

    public ProcessArgs(URL url, String args[], ControlPanel control) 
    throws NoSuchFieldException, MalformedURLException, IOException, ParseException, InterruptedException {
        TimeZone.setDefault(myUTC);
        OldWorld.load();

        
        final URL urlCopy = url;
        final ControlPanel controlCopy = control;
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                controlCopy.setServer(urlCopy) ;
            }
        });
        

        // set the start time to 23 hours ago
        Calendar cal = new GregorianCalendar(myUTC);
        
        float defaultDuration = 24.0f;

        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        format.setTimeZone(myUTC);
        
        // zeroMillis is the "date" for 00:00.
        long zeroMillis=0;
        try {
            Date zeroTime = format.parse("00:00");
            zeroMillis = zeroTime.getTime();
        } catch(Exception e) {}
        
        Calendar now = new GregorianCalendar(myUTC);
        
        GregorianCalendar midnightPast = new GregorianCalendar(myUTC);
        midnightPast.set(Calendar.HOUR_OF_DAY, 0);
        midnightPast.set(Calendar.MINUTE, 0);
        midnightPast.set(Calendar.SECOND, 0);
        
        Calendar start = null;
        
//        = new GregorianCalendar(TimeZone.getTimeZone("GMT0"));
        
        SuperDarnSite site;
        ChannelId channel;
        PlotType plotType;
        int beamNumber;
        boolean oldData;
        PlotParameter plotParameter;
        boolean groundScatter;
        float duration;
        double limit;
        int zoom;
        double scale;
        int width;
        int height;
        int panelX;
        int panelY;
        int x;
        int y;
        boolean panelIconified;
        
        for(String arg: args) {

//System.out.println("arg: "+arg);
            String[] parameters = arg.split(",");
            site = null;
            channel = ChannelId.A;
            plotType = PlotType.value("TIME");
            beamNumber = 8;
            oldData =  false;
            plotParameter = PlotParameter.value("VELOCITY");
            groundScatter = true;
            duration = defaultDuration;
            zoom = 0;
            scale = -1.0;
            width = -1;
            height = -1;
            panelX = -1;
            panelY = -1;
            x = -1;
            y = -1;
            panelIconified = false;
            limit = -1.0;
            
            start = null;
            
            Dimension size = null;
            Point loc = null;
            Point controlLoc = null;
            
                try {
                    for(int i=0; i<parameters.length; i++) {
                        String parameter = parameters[i];
                        String[] bits = parameter.split("=");
                        if ( bits.length != 2 ) {
                            throw new ParseException("Error in arg: "+arg+", format must be parameter=value", i);
                        }
//System.out.println("bits: "+bits[0]+" "+bits[1]);
                        DisplayParameter par = DisplayParameter.value(bits[0]);
    
                        switch( par ) {
                            case START:
                                Date d = format.parse(bits[1]);
                                // get the offset hours in milliseconds.
                                int milliSecs = (int) (d.getTime() - zeroMillis);
                                if ( milliSecs > 1000*24*60*60 || milliSecs < -1000*24*60*60 ) {
                                    throw new ParseException("start offset out of range", 0);
                                }

                                // calculate the offset as a true date.
                                cal.setTime(midnightPast.getTime());
                                cal.add(Calendar.MILLISECOND, milliSecs);

                                start = cal;
                                
                                break;

                            case DURATION:
                                Date endDate = format.parse(bits[1]);
                                // get the offset hours in milliseconds.
                                cal.setTime(endDate);
                                duration = (cal.get(Calendar.DAY_OF_YEAR)-1)*24.0f + 
                                       cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE) / 60.0f;
                                milliSecs = (int) (duration*60*60)*1000;
                                if ( milliSecs > 1000*24*60*60 || milliSecs < -1000*24*60*60 ) {
                                    throw new ParseException("duration out of range", 0);
                                }
                    
                                break;

                            case BEAM:
                                beamNumber = Integer.parseInt(bits[1]);
                                break;

                            case CHANNEL:
                                channel = ChannelId.value(bits[1]);
                                break;

                            case TYPE:
                                plotType = PlotType.value(bits[1]);
//System.out.println("plot type: "+plotType);
                                break;

                            case SITE:
                                if ( bits[1].length() == 1 ) 
                                    site = SuperDarnSiteList.getList().getByIdentifier(bits[1].toLowerCase());
                                else if ( bits[1].length() == 3 )
                                    site = SuperDarnSiteList.getList().getByShortName(bits[1].toLowerCase());
                                else
                                    site = SuperDarnSiteList.getList().get(bits[1]);
                                if ( site == null )
                                    throw new ParseException("parameter: "+bits[1]+" is not a valid site, in: "+arg,i);
                                break;

                            case PARAMETER:
                            case PAR:
                                plotParameter = PlotParameter.value(bits[1]);
                                break;

                            case LIMIT:
                                limit = Double.parseDouble(bits[1]);
                                break;

                            case SCALE:
                                scale = Double.parseDouble(bits[1]);
                                break;

                            case OLDDATA:
                                oldData = bits[1].equalsIgnoreCase("yes") || bits[1].equalsIgnoreCase("true");
                                break;

                            case GROUND_SCATTER:
                            case GS:
                                groundScatter = bits[1].equalsIgnoreCase("yes") || bits[1].equalsIgnoreCase("true");
                                break;

                            case ZOOM:
                                zoom = Integer.parseInt(bits[1]);
                                break;

                            case X:
                                x = Integer.parseInt(bits[1]);
                                break; 

                            case Y:
                                y = Integer.parseInt(bits[1]);
                                break;

                            case W:
                                width = Integer.parseInt(bits[1]);
                                break;

                            case H:
                                height = Integer.parseInt(bits[1]);
                                break;                            

                            case PANEL_X:
                            case PANX:
                                panelX = Integer.parseInt(bits[1]);
                                break; 

                            case PANEL_Y:
                            case PANY:
                                panelY = Integer.parseInt(bits[1]);
                                break;

                            case PANEL_ICONIFIED:
                            case PANI:
                                panelIconified = bits[1].equalsIgnoreCase("yes") || bits[1].equalsIgnoreCase("true");
                                break;

                        }

                    }

                }
                
                catch(IllegalArgumentException e) {
                     throw new ParseException("Error in arg: "+arg+": "+e.getMessage(),0);
                }
            

                // default start time is now - duration.
                if ( start == null ) {
                    start = new GregorianCalendar(myUTC);

                    if ( oldData ) {
                        // calculate the offset as a true date.
                        int  milliSecs = (int) (duration*60*60)*1000;
                        start.add(Calendar.MILLISECOND, -milliSecs);

                        // zero the minutes and add an hour
                        if ( start.get(Calendar.MINUTE) > 0 ) {
                            start.set(Calendar.MINUTE, 0);
                            start.add(Calendar.HOUR_OF_DAY, 1);
                        }
                    }           
                }

                if ( oldData && start.after(now) ) {
                    start.add(Calendar.HOUR_OF_DAY, -24);
                }

                if ( x >= 0 && y >= 0 ) 
                    loc = new Point(x,y);
                if ( width > 0 && height > 0 )
                    size = new Dimension(width,height);

                if ( panelX >= 0 && panelY >= 0 ) 
                    control.setLocation(new Point(panelX, panelY));
                
                if ( panelIconified ) {
                    int state = control.getExtendedState();

                    // Set the iconified bit
                    state |= Frame.ICONIFIED;

                    // Iconify the control
                    control.setExtendedState(state);
                }

                if ( site != null ) {

//                    control.setSiteChannel(site.getName(), channel);
//
//
                    control.setRadar(site.getCompactName());
//System.out.println("plot type: "+plotType);
                    switch ( plotType ) {
                            case TIME:
    //                            control.setTimePlotStart(
    //                                    start.get(Calendar.HOUR_OF_DAY), start.get(Calendar.MINUTE), start.get(Calendar.SECOND),
    //                                    duration);
    //
    //                            switch (plotParameter) {
    //                                case VELOCITY:
    //                                    control.setTimePlotDefaults(oldData,true,false,false,beamNumber,zoom);
    //                                    break;
    //                                case POWER:
    //                                    control.setTimePlotDefaults(oldData,false,true,false,beamNumber,zoom);
    //                                    break;
    //                                case WIDTH:
    //                                    control.setTimePlotDefaults(oldData,false,false,true,beamNumber,zoom);
    //                                    break;
    //                            }
    //
    //                            control.setTimePlotDimensions(loc, size);
    //                            control.doTimePlot();
    //
    //                            // if it's a time plot, and old data is being plotted wait
    //                            // 5 secs before doing anything else. This gives the old data 
    //                            // thread a chance to load data and hopefully avoid an earlier 
    //                            // problem where Windows would run out of sockets if more than
    //                            // one time windows were loading old data simultaneously.
    //                            if ( oldData ) {
    //                                try {
    //                                    Thread.sleep(5000);
    //                                }
    //                                catch(InterruptedException e) {}
    //                            }
System.out.println("ProcessArgs: start: "+start);
                                control.doTimePlot(site.getCompactName(), start.getTime(), duration, channel,
                                        beamNumber, plotParameter, limit, groundScatter, oldData, zoom,
                                        size, loc);

                                break;
                            case FAN:
//                                switch (plotParameter) {
//                                    case VELOCITY:
//                                        control.setFanPlotDefaults(true,false,false);
//                                        break;
//                                    case POWER:
//                                        control.setFanPlotDefaults(false,true,false);
//                                        break;
//                                    case WIDTH:
//                                        control.setFanPlotDefaults(false,false,true);
//                                        break;
//                                }
//
//                                control.setFanPlotDimensions(loc, size);
                                Geographic centre = site.siteCentre(1, 45, 70);
                                Projection proj = new Stereographic(3.5, centre);

//System.out.println("latest version fan plot, for site "+site.getName()+" centre: "+centre);
                                control.doFanPlot(site.getCompactName(), channel, plotParameter, limit, groundScatter,
                                        scale, size, loc, proj);

                                break;

                        }
                }
            
        }
    }
    
    private static String capitalize(String arg) {
        if ( arg == null || arg.length() == 0 )
            return arg;
        
        return arg.substring(0, 1).toUpperCase() + arg.substring(1).toLowerCase();
    }
}
