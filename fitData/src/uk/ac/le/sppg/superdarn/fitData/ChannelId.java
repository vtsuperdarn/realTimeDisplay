/*
 * ChannelIds.java
 *
 * Created on 05 December 2007, 17:09
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.ac.le.sppg.superdarn.fitData;

/**
 *
 * @author nigel
 */
public enum ChannelId {
   A, B, UNKNOWN;
   
    @Override
   public String toString() {
       if ( name().equals("UNKNOWN") ) {
           return "?";
       }
       
       return name();
   }
    public static ChannelId value(String str) 
    throws IllegalArgumentException {
        return valueOf(str.toUpperCase());
    }
}
