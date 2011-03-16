package info.homepluspower.nearbymetars;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

public class MetarItem extends OverlayItem {
	public static enum SkyConds {
		CLR,
		FEW,
		SCT,
		BKN,
		OVC
	}
	
	private SkyConds skyCond;
	
	public static GeoPoint coordsToGeoPoint(double latitude, double longitude) {
		return new GeoPoint((int)(latitude *1e6), (int)(longitude *1e6));
	}
	
	public MetarItem(GeoPoint p, String location, String rawMetar, SkyConds skyCond) {
		super(p, location, rawMetar);
		this.skyCond = skyCond;
		
	}
	
	public void draw(Canvas canvas, MapView mapView) {
		Projection projection = mapView.getProjection();
		Point point = new Point();
		
		projection.toPixels(mPoint, point);
		float project = projection.metersToEquatorPixels((float) 1609.344);
		Log.d("NearbyMetars", "Value of project: " + Float.toString(project));
		if(project < 10.0) {
			Log.v("NearbyMetars", "Changing project to 10");
			project = 10.0f;
		}
		
		Paint paint = new Paint();
		
		switch(skyCond) {
		case CLR:
			paint.setARGB(204, 255, 255, 255);
			break;
		case FEW:
			paint.setARGB(51, 0, 0, 0);
			break;
		case SCT:
			paint.setARGB(102, 0, 0, 0);
			break;
		case BKN:
			paint.setARGB(153, 0, 0, 0);
			break;
		case OVC:
			paint.setARGB(204, 0, 0, 0);
			break;
		}
		
		paint.setStyle(Paint.Style.FILL);
		
		canvas.drawCircle(point.x, point.y, project, paint);
		
		paint.setARGB(255, 0, 0, 0);
		paint.setStyle(Paint.Style.STROKE);
		RectF oval = new RectF(point.x-project, point.y-project, point.x+project, point.y+project);
		canvas.drawArc(oval, 0, 360.0f, false, paint);
	}
}
