/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.le.sppg.superdarn.fitData;

import java.io.Serializable;

/**
 *
 * @author nigel
 */
public class Origin implements Cloneable, Serializable {
    private static final long serialVersionUID = 0x0001L;

    // code indicating origin of data
    public char code = '\0';
    // ASCII date/time of the creation of the data
    public String time = "";
    // command line or control program which generated the data
    public String command = "";

    public Origin clone() {
      Origin result = new Origin();
      result.code = code;
      result.time = new String(time);
      result.command = new String(command);
      return result;
      
    }
}
