/*
 * RealTimeDisplay.java
 *
 * Created on 05 December 2007, 13:39
 */

package uk.ac.le.sppg.superdarn.dataServer;

import uk.ac.le.sppg.coords.superdarn.SuperDarnSite;
import uk.ac.le.sppg.coords.superdarn.SuperDarnSiteList;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.*;
import javax.servlet.http.*;
import uk.ac.le.sppg.superdarn.realTimeControl.DisplayParameter;

import uk.ac.le.sppg.superdarn.realTimeJNLP.RunIt;

/**
 *
 * @author nigel
 * @version
 */
public class RealTimeMulti extends HttpServlet {
    
    final String mainClassName = RunIt.class.getName();
    
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        final SimpleDateFormat format = new SimpleDateFormat("HH:mm");

        ArrayList<String> jnlpArguments = new ArrayList<String>();
        String serverUrl = "http://"+request.getServerName()+":"+String.valueOf(request.getServerPort());
        String servletPath = request.getContextPath();
        
        // parse the URL arguments and generate the JNLP parameters
        for(Enumeration e=request.getParameterNames(); e.hasMoreElements();) {
            String parameterList = e.nextElement().toString();

            String jnlpArgument = null;
//System.out.println("parameter list: "+parameterList);

            for(String arg: parameterList.split(",")) {
//System.out.println("arg: "+arg);
                String[] parBits = arg.split(" ");
                
                if ( parBits.length != 2 ) {
                    throw new IllegalArgumentException("Invalid specification, "+arg+ " in "+parameterList);
                }
                
//System.out.println("bits: "+parBits[0]+": "+parBits[1]);
                String parameter = parBits[0];
                String value = parBits[1];
                
                try {
                    DisplayParameter par = DisplayParameter.valueOf(parameter.toUpperCase());

                    String errorMessage = value+" is not a valid value for parameter "+parameter;

                    try {
                        switch(par) {
                            case SERVER:
                              serverUrl = value;
                              break;
                            case START:
                            case DURATION:
                                format.parse(value);
                                break;
                            case BEAM:
                            case ZOOM:
                            case X:
                            case Y:
                            case W:
                            case H:
                            case PANEL_X:
                            case PANX:
                            case PANEL_Y:
                            case PANY:
                                Integer.parseInt(value);
                                break;
                            case CHANNEL:
                                uk.ac.le.sppg.superdarn.fitData.ChannelId.valueOf(value);
                                break;
                            case TYPE:
                                uk.ac.le.sppg.superdarn.realTimeControl.PlotType.value(value);
                                break;
                            case SITE:
                                SuperDarnSite site = null;
                                if ( value.length() == 1 ) 
                                    site = SuperDarnSiteList.getList().getByIdentifier(value.toLowerCase());
                                else if ( value.length() == 3 )
                                    site = SuperDarnSiteList.getList().getByShortName(value.toLowerCase());
                                else
                                    site = SuperDarnSiteList.getList().get(value);
                                if ( site == null ) {
                                    throw new ParseException("invalid site: "+value,0);
                                }
                                break;
                            case PARAMETER:
                            case PAR:
                                uk.ac.le.sppg.superdarn.dataDisplay.PlotParameter.valueOf(value);
                                break;
                            case OLDDATA:
                            case GROUND_SCATTER:
                            case GS:
                            case PANEL_ICONIFIED:
                                if ( ! value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true") ) {
                                    throw new ParseException("boolean or yes/no value required for par: "+par,0);
                                }
                                break;
                        }
                    }
                    catch(ParseException pe) {
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST, errorMessage);
                        return;
                    }
                    catch(IllegalArgumentException ex) {
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST, errorMessage);
                        return;
                    }

                    if ( jnlpArgument != null )
                        jnlpArgument += ","+par+"="+value;
                    else
                        jnlpArgument = par+"="+value;

                }
                catch(IllegalArgumentException ex) {
                    // illegal parameter, send back an HTTP error message
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, parameter+" is not a valid parameter");
                    return;
                }
            }
            
            if ( jnlpArgument != null ) {
                jnlpArguments.add(jnlpArgument);
            }
        }
        
        response.addDateHeader("Last-Modified:", System.currentTimeMillis());
        response.setContentType("application/x-java-jnlp-file");
        
        PrintWriter writer = response.getWriter();
        
        writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        writer.println("<jnlp spec=\"1.0+\" codebase=\""+serverUrl+servletPath+"\">");
        writer.println("    <information>");
        writer.println("        <title>SuperDARN real-time display</title>");
        writer.println("        <vendor>University of Leicester, Space Plasma Physics Group</vendor>");
        writer.println("        <description>Displays real-time data from SuperDARN radars.");
        writer.println("                     Dynamically generated JNLP by RealTimeMulti.</description>");
        writer.println("        <description kind=\"short\">SuperDARN real-time display</description>");
        writer.println("        <homepage href=\"http://"+request.getServerName()+"\"/>");
        writer.println("        <offline-allowed/>");
        writer.println("    </information>");
        writer.println("    <resources>");
        writer.println("        <j2se version=\"1.5+\" href=\"http://java.sun.com/products/autodl/j2se\"/>");
        writer.println("        <jar href=\"realTimeJNLP.jar\" main=\"true\" download=\"eager\"/>");
        writer.println("        <jar href=\"lib/realTimeControl.jar\" download=\"eager\"/>");
        writer.println("        <jar href=\"lib/dataDisplay.jar\" download=\"eager\"/>");
        writer.println("        <jar href=\"lib/colour.jar\" download=\"eager\"/>");
        writer.println("        <jar href=\"lib/fitData.jar\" download=\"eager\"/>");
        writer.println("        <jar href=\"lib/worldMap.jar\" download=\"eager\"/>");
        writer.println("        <jar href=\"lib/coords.jar\" download=\"eager\"/>");
        writer.println("        <jar href=\"lib/display.jar\" download=\"eager\"/>");
        writer.println("        <jar href=\"lib/numbers.jar\" download=\"eager\"/>");
        writer.println("        <jar href=\"lib/coords-superdarn.jar\" download=\"eager\"/>");
        writer.println("        <jar href=\"lib/dataServerAPI.jar\" download=\"eager\"/>");
        writer.println("    </resources>");
        writer.println("    <application-desc main-class=\""+mainClassName+"\">");
        for(String arg:jnlpArguments) {
            writer.println("        <argument>"+arg+"</argument>");
        }
        writer.println("    </application-desc>");
        writer.println("</jnlp>");
        
    }
    
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }
    
    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }
    
    /** Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Generates jnlp for the realTimeDisplay Web Start service";
    }
    // </editor-fold>
    private static String capitalize(String arg) {
        if ( arg == null || arg.length() == 0 )
            return arg;
        
        return arg.substring(0, 1).toUpperCase() + arg.substring(1).toLowerCase();
    }
}
