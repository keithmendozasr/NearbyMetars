/******************************************************************************
 * Copyright 2011,2012 Keith Mendoza
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package info.homepluspower.nearbymetars;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

public class MetarItem extends OverlayItem implements Parcelable {
	private static final String logTag = "MetarItem";
	
	public MetarItem(GeoPoint point, java.lang.String title, java.lang.String snippet) {
		super(point, title, snippet);
	}
	
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
	private double windDir;
	private int windSpeed;
	private static final double degToRadFactor = (Math.PI/180);
	
	public static GeoPoint coordsToGeoPoint(double latitude, double longitude) {
		return new GeoPoint((int)(latitude *1e6), (int)(longitude *1e6));
	}
	
	private static final double degToRad(double deg) {
		return deg * degToRadFactor;
	}
	
	public MetarItem(GeoPoint p, String location, String rawMetar, SkyConds skyCond, int windDir, int windSpeed) {
		super(p, location, rawMetar);
		this.skyCond = skyCond;
		this.windDir = (windDir > 0) ? degToRad(windDir) : 0;
		this.windSpeed = windSpeed;
	}
		
	public void draw(Canvas canvas, MapView mapView) {
		//Get the bounds of the icon
		Point point = new Point();
		Projection projection = mapView.getProjection();
		projection.toPixels(mPoint, point);
		
		float project = (float)(projection.metersToEquatorPixels((float)1609.344));
		if(project < 10 )
			project = 10.0f;
		Log.v(logTag, "Value of project: " + Float.toString(project));
		Log.v(logTag, "Value of point: " + point.toString());
		final RectF drawPos = new RectF(point.x-project, point.y-project, point.x+project, point.y+project);
		
		//Verify that it's worth drawing the icon on screen
		if(canvas.quickReject(drawPos, Canvas.EdgeType.AA)) {
			Log.d(logTag, "Not drawing icon for " + this.mTitle);
			return;
		}
		
		Log.d(logTag, "Drawing icon for " + this.mTitle);
		
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
		
		//Draw the wind bar if wind is NOT variable
		if(windDir > 0)
		{
			final float dirLen = project * 3;
			
			//This has been modified to go the opposite direction of
			//standard polar to Cartesian plotting
			float endX, endY;
			endX = (float)(point.x + dirLen * Math.sin(windDir));
			endY = (float)(point.y - dirLen * Math.cos(windDir));
			canvas.drawLine(point.x, point.y, endX, endY, paint);
			
			//Draw the wind speed
			if(windSpeed > 100) {
				Log.w(logTag, "Wind speed " + Integer.toString(windSpeed) + " will not be displayed graphically");
				return;
			}
			
			Log.d(logTag, "Drawing wind barb");
			double barbAngle;
			final int barbSpace = (int)dirLen/8;
			float barbX, barbY;
			
			if(project<=10)
				project = 20;
			
			barbAngle = windDir + degToRad(90);
			int tmpWindSpeed = windSpeed;
			if(tmpWindSpeed >= 50)
			{
				float startX = endX;
				float startY = endY;
				
				paint.setStyle(Paint.Style.FILL_AND_STROKE);
				
				Log.d(logTag, "Windspeed over 50");
				Path barbPath = new Path();
				barbPath.moveTo(endX, endY);
				endX -= (float)(barbSpace*Math.sin(windDir));
				endY += (float)(barbSpace*Math.cos(windDir));
				barbPath.lineTo((float)(endX + project * Math.sin(barbAngle)), (float)(endY - project * Math.cos(barbAngle)));
				endX -= (float)(barbSpace*Math.sin(windDir));
				endY += (float)(barbSpace*Math.cos(windDir));
				barbPath.lineTo(endX, endY);
				barbPath.lineTo(startX, startY);
				
				canvas.drawPath(barbPath, paint);
				tmpWindSpeed -= 50;
				
				endX -= (float)(barbSpace*Math.sin(windDir));
				endY += (float)(barbSpace*Math.cos(windDir));
			}
			
			barbAngle = windDir + degToRad(80);
			for(int i=0; i<(int)(tmpWindSpeed/10); i++) {
				barbX = (float)(endX + project * Math.sin(barbAngle));
				barbY = (float)(endY - project * Math.cos(barbAngle));
				canvas.drawLine(endX, endY, barbX, barbY, paint);
				endX -= (float)(barbSpace*Math.sin(windDir));
				endY += (float)(barbSpace*Math.cos(windDir));
				
				Log.v(logTag, "New value of endX: " + Float.toString(endX) +" New value of endY: " + Float.toString(endY));
			}
			
			if((tmpWindSpeed % 10) > 0)
			{
				Log.v(logTag, "Drawing half-size barb");
				barbX = (float)(endX + (project/2) * Math.sin(barbAngle));
				barbY = (float)(endY - (project/2) * Math.cos(barbAngle));
				canvas.drawLine(endX, endY, barbX, barbY, paint);
			}
			
		}
	}

	public static final Parcelable.Creator<MetarItem> CREATOR = new Parcelable.Creator<MetarItem>() {
		public MetarItem createFromParcel(Parcel in) {
			return new MetarItem(in);
		}
		
		public MetarItem[] newArray(int size) {
			return new MetarItem[size];
		}
	};
	
	private MetarItem(Parcel in) {
		super(new GeoPoint(in.readInt(), in.readInt()), in.readString(), in.readString());
		skyCond = SkyConds.valueOf(in.readString());
		windDir = in.readDouble();
		windSpeed = in.readInt();
	}
	
	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(this.mPoint.getLatitudeE6());
		dest.writeInt(this.mPoint.getLongitudeE6());
		dest.writeString(this.mTitle);
		dest.writeString(this.mSnippet);
		dest.writeString(skyCond.toString());
		dest.writeDouble(windDir);
		dest.writeInt(windSpeed);
	}
}
