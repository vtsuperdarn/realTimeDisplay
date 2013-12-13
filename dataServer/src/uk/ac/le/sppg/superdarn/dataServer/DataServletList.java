/*
 * Created on 06-Aug-2003
 */
package uk.ac.le.sppg.superdarn.dataServer;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import javax.servlet.GenericServlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import uk.ac.le.sppg.superdarn.rmiServer.server.RmiServerInterface;

/**
 * @author Nigel Wade
 */
public class DataServletList extends GenericServlet {
    
    private static final long serialVersionUID = 0x5253505047000021L;
    
    
    private ServletConfig config = null;
    private ServletContext context = null;
    
    private final Date initialDate = new Date();
    
    String servletName;
    
    
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
        return "dataServer.DataServletList; Version 1.0; Author: Nigel Wade; Date: 2003 Jun 11";
    }
    
    
    public synchronized void log( String msg ) {
        context.log( "DataServletList."+servletName+": "+msg );
    }
    
    // this method is run once, when the servlet is initialized.
    public void init( ServletConfig config )
    throws ServletException {
        
        
        this.config = config;
        context = getServletContext();
        
        servletName = new String(config.getServletName());
        
        
        log( "initialized at: " + initialDate );
        
    }
    
    
    // this method is executed each time a request is received from a client
    // at the moment it only responds to HTTP requests, but this will change in future
    // so it can handle applet comms using serialized objects
    public void service( ServletRequest request, ServletResponse response )
    throws ServletException, IOException {
        
        log( "service request received at: " + initialDate );
        
        ArrayList dataList = getList();
        
        if ( request.getContentLength() > 0 && request.getContentType() != null &&
                request.getContentType().equals("application/octet-stream") ) {
            
            ObjectOutputStream outputStream = new ObjectOutputStream( response.getOutputStream() );
                        
            outputStream.writeObject( dataList );
            
            outputStream.close();
            
        } else {
            response.setContentType( "text/html" );
            PrintWriter out = response.getWriter();
            
            out.println( "<html><head><title>FitRemote</title></head>");
            
            
            for( Iterator i = dataList.iterator(); i.hasNext(); ) {
                String name = (String)i.next();
                out.println( name );
                out.println( "<br>" );
            }
            
            out.println( "</body></html>" );
            out.close();
        }
    }
    
    
    
    public void destroy() {
        log( "destroy method called" );
    }
    
    
    private ArrayList<String> getList()  throws ServletException {
        
        // return the list of data source names
        
        ArrayList<String> result = new ArrayList<String>();

        
        try {
//            String[] rmiServerNames = Naming.list("//localhost");
            
//            for( int i=0; i<rmiServerNames.length; i++ ){
            for(String rmiServerName: Naming.list("//localhost") ) {
                if ( rmiServerName.matches(".*\\.rmiServer_"+RmiServerInterface.versionString)) {
//              System.out.println("entry matches");
                    String[] bits = rmiServerName.split("/");
                    String site = bits[bits.length-1].split("\\.")[0];
                    result.add(site);
                }
            }
        } catch( RemoteException e ) {
            throw new ServletException( "Error contacting RMI registry ");
        } catch( MalformedURLException e ) {
            throw new ServletException( "Error in registry URL");
        }
        return result;
    }
    
}
