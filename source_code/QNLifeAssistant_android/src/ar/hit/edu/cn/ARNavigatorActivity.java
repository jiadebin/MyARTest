/**
 *这是实景导航主界面类 
 * 
 * @author JiaDebin
 * @version 1.0, 20/3/2012
 */

package ar.hit.edu.cn;

import hit.edu.cn.R;
import hit.edu.cn.main_activity;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.wikitude.architect.ArchitectUrlListener;
import com.wikitude.architect.ArchitectView;

public class ARNavigatorActivity extends Activity implements ArchitectUrlListener{

	
	/*本机自建无线网ip*/
	/*图片库服务器*/
	public static final String wsUrl=main_activity.HOST+"picTransport?wsdl";
	
	public static final String wsNamespace="http://ws.apache.org/axis2";
	
	private static final String TAG="ARNavigatorActivity";
	
	private final double START_LATITUDE=45.74084623;
	private final double START_LONGITUDE=126.63026929;

	private ArchitectView arview;
	private LocationManager lm;
	private Location location=null;
	private myLocationListener networkListener;
	private myLocationListener gpsListener;
	private int poiNum;
	private List<POIBean> poiBeanList=new ArrayList<POIBean>();
	private JSONArray array=new JSONArray();
	private Handler handler;
	protected static final int TASK_DONE = 0;
	protected static final int TASK_UNDONE = 1;
	protected static final int LOAD_COMPLETE = 2;
	
