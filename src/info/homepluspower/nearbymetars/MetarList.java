package info.homepluspower.nearbymetars;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;

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
			MetarItem item;
			for(int i=0; i<mOverlays.size(); i++) {
				item = mOverlays.get(i);
				item.draw(canvas, mapView);
			}
		}
	}
	
	public void reset() {
		mOverlays.clear();
		populate();
	}
}
