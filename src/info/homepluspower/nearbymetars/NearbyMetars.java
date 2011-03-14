package info.homepluspower.nearbymetars;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class NearbyMetars extends MapActivity implements LocationListener {
    /** Called when the activity is first created. */
	private static MapView mapView;
	private static MetarList metarList = null;
	private static LocationManager locationManager;
	
	private MetarDataRetriever dataRetrieverTask;
	
	/**
     * Do the retrieval and parsing of metar data. This assumes that metarList will be instantiated by someone else
     * @param location
     */
    private void parseMetarData(Location location) {
    	if(dataRetrieverTask != null) {
    		Log.d("NearbyMetars", "Cancelling existing metar processing");
    		if(dataRetrieverTask.getStatus() == MetarDataRetriever.Status.RUNNING)
    		{
    			dataRetrieverTask.cancel(true);
    			try {
    				Log.d("NearbyMetars", "Waiting for existing retrieval to return");
					dataRetrieverTask.get();
				} catch (InterruptedException e) {
				} catch (ExecutionException e) {
				} catch (CancellationException e) {					
				}
				Log.d("NearbyMetars", "Existing retrieval back");
    		}
    		else
    			Log.d("NearbyMetars", "Existing retrieval already cancelled");
    	}
    	
    	Log.d("NearbyMetars", "Start retrieving new data");
    	dataRetrieverTask = (MetarDataRetriever)new MetarDataRetriever().execute(metarList, location);
    }
    
    private void getMetarData(Location location) {
    	if(metarList == null) {
    		Log.d("NearbyMetars", "First time loading MetarList");
    		metarList = new MetarList(getResources().getDrawable(R.drawable.overlaydefault), mapView.getContext());
    		parseMetarData(location);
    		List<Overlay> mapOverlays = mapView.getOverlays();
            mapOverlays.add(metarList);
    	}
    	else {
    		Log.d("NearbyMetars", "Refreshing existing MetarList");
    		parseMetarData(location);
    	}
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        locationManager = (LocationManager)this.getSystemService(LOCATION_SERVICE);
        
        mapView = (MapView)findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        mapView.preLoad();
        
        Log.d("NearbyMetars", "Retrieve data from last known location");
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        getMetarData(location);
    }
    
    @Override
    protected boolean isRouteDisplayed() {
    	return false;
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	Log.d("NearbyMetars", "Resume");
    	
    	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 300000L, 1609.344f, this);
    	locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 300000L, 1609.344f, this);
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	Log.d("NearbyMetars", "Pause");
    	
    	if(dataRetrieverTask != null) {
    		dataRetrieverTask.cancel(true);
    		dataRetrieverTask = null;
    	}
    	
    	locationManager.removeUpdates(this);
    }

    public void onLocationChanged(Location location) {
		Log.d("NearbyMetars", "Got new location");
		MapController controller = mapView.getController();
		controller.animateTo(MetarItem.coordsToGeoPoint(location.getLatitude(), location.getLongitude()));
		controller.setZoom(10);
		getMetarData(location);
	}

	public void onProviderDisabled(String provider) {
	}

	public void onProviderEnabled(String provider) {
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
}