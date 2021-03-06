package adventures.ad.appic.main.custom;

import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.List;

import adventures.ad.appic.app.R;
import adventures.ad.appic.main.activities.MainActivity;
import adventures.ad.appic.main.custom.MessageBox;
import adventures.ad.appic.web.Connection;

/**
 * Created by vivid_000 on 2/1/2015.
 */
public class MapInit extends FragmentActivity {

    private ArrayList<Marker> markers = new ArrayList<Marker>();
    private boolean FirstLocation = true;
    private ArrayList<Polygon> polygons = new ArrayList<Polygon>();
    private MainActivity mainActivity;


    public int mapInit(GoogleMap map) {
        updateMap(map);
        return findMarkers(map.getMyLocation());
    }


    public void initMarkers(GoogleMap mMap, Connection con) {
        Log.e("size: ", con.getLocationlist().size()+"");
        for(int i = 0; i < con.getLocationlist().size(); i++) {
            markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(con.getLocationlist().get(i).getCoordy()), Double.parseDouble(con.getLocationlist().get(i).getCoordx()))).title(con.getLocationlist().get(i).getName())));
        }
     /*   markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(50.938288, 5.348595)).title("PXL gebouw B")));
        markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(50.835884, 5.189746)).title("Ergens in St. Truiden")));
        markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(50.855321, 5.383759)).title("Thuis")));
        markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(50.856450, 5.381367)).title("Random1")));
        markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(50.917033, 5.342780)).title("Random2")));
*/
        for (int i = 0; i < markers.size(); i++) {
            markers.get(i).setVisible(false);
        }
    }

    private void updateMap(GoogleMap mMap) {

        int radius = 5; //radius in km

        if(polygons.size() != 0) {
            polygons.get(0).remove();
        }

        List circle = new ArrayList();

        for(int i = -180; i < 180; i++)
        {
            circle.add(computeOffset(new LatLng(mMap.getMyLocation().getLatitude(), mMap.getMyLocation().getLongitude()), radius, i));
        }

        PolygonOptions polygonOptions = new PolygonOptions()
                .add(new LatLng(85, -180), new LatLng(85, -90), new LatLng(85, 0), new LatLng(85, 90), new LatLng(85, 179.9), new LatLng(-85, 179.9), new LatLng(-85, 90), new LatLng(-85, 0), new LatLng(-85, -90), new LatLng(-85, -180), new LatLng(85, -180))
                .addHole(circle)
                .fillColor(0x50000000)
                .strokeColor(0x50000000)
                .strokeWidth(1);

        polygons.add(0, mMap.addPolygon(polygonOptions));
    }


    public boolean checkLocationService(LocationManager lm) {
        boolean gps_enabled = false, network_enabled = false;

        gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!gps_enabled && !network_enabled) {
            return false;
        }
        return true;
    }

    public boolean testConnection(GoogleMap mMap, int CONNECTIONATTEMPTS, int[] c) {
        if (c[0] < CONNECTIONATTEMPTS) {
            mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(0, 0)));
            c[0] = c[0] + 1;
        }
        if (c[0] == CONNECTIONATTEMPTS) {
            c[0] = c[0] + 1;
            return false;
        }
        return true;
    }

    private double rad(double x) {
        return x*Math.PI/180;
    }

    /**
     * Returns the LatLng resulting from moving a distance from an origin
     * in the specified heading (expressed in degrees clockwise from north).
     * @param from     The LatLng from which to start.
     * @param distance The distance to travel.
     * @param heading  The heading in degrees clockwise from north.
     */
    private static LatLng computeOffset(LatLng from, double distance, double heading) {
        distance /= 6371;
        heading = Math.toRadians(heading);
        // http://williams.best.vwh.net/avform.htm#LL
        double fromLat = Math.toRadians(from.latitude);
        double fromLng = Math.toRadians(from.longitude);
        double cosDistance = Math.cos(distance);
        double sinDistance = Math.sin(distance);
        double sinFromLat = Math.sin(fromLat);
        double cosFromLat = Math.cos(fromLat);
        double sinLat = cosDistance * sinFromLat + sinDistance * cosFromLat * Math.cos(heading);
        double dLng = Math.atan2(
                sinDistance * cosFromLat * Math.sin(heading),
                cosDistance - sinFromLat * sinLat);
        return new LatLng(Math.toDegrees(Math.asin(sinLat)), Math.toDegrees(fromLng + dLng));
    }

    private int findMarkers(Location myLocation) {
        int events = 0;
        int radius = 5; //radius in km
        double lat = myLocation.getLatitude();
        double lng = myLocation.getLongitude();
        int EARTHRADIUS = 6371; // radius of earth in km
        for (int i = 0; i < markers.size(); i++) {
            double mlat = markers.get(i).getPosition().latitude;
            double mlng = markers.get(i).getPosition().longitude;
            double dLat = rad(mlat - lat);
            double dLong = rad(mlng - lng);
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                    Math.cos(rad(lat)) * Math.cos(rad(lat)) * Math.sin(dLong / 2) * Math.sin(dLong / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            double d = EARTHRADIUS * c;
            if (d < radius) {       //radius in km
                markers.get(i).setVisible(true);
               events++;
            }
            else{
                markers.get(i).setVisible(false);
            }
        }
        return events;
    }

    public float rangeTo(Location loc, Marker marker){
        Location dest = new Location("");
        dest.setLatitude(marker.getPosition().latitude);
        dest.setLongitude(marker.getPosition().longitude);
        float curDistance = loc.distanceTo(dest);

        return curDistance;
    }

    public boolean isFacing(Location loc, Marker marker, float bearing){
        float bearingTo;
        int accuracy = 45; //accuracy in degrees
        Location dest = new Location("");
        dest.setLatitude(marker.getPosition().latitude);
        dest.setLongitude(marker.getPosition().longitude);
        bearingTo = loc.bearingTo(dest);

        if (bearingTo < 0){
            bearingTo = 360 + bearingTo;
        }
        if (bearing < 0 ) {
            bearing = 360 + bearing;
        }

        Log.e("bearin", bearing+"");
        Log.e("bearinto", bearingTo+"");

        if(-accuracy <= (bearing - bearingTo) && (bearing - bearingTo) <= accuracy )
            return true;
        return false;
    }
}
