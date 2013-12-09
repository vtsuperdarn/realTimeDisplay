package uk.ac.le.sppg.superdarn.dataServer;

import uk.ac.le.sppg.superdarn.dataServer.*;
import uk.ac.le.sppg.general.display.Logger;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.GenericServlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import uk.ac.le.sppg.superdarn.rmiServer.server.RmiServerInterface;
import uk.ac.le.sppg.superdarn.fitData.ChannelId;
import uk.ac.le.sppg.superdarn.fitData.FitacfData;

/**
 * The servlet which makes remote data available.
 *
 * @author Nigel Wade
 */
public class FitRemoteServlet extends GenericServlet implements FitRemote2 {
    
    private static final long serialVersionUID = 0x5253505047000022L;
    
    HashMap<String, RmiServerInterface> instanceMap = new HashMap<String,RmiServerInterface>();
    final boolean trace = false;
    
    // this implements the interface DisplayString which is used by QnxNetInterface
    // for logging error and status messages
    private class ServletLogger implements Logger {
        
        PrintStream outputStream = System.out;
        
        public void displayString(String message) {
            log(message);
        }
        public void displayStringLn(String message) {
             displayString(message);
        }
       
        public void displayStringArray(String[] messages) {
            for (int i = 0; i < messages.length; i++)
                log(messages[i]);
        }
        
        public void displayStringArrayLn(String[] messages) {
            displayStringArray(messages);
        }

            public void alertString(String message) {
            log(message);
        }
        
        public PrintStream getStream() {
            return outputStream;
        }
    }
    
    //
    // class variables
    //
    
    String servletName = "dataServer";
    ChannelId[] channels;
    
    private ServletConfig config = null;
    private ServletContext context = null;
    
    private final Date initialDate = new Date();
    
    
    // the logger used by SperNetInterface
    private ServletLogger logString = new ServletLogger();
    
    
    //
    // private methods
    //
    
    // Registry methods
    
    protected String getRegistryName() {
        
        return servletName;
    }
    
    // servlet context methods
    
    public ServletConfig getServletConfig() {
        return config;
    }
    
    public ServletContext getServletContext() {
        return config.getServletContext();
    }
    
    public String getInitParameter(String name) {
        return config.getInitParameter(name);
    }
    
    public String getServletInfo() {
        return "dataServer.FitRemoteServlet; Version 2.0; Author: Nigel Wade; Date: 2004 May 18";
    }
    
    public synchronized void log(String msg) {
        if (context == null) {
            System.out.println("FitRemote." + servletName + ": " + msg);
        } else {
            context.log("FitRemote." + servletName + ": " + msg);
        }
    }
    
    // this method is run once, when the servlet is initialized.
    public void init(ServletConfig config) throws ServletException {
        
        super.init(config);
        
        this.config = config;
        context = getServletContext();
        
        try {
//            String[] rmiServerNames = Naming.list("//localhost");
            for( String rmiServerName:Naming.list("//localhost")) {
//              System.out.println("RMI server: "+rmiServerName);
                if ( rmiServerName.endsWith(".rmiServer_"+RmiServerInterface.versionString)) {
                    try {
                        bindRegistrySite(rmiServerName);
                    } catch( NotBoundException e) {
                        log("Unable to bind to RMI server "+rmiServerName +", but it's in the registry list");
                    }
                }
            }
        } catch( RemoteException e ) {
            throw new ServletException("Unable to contact RMI registry");
        } catch( MalformedURLException e ) {
            throw new ServletException("Malfomed registry URL");
        }
        
        log("initialized at: " + initialDate);
        
    }
    