	private int netState=0;
	private int gpsState=0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); //隐藏标题
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); 
		
		if(!ArchitectView.isDeviceSupported(this)){
			Toast.makeText(this, "很抱歉，该设备硬件不支持此功能", Toast.LENGTH_LONG);
			this.finish();
			return;
		}
		setContentView(R.layout.ar_navigator);
		
		arview=(ArchitectView) findViewById(R.id.architectview);
		arview.onCreate("");
		
		lm=(LocationManager) getSystemService(Context.LOCATION_SERVICE);
		networkListener=new myLocationListener();
        gpsListener=new myLocationListener();
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, networkListener);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpsListener);
        
        loadPoiAsyncTask loadPoiTask=new loadPoiAsyncTask(this);
        loadPoiTask.execute(new Object());
        handler=new Handler(){

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				switch(msg.what){
				case TASK_DONE:
					Log.v(TAG, "POIBean is ok");
					POIBean abean=(POIBean)msg.obj;
					poiBeanList.add(abean);
    				try {
						array.put(abean.toJSONObject());
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				case TASK_UNDONE:
					Log.v(TAG, "POIBean is undone");
					Toast.makeText(ARNavigatorActivity.this, "Sorry!网络传输出错,请确保您的手机网络处于打开状态,现在请退出尝试重新启动本应用~~", Toast.LENGTH_LONG).show();
					break;
				case LOAD_COMPLETE:
					Log.v(TAG, "load complete");
					try {
						loadMyArchitectWorld();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Toast.makeText(ARNavigatorActivity.this, "信息加载完毕,Now用手机摄像头扫描一下四周,看看有什么新发现~~", Toast.LENGTH_LONG).show();
					
					break;
				}
			}
	    	
	    };
	}

	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onPostCreate(savedInstanceState);
		Log.v(TAG, "in onPostCreate!!!");
		if(arview!=null){
			arview.onPostCreate();
		}
	    arview.registerUrlListener(this);
	
	}


	private class loadPoiAsyncTask extends AsyncTask<Object, Integer, POIBean>{

		@Override
		protected void onPostExecute(POIBean result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			Log.v(TAG, "in onPostExcute");
			progress.dismiss();
		}
		private ProgressDialog progress;
		public loadPoiAsyncTask(Context context){
			progress=new ProgressDialog(context);
			progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progress.setIndeterminate(false);
			progress.setCancelable(true);
			progress.setMessage("正在加载地点信息,请稍等...");
			progress.show();
		}
		@Override
		protected POIBean doInBackground(Object... arg0) {
			// TODO Auto-generated method stub
			while(netState==0){
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
    		String wsMethodname="getPoiNum";
    		SoapObject request=new SoapObject(wsNamespace, wsMethodname);
    		SoapSerializationEnvelope envelop=new SoapSerializationEnvelope(SoapEnvelope.VER11);
    		envelop.bodyOut=request;
    		HttpTransportSE ht=new HttpTransportSE(wsUrl);
    		
    		try {
    			ht.call(null, envelop);
    			if(envelop.getResponse()!=null){
    				Object result=(Object) envelop.getResponse();
    				
    				poiNum=Integer.parseInt(result.toString());
    				
    				wsMethodname="getPoiBean";
    	    		envelop=new SoapSerializationEnvelope(SoapEnvelope.VER11);
    	    		/*将序号当做id,这么做不太好，但是没办法了*/
    				for(int i=1; i<=poiNum; i++){
    					request=new SoapObject(wsNamespace, wsMethodname);
    					request.addProperty("id", i);
    					Log.v(TAG, "i="+i);
    					envelop.bodyOut=request;
	    	    		ht.call(null, envelop);
		    			if(envelop.getResponse()!=null){
		    				SoapObject poi=(SoapObject) envelop.getResponse();
		    				
		    				String id=poi.getProperty("id").toString();
		    				Log.v(TAG, "return id="+id);
		    				String name=poi.getProperty("name").toString();
		    				String desc=poi.getProperty("description").toString();
		    				int type=Integer.parseInt(poi.getProperty("type").toString());
		    				double lat=Double.parseDouble(poi.getProperty("latitude").toString());
		    				double lon=Double.parseDouble(poi.getProperty("longitude").toString());
		    				POIBean bean=new POIBean(id, name, desc, type, lat, lon);
		    				Message message=handler.obtainMessage();
		    				message.what=TASK_DONE;
		    				message.obj=bean;
		    				message.sendToTarget();
		    			}
		    			
		    			else{
		    				Message message=handler.obtainMessage();
		    				message.what=TASK_UNDONE;
		    				message.sendToTarget();
		    			}
		    			request=null;
    				}
    				Message message=handler.obtainMessage();
    				message.what=LOAD_COMPLETE;
    				message.sendToTarget();
    			}
    			else{
    				Message message=handler.obtainMessage();
    				message.what=TASK_UNDONE;
    				message.sendToTarget();
    			}
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    			Message message=handler.obtainMessage();
				message.what=TASK_UNDONE;
				message.sendToTarget();
    		} catch (XmlPullParserException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    			Message message=handler.obtainMessage();
				message.what=TASK_UNDONE;
				message.sendToTarget();
			}
			return null;
		}
		
	}
	private void loadMyArchitectWorld() throws IOException, JSONException {
		// TODO Auto-generated method stub
		Log.v(TAG, "in loadMyArchitectWorld");
		arview.load("tutorial1.html");
		
		arview.callJavascript("newData('"+array.toString()+"')");
	}

	@Override
	public boolean urlWasInvoked(String url) {
		// TODO Auto-generated method stub
		Log.v(TAG, "url is invoked!"+url);
		
		List<NameValuePair> queryParams = URLEncodedUtils.parse(URI.create(url), "UTF-8");
		
		String id = "";
		// getting the values of the contained GET-parameters
		for(NameValuePair pair : queryParams)
		{
			if(pair.getName().equals("id"))
			{
				id = pair.getValue();
			}
		}
		
		/*
		 * get the corresponding poi bean for the given id
		 * 根据点击的id来找到这个Bean，注意这里不能饿直接把id当做该bean在list里的位置，因为位置是从0开始的，而id
		 * 从一开始，所以要-1
		 */
		POIBean bean = poiBeanList.get(Integer.parseInt(id)-1);
		//start a new intent for displaying the content of the bean
		Intent intent = new Intent(this, ARDetailActivity.class);
		intent.putExtra("ID", bean.getId());
		intent.putExtra("NAME", bean.getName());
		intent.putExtra("DESC", bean.getDescription());
		this.startActivity(intent);
		return true;
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		Log.v(TAG, "in onReaume");
		super.onResume();
		if(arview!=null){
			arview.onResume();
		}
		//location=lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if(location!=null){
			arview.setLocation((float)location.getLatitude(), (float)location.getLongitude(), location.getAccuracy());
		}
		else
			arview.setLocation((float)START_LATITUDE, (float)START_LONGITUDE, 1f);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Log.v(TAG, "in onDestroy");
		super.onDestroy();
		if(arview!=null){
			arview.onDestroy();
		}
	}

	@Override
	public void onLowMemory() {
		// TODO Auto-generated method stub
		Log.v(TAG, "in onLowMemory");
		super.onLowMemory();
		if(arview!=null){
			arview.onLowMemory();
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		Log.v(TAG, "in onPause");
		super.onPause();
		if(arview!=null){
			arview.onPause();
			}
		}
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		Log.v(TAG, "in onRestart");
		super.onRestart();
	}


	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		Log.v(TAG, "in onStart");
		super.onStart();
		if(gpsState==0){
			checkGPS();
		}
		if(netState==0){
			checkNetwork();
		}
	}


	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		Log.v(TAG, "in onStop");
		super.onStop();
	}
	private Builder gpsDialog=null;
	private Builder netDialog=null;
	
	private void checkNetwork()
    {
		Log.v(TAG, "in net check!");
        ConnectivityManager conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        State mobile = conMan.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
        State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
        
        //如果3G网络和WIFI网络都未连接，且不是处于正在连接状态 则进入Network Setting界面 由用户配置网络连接
        if(mobile==State.CONNECTED||wifi==State.CONNECTED){
        	netState=1;
        	return;
        }
        else{  /*此处处理与gpsDialog有所不同 ,因为gps提示框先弹出,所以在最下面,而网络提示窗口在最上面
         		所以允许本窗口再次检查再次弹出
         		*/
        	
	        	netState=0;
	        	
	        	netDialog = new AlertDialog.Builder(this).setTitle("检测到您处于断网状态")
				.setMessage("为了与服务器通信以获取地点信息,请确保手机能够连接网络,是否现在设置?(由于网络打开时需要一定时间进行初始化连接," +
						"所以如果您确定已经打开了,那么请点击右侧按钮取消该提示)");
	        	netDialog.setPositiveButton("马上去设置", new DialogInterface.OnClickListener() 
				{                
					public void onClick(DialogInterface dialog, int whichButton) {    
						Intent myIntent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
						startActivity(myIntent);
						}            
				  }
				).setNeutralButton("不,已经打开", new DialogInterface.OnClickListener() 
				{        
					public void onClick(DialogInterface dialog, int whichButton) 
					{
						dialog.cancel();
					}            
				}).show();
        	}
    }
	
	/*
	 * 检查GPS是否开启的函数
	 */
	
	private void checkGPS(){
		boolean state = false;
		Log.v(TAG, "in gpsCheck");
		LocationManager lm = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE );
		state = lm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER );
		if(state==true){
			gpsState=1;
			return;
		}
		else
		{
			gpsState=0;
			if(gpsDialog!=null){    /*防止该对话框弹出两次*/
//				gpsDialog.show();
			}
			else{
				gpsDialog = new AlertDialog.Builder(this).setTitle("检测到GPS未开启")
				.setMessage("为了获得更精确的地点信息,请确保您手机的GPS已开启,是否现在去设置?(由于GPS打开时需要一定时间进行初始化," +
						"所以如果您确定已经打开了,那么请点击右侧按钮取消该提示)");            
				gpsDialog.setPositiveButton("马上去设置", new DialogInterface.OnClickListener() 
				{                
					public void onClick(DialogInterface dialog, int whichButton) {    
						Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS );
						startActivity(myIntent);
						}            
				  }
				).setNeutralButton("不,已经打开", new DialogInterface.OnClickListener() 
					{        
						public void onClick(DialogInterface dialog, int whichButton) 
						{
							dialog.cancel();
						}            
					}).show();
			}
		}
	}
	
private class myLocationListener implements LocationListener{

		
		@Override
		public void onLocationChanged(Location newloc) {
			// TODO Auto-generated method stub
			if(isBetterLocation(newloc, location))
			{
				location=newloc;
			}
			else
				return;  //do nothing
			
			if(location.getProvider().equals(LocationManager.NETWORK_PROVIDER)){
//				Log.v(TAG, "provider is network,so remove it!");
				lm.removeUpdates(this);
			}
			else if(location.getProvider().equals(LocationManager.GPS_PROVIDER)){
//				Log.v(TAG, "provider is gps!");
			}
			else
				Log.v(TAG, "cannot get the location provider!");
			
			//Very important to inform ArchitectView about location changes
			if(arview != null)
				arview.setLocation((float)(location.getLatitude()), (float)(location.getLongitude()), location.getAccuracy());
		
		}

		

		/** Determines whether one Location reading is better than the current Location fix
		  * @param location  The new Location that you want to evaluate
		  * @param currentBestLocation  The current Location fix, to which you want to compare the new one
		  */
		private static final int TWO_MINUTES = 1000 * 60 * 2;    //2 minutes
		protected boolean isBetterLocation(Location location, Location currentBestLocation) {
		    if (currentBestLocation == null) {
		        // A new location is always better than no location
		        return true;
		    }

		    // Check whether the new location fix is newer or older
		    long timeDelta = location.getTime() - currentBestLocation.getTime();
		    boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
		    boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
		    boolean isNewer = timeDelta > 0;

		    // If it's been more than two minutes since the current location, use the new location
		    // because the user has likely moved
		    if (isSignificantlyNewer) {
		        return true;
		    // If the new location is more than two minutes older, it must be worse
		    } else if (isSignificantlyOlder) {
		        return false;
		    }

		    // Check whether the new location fix is more or less accurate
		    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
		    boolean isLessAccurate = accuracyDelta > 0;
		    boolean isMoreAccurate = accuracyDelta < 0;
		    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		    // Check if the old and new location are from the same provider
		    boolean isFromSameProvider = isSameProvider(location.getProvider(),
		            currentBestLocation.getProvider());

		    // Determine location quality using a combination of timeliness and accuracy
		    if (isMoreAccurate) {
		        return true;
		    } else if (isNewer && !isLessAccurate) {
		        return true;
		    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
		        return true;
		    }
		    return false;
		}

		/** Checks whether two providers are the same */
		private boolean isSameProvider(String provider1, String provider2) {
		    if (provider1 == null) {
		      return provider2 == null;
		    }
		    return provider1.equals(provider2);
		}
		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}
	}
}
