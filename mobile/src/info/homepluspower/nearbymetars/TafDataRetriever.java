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

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author mendozak
 *
 */
public class TafDataRetriever extends AsyncTask<Object, Void, Void> implements DialogInterface.OnCancelListener {
	private static final String logTag = "TafDataRetriever";
	
	private class TafParser extends DefaultHandler {
		private String text;
		private String rawTaf;

		@Override
		public void startDocument() {
			text="";
		}
		
		@Override
		public void endElement(String URI, String localName, String qName) throws SAXException {
			if(isCancelled()) {
				Log.d("NearbyMetars", "Parsing cancel detected at endElement");
				throw new SAXException("parsing cancelled");
			}
			
			Log.v("NearbyMetars", "End element: " + localName);
		
			if(localName.equals("raw_text"))
				rawTaf = text;			
			text="";
		}
		
		@Override
		public void characters(char[] ch, int start, int length) {
			String tmp = new String(ch);
			Log.v(logTag, "characters: >>>" + tmp + "<<< start: " + Integer.toString(start) + " length: " + Integer.toString(length));
			Log.v(logTag, "Substring to append: " + tmp.substring(start, start+length));
			text = text + tmp.substring(start, start+length).trim();
		}
	}

	private Context callerContext;
	private TextView txtView;
	private Button showBtn;
	private TafParser tafParser;
	
	//Stuff for Toast message
	String toastMsg = null;
	
	public TafDataRetriever(Context callerContext, TextView txtView, Button showBtn) {
		this.callerContext = callerContext;
		this.txtView = txtView;
		this.showBtn = showBtn;
	}
	
	@Override
	protected Void doInBackground(Object... params) {
		String airportID = (String)params[0];
		
		String url = callerContext.getString(R.string.adds_url) + "dataSource=tafs&requestType=retrieve&format=xml&hoursBeforeNow=0&timeType=valid&mostRecent=true&stationString=" + airportID;
		Log.d(logTag, "URL to use: " + url);
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			tafParser = new TafParser();
			parser.parse(url, tafParser);
		} catch(SAXException e) {
			if(isCancelled())
				Log.d("NearbyMetars", "Cancelling parsing");
			else {
				Log.e("NearbyMetars", "Parsing exception: " + e.getMessage());
				toastMsg = new String("Parsing exception: " + e.getMessage());
			}	
		} catch(IOException e) {
			Log.e("NearbyMetars", "Failed to retrieve data");
			toastMsg = new String("Failed to retrieve METAR data, try again later");
		} catch(ParserConfigurationException e) {
			Log.e("NearbyMetars", "No matching SAX parser");
			toastMsg = new String("No matching SAX parser.");
		}
		return null;
	}
	
	@Override
	protected void onPostExecute (Void result) {
		Log.v("NearbyMetars", "onPostExecute");
		
		if(toastMsg != null) {
			Log.v("NearbyMetars", "Showing toast message");
			Toast.makeText(callerContext, toastMsg, Toast.LENGTH_LONG).show();
		}
		
		Log.d(logTag, "Done retrieving TAF data");
		txtView.setText(tafParser.rawTaf);
		showBtn.setClickable(true);
	}
	
	public void onCancel(DialogInterface dialog) {
		this.cancel(true);		
	}
}
