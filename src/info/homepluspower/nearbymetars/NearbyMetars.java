package info.homepluspower.nearbymetars;

import java.util.List;

import android.app.ProgressDialog;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class NearbyMetars extends MapActivity implements LocationListener {
    /** Called when the activity is first created. */
	private static MapView mapView;
	private static MetarList metarList = null;
	private static LocationManager locationManager;
	
	/**
     * Do the retrieval and parsing of metar data. This assumes that metarList will be instantiated by someone else
     * @param location
     */
    private void parseMetarData(Location location) {
    	MetarDataRetriever dataRetriever = new MetarDataRetriever();
    	
    	Log.d("NearbyMetars", "Showing progress dialog");
    	ProgressDialog dialog = ProgressDialog.show(mapView.getContext(), "Getting Metar Data", "Retrieving METAR data. Stand-by", true);
    	
    	dataRetriever.getMetarData(metarList, location);
    	
    	dialog.cancel();
    	Log.d("NearbyMetars", "Closing dialog");
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
	
	//Menu stuff
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.getmetar:
			GeoPoint center = mapView.getMapCenter();
			Location location = new Location("");
			location.setLatitude(center.getLatitudeE6()/1e6);
			location.setLongitude(center.getLongitudeE6()/1e6);
			getMetarData(location);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}