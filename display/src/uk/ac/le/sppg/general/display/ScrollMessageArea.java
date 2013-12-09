package uk.ac.le.sppg.general.display;

import java.awt.Toolkit;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.Document;

/**
 * A javax.swing implementation of {@link DisplayString} which creates a 
 * scrollable, non-editable, JTextArea and uses that to display strings.
 * <p>
 * An {@link DisplayString#alertString(String) alertString}
 *  is displayed in a
 * {@link JOptionPane} <code>showMessageDialog()</code>.
 * 
 * @author Nigel Wade
 */
public class ScrollMessageArea implements Logger {

    /**
     * action value is to jump.
     * If the action is set to <code>NEWJUMP</code> with {@link #setAction(int)}
     * then new messages will cause the JTextArea to jump to the end
     * to show the new messages.
     */
    public static final int NEWJUMP = 1;
    /**
     * action value is to ring the bell.
     * If the action is set to <code>NEWBEEP</code> with {@link #setAction(int)}
     * then new messages will cause the terminal bell to be rung.
     * The JTextArea will continue to display the current location.
     */
    public static final int NEWBEEP = 2;

    private JScrollPane scrollPane;

    private JTextArea textArea;

    private int action = NEWJUMP;

    private void initScrollMessageArea() {

        textArea.setEditable(false);
        scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

    }

    /**
     * Constructs a <code>ScrollMessageArea</code> which contains a JTextArea.
     * 
     * @param doc
     * document model
     * @param text
     * initial text to display
     * @param rows
     * number of rows in display
     * @param columns
     * number of columns in display
     * <p>
     * See the constructor <code>JTextArea(Document,String,int,int)</code> 
     * in {@link javax.swing.JTextArea} for details.
     * @throws IllegalArgumentException
     * if rows or columns is negative
     */
    public ScrollMessageArea(Document doc, String text, int rows, int columns) 
    throws IllegalArgumentException {

        textArea = new JTextArea(doc, text, rows, columns);
        initScrollMessageArea();

    }

    /**
     * Constructs a <code>ScrollMessageArea</code> which contains a JTextArea.
     * 
     * @param text
     * initial text to display
     * @param rows
     * number of rows in display
     * @param columns
     * number of columns in display
     * <p>
     * See the constructor <code>JTextArea(String,int,int)</code> 
     * in {@link javax.swing.JTextArea} for details.
     * @throws IllegalArgumentException
     * if rows or columns is negative
     */
    public ScrollMessageArea(String text, int rows, int columns) 
    throws IllegalArgumentException {

        textArea = new JTextArea(text, rows, columns);
        initScrollMessageArea();
    }

    /**
     * Constructs a <code>ScrollMessageArea</code> which contains a JTextArea.
     * 
     * @param doc
     * document model
     * <p>
     * See the constructor <code>JTextArea(Document)</code> 
     * in {@link javax.swing.JTextArea} for details.
     */
    public ScrollMessageArea(Document doc) {
        this(doc, null, 0, 0);
    }

    /**
     * Constructs a <code>ScrollMessageArea</code> which contains a JTextArea.
     * 
     * @param text
     * initial text to display
     * <p>
     * See the constructor <code>JTextArea(String)</code> 
     * in {@link javax.swing.JTextArea} for details.
     */
    public ScrollMessageArea(String text) {
        this(text, 0, 0);
    }

    /**
     * Constructs a <code>ScrollMessageArea</code> which contains a JTextArea.
     * 
     * @param rows
     * number of rows in display
     * @param columns
     * number of columns in display
     * <p>
     * See the constructor <code>JTextArea(int,int)</code> 
     * in {@link javax.swing.JTextArea} for details.
     * @throws IllegalArgumentException
     * if rows or columns is negative
     */
    public ScrollMessageArea(int rows, int columns) 
    throws IllegalArgumentException {
        this(null, rows, columns);
    }

    /**
     * Constructs a <code>ScrollMessageArea</code> which contains a JTextArea.
     * <p>
     * See the constructor <code>JTextArea()</code>
     * in {@link javax.swing.JTextArea} for details.
     */

    public ScrollMessageArea() {
        this(null, 0, 0);
    }

    /**
     * Adds an array of messages to the display area.
     * The messages will be added "as is", there will be
     * no interviening white space, newlines etc. added between
     * each message.
     * <p>
     * @param messages
     * the array of messages to be displayed
     * @see #addMessagesln(String[])
     */
    public void addMessages(String[] messages) {
        for (int i = 0; i < messages.length; i++)
            addMessage(messages[i]);
    }

    /**
     * Adds an array of messages to the display area.
     * The messages will be added one per line.
     * <p>
     * @param messages
     * the array of messages to be displayed
     * @see #addMessages(String[])
     */
    public void addMessagesln(String[] messages) {
        for (int i = 0; i < messages.length; i++)
            addMessageln(messages[i]);
    }

    /**
     * Adds a single message to the current line in the display 
     * area without adding any terminating newline.
     * @param message
     * the message to add
     * @see #addMessageln(String)
     */
    public void addMessage(String message) {

        int caret = textArea.getCaretPosition();
        int len = textArea.getDocument().getLength();

        textArea.append(message);

        // if the value is the previous maximum, move the scroll pane to the end
        if (len == caret || action == NEWJUMP) {
            textArea.setCaretPosition(textArea.getDocument().getLength());
        } else {
            // take the relevent action
            if (action == NEWBEEP) {
                Toolkit.getDefaultToolkit().beep();
            }
        }
    }

    /**
     * Adds a single message to the current line in the display 
     * area with a terminating newline.
     * @param message
     * the message to add
     * @see #addMessage(String)
     */
    public void addMessageln(String message) {
        addMessage(message + "\n");
    }

    /**
     * Sets the action to take when a new message is added to the
     * display area.
     * The action should be either {@link #NEWJUMP} or 
     * {@link #NEWBEEP}.
     * If <code>newAction</code> is neither then no action will be taken when new
     * messages are added.
     * @param newAction
     * the action to be taken.
     */
    public void setAction(int newAction) {
        action = newAction;
    }

    /**
     * Gets the <code>JScrollPane</code> used by the <code>ScrollMessageArea</code>.
     * 
     * @return
     * the scroll pane.
     */
    public JScrollPane getScrollPane() {
        return scrollPane;
    }

    /* (non-Javadoc)
     * @see display.DisplayString#displayString(java.lang.String)
     */
    public void displayString(String message) {
        addMessage(message);
    }

    public void displayStringLn(String message) {
        addMessageln(message);
    }
    /* (non-Javadoc)
     * @see display.DisplayString#displayStringArray(java.lang.String[])
     */
    public void displayStringArray(String[] messages) {
        addMessages(messages);
    }

    public void displayStringArrayLn(String[] messages) {
        addMessagesln(messages);
    }
    /**
     * Displays the message in a 
     * {@link JOptionPane} <code>showMessageDialog()</code>.
     * @param message
     * the alert message which is to be displayed.
     * @see JOptionPane#showMessageDialog(Component,Object,String,int)
     */
    public void alertString(String message) {
        JOptionPane.showMessageDialog(textArea, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }

}
