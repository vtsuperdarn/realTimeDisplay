package uk.ac.le.sppg.coords.superdarn;

/*GeoMap.java
  Author R.J.Barnes
  =================*/

/*
 $License$
*/


/* 
 $Log: GeoMap.java,v $
 Revision 1.4  2001/10/11 13:13:50  barnes
 Added License Condition Tag.

 Revision 1.3  1999/12/15 20:54:44  barnes
 Modificaitons to include the receiver rise time in the calculation.

 Revision 1.2  1999/11/11 22:58:28  barnes
 Modifications for fille coastlines.

 */


import uk.ac.le.sppg.coords.Geographic;
import uk.ac.le.sppg.coords.GeneralSiteList;

public class GeoMap {

  static double rho, lat, lon;

  private static double cosd(double d) { 
   return Math.cos(d*Math.PI/180.0); 
  }

  private static double sind(double d) { 
    return Math.sin(d*Math.PI/180.0); 
  }

  private static double tand(double d) {
    return Math.tan(d*Math.PI/180.0);
  }

  private static double acosd(double x) {
    return Math.acos(x)*180.0/Math.PI;
  }
  
  private static double asind(double x) {
    return Math.asin(x)*180.0/Math.PI;
  }

  private static double atand(double x) { 
    return Math.atan(x)*180.0/Math.PI; 
  }

  private static double atan2d(double x,double y) {
     return Math.atan2(x,y)*180.0/Math.PI; 
  }

  private static double slantRange(int lagfr,int smsep,
		   double rxris,double range_edge,
		   int range_gate) {
    return (lagfr-rxris+(range_gate-1)*smsep+range_edge)*0.15;
  }

  private static void fldpnt(double rrho,double rlat,double rlon,double ral,
		      double rel,double r) {
  
     double rx,ry,rz,sx,sy,sz,tx,ty,tz;
     double sinteta;
  
     /* convert from global spherical to global cartesian*/

     sinteta=sind(90.0-rlat);
     rx=rrho*sinteta*cosd(rlon);
     ry=rrho*sinteta*sind(rlon);
     rz=rrho*cosd(90.0-rlat);

     sx=-r*cosd(rel)*cosd(ral);
     sy=r*cosd(rel)*sind(ral);
     sz=r*sind(rel);

     tx  =  cosd(90.0-rlat)*sx + sind(90.0-rlat)*sz;
     ty  =  sy;
     tz  = -sind(90.0-rlat)*sx + cosd(90.0-rlat)*sz;
     sx  =  cosd(rlon)*tx - sind(rlon)*ty;
     sy  =  sind(rlon)*tx + cosd(rlon)*ty;
     sz  =  tz;

     tx=rx+sx;
     ty=ry+sy;
     tz=rz+sz;

     /* convert from cartesian back to global spherical*/
     rho=Math.sqrt((tx*tx)+(ty*ty)+(tz*tz));
     lat=90.0-acosd(tz/(rho));
     if ((tx==0) && (ty==0)) lon=0;
     else lon=atan2d(ty,tx);
  }

  private static double geocnvrt(double gdlat,double gdlon,
			  double xal,double xel) {

    double kxg,kyg,kzg,kxr,kyr;
    double rlat,del;
    double a=6378.16;
    double b;

    b=a*(1.0-1.0/298.25);

    kxg=cosd(xel)*sind(xal);
    kyg=cosd(xel)*cosd(xal);
    kzg=sind(xel);

    rlat=atand( (b*b)/(a*a)*tand(gdlat));
    del=gdlat-rlat;

    kxr=kxg;
    kyr=kyg*cosd(del)+kzg*sind(del);
    return atan2d(kxr,kyr);
  }