    // this method is executed each time a request is received from a client
    // at the moment it only responds to HTTP requests, but this will change in future
    // so it can handle applet comms using serialized objects
    public void service(ServletRequest request, ServletResponse response)
    throws ServletException, IOException {
        
        if (request.getContentLength() > 0
                && "application/octet-stream".equals(request.getContentType())) {
            
            Object result = null;
            
            ObjectInputStream inputStream =
                    new ObjectInputStream(request.getInputStream());
            ObjectOutputStream outputStream =
                    new ObjectOutputStream(response.getOutputStream());
            
            try {
                // get the method.
                Object o = inputStream.readObject();
                Method method;
                
//                System.out.println(o);
                
                // if it's not a Method return an exception.
                if (!(o instanceof FitRemote2.Method)) {
                    throw new IOException("first parameter not a Method");
                }
                
                method = (Method) o;
                
                if (method == Method.GETTIME) {
                    o = inputStream.readObject();
                    if (!(o instanceof String)) {
                        throw new IOException("site must be String");
                    }
                    String site = (String) o;
                    
                    o = inputStream.readObject();
                    if (!(o instanceof String)) {
                        throw new IOException("channel must be String");
                    }
                    ChannelId channel = (ChannelId) o;
                    
                    o = inputStream.readObject();
                    if (!(o instanceof Long)) {
                        throw new IOException("time for GETTIME must be Long");
                    }
                    long time = ((Long) o).longValue();
                    
                    result = get(site, channel, time);
                } else if (method == Method.LATEST) {
                    o = inputStream.readObject();
                    if (!(o instanceof String)) {
                        throw new IOException("site must be String");
                    }
                    String site = (String) o;
                    
                    o = inputStream.readObject();
                    if (!(o instanceof ChannelId)) {
                        throw new IOException("channel must be ChannelId");
                    }
                    ChannelId channel = (ChannelId) o;
                    
                    result = latest(site, channel);
                } else if (method == Method.LATESTBEAM) {
                    o = inputStream.readObject();
                    if (!(o instanceof String)) {
                        throw new IOException("site must be String");
                    }
                    String site = (String) o;
                    
                    o = inputStream.readObject();
                    if (!(o instanceof ChannelId)) {
                        throw new IOException("channel must be ChannelId");
                    }
                    ChannelId channel = (ChannelId) o;
                    
                    o = inputStream.readObject();
                    if (!(o instanceof Integer)) {
                        throw new IOException("beam for LATESTBEAM must be Integer");
                    }
                    int beam = ((Integer) o).intValue();
                    
                    result = latest(site, channel, beam);
                } else if (method == Method.NEXTTIMEBEAMTIMEOUT) {
                    o = inputStream.readObject();
                    if (!(o instanceof String)) {
                        throw new IOException("site must be String");
                    }
                    String site = (String) o;
                    
                    o = inputStream.readObject();
                    if (!(o instanceof ChannelId)) {
                        log(o.getClass().toString());
                        throw new IOException("channel must be ChannelId");
                    }
                    ChannelId channel = (ChannelId) o;
                    
                    o = inputStream.readObject();
                    if (!(o instanceof Long)) {
                        throw new IOException("time for NEXTTIMEBEAMTIMEOUT must be Long");
                    }
                    long time = ((Long) o).longValue();
                    
                    o = inputStream.readObject();
                    if (!(o instanceof Integer)) {
                        throw new IOException("beam for NEXTTIMEBEAMTIMEOUT must be Integer");
                    }
                    int beam = ((Integer) o).intValue();
                    
                    o = inputStream.readObject();
                    if (!(o instanceof Integer)) {
                        throw new IOException("timeout for NEXTTIMEBEAMTIMEOUT must be Integer");
                    }
                    int timeout = ((Integer) o).intValue();
                    
                    result = next(site, channel, time, beam, timeout);
                } else if (method == Method.NEXTTIMETIMEOUT) {
                    o = inputStream.readObject();
                    if (!(o instanceof String)) {
                        throw new IOException("site must be String");
                    }
                    String site = (String) o;
                    
                    o = inputStream.readObject();
                    if (!(o instanceof ChannelId)) {
                        log(o.getClass().toString());
                        throw new IOException("channel must be ChannelId");
                    }
                    ChannelId channel = (ChannelId) o;
                    
                    o = inputStream.readObject();
                    if (!(o instanceof Long)) {
                        throw new IOException("time for NEXTTIMETIMEOUT must be Long");
                    }
                    long time = ((Long) o).longValue();
                    
                    o = inputStream.readObject();
                    if (!(o instanceof Integer)) {
                        throw new IOException("timeout for NEXTTIMETIMEOUT must be Integer");
                    }
                    int timeout = ((Integer) o).intValue();
                    result = next(site, channel, time, timeout);
                } else if (method == Method.PREVIOUSTIME) {
                    o = inputStream.readObject();
                    if (!(o instanceof String)) {
                        throw new IOException("site must be String");
                    }
                    String site = (String) o;
                    
                    o = inputStream.readObject();
                    if (!(o instanceof ChannelId)) {
                        throw new IOException("channel must be ChannelId");
                    }
                    ChannelId channel = (ChannelId) o;
                    
                    o = inputStream.readObject();
                    if (!(o instanceof Long)) {
                        throw new IOException("time for PREVIOUSTIME must be Long");
                    }
                    long time = ((Long) o).longValue();
                    
                    result = previous(site, channel, time);
                } else if (method == Method.PREVIOUSTIMEBEAM) {
                    o = inputStream.readObject();
                    if (!(o instanceof String)) {
                        throw new IOException("site must be String");
                    }
                    String site = (String) o;
                    
                    o = inputStream.readObject();
                    if (!(o instanceof ChannelId)) {
                        throw new IOException("channel must be ChannelId");
                    }
                    ChannelId channel = (ChannelId) o;
                    
                    o = inputStream.readObject();
                    if (!(o instanceof Long)) {
                        throw new IOException("time for PREVIOUSTIMEBEAM must be Long");
                    }
                    long time = ((Long) o).longValue();
                    
                    o = inputStream.readObject();
                    if (!(o instanceof Integer)) {
                        throw new IOException("beam for PREVIOUSTIMEBEAM must be Integer");
                    }
                    int beam = ((Integer) o).intValue();
                    
                    result = previous(site, channel, time, beam);
                } else if (method == Method.OLDEST) {
                    o = inputStream.readObject();
                    if (!(o instanceof String)) {
                        throw new IOException("site must be String");
                    }
                    String site = (String) o;
                    
                    o = inputStream.readObject();
                    if (!(o instanceof ChannelId)) {
                        throw new IOException("channel must be ChannelId");
                    }
                    ChannelId channel = (ChannelId) o;
                    
                    result = oldest(site, channel);
                } else if (method == Method.OLDESTBEAM) {
                    o = inputStream.readObject();
                    if (!(o instanceof String)) {
                        throw new IOException("site must be String");
                    }
                    String site = (String) o;
                    
                    o = inputStream.readObject();
                    if (!(o instanceof ChannelId)) {
                        throw new IOException("channel must be ChannelId");
                    }
                    ChannelId channel = (ChannelId) o;
                    
                    o = inputStream.readObject();
                    if (!(o instanceof Integer)) {
                        throw new IOException("beam for OLDESTBEAM must be Integer");
                    }
                    int beam = ((Integer) o).intValue();
                    
                    result = oldest(site, channel, beam);
                } else if (method == Method.CHECKNET) {
                    result = new Boolean(checkNet());
                } else if (method == Method.GETCHANNELS) {
                    o = inputStream.readObject();
                    if (!(o instanceof String)) {
                        throw new IOException("site must be String");
                    }
                    String site = (String) o;
                    
                    result = channels(site);
                }
                
                //System.out.println( "return object class: "+result.getClass() );
                outputStream.writeObject(result);
                
            } catch (ClassNotFoundException e) {
                log("Class not found exception ");
                outputStream.writeObject(
                        new IOException("class not found unmarshalling parameters"));
            } catch (IOException e) {
                log("IO Exception ");
                e.printStackTrace();
                outputStream.writeObject(e);
            } catch (Exception e) {
                log("Exception");
                e.printStackTrace();
            } finally {
                inputStream.close();
                outputStream.close();
            }
            
            return;
            
        }
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        
        out.println("<html><head><title>FitRemote</title></head>");
        out.println(servletName);
        out.println("</body></html>");
        out.close();
    }
    
