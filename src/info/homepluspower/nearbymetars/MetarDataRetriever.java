/******************************************************************************
 * Copyright 2011 Keith Mendoza
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

import info.homepluspower.nearbymetars.MetarItem.SkyConds;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

/**
 * @author mendozak
 *
 */
public class MetarDataRetriever extends AsyncTask<Object, Void, MetarList> {
	
	private class MetarParser extends DefaultHandler {
		private String text;
		public MetarList list;
		
		private MetarItem.SkyConds skyCond;
		private double latitude, longitude;
		private String rawMetar, location;
		
		public MetarParser(MetarList list) {
			text = new String();
			this.list = list;
		}
		
		@Override
		public void startDocument() {
			text="";
		}
		
		@Override
		public void startElement(String URI, String localName, String qName, Attributes attributes) throws SAXException {
			if(isCancelled()) {
				Log.d("NearbyMetars", "Parsing cancel detected at startElement");
				throw new SAXException("parsing cancelled");
			}
			
			Log.d("NearbyMetarsParser", "Start element: " + localName);
			if(localName.equals("sky_condition")) {
				String cover = attributes.getValue("sky_cover");
				SkyConds tmpSkyCond = SkyConds.valueOf(cover);
				if(skyCond == null || tmpSkyCond.ordinal() > skyCond.ordinal())
					skyCond = tmpSkyCond;
			}
		}
		
		@Override
		public void endElement(String URI, String localName, String qName) throws SAXException {
			if(isCancelled()) {
				Log.d("NearbyMetars", "Parsing cancel detected at endElement");
				throw new SAXException("parsing cancelled");
			}
			
			Log.d("NearbyMetarsParser", "End element: " + localName);
			
			if(localName.equals("METAR")) {
				Log.d("NearbyMetars", "Inserting MetarItem\nLocation: " + location + "\nSky condition: " + skyCond.toString());
				MetarItem metarItem = new MetarItem(MetarItem.coordsToGeoPoint(latitude, longitude), location, rawMetar, skyCond);
				list.addOverlay(metarItem);
				skyCond = null;
			}
			else if(localName.equals("raw_text"))
				rawMetar = text;
			else if(localName.equals("station_id"))
				location = text;
			else if(localName.equals("latitude"))
				latitude = Double.valueOf(text);
			else if(localName.equals("longitude"))
				longitude = Double.valueOf(text);
			
			text="";
		}
		
		@Override
		public void characters(char[] ch, int start, int length) {
			String tmp = new String(ch);
			Log.d("NearbyMetarsParser", "characters: >>>" + tmp + "<<< start: " + Integer.toString(start) + " length: " + Integer.toString(length));
			Log.d("NearbyMetarsParser", "Substring to append: " + tmp.substring(start, start+length));
			text = text + tmp.substring(start, start+length).trim();
		}
	}
	
	@Override
	protected MetarList doInBackground(Object... params) {
		MetarList list = (MetarList)params[0];
		Location location = (Location)params[1];
		
		String url = "http://weather.aero/dataserver_current/httpparam?dataSource=metars&requestType=retrieve&format=xml&hoursBeforeNow=1&mostRecentForEachStation=true&radialDistance=50;" + Double.toString(location.getLongitude()) + "," + Double.toString(location.getLatitude());
		list.reset();
		Log.d("NearbyMetars", "URL to use: " + url);
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			
			parser.parse(url, new MetarParser(list));
		} catch(Exception e) {
			if(isCancelled())
				Log.d("NearbyMetars", "Cancelling parsing");
			else
				Log.e("NearbyMetars", "Parsing exception: " + e.getMessage());
		}
		return list;
	}
	
}
