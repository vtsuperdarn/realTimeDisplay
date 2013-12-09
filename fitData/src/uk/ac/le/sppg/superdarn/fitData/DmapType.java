/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.le.sppg.superdarn.fitData;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author nigel
 */
public enum DmapType implements Serializable {
  
  CHAR(1), SHORT(2), INT(3), FLOAT(4), DOUBLE(8), STRING(9), UNKNOWN(-1);

  private static final Map<Integer,DmapType> lookup
        = new HashMap<Integer,DmapType>();

  static {
        for(DmapType s : EnumSet.allOf(DmapType.class))
             lookup.put(s.getCode(), s);
  }

  private int code;

  private DmapType(int code) {
    this.code = code;
  }

  public int getCode() { return code; }

  public static DmapType get(int code) {
    DmapType result = lookup.get(code);
    if ( result == null ) {
      result = UNKNOWN;
    }
    return result;
  }

}
