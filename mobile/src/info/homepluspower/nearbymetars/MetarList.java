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

import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class MetarList extends ItemizedOverlay<MetarItem> {
	
	private ArrayList<MetarItem> mOverlays = new ArrayList<MetarItem>();
	private Context mContext;
	private static final String logTag = "MetarList";
	
	private TextView metarTxt;
	
	public MetarList(Drawable defaultMarker, Context context) {
		super(boundCenter(defaultMarker));
		mContext = context;
		populate();
	}
	
	public void addOverlay(MetarItem overlay) {
		Log.v(logTag, "Adding overlay item");
		mOverlays.add(overlay);
		setLastFocusedIndex(-1);
		populate();
	}
	
	@Override
	protected MetarItem createItem(int i) {
		return mOverlays.get(i);
	}

	@Override
	public int size() {
		Log.v(logTag, "size called, returning " + Integer.toString(mOverlays.size()));
		return mOverlays.size();
	}
	
	@Override
	protected boolean onTap(int index) {
		Log.v(logTag, "Item tapped with index " + Integer.toString(index));
		OverlayItem item = mOverlays.get(index);
		
		final Dialog dialog = new Dialog(mContext);
		dialog.setContentView(R.layout.metar_taf);
		dialog.setTitle(item.getTitle());
		
		metarTxt = (TextView)dialog.findViewById(R.id.metarText);
		metarTxt.setText(item.getSnippet());
		
		Button showBtn = (Button)dialog.findViewById(R.id.tafBtn);
		showBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				Log.v(logTag, "Got click from tafBtn");
				TextView txtBox = (TextView)dialog.findViewById(R.id.tafText);
				switch(txtBox.getVisibility())
				{
				case View.VISIBLE:
					txtBox.setVisibility(View.GONE);
					((Button)arg0).setText(R.string.show_taf);
					break;
				case View.GONE:
					txtBox.setVisibility(View.VISIBLE);
					((Button)arg0).setText(R.string.hide_taf);
					break;
				default:
					break;
				}
			}
		});
		
		dialog.show();
		Log.v(logTag, "Dialog shown. Waiting for TAF data");
		
		new TafDataRetriever(mContext, (TextView)dialog.findViewById(R.id.tafText), (Button)dialog.findViewById(R.id.tafBtn)).execute(item.getTitle());
		return true;
	}
	
	@Override
	public void draw(android.graphics.Canvas canvas, MapView mapView, boolean shadow) {
		if(!shadow) {
			Log.v(logTag, "Drawing items");
			MetarItem item;
			for(int i=0; i<mOverlays.size(); i++) {
				item = mOverlays.get(i);
				item.draw(canvas, mapView);
			}
		}
	}
	
	public void reset() {
		Log.v(logTag, "Clearing overlay items");
		mOverlays.clear();
		setLastFocusedIndex(-1);
		populate();
	}
	
	public void saveListToBundle(Bundle outState) {
		outState.putParcelableArrayList("metarlist", mOverlays);
	}
	
	public void getListFromBundle(Bundle savedInstanceState) {
		mOverlays = savedInstanceState.getParcelableArrayList("metarlist");
		setLastFocusedIndex(-1);
		populate();
	}
}
