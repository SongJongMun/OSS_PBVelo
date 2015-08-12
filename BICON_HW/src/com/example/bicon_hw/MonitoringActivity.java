package com.example.bicon_hw;

import java.util.ArrayList;
import java.util.Collection;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.radiusnetworks.ibeacon.IBeacon;
import com.radiusnetworks.ibeacon.IBeaconConsumer;
import com.radiusnetworks.ibeacon.IBeaconManager;
import com.radiusnetworks.ibeacon.RangeNotifier;
import com.radiusnetworks.ibeacon.Region;

public class MonitoringActivity extends Activity implements IBeaconConsumer {
	protected static final String TAG = "MonitoringActivity";

	private ListView list = null;
	private BeaconAdapter adapter = null;
	private ArrayList<IBeacon> arrayL = new ArrayList<IBeacon>();
	private LayoutInflater inflater;

	private BeaconServiceUtility beaconUtill = null;
	private IBeaconManager iBeaconManager = IBeaconManager.getInstanceForApplication(this);

	LocationManager locationManager;
	double longitude, latitude;
	float accuracy;
	String updatedB;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_monitor);
		//레이아웃 생성
		
		beaconUtill = new BeaconServiceUtility(this);
		//비콘서비스를 제공하는 객체 생성
		
		list = (ListView) findViewById(R.id.list);
		//비콘리스트를 표시하는 리스트뷰객체 가져옴
		
		adapter = new BeaconAdapter();
		//내부 private 객체 가져옴
		
		list.setAdapter(adapter);
		//리스트 뷰 내부적으로 List Adapter를 통해서만 데이터에 접근
		
		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		//레이아웃 XML파일을 View객체로 만듬
		
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onStart() {
		super.onStart();
		beaconUtill.onStart(iBeaconManager, this);
		// iBeaconManger와 현재 객체를 Bind한다.
	}

	@Override
	protected void onStop() {
		beaconUtill.onStop(iBeaconManager, this);
		super.onStop();
		// iBeaconManger와 현재 객체를 unBind한다.
	}

	/**Specifies a class that should be called each time the IBeaconService gets sees or stops seeing a Region of iBeacons.**/
	@Override
	public void onIBeaconServiceConnect() {
		/**
		 * Specifies a class that should be called each time the IBeaconService gets ranging data,
		 *  which is nominally once per second when iBeacons are detected.
		 **/
		iBeaconManager.setRangeNotifier(new RangeNotifier() {
			@Override
			public void didRangeBeaconsInRegion(Collection<IBeacon> iBeacons, Region region) {
				arrayL.addAll((ArrayList<IBeacon>) iBeacons);
				adapter.notifyDataSetChanged();
				//Notifies the attached observers that the underlying data has been changed and 
				//any View reflecting the data set should refresh itself.
			}
		});

		/**
		 * Tells the IBeaconService to start looking for iBeacons that match the passed Region object, 
		 * and providing updates on the estimated distance very seconds while iBeacons in the Region are visible. 
		 * Note that the Region's unique identifier must be retained to later call the stopRangingBeaconsInRegion method.
		 * */
		try {
			iBeaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		/**
		 * Tells the IBeaconService to start looking for iBeacons that match the passed Region object, 
		 * and providing updates on the estimated distance very seconds while iBeacons in the Region are visible. 
		 * Note that the Region's unique identifier must be retained to later call the stopRangingBeaconsInRegion method.
		 * */
		try {
			iBeaconManager.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId", null, null, null));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private class BeaconAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			if (arrayL != null && arrayL.size() > 0)
				return arrayL.size();
			else
				return 0;
		}

		@Override
		public IBeacon getItem(int arg0) {
			return arrayL.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		/**
		 * Get a View that displays the data at the specified position in the data set. 
		 * You can either create a View manually or inflate it from an XML layout file. When the View is inflated, 
		 * the parent View (GridView, ListView...) will apply default layout parameters unless you use 
		 * */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			try {
				ViewHolder holder;

				if (convertView != null) {
					holder = (ViewHolder) convertView.getTag();
				} else {
					holder = new ViewHolder(convertView = inflater.inflate(R.layout.tupple_monitoring, null));
				}
				
				if (arrayL.get(position).getProximityUuid() != null)
					holder.beacon_uuid.setText("UUID: " + arrayL.get(position).getProximityUuid());

					holder.beacon_major.setText("Major: " + arrayL.get(position).getMajor());
	
					holder.beacon_minor.setText(", Minor: " + arrayL.get(position).getMinor());
	
					holder.beacon_proximity.setText("Proximity: " + arrayL.get(position).getProximity());
	
					holder.beacon_rssi.setText(", Rssi: " + arrayL.get(position).getRssi());
	
					holder.beacon_txpower.setText(", TxPower: " + arrayL.get(position).getTxPower());
	
					holder.beacon_range.setText("" + arrayL.get(position).getAccuracy());
					

			} catch (Exception e) {
				e.printStackTrace();
			}

			return convertView;
		}

		
		private class ViewHolder {
			private TextView beacon_uuid;
			private TextView beacon_major;
			private TextView beacon_minor;
			private TextView beacon_proximity;
			private TextView beacon_rssi;
			private TextView beacon_txpower;
			private TextView beacon_range;
			private TextView beacon_provider;

			public ViewHolder(View view) {
				beacon_uuid = (TextView) view.findViewById(R.id.BEACON_uuid);
				beacon_major = (TextView) view.findViewById(R.id.BEACON_major);
				beacon_minor = (TextView) view.findViewById(R.id.BEACON_minor);
				beacon_proximity = (TextView) view.findViewById(R.id.BEACON_proximity);
				beacon_rssi = (TextView) view.findViewById(R.id.BEACON_rssi);
				beacon_txpower = (TextView) view.findViewById(R.id.BEACON_txpower);
				beacon_range = (TextView) view.findViewById(R.id.BEACON_range);
				beacon_provider = (TextView) view.findViewById(R.id.BEACON_provider);

				view.setTag(this);
			}
		}
	}

}