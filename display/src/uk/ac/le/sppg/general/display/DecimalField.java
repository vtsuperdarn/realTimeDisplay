package uk.ac.le.sppg.general.display;

import java.awt.Toolkit;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import javax.swing.JTextField;
import javax.swing.text.*;

/**
 * Extends JTextField to provide a field which will only accept 
 * decimal numbers.
 * 
 * @author Nigel Wade
 */
public class DecimalField extends JTextField {
    private static final long serialVersionUID = 0x5253505047000011L;
    
    private Toolkit toolkit;
    private NumberFormat decimalFormatter;

    /**
     * Creates a DecimalField with initial value and width specified.
     * @param value
     * the initial value for the field
     * @param columns
     * the number of columns to display in the field
     */
    public DecimalField( double value, int columns ) {
        super( columns );
	
	    ConstructDecimalField( value );
    }

    /**
     * Creates a DecimalField with initial value specified.
     * @param value
     * the initial value for the field
     */
    public DecimalField( double value ) {
        super();
	
	    ConstructDecimalField( value );
    }

    private void ConstructDecimalField( double value ) {
        toolkit = Toolkit.getDefaultToolkit();
        decimalFormatter = NumberFormat.getNumberInstance( Locale.UK );
    	if ( decimalFormatter == null )
    	    decimalFormatter = NumberFormat.getNumberInstance( Locale.US );
        decimalFormatter.setParseIntegerOnly( false );
        setValue( value );
    }

    /**
     * returns the current value of the field
     * @return
     * the current value as a double
     */
    public double getValue() {
        double retVal = 0.0;

        try {
            retVal = decimalFormatter.parse(getText()).doubleValue();
        } catch (ParseException e) {
            // This should never happen because insertString allows
            // only properly formatted data to get in the field.
            Toolkit.getDefaultToolkit().beep();
        }
        return retVal;
    }

    /**
     * Sets the value of the field
     * @param value
     * the value which will replace the current contents
     */
    public void setValue(double value) {
        setText( decimalFormatter.format(value) );
    }

    /**
     * Sets the value of the field
     * @param value
     * the value which will replace the current contents
     */
    public void setValue(int value) {
        setText(decimalFormatter.format(value));
    }

    protected Document createDefaultModel() {
        return (Document) new DecimalDocument();
    }

    protected class DecimalDocument extends PlainDocument {
        private static final long serialVersionUID = 0x5253505047000012L;
        
        public void insertString(int offs, 
                                 String str,
                                 AttributeSet a) 
                throws BadLocationException {

            StringBuffer buffer = new StringBuffer( getText( 0, getLength()) );
    
    	    buffer.insert( offs, str );
    
    	    buffer.toString();
    
    	    try {
                decimalFormatter.parse( str );
        		super.insertString(offs, str, a);
    	    }
    	    catch( ParseException e ) {
    		    toolkit.beep();
    	    }
        }
    }

}
