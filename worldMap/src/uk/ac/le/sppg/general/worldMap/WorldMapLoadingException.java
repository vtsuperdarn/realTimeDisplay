/*
 * Created on 10-Feb-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package uk.ac.le.sppg.general.worldMap;

/**
 * @author nigel
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WorldMapLoadingException extends Exception {
    private static final long serialVersionUID = 0x5253505047000042L;
    
	public WorldMapLoadingException() {	}
	public WorldMapLoadingException(String message) {
		super(message);
	}
	public WorldMapLoadingException(String message, Throwable cause) {
		super(message, cause);
	}
	public WorldMapLoadingException(Throwable cause) {
		super(cause);
	}
}
