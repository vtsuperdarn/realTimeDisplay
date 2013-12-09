package uk.ac.le.sppg.general.display;

/**
 * Interface for displaying strings.
 * 
 * This interface can be used by any class which wants to output
 * trace or debugging information. It is up to any class which 
 * uses objects of that class to provide it with a destination
 * for that output by implementing this interface.
 * 
 * @author Nigel Wade
 */
public interface Logger {
    /**
     * Method to display a single string
     * @param message
     * the message to be displayed
     */
    public void displayString( String message );
    public void displayStringLn(String message );
    /**
     * Method to display an array of strings.
     * @param messages
     * the array of strings to be displayed.
     * 
     */
    public void displayStringArray( String[] messages );
    public void displayStringArrayLn(String[] messages );
    /**
     * Method to display a string, which may be highlighted
     * in some way to indicate that it is an alert.
     * @param message
     * An important messsage to be displayed.
     */
    public void alertString( String message );

}