    public void destroy() {
        log("destroy method called");
        
    }
    
    /* (non-Javadoc)
     * @see dataServlet.FitRemote2#get(java.lang.String, java.lang.String, long)
     */
    public FitacfData get(String site, ChannelId channel, long time) throws IOException {
        
        try {
            RmiServerInterface local = getSiteBinding(site);
            FitacfData result = local.get(channel, time);
            if ( trace && result != null )
                log(result.radarParms.date+" "+result.radarParms.txFrequency);
            return result;
        } catch( Exception e ) {
            removeSiteBinding(site);
            throw new IOException(e.getMessage());
        }
        
    }
    
    /* (non-Javadoc)
     * @see dataServlet.FitRemote2#next(java.lang.String, java.lang.String, long, int, int)
     */
    public FitacfData next(String site, ChannelId channel, long afterTime, int beamNumber, int timeoutSecs) throws IOException {
        try {
            RmiServerInterface local = getSiteBinding(site);
            FitacfData result = local.next(channel, afterTime, beamNumber, timeoutSecs);
            if ( trace && result != null )
                log(result.radarParms.date+" "+result.radarParms.txFrequency);
            return result;
        } catch( Exception e ) {
            removeSiteBinding(site);
            throw new IOException(e.getMessage());
        }
    }
    
