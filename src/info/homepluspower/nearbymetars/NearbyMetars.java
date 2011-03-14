package info.homepluspower.nearbymetars;

import java.util.List;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class NearbyMetars extends MapActivity implements LocationListener {
    /** Called when the activity is first created. */
	private static MapView mapView;
	private static MetarList metarList = null;
	private static LocationManager locationManager;
	
	private MetarDataRetriever dataRetrieverTask;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        locationManager = (LocationManager)this.getSystemService(LOCATION_SERVICE);
        
        mapView = (MapView)findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        mapView.setReticleDrawMode(MapView.ReticleDrawMode.DRAW_RETICLE_OVER);
        mapView.preLoad();
    }
    
    @Override
    protected boolean isRouteDisplayed() {
    	return false;
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	Log.d("NearbyMetars", "Resume");
    	dataRetrieverTask = null;
    	
    	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 300000L, 1609.344f, this);
    	locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 300000L, 1609.344f, this);
    	
//    	metarList = new MetarList(getResources().getDrawable(R.drawable.overlaydefault), mapView.getContext());
//		new MetarDataRetriever().execute(metarList, locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
    }
    
    private void cancelMetarProcessing() {
    	if(dataRetrieverTask != null) {
			Log.d("NearbyMetars", "Cancelling existing metar processing");
			dataRetrieverTask.cancel(true);
			dataRetrieverTask = null;
		}	
    }
    @Override
    protected void onPause() {
    	super.onPause();
    	Log.d("NearbyMetars", "Pause");
    	cancelMetarProcessing();
    	locationManager.removeUpdates(this);
    }

    public void onLocationChanged(Location location) {
		Log.d("NearbyMetars", "Got new location");
		mapView.getController().animateTo(MetarItem.coordsToGeoPoint(location.getLatitude(), location.getLongitude()));
		
		if(metarList == null) {
			List<Overlay> mapOverlays = mapView.getOverlays();
	        metarList = new MetarList(getResources().getDrawable(R.drawable.overlaydefault), mapView.getContext());
	        mapOverlays.add(metarList);	
		}
		
		cancelMetarProcessing();
		dataRetrieverTask = new MetarDataRetriever();
		dataRetrieverTask.execute(metarList, location);
		
		Log.d("NearbyMetars", "Done handling onLocationChanged");
	}

	public void onProviderDisabled(String provider) {
	}

	public void onProviderEnabled(String provider) {
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
}