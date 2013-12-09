package uk.ac.le.sppg.general.display;

import java.awt.Toolkit;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

/**
 * Extends JTextField so that it will only accept a single character, or an escaped
 * character code.
 * 
 * @author Nigel Wade
 */
public class CharacterField extends JTextField {
    private static final long serialVersionUID = 0x525350504700000FL;
    
    private Toolkit toolkit;

    /**
     * Creates a CharacterField
     * @param value
     * the initial contents of the field
     * @param columns
     * the number of columns in the field
     */
    public CharacterField(char value, int columns) {
        super(columns);
        init(value);
    }

    /**
     * Creates a CharacterField
     * @param value
     * the initial value of the field
     */
    public CharacterField(char value) {
        init(value);
    }

    private void init(char value) {
        toolkit = Toolkit.getDefaultToolkit();
        setValue(value);
    }

    /**
     * Returns the current contents of the field
     * @return
     * the char value of the field.
     */
    public char getValue() {

        char retVal = '\0';

        try {
            // if the string begins with a "\" then parse the rest
            // of the string as a hex digit
            String str = getText();
            if (str.charAt(0) == '\\')
                retVal = (char) Integer.parseInt(str.substring(1), 16);
            else
                retVal = str.charAt(0);

        } catch (NumberFormatException e) {
            // This should never happen because insertString allows
            // only properly formatted data to get in the field.
            toolkit.beep();
        }

        return retVal;
    }

    /**
     * Sets the value of the field
     * @param value
     * the char value to replace the current contents
     */
    public void setValue(char value) {

        setText(String.valueOf(value));
    }

    protected Document createDefaultModel() {
        return (Document) new CharacterDocument();
    }

    protected class CharacterDocument extends PlainDocument {
        private static final long serialVersionUID = 0x5253505047000010L;
        
        public void remove(int offs, int len) throws BadLocationException {

            StringBuffer buffer = new StringBuffer(getText(0, getLength()));

            buffer.delete(offs, offs + len);

            int nChars = buffer.length();
 
            // the new buffer length is > 1 then the first character must be a \

            if (nChars > 1) {
                if (buffer.charAt(0) != '\\') {
                    toolkit.beep();
                    return;
                }

                // try to parse the rest of the string as a hex number
                try {
                    Integer.parseInt(buffer.substring(1), 16);
                } catch (NumberFormatException e) {
                    toolkit.beep();
                    return;
                }
            }

            super.remove(offs, len);

        }

        public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {

            StringBuffer strBuff = new StringBuffer(str);

            // if the first character is a control convert it to a \xx equivalent
            if (Character.isISOControl(strBuff.charAt(0))) {
                char c = strBuff.charAt(0);
                strBuff.setCharAt(0, '\\');
                strBuff.insert(1, Integer.toHexString((int) c));
            }

            // get the current contents in a StringBuffer...
            StringBuffer buffer = new StringBuffer(getText(0, getLength()));

            // ...and insert the new string.
            buffer.insert(offs, strBuff.toString());

            int nChars = buffer.length();

            // the new buffer length is > 1 then the first character must be a \

            if (nChars > 1) {
                if (buffer.charAt(0) != '\\') {
                    toolkit.beep();
                    return;
                }

                // try to parse the rest of the string as a hex number
                try {
                    Integer.parseInt(buffer.substring(1), 16);
                } catch (NumberFormatException e) {
                    toolkit.beep();
                    return;
                }
            }

            super.insertString(offs, strBuff.toString(), a);

        }
    }

}
