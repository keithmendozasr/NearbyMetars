package info.homepluspower.nearbymetars;

import android.os.Bundle;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

public class NearbyMetars extends MapActivity {
	private static MapView mapView;
	private static MetarList metarList = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Log.d("NearbyMetars", "onCreate called");
        
        mapView = (MapView)findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        mapView.preLoad();
        
        metarList = new MetarList(getResources().getDrawable(R.drawable.overlaydefault), mapView.getContext());
        GeoPoint center = MetarItem.coordsToGeoPoint(33.6756667, -117.8682222);
        MetarItem i = new MetarItem(center, "TEST", "DRAW TEST DATA", MetarItem.SkyConds.FEW, 360, 50);
        metarList.addOverlay(i);
        	
        mapView.getOverlays().add(metarList);
        mapView.getController().animateTo(center);
    }
    
    @Override
    protected boolean isRouteDisplayed() {
    	return false;
    }
}