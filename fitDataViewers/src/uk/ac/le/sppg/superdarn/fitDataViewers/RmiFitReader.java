/*
 * Created on 18-May-2004
 */
package uk.ac.le.sppg.superdarn.fitDataViewers;

import uk.ac.le.sppg.coords.superdarn.SuperDarnSiteList;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashMap;
import uk.ac.le.sppg.superdarn.rmiServer.server.RmiServerInterface;
import uk.ac.le.sppg.superdarn.fitData.ChannelId;
import uk.ac.le.sppg.superdarn.fitData.FitacfData;

/**
 * @author Nigel Wade
 */
public class RmiFitReader {

            
    public static void main(String[] args) {

        SuperDarnSiteList siteList = SuperDarnSiteList.getList();
                        
        HashMap<String,RmiServerInterface> instanceMap = new HashMap<String,RmiServerInterface>();
        
        if (args.length != 1) {
            System.err.print("RmiFitReader requires 1 argument,");
            System.err.println(" the server on which the realtime RMI data server is running ");
            System.exit(1);
        }

        try {
            String[] rmiServerNames = Naming.list("//" + args[0]);
//System.out.println("got "+rmiServerNames.length+" rmi server names");
            for (String serverName: rmiServerNames) {
//                System.out.println(serverName);
                if ( serverName.contains("rmiServer")) {
                    System.out.println("Attempt bind to server for " + serverName);
                    try {
                        bindRegistrySite(serverName, instanceMap);
                    } catch (NotBoundException e) {
                        System.out.println("Unable to bind to server for site " + serverName + ", but it's in the registry list");
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

//        Set keys = instanceMap.keySet();
//        Iterator i = keys.iterator();
        long date = 0;
        for(String site: instanceMap.keySet()) {
//            String site = (String) i.next();
            System.out.println("Data for site: " + site);
            RmiServerInterface fl = instanceMap.get(site);

            try {
                ChannelId ch[] = fl.channels();
                System.out.println("  channels:");
                for (int j = 0; j < ch.length; j++) {
                    System.out.print("      " + ch[j]);
                    FitacfData fit = fl.latest(ch[j]);
                    if ( fit == null ) {
                        System.out.println(" null");
                    }
                    else {
                        System.out.print(" fit rev " + fit.radarParms.revision.major + "." + fit.radarParms.revision.minor);
                        System.out.print(": " + siteList.getById(fit.radarParms.stationId).getName());
                        System.out.print(": " + fit.radarParms.date);
                        date = fit.radarParms.date.getTime();
                        System.out.println();
                    }
                }

            } catch (RemoteException e) {
                System.out.println("Remote exception");
                e.printStackTrace();
            }

        }

        try {
            while(true) {
                for(String site: instanceMap.keySet()) {
                    RmiServerInterface fl = instanceMap.get(site);

                    ChannelId ch[] = fl.channels();
                    for (int j = 0; j < ch.length; j++) {
                        System.out.print("  " + ch[j]);
                        FitacfData fit = fl.next(ch[j], date, 10);
                        if ( fit == null ) {
                            System.out.print(" null");
                        }
                        else {
                          System.out.print(": " + siteList.getById(fit.radarParms.stationId).getName());
                          System.out.print(": " + fit.radarParms.date);
                          date = fit.radarParms.date.getTime();
                        }
                    }
                    System.out.println();
                }
            }

        } catch (RemoteException e) {
            System.out.println("Remote exception");
            e.printStackTrace();
        }
    }

    static RmiServerInterface getSiteBinding(String site, HashMap<String,RmiServerInterface> map) 
            throws NotBoundException, RemoteException, MalformedURLException {

        RmiServerInterface result;

        if (!map.containsKey(site)) {
            result = bindRegistrySite(site, map);
        } else {
            result = map.get(site);
        }

        return result;


    }

    static RmiServerInterface bindRegistrySite(String site, HashMap<String,RmiServerInterface> map) 
            throws NotBoundException, RemoteException, MalformedURLException {

        RmiServerInterface server;

        server = (RmiServerInterface) Naming.lookup(site);
        synchronized (map) {
            map.put(site, server);
            System.out.println("Got server for " + site);
        }


        return server;
    }
}
