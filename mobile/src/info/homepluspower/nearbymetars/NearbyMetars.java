package info.homepluspower.nearbymetars;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

public class NearbyMetars extends MapActivity implements LocationListener {
	private static MapView mapView;
	private static MetarList metarList = null;
	private static LocationManager locationManager;
	private static MetarDataRetriever dataRetriever;
	private static Calendar lastLoad;
	private static ProgressDialog waitForLocationDlg;
	
	/**
     * Do the retrieval and parsing of metar data. This assumes that metarList will be instantiated by someone else
     * @param location
     */
    private void getMetarData(Location location) {
    	if(dataRetriever!= null && dataRetriever.getStatus() == AsyncTask.Status.RUNNING) {
    		Log.d("NearbyMetars", "In the middle of another processing. Stop that one");
    		dataRetriever.cancel(true);
    		try {
    			dataRetriever.get();
    		} catch(Exception e) {
    			Log.d("NearbyMetars", "Previous processing stopped");
    		}
    	}
    	else
    		Log.d("NearbyMetars", "No running process");
    		
    	dataRetriever = (MetarDataRetriever) new MetarDataRetriever(NearbyMetars.this, mapView).execute(metarList, location);
    	lastLoad = Calendar.getInstance();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Log.d("NearbyMetars", "onCreate called");
        locationManager = (LocationManager)this.getSystemService(LOCATION_SERVICE);
        
        mapView = (MapView)findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        mapView.preLoad();
        
        metarList = new MetarList(getResources().getDrawable(R.drawable.overlaydefault), mapView.getContext());

        if(savedInstanceState != null) {
        	Log.d("NearbyMetars", "savedInstanceState not null. Getting last load time from bundle");
        
        	//Load last metar list and time last retrieved
        	lastLoad = (Calendar) savedInstanceState.getSerializable("lastloadtime");
        	metarList.getListFromBundle(savedInstanceState);
        }
        else
        	Log.d("NearbyMetars", "savedInstanceState null.");
        	
        mapView.getOverlays().add(metarList);
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	Log.d("NearbyMetars", "Saving metarList to bundle");
    	outState.putSerializable("lastloadtime", lastLoad);
    	metarList.saveListToBundle(outState);
    }
    
    @Override
    protected boolean isRouteDisplayed() {
    	return false;
    }
    
    private void startLocationListener() {
    	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 300000L, 1609.344f, this);
    	locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 300000L, 1609.344f, this);
    }
    
    private void stopLocationListener() {
    	locationManager.removeUpdates(this);
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	Log.d("NearbyMetars", "Resume");
    	
    	if(lastLoad != null) {
    		//Stuff for debugging
        	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        	Log.d("NearbyMetars", "Value of lastLoad: " + sdf.format(lastLoad.getTime()));
        	
        	Calendar curTime = Calendar.getInstance();
        	curTime.add(Calendar.HOUR, -1);
        	Log.d("NearbyMetars", "Current time minus 1 hour: " + sdf.format(curTime.getTime()));
        	if(curTime.before(lastLoad)) {
        		Log.d("NearbyMetars", "Using saved metar list");
        		return;
        	}
        	else
        		Log.d("NearbyMetars", "Saved data too old");
    	}
    	
    	Log.d("NearbyMetars", "Need to get METAR data");
    	waitForLocationDlg = ProgressDialog.show(this, "Wait for location", "Determining location, stand-by", true, true);
    	
    	startLocationListener();
    }
    
    private void cancelWaitForLocation() {
    	if(waitForLocationDlg != null) {
			waitForLocationDlg.cancel();
			waitForLocationDlg = null;
		}
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	Log.d("NearbyMetars", "Pause");
    	cancelWaitForLocation();
    	stopLocationListener();
    }

    private void getMetarAndCenter(Location location) {
    	cancelWaitForLocation();
		MapController controller = mapView.getController();
		controller.animateTo(MetarItem.coordsToGeoPoint(location.getLatitude(), location.getLongitude()));
		controller.setZoom(10);
		getMetarData(location);
    }
    
    public void onLocationChanged(Location location) {
		Log.d("NearbyMetars", "Got new location");
		getMetarAndCenter(location);
		stopLocationListener();
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
		case R.id.getmetarloc:
			Log.v("NearbyMetars", "Get metar at current location from GPS");
			startLocationListener();
			return true;
		case R.id.showabout:
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			final AlertDialog dialog = builder.create();
			dialog.setTitle("About " + getText(R.string.app_name));
			
			final TextView textView = new TextView(this);
			final String msgText = "Version " + getText(R.string.app_version) + " Go to http://aviationapps.homepluspower.info/NearbyMetars for more info";
			final SpannableString s = new SpannableString(msgText);
			Linkify.addLinks(s, Linkify.WEB_URLS);
			textView.setText(s);
			textView.setMovementMethod(LinkMovementMethod.getInstance());
			textView.setOnClickListener(
					new OnClickListener() {
						public void onClick(View v) {
							dialog.dismiss();
						}
					}
					);
			dialog.setView(textView);
			dialog.show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}