package uk.ac.le.sppg.general.display;

import java.awt.Toolkit;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import uk.ac.le.sppg.general.numbers.UnsignedInt16;
import uk.ac.le.sppg.general.numbers.UnsignedInt32;
import uk.ac.le.sppg.general.numbers.UnsignedInt8;

/**
 * Extends JTextField to provide a field which will only accept 
 * integer numbers of several class types.
 * 
 * @author Nigel Wade
 */
public class IntegerField extends JTextField {
    private static final long serialVersionUID = 0x5253505047000013L;
    
    private Toolkit toolkit;
    private NumberFormat integerFormatter;

    private Class integerClass = Long.class;
    

    /**
     * Creates a IntegerField with initial value and width specified. 
     * The integer value will be restricted according to the limits
     * of the integer type Class.
     * <p>
     * The supporte integer class types are:
     * Short, Integer, Long, 
     *  UnsignedInt8, UnsignedInt16, UnsignedInt32.
     * Any other type will result in a ClassNotSupportedException
     * being thrown.
     * @param value
     * the initial value for the field
     * @param columns
     * the number of columns to display in the field
     * @param Class
     * the class of integer which will accepted
     * @throws IllegalArgumentException
     * if the Class c is not supported by IntegerField.
     */
    public IntegerField(long value, int columns, Class c) 
    throws IllegalArgumentException {

        super(columns);

        if (c != Short.class &&
            c != Integer.class &&
            c != Long.class &&
            c != UnsignedInt8.class &&
            c != UnsignedInt16.class &&
            c != UnsignedInt32.class ) {
            throw new IllegalArgumentException( "Class: "+c.getName()+" cannot be used in an IntegerField ");
        }

        init(value);
        integerClass = c;
    }

    /**
     * Creates a IntegerField with initial value. 
     * The integer value will be restricted according to the limits
     * of the integer type Class.
     * <p>
     * The supporte integer class types are:
     * Short, Integer, Long, 
     *  UnsignedInt8, UnsignedInt16, UnsignedInt32.
     * Any other type will result in a ClassNotSupportedException
     * being thrown.
     * @param value
     * the initial value for the field
     * @param Class
     * the class of integer which will accepted
     * @throws IllegalArgumentException
     * if the Class c is not supported by IntegerField.
     */
    public IntegerField(long value, Class c) 
    throws IllegalArgumentException {

        if (c != Short.class &&
            c != Integer.class &&
            c != Long.class &&
            c != UnsignedInt8.class &&
            c != UnsignedInt16.class &&
            c != UnsignedInt32.class ) {
            throw new IllegalArgumentException( "Class: "+c.getName()+" cannot be used in an IntegerField ");
        }

        init(value);
        integerClass = c;
    }

    /**
     * Creates a IntegerField with initial value.
     * The class of integer will be Long.
     * @param value
     * the initial value for the field
     */
    public IntegerField(long value) {
        init(value);
    }

    private void init(long value) {
        toolkit = Toolkit.getDefaultToolkit();
        //integerFormatter = NumberFormat.getNumberInstance( Locale.UK );
        //if ( integerFormatter == null )
        integerFormatter = NumberFormat.getNumberInstance(Locale.US);
        integerFormatter.setParseIntegerOnly(true);
        setValue(value);
    }

    /**
     * Returns the current value of the field
     * @return
     * The long value of the field
     */
    public long getValue() {
        long retVal = 0;
        try {
            retVal = integerFormatter.parse(getText()).longValue();
        } catch (ParseException e) {
            // This should never happen because insertString allows
            // only properly formatted data to get in the field.
            toolkit.beep();
        }
        return retVal;
    }

    /**
     * Sets the value of the field
     * @param value
     * the new value of the field
     * @throws NumberFormatException
     * if the new value is not within the allowed limits for the
     * integer class of the field.
     */
    public void setValue(long value) throws NumberFormatException {

        if (integerClass == Short.class
            && value > Short.MAX_VALUE
            || integerClass == Short.class
            && value < Short.MIN_VALUE
            || integerClass == Integer.class
            && value > Integer.MAX_VALUE
            || integerClass == Integer.class
            && value < Integer.MIN_VALUE
            || integerClass == Long.class
            && value > Long.MAX_VALUE
            || integerClass == Long.class
            && value < Long.MIN_VALUE
            || integerClass == UnsignedInt8.class
            && value > UnsignedInt8.MAX_VALUE
            || integerClass == UnsignedInt8.class
            && value < UnsignedInt8.MIN_VALUE
            || integerClass == UnsignedInt16.class
            && value > UnsignedInt16.MAX_VALUE
            || integerClass == UnsignedInt16.class
            && value < UnsignedInt16.MIN_VALUE
            || integerClass == UnsignedInt32.class
            && value > UnsignedInt32.MAX_VALUE
            || integerClass == UnsignedInt16.class
            && value < UnsignedInt32.MIN_VALUE)
            throw new NumberFormatException();

        setText(integerFormatter.format(value));
    }

    protected Document createDefaultModel() {
        return new IntegerDocument();
    }

    protected class IntegerDocument extends PlainDocument {
        private static final long serialVersionUID = 0x5253505047000014L;
        
        public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {

            StringBuffer buffer = new StringBuffer(getText(0, getLength()));

            buffer.insert(offs, str);

            buffer.toString();

            try {
                Number n = integerFormatter.parse(str);

                long l = n.longValue();

                if (integerClass == Short.class
                    && l > Short.MAX_VALUE
                    || integerClass == Short.class
                    && l < Short.MIN_VALUE
                    || integerClass == Integer.class
                    && l > Integer.MAX_VALUE
                    || integerClass == Integer.class
                    && l < Integer.MIN_VALUE
                    || integerClass == Long.class
                    && l > Long.MAX_VALUE
                    || integerClass == Long.class
                    && l < Long.MIN_VALUE
                    || integerClass == UnsignedInt8.class
                    && l > UnsignedInt8.MAX_VALUE
                    || integerClass == UnsignedInt8.class
                    && l < UnsignedInt8.MIN_VALUE
                    || integerClass == UnsignedInt16.class
                    && l > UnsignedInt16.MAX_VALUE
                    || integerClass == UnsignedInt16.class
                    && l < UnsignedInt16.MIN_VALUE
                    || integerClass == UnsignedInt32.class
                    && l > UnsignedInt32.MAX_VALUE
                    || integerClass == UnsignedInt16.class
                    && l < UnsignedInt32.MIN_VALUE)
                    throw new ParseException("Out of range", str.length());

                super.insertString(offs, str, a);
            } catch (ParseException e) {
                toolkit.beep();
            }
        }

    }

}