    /* (non-Javadoc)
     * @see dataServlet.FitRemote2#previous(java.lang.String, java.lang.String, long, int)
     */
    public FitacfData previous(String site, ChannelId channel, long beforeTime, int beamNumber) throws IOException {
        try {
            RmiServerInterface local = getSiteBinding(site);
            FitacfData result = local.previous(channel, beforeTime, beamNumber);
            if ( trace && result != null )
                log(result.radarParms.date+" "+result.radarParms.txFrequency);
            return result;
        } catch( Exception e ) {
            removeSiteBinding(site);
            throw new IOException(e.getMessage());
        }
        
    }
    
    /* (non-Javadoc)
     * @see dataServlet.FitRemote2#latest(java.lang.String, java.lang.String, int)
     */
    public FitacfData latest(String site, ChannelId channel, int beamNumber) throws IOException {
        try {
            RmiServerInterface local = getSiteBinding(site);
            FitacfData result = local.latest(channel, beamNumber);
            if ( trace && result != null )
                log(result.radarParms.date+" "+result.radarParms.txFrequency);
            return result;
        } catch( Exception e ) {
            removeSiteBinding(site);
            throw new IOException(e.getMessage());
        }
    }
    
    /* (non-Javadoc)
     * @see dataServlet.FitRemote2#oldest(java.lang.String, java.lang.String, int)
     */
    public FitacfData oldest(String site, ChannelId channel, int beamNumber) throws IOException {
        try {
            RmiServerInterface local = getSiteBinding(site);
            FitacfData result = local.oldest(channel, beamNumber);
            if ( trace && result != null )
                log(result.radarParms.date+" "+result.radarParms.txFrequency);
            return result;
        } catch( Exception e ) {
            removeSiteBinding(site);
            throw new IOException(e.getMessage());
        }
    }
    
    /* (non-Javadoc)
     * @see dataServlet.FitRemote2#next(java.lang.String, java.lang.String, long, int)
     */
    public FitacfData next(String site, ChannelId channel, long afterTime, int timeout) throws IOException {
        try {
            RmiServerInterface local = getSiteBinding(site);
            FitacfData result = local.next(channel, afterTime, timeout);
            if ( trace && result != null )
                log(result.radarParms.date+" "+result.radarParms.txFrequency);
            return result;
        } catch( Exception e ) {
            removeSiteBinding(site);
            throw new IOException(e.getMessage());
        }
    }
    
    /* (non-Javadoc)
     * @see dataServlet.FitRemote2#previous(java.lang.String, java.lang.String, long)
     */
    public FitacfData previous(String site, ChannelId channel, long beforeTime) throws IOException {
        try {
            RmiServerInterface local = getSiteBinding(site);
            FitacfData result = local.previous(channel, beforeTime);
            if (trace &&  result != null )
                log(result.radarParms.date+" "+result.radarParms.txFrequency);
            return result;
        } catch( Exception e ) {
            removeSiteBinding(site);
            throw new IOException(e.getMessage());
        }
    }
    
    /* (non-Javadoc)
     * @see dataServlet.FitRemote2#latest(java.lang.String, java.lang.String)
     */
    public FitacfData latest(String site, ChannelId channel) throws IOException {
        try {
            RmiServerInterface local = getSiteBinding(site);
            FitacfData result = local.latest(channel);
            if ( trace && result != null )
                log(result.radarParms.date+" "+result.radarParms.txFrequency);
            return result;
        } catch( Exception e ) {
            removeSiteBinding(site);
            throw new IOException(e.getMessage());
        }
    }
    
    /* (non-Javadoc)
     * @see dataServlet.FitRemote2#oldest(java.lang.String, java.lang.String)
     */
    public FitacfData oldest(String site, ChannelId channel) throws IOException {
        try {
            RmiServerInterface local = getSiteBinding(site);
            FitacfData result = local.oldest(channel);
            if ( trace && result != null )
                log(result.radarParms.date+" "+result.radarParms.txFrequency);
            return result;
        } catch( Exception e ) {
            removeSiteBinding(site);
            throw new IOException(e.getMessage());
        }
    }
    
    /* (non-Javadoc)
     * @see dataServlet.FitRemote2#checkNet()
     */
    public boolean checkNet() throws IOException {
        return true;
    }
    
    /* (non-Javadoc)
     * @see dataServlet.FitRemote2#channels(java.lang.String)
     */
    public ChannelId[] channels(String site) throws IOException {
        try {
            RmiServerInterface local = getSiteBinding(site);
            return local.channels();
        } catch( Exception e ) {
            removeSiteBinding(site);
            throw new IOException(e.getMessage());
        }
    }
    
    
    
    private RmiServerInterface getSiteBinding( String site ) throws ServletException {
        
        RmiServerInterface result;
        
        try {
            synchronized( instanceMap ) {
                if ( ! instanceMap.containsKey(site) ) {
                    result = bindRegistrySite( site );
                } else {
                    result = instanceMap.get(site);
                }
            }
            
            return result;
        } catch( NotBoundException e) {
            removeSiteBinding(site);
            throw new ServletException( "No server bound for site "+site );
        } catch( RemoteException e ) {
            removeSiteBinding(site);
            throw new ServletException( "Unable to contact RMI registry");
        
       }
    }

    private void removeSiteBinding( String site ) {
        synchronized( instanceMap ) {
            if ( instanceMap.containsKey(site) ) {
                instanceMap.remove(site);
    System.out.println("remove site from map");
            }
        }

    }

    private RmiServerInterface bindRegistrySite(String rmiName) throws NotBoundException,  RemoteException, ServletException {
        
        RmiServerInterface server;
        String[] bits = rmiName.split("/");
        String site = bits[bits.length-1].split("\\.")[0];
System.out.println("bind registry for site: "+rmiName+" :"+site);
        
        try {
            for(String rmiServerName: Naming.list("//localhost") ) {
            
                if ( rmiServerName.endsWith(site+".rmiServer_"+RmiServerInterface.versionString)) {
                    server = (RmiServerInterface)Naming.lookup(rmiServerName);

                    synchronized( instanceMap) {
                        // save the site name as the key, the string "site" contains the full registry name
                        instanceMap.put( site, server );
                    }
                    return server;
                }
            }
        } catch( MalformedURLException e ) {
            throw new ServletException("Malformed URL for registry lookup");
        }
        
        return null;
    }
}