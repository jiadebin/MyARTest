/**
 *这是本应用的我的地点界面类 
 * 
 * @author JiaDebin
 * @version 1.0, 20/3/2012
 */

package hit.edu.cn;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.LocationListener;
import com.baidu.mapapi.MKAddrInfo;
import com.baidu.mapapi.MKDrivingRouteResult;
import com.baidu.mapapi.MKLocationManager;
import com.baidu.mapapi.MKPoiResult;
import com.baidu.mapapi.MKSearch;
import com.baidu.mapapi.MKSearchListener;
import com.baidu.mapapi.MKTransitRouteResult;
import com.baidu.mapapi.MKWalkingRouteResult;
import com.baidu.mapapi.MapActivity;
import com.baidu.mapapi.MapController;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.MyLocationOverlay;
import com.baidu.mapapi.OverlayItem;

import dbtool.hit.edu.cn.DBTool;

public class map_activity extends MapActivity implements LocationListener {
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflater=getMenuInflater();
		inflater.inflate(R.menu.map_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.item_help_map) {
			AlertDialog.Builder builder=new Builder(this);
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.cancel();
				}
			});
			builder.setMessage(R.string.map_help);
			builder.setTitle("使用帮助");
			builder.create().show();
		} else if (item.getItemId() == R.id.item_changeMode_map) {
			if(mapMode==0){
				mapView.setSatellite(true);
				mapMode=1;
				item.setTitle("切换到常规模式");
			}
			else{
				mapView.setSatellite(false);
				mapMode=0;
				item.setTitle("切换到卫星地图");
			}
		}
		return super.onOptionsItemSelected(item);
	}

	public static final String TAG = "map_activity";
	private int mapMode=0;  /*地图模式，0代表普通，1代表卫星模式*/
	private BMapManager mapManager;
	private MapView mapView;
	private MapController mapControl;
	private GeoPoint point=null;
	
	private MKLocationManager locationManager=null;
	private MyLocationOverlay locationOverlay;
	private touchOverlay touchOverlay;              //提供长按地图响应服务
	
	private MKSearch mksearch = null;
	
	LayoutInflater layoutInflater;
	View view;
	AlertDialog.Builder builder;
	AlertDialog dialog=null;
	EditText et_name;
	EditText et_lat;
	EditText et_lon;
	EditText et_addrDesc;
	double lat, lon;
	private DBTool dbtool;
	private Cursor cursor;
	private customedItemizedOverlay itemOverlay=null;
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setContentView(R.layout.map_activity);
		Log.v(TAG, "new the DBTool");
		dbtool=new DBTool(this);
		cursor=dbtool.select();
		Log.v(TAG, "new the cursor "+cursor.getCount());
		Drawable marker = this.getResources().getDrawable(R.drawable.iconmarka);  
        // 为maker定义位置和边界  
        marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());
		itemOverlay=new customedItemizedOverlay(marker, this);
		
		Log.v(TAG, "in onCreate");
		mapView = (MapView) findViewById(R.id.mapview);
		mapManager = new BMapManager(getApplication());
		mapManager.init("D52F4D689069CE4D8CC4271C2D4F7CA10D1B4F89", null);
		
		super.initMapActivity(mapManager);
		locationManager = mapManager.getLocationManager();
		locationManager.enableProvider(MKLocationManager.MK_GPS_PROVIDER);
		
		locationOverlay = new MyLocationOverlay(this, mapView);
		locationOverlay.enableMyLocation();
		locationOverlay.enableCompass();
		mapView.getOverlays().add(locationOverlay);
		
		locationManager.requestLocationUpdates(this);
		
		mapView.setSatellite(false);
		mapView.setBuiltInZoomControls(true);
		mapView.setDrawOverlayWhenZooming(true);
		mapMode=0;
		
		Log.v(TAG, "onCreate");
		point = new GeoPoint((int) (45.74084623 * 1E6), (int) (126.63026929 * 1E6));
		mapControl = mapView.getController();
		mapControl.setCenter(point);
		mapControl.setZoom(18);
		
		if(mksearch==null){
			mksearch = new MKSearch();
			mksearch.init(mapManager, new myMKSearchListener());
		}
		touchOverlay = new touchOverlay(this, mksearch);
		mapView.getOverlays().add(touchOverlay);
		
		layoutInflater=(LayoutInflater) getApplication().getSystemService(LAYOUT_INFLATER_SERVICE);
		view=layoutInflater.inflate(R.layout.save_addr_dialog, null);
		
		builder=new Builder(this);
		builder.setTitle("保存自定义地点信息");
		builder.setView(view);
		builder.setPositiveButton("保存", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Log.v(TAG, "in save,name is"+et_name.getText());
				
				/*注意此处判断是否为空串时，一定要先toString(),否则判断不出来*/
				if(et_name.getText().toString().trim().equals("")){
					Toast.makeText(map_activity.this, "地点名称不能为空~", Toast.LENGTH_LONG).show();
				}
				else{
					long returnVal=dbtool.insert(et_name.getText().toString(), lat, lon, et_addrDesc.getText().toString());
					if(returnVal==-1)
						Toast.makeText(map_activity.this, "保存失败~", Toast.LENGTH_LONG).show();
					else{
						Toast.makeText(map_activity.this, "保存成功~", Toast.LENGTH_LONG).show();
						et_name.setText("");
						loadMyItems();
					}
				}
			}
		});
		builder.setNegativeButton("取消", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.cancel();
			}
		});
		dialog=builder.create();    /*用dialog才能避免在重复调用prepareSaveDialog函数时出现一个子view的有多个父容器的错误*/
		et_name=(EditText) view.findViewById(R.id.et_name);
		et_lat=(EditText) view.findViewById(R.id.et_lat);
		et_lon=(EditText) view.findViewById(R.id.et_lon);
		et_addrDesc=(EditText) view.findViewById(R.id.et_addrDesc);
		et_addrDesc.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				et_addrDesc.selectAll();
			}
		});
		
		loadMyItems();   /*此调用要放在mapView初始化之后*/
	}
	
	private void loadMyItems() {
		// TODO Auto-generated method stub
		if(mapView.getOverlays().size()>2){
			mapView.getOverlays().remove(itemOverlay);
			Log.v(TAG, "remove the old itemOverlay");
		}
		GeoPoint point;  
		OverlayItem overlayItem;  
		double lat, lon;
		String name, desc;
		int rowIndex=0;
		cursor=dbtool.select();    /*注意这里要重新读取cursor,因为添加了新项目之后cursor并不是自动更新的*/
		if(cursor.getCount()>0){
			cursor.moveToFirst();
			for(rowIndex=0; rowIndex<cursor.getCount(); rowIndex++){
				Log.v(TAG, "now begin add item, rows is"+cursor.getCount());
				cursor.moveToPosition(rowIndex);
				name=cursor.getString(cursor.getColumnIndex(DBTool.LOCATION_NAME));
				lat=cursor.getDouble(cursor.getColumnIndex(DBTool.LOCATION_LAT));
				lon=cursor.getDouble(cursor.getColumnIndex(DBTool.LOCATION_LON));
				desc=cursor.getString(cursor.getColumnIndex(DBTool.LOCATION_DESC));
				point=new GeoPoint((int)(lat*1E6), (int)(lon*1E6));
				overlayItem = new OverlayItem(point, name, desc);  
				itemOverlay.addItem(overlayItem);  
			}
			Log.v(TAG, "add items over");
	        mapView.getOverlays().add(itemOverlay);
		}
		else
			Toast.makeText(this, "您还没有创建过地点,长按地图尝试创建自己的新地点吧~", Toast.LENGTH_LONG).show();
	}

	public void prepareSaveDialog(GeoPoint point, String addrDesc){
		if(et_lon!=null&&et_lat!=null){
			lat=((double)point.getLatitudeE6())/1000000;
			lon=((double)point.getLongitudeE6())/1000000;
			et_lat.setText(""+lat);
			et_lon.setText(""+lon);
		}
		if(addrDesc!=null)
			et_addrDesc.setText(addrDesc);
		dialog.show();
	}
	/*内部类,用于长按地图时弹出保存对话框*/
	private class myMKSearchListener implements MKSearchListener{

		@Override
		public void onGetAddrResult(MKAddrInfo result, int iError) {
			// TODO Auto-generated method stub
			if(result!=null){
				Log.v(TAG, "now invoke prepareSaveDialig()");
				prepareSaveDialog(result.geoPt, result.strAddr);
				return;
			}
			
			return;
		}

		@Override
		public void onGetDrivingRouteResult(MKDrivingRouteResult result,
				int iError) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onGetPoiResult(MKPoiResult result, int type, int iError) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onGetTransitRouteResult(MKTransitRouteResult result,
				int iError) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onGetWalkingRouteResult(MKWalkingRouteResult result,
				int iError) {
			// TODO Auto-generated method stub
			
		}
		
	}
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		gpsCheck();
		super.onStart();
		Log.v(TAG, "in onStart");
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Log.v(TAG, "in onDestroy");
		if(mapManager!=null)
		{
			mapManager.destroy();
			mapManager = null;
		}
		locationManager = null;
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		Log.v(TAG, "in onPause");
		if(mapManager!=null)
		{
			mapManager.stop();
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		Log.v(TAG, "in onResume");
		if(mapManager!=null)
		{
			mapManager.start();
		}
		super.onResume();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		Log.v(TAG, "in onStop");
		super.onStop();
	}
	
	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean isLocationDisplayed() {
		// TODO Auto-generated method stub
		return locationOverlay.isMyLocationEnabled();
	}
	/*
	 * 因为已经用了MyLocationOverlay，它是专门显示当前位置的覆盖物，而且enableMyLocation()之后，
	 * 自动向gps和net注册了更新,所以不需要自己实现locationListener接口了
	 */
	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		if(location!=null)
		{
			point.setLatitudeE6((int)(location.getLatitude()*1000000));
			point.setLongitudeE6((int)(location.getLongitude()*1000000));
			mapControl.setCenter(point);
			Log.v(TAG, "location change");
		}
	}
	
	/*检查GPS是否开启的函数*/
	private boolean gpsCheck(){
		boolean state = false;
		Log.v(main_activity.TAG, "poi onStart");
		LocationManager lm = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE );
		state = lm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER );
		if(state){}
		else
		{
			Builder b = new AlertDialog.Builder(this).setTitle("GPS未开启")
			.setMessage("为了获得更精确的位置信息,建议您打开GPS,是否现在进行设置？");            
			b.setPositiveButton("是", new DialogInterface.OnClickListener() 
			{                
				public void onClick(DialogInterface dialog, int whichButton) {    
					Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS );
					startActivity(myIntent);
					/*
					Intent mIntent = new Intent("/");                    
					ComponentName comp = new ComponentName(   
					"com.android.settings", 
					"com.android.settings.ACTION_SECURITY_SETTINGS"); 
					mIntent.setComponent(comp);  
					mIntent.setAction("android.intent.action.VIEW");   
					startActivityForResult(mIntent,0);  // 如果在设置完成后需要再次进行操作，可以重写操作代码，在这里不再重写                
					*/
					}            
			  }
			).setNeutralButton("否", new DialogInterface.OnClickListener() 
				{        
					public void onClick(DialogInterface dialog, int whichButton) 
					{
						dialog.cancel();
					}            
				}).show();
		}
		return state;
	}
}
