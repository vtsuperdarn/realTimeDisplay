package uk.ac.le.sppg.general.display;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.Timer;

/**
 * Extends JButton to create a button with text which alternates
 * between two colours. The two colours can be set with the methods
 * <code>setForeground</code> and <code>setBlink</code>. The interval
 * between changes in colour can be set in the constructor, the default 
 * is 1 second.
 * <p>
 * Initially the <code>BlinkButton</code> is disabled and works just
 * as a JButton. When enabled the button will alternate the foreground
 * colour between the normal foreground colour and the blink colour.
 * It can be started and stopped with the <code>start()</code> and
 * <code>stop()</code> methods.
 * 
 * @author Nigel Wade
 */

public class BlinkButton extends JButton {
    private static final long serialVersionUID = 0x525350504700000EL;
    
    private Timer blinkTimer;
    private boolean blinked;
    private Color blinkColour = Color.black;
    private Color foreground;

    /**
     * Creates a button with text and default blink interval of 1s.
     * @param text
     * The text to be displayed on the button.
     */
    public BlinkButton(String text) {
        this();
        this.setText(text);
    }

    /**
     * Creates a button with text and sets the blink interval.
     * @param text
     * The text to be displayed on the button.
     * @param interval
     * The number of milliSeconds between changes in colour.
     */
    public BlinkButton(String text, int interval ) {
        this(interval);
        this.setText(text);
    }

    public BlinkButton() {
        super();
        blinkTimer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                blinked = !blinked;
                repaint();
            }
        });
        blinkTimer.setRepeats(true);
        foreground = getForeground();
    }

    /**
     * Creates a <code>BlinkButton</code> which will blink at the specified intervael.
     * @param interval
     * the blink interval in milliseconds.
     */
    public BlinkButton(int interval) {
        super();
        blinkTimer = new Timer(interval, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                blinked = !blinked;
                repaint();
            }
        });
        blinkTimer.setRepeats(true);
        foreground = getForeground();
    }

    /**
     * Sets the foreground colour
     */
    public void setForeground(Color foreground) {
        this.foreground = foreground;
        if (!blinked)
            super.setForeground(foreground);
    }

    /**
     * Sets the blink colour
     * @param blink
     * the colour to blink (alternate) with the foregroundColor
     */
    public void setBlink(Color blink) {
        this.blinkColour = blink;
    }

    public void paintComponent(Graphics g) {

        if (blinked) {
            super.setForeground(foreground);
        } else {
            super.setForeground(blinkColour);
        }
        super.paintComponent(g);
    }

    /**
     * Overrides the default method to also start and stop
     * the <code>BlinkButton</code> when it is enabled/disabled.
     */
    public void setEnabled(boolean enable) {
        super.setEnabled(enable);
        if (enable)
            start();
        else
            stop();
    }

    /**
     * Starts the <code>BlinkButton</code> blinking.
     */
    public void start() {
        blinkTimer.start();
    }
    /**
     * Stops the <code>BlinkButton</code> blinking.
     */
    public void stop() {
        super.setForeground(foreground);
        blinked = false;
        blinkTimer.stop();
    }

}
