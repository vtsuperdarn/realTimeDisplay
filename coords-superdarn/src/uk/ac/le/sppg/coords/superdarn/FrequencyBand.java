/*
 * FrequencyBand.java
 *
 * Created on 20 September 2007, 12:23
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.ac.le.sppg.coords.superdarn;

/**
 *
 * @author nigel
 */
// the FrequencyBand class is used to define the frequency allocation
// bands within which some SuperDARN radars have to operate.
// A frequency band is identified by a particular band number, and each
// band has a lower and upper frequency.
public  class FrequencyBand {
    int band;
    int lower;
    int upper;
    /**
     * creates a new instance of a FrequencyBand
     * @param b
     * the integer band number
     * @param lower
     * the lower limit in kHz.
     * @param upper
     * the upper limit in kHz.
     */
    public FrequencyBand(int b, int lower, int upper) {
        this.band = b;
        this.lower = lower;
        this.upper = upper;
    }
    
    /**
     * @return
     * the band number for this FrequencyBand
     */
    public int getBand() { return band;}
    /**
     * @return
     * the lower limit of this FrequencyBand in kHz.
     */
    public int getLower() { return lower;}
    /**
     * @return
     * the upper limit of this FrequencyBand in kHz.
     */
    public int getUpper() { return upper; }

// the frequency allocation bands for the CUTLASS radars
 static FrequencyBand[] finlandBands =
{
    new FrequencyBand(0, 8305, 8335),
    new FrequencyBand(1, 8965, 9040),
    new FrequencyBand(2, 9900, 9985),
    new FrequencyBand(3, 11075, 11275),
    new FrequencyBand(4, 11550, 11600),
    new FrequencyBand(5, 12370, 12415),
    new FrequencyBand(6, 13200, 13260),
    new FrequencyBand(7, 15010, 15080),
    new FrequencyBand(8, 16210, 16360),
    new FrequencyBand(9, 16555, 16615),
    new FrequencyBand(10, 17970, 18050),
    new FrequencyBand(11, 18850, 18865),
    new FrequencyBand(12, 19415, 19680),
    new FrequencyBand(13, 19705, 19755),
    new FrequencyBand(14, 19800, 19990)};

 static FrequencyBand[] icelandBands =
{
    new FrequencyBand(20, 8000, 8195),
    new FrequencyBand(21, 8430, 8850),
    new FrequencyBand(22, 8985, 9395),
    new FrequencyBand(23, 10155, 10655),
    new FrequencyBand(24, 10655, 11175),
    new FrequencyBand(25, 11290, 11450),
    new FrequencyBand(26, 11475, 11595),
    new FrequencyBand(27, 12105, 12235),
    new FrequencyBand(28, 12305, 12510),
    new FrequencyBand(29, 12590, 13280),
    new FrequencyBand(30, 13360, 13565),
    new FrequencyBand(31, 13875, 13995),
    new FrequencyBand(32, 14400, 15015),
    new FrequencyBand(33, 15805, 16365),
    new FrequencyBand(34, 16500, 16685),
    new FrequencyBand(35, 16820, 17475),
    new FrequencyBand(36, 18175, 18770),
    new FrequencyBand(37, 18835, 18885),
    new FrequencyBand(38, 19910, 20000),
    new FrequencyBand(39, 10155, 11175)};


}

