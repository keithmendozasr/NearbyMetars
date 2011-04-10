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
		SKC,
		CLR,
		FEW,
		SCT,
		BKN,
		OVC,
		OVX
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
		//Get the bounds of the icon
		Point point = new Point();
		Projection projection = mapView.getProjection();
		projection.toPixels(mPoint, point);
		float project = projection.metersToEquatorPixels((float)1609.344);
		Log.v("NearbyMetars", "Value of project: " + Float.toString(project));
		if(project < 10.0) {
			Log.v("NearbyMetars", "Changing project to 10");
			project = 10.0f;
		}
		final RectF drawPos = new RectF(point.x-project, point.y-project, point.x+project, point.y+project);
		
		//Get the paint to use for drawing the icons
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.STROKE);
		paint.setARGB(179, 0, 0, 0);
		paint.setStrokeWidth(2.0f);
		paint.setStrokeCap(Paint.Cap.BUTT);		
		switch(skyCond) {
		case CLR:
			canvas.drawRect(drawPos, paint);
			break;
		case SKC:
			canvas.drawCircle(point.x, point.y, project, paint);	
			break;
		case FEW:
			canvas.drawCircle(point.x, point.y, project, paint);
			canvas.drawLine(point.x, drawPos.top, point.x, drawPos.bottom, paint);
			break;
		case SCT:
			canvas.drawArc(drawPos, 0, 270, false, paint);
			paint.setStyle(Paint.Style.FILL_AND_STROKE);
			canvas.drawArc(drawPos, 270, 90, true, paint);
			break;
		case BKN:
			canvas.drawArc(drawPos, 180, 90, false, paint);
			paint.setStyle(Paint.Style.FILL_AND_STROKE);
			canvas.drawArc(drawPos, 270, 270, true, paint);
			break;
		case OVC:
			paint.setStyle(Paint.Style.FILL_AND_STROKE);
			canvas.drawCircle(point.x, point.y, project, paint);
			break;
		case OVX:
			canvas.drawArc(drawPos, 45, 180, true, paint);
			canvas.drawArc(drawPos, 135, 180, true, paint);
			canvas.drawArc(drawPos, 315, 90, true, paint);
			break;
		}
	}
}
