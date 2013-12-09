/*
 * SiteDetails.java
 *
 * Created on 19 November 2007, 14:48
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.ac.le.sppg.coords;

/**
 *
 * @author nigel
 */
public class SiteDetails {
    
    /**
     * the <code>Geographic</code> location of the site.
     */
    private final Geographic geographic;
    /**
     * the <code>Geocentric</code> location of the site.
     */
    private final Geocentric geocentric;
    /**
     * the <code>TransformMatrix</code> to convert from local
     * to <code>Geocentric</code> coordinates.
     */
    private final TransformMatrix localToGeocentric;
    /**
     * the <code>TransformMatrix</code> to convert from
     * <code>Geocentric</code> to local oordinates.
     */
    private final TransformMatrix geocentricToLocal;

    public SiteDetails( Geographic geographic) {
        this.geographic = geographic;

        geocentric = geographic.toGeocentric();

        localToGeocentric = new TransformMatrix();
        geocentricToLocal = new TransformMatrix();
        makeTransformMatrices();

    }

    public SiteDetails(Geocentric geocentric) {
        this.geocentric = geocentric;

        geographic = geocentric.toGeographic();

        localToGeocentric = new TransformMatrix();
        geocentricToLocal = new TransformMatrix();
        makeTransformMatrices();

    }

    private void makeTransformMatrices() {

        double clat = Math.cos(geographic.latRadians);
        double slat = Math.sin(geographic.latRadians);
        double clon = Math.cos(geographic.lonRadians);
        double slon = Math.sin(geographic.lonRadians);

        /* matrix for geocentric to local cartesian conversion. */
        geocentricToLocal.x[0] = slat * clon;
        geocentricToLocal.x[1] = slat * slon;
        geocentricToLocal.x[2] = -clat;
        geocentricToLocal.y[0] = -slon;
        geocentricToLocal.y[1] = clon;
        geocentricToLocal.y[2] = 0;
        geocentricToLocal.z[0] = clat * clon;
        geocentricToLocal.z[1] = clat * slon;
        geocentricToLocal.z[2] = slat;

        /* matrix for local cartesian to geocentric conversion. */
        localToGeocentric.x[0] = slat * clon;
        localToGeocentric.x[1] = -slon;
        localToGeocentric.x[2] = clat * clon;
        localToGeocentric.y[0] = slat * slon;
        localToGeocentric.y[1] = clon;
        localToGeocentric.y[2] = clat * slon;
        localToGeocentric.z[0] = -clat;
        localToGeocentric.z[1] = 0;
        localToGeocentric.z[2] = slat;
    }

    public Geocentric getGeocentric() {
        return geocentric;
    }

    public TransformMatrix getGeocentricToLocal() {
        return geocentricToLocal;
    }

    public Geographic getGeographic() {
        return geographic;
    }

    public TransformMatrix getLocalToGeocentric() {
        return localToGeocentric;
    }
    

}