  private static void fldPnth(double gdlat,double gdlon,
               double psi,double bore,double fh,
               double r) {

    double rrad,rlat,rlon;
    double tan_azi,azi,rel,xel,fhx,xal,rrho,ral,xh;
    double frad;  
    double a=6378.16;
    double b,e2;

    b=a*(1.0-1.0/298.25);
    e2=(a*a)/(b*b)-1;
 
    if (fh<=150) xh=fh;
    else {
      if (r<=600) xh=115;
      else if ((r>600) && (r<800)) xh=(r-600)/200*(fh-115)+115;
      else xh=fh;
    }

    if (r<150) xh=(r/150.0)*115.0;

    rlat=atand( (b*b) / (a*a) *tand(gdlat));
    rlon=gdlon;
    if (rlon>180) rlon=rlon-360;
    rrad=a/Math.sqrt(1.0+e2*sind(rlat)*sind(rlat));
    rrho=rrad;
    frad=rrad;
 

    do {
      rho=frad+xh;
  
      rel=asind( ((rho*rho) - (rrad*rrad) - (r*r)) / (2*rrad*r));
      xel=rel;
      if (((cosd(psi)*cosd(psi))-(sind(xel)*sind(xel)))<0) tan_azi=1e32;
      else tan_azi=Math.sqrt( (sind(psi)*sind(psi))/
                ((cosd(psi)*cosd(psi))-(sind(xel)*sind(xel))));
      if (psi>0) azi=atand(tan_azi)*1.0;
      else azi=atand(tan_azi)*-1.0;
      xal=azi+bore;
      ral=geocnvrt(gdlat,gdlon,xal,xel);

      fldpnt(rrho,rlat,rlon,ral,rel,r);

      frad=a/Math.sqrt(1.0+e2*sind(lat)*sind(lat));

      fhx=rho-frad; 
    } while(Math.abs(fhx-xh) > 0.5);
  } 

  /**
   * GeoMap.geo calculates the <code>Geographic</code> location of a range cell
   * for a specified radar.
   * The location of the point is specified  by the beam number, gate number and 
   * the start range and range increments.
   * The location of the centre of the gate or the edge of the gate can be calculated.
   * @param centre
   * If <code>true</code> the centre of the range cell will be calculated, otherwise
   * the corner nearest the radar/boresite will be calculated.
   * @param site
   * the radar site for which the range cell should be calculated. 
   * @param firstRange
   * the distance to the first range cell in km.
   * @param rangeSeparation
   * the separation of the range cells in km.
   * @param rxRise
   * the receiver rise time in uS. If 0 the default for the radar will be used.
   * @param beamNumber
   * the beam number
   * @param gateNumber
   * the gate number
   * @return
   * the <code>Geographic</code> location of the range cell.
   */

public synchronized static Geographic geo(boolean centre, SuperDarnSite site,int firstRange,
			  int rangeSeparation,int rxRise,int beamNumber,int gateNumber) {
    double psi,d,rx;
    double re=6356.779;
    int lagfr=20*firstRange/3;
    int smsep=20*rangeSeparation/3;
    int height=0;

    double bm_edge=0;
    double range_edge=0;

    if (site==null) return null;
    if ((firstRange==0) || (rangeSeparation==0)) return null; 
    if ( centre ) {
      bm_edge=site.getBeamSeparation()*0.5;
      range_edge=-0.5*smsep;
    }

    if (rxRise==0) rx=site.getRxRiseTime();
    else rx=rxRise;

//    psi=site.getBeamSeparation()*(beamNumber-7.5)+bm_edge;
    psi=site.getBeamSeparation()*(beamNumber-site.getMaxBeams()/2.0)+bm_edge;
    d=slantRange(lagfr,smsep,rx,range_edge,gateNumber+1);
    if (height < 90) height=
      (int) (-re+Math.sqrt((re*re)+2*d*re*sind(height)+(d*d)));
    fldPnth(site.getGeographic().latitude,site.getGeographic().longitude,psi,site.getBoreSite(),height,d);
    return new Geographic(lat,lon, height*1000.0); 
  }
}

 
