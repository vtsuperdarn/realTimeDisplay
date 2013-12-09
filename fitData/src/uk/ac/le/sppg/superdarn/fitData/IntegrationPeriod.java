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
public class IntegrationPeriod implements Serializable, Cloneable {
    private static final long serialVersionUID = 0x1L;

    int seconds;
    int microSeconds;
}
