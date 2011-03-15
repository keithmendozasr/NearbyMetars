package info.homepluspower.nearbymetars;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class MetarList extends ItemizedOverlay<MetarItem> {
	
	private ArrayList<MetarItem> mOverlays = new ArrayList<MetarItem>();
	private Context mContext; 
	
	public MetarList(Drawable defaultMarker, Context context) {
		super(boundCenter(defaultMarker));
		mContext = context;
	}
	
	public void addOverlay(MetarItem overlay) {
		Log.v("NearbyMetars", "Adding overlay item");
		mOverlays.add(overlay);
		populate();
	}
	
	@Override
	protected MetarItem createItem(int i) {
		return mOverlays.get(i);
	}

	@Override
	public int size() {
		return mOverlays.size();
	}

	@Override
	protected boolean onTap(int index) {
		Log.v("NearbyMetars", "Item tapped");
		if(index >= size()) {
			Log.e("NearbyMetars", "Requesting index outside available items");
			Log.d("NearbyMetars", "index value: " + Integer.toString(index) + " number of items available: " + Integer.toString(size()));
			return false; 
		}
		
		OverlayItem item = mOverlays.get(index);
		AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
		dialog.setTitle(item.getTitle());
		dialog.setMessage(item.getSnippet());
		dialog.show();
		return true;
	}
	
	@Override
	public void draw(android.graphics.Canvas canvas, MapView mapView, boolean shadow) {
		if(!shadow) {
			Log.v("NearbyMetars", "Drawing items");
			MetarItem item;
			for(int i=0; i<mOverlays.size(); i++) {
				item = mOverlays.get(i);
				item.draw(canvas, mapView);
			}
		}
	}
	
	public void reset() {
		Log.v("NearbyMetars", "Clearing overlay items");

		mOverlays.clear();
		populate();
	}
}
