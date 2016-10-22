package activity.hit.edu.cn;

import hit.edu.cn.R;
import hit.edu.cn.main_activity;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;


import activity.hit.edu.cn.MyRefreshListView.OnRefreshListener;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import ar.hit.edu.cn.ARNavigatorActivity;
import ar.hit.edu.cn.POIBean;

public class MyActivitiesRefreshActivity extends ListActivity {   
	MyRefreshListView listView;
	private static final String TAG="My";
	static List<Map<String, Object>> list=null;
	static String lastRefreshTime="2012-01-01 00:00:00";
	static String lastRefreshTimeForDisplay="2012-01-01 00:00:00";
	
	public static final String wsUrl=main_activity.HOST+"activityInfo?wsdl";
	public static final String wsNamespace="http://ws.apache.org/axis2";
	int activityNum;
 
	private int netState=0;
	private Builder netDialog=null;
	
	private Handler handler;
	protected static final int NETWORK_ERROR = 1;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.v(TAG, "in onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pull_to_refresh);

        listView = (MyRefreshListView) getListView();
        listView.setSelector(R.drawable.remove_listview_default_selector_color); //去掉ListView Selector选中时默认黄色底纹一闪的效果 
        // Set a listener to be invoked when the list should be refreshed.
        listView.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Do work to refresh the list here.
                new GetDataTask().execute();
            }
        });

        listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
//				Toast.makeText(MyActivitiesRefreshActivity.this, ""+position, Toast.LENGTH_SHORT).show();
    			if (((MyRefreshListView)parent).getTag() != null){                          
                     ((View)((MyRefreshListView)parent).getTag()).setBackgroundDrawable(null);
                }
                ((MyRefreshListView)parent).setTag(view);
                view.setBackgroundColor(Color.DKGRAY); 
                
				if(list!=null){
					AlertDialog.Builder builder=new Builder(MyActivitiesRefreshActivity.this);
					builder.setPositiveButton("我知道了", null);
					builder.setTitle("详细信息");
					String msg=list.get(position-1).get("content").toString();
					if(msg!=null){
						builder.setMessage(msg);
					}
					else{
						builder.setMessage("无详细信息~");
					}
					AlertDialog ad=builder.create();
					ad.setCanceledOnTouchOutside(true);
					ad.show();
				}
				
			}
		});
        SimpleAdapter adapter = new SimpleAdapter(this, getData(), R.layout.listviewitemlayout,
        		new String[]{"title", "time"},
        		new int[]{R.id.title, R.id.time});

        setListAdapter(adapter);
        
        handler=new Handler(){

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				switch(msg.what){
					case NETWORK_ERROR:
						Log.v(TAG, "POIBean is undone");
						Toast.makeText(MyActivitiesRefreshActivity.this, "网络传输出错~~", Toast.LENGTH_LONG).show();
						break;
				}
			}
	    	
	    };
    }

    private List<Map<String, Object>> getData(){
    	if(list==null){
    		Log.v(TAG, "in getData, in if");
    		list=new ArrayList<Map<String, Object>>();
        	Map<String, Object> map=new HashMap<String, Object>();
        	map.put("title", "欢迎使用 ~");
        	map.put("time", "提示:点击列表条目可以查看详细信息. ");
        	map.put("content", "欢迎使用全能出行助手！");
        	list.add(0, map);
    	}
    	else{
    		Log.v(TAG, "in getData, in else");
    		listView.onRefreshComplete("最后更新时间："+lastRefreshTimeForDisplay);
    	}    	
    	return list;
    }
    private class GetDataTask extends AsyncTask<Void, Void, String[]> {

    	@Override
        protected String[] doInBackground(Void... params) {
            // Simulates a background job.
            
            String wsMethodname="getActivityNum";
    		SoapObject request=new SoapObject(wsNamespace, wsMethodname);
    		request.addProperty("lastTime", lastRefreshTime);
    		SoapSerializationEnvelope envelop=new SoapSerializationEnvelope(SoapEnvelope.VER11);
    		envelop.bodyOut=request;
    		HttpTransportSE ht=new HttpTransportSE(wsUrl);
    		try {
    			Log.v(TAG, "in doinbackground");
    			ht.call(null, envelop);
    			if(envelop.getResponse()!=null){
//    				Log.v(TAG, "aaaaaaaaaaaaaaaaaaaaaaaaaaaa");
    				Object result=(Object) envelop.getResponse();
    				
    				activityNum=Integer.parseInt(result.toString());
    				Log.v(TAG, ""+activityNum);
//    				Toast.makeText(this.context, ""+activityNum, Toast.LENGTH_LONG).show();  //在这里用不了
    				
    				wsMethodname="getActivityInfo";
    	    		envelop=new SoapSerializationEnvelope(SoapEnvelope.VER11);
    	    		/*将序号当做id,这么做不太好，但是没办法了*/
    				for(int i=activityNum; i>=1; i--){
    					request=new SoapObject(wsNamespace, wsMethodname);
    					request.addProperty("num", i);
    					Log.v(TAG, "i="+i);
    					envelop.bodyOut=request;
	    	    		ht.call(null, envelop);
		    			if(envelop.getResponse()!=null){
		    				SoapObject activityBean=(SoapObject) envelop.getResponse();
		    				
		    				String id=activityBean.getProperty("id").toString();
		    				Log.v(TAG, "return id="+id);
		    				String title=activityBean.getProperty("title").toString();
		    				String content=activityBean.getProperty("content").toString();
		    				String time=activityBean.getProperty("publishtime").toString();
		    				
		    				lastRefreshTime=time;   //更新最后刷新时间
		    				
		    				Map<String, Object> map=new HashMap<String, Object>();
		    		    	map.put("title", title);
		    		    	map.put("time",    "发布时间："+time);
		    		    	map.put("content", "  "+content);
		    		    	list.add(0, map);
		    			}		    			
		    			else{}
		    			request=null;
    				}
    			}
    			else{}
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			Log.v(TAG, "io");
    			e.printStackTrace();
    			Message message=handler.obtainMessage();
				message.what=NETWORK_ERROR;
				message.sendToTarget();
    		} catch (XmlPullParserException e) {
    			// TODO Auto-generated catch block
    			Log.v(TAG, "xml");
    			e.printStackTrace();
    			Message message=handler.obtainMessage();
				message.what=NETWORK_ERROR;
				message.sendToTarget();
			}
            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {

            DateFormat   df   =   new   SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");   
            lastRefreshTimeForDisplay=df.format(new   Date());
            listView.onRefreshComplete("最后更新时间："+lastRefreshTimeForDisplay);

            super.onPostExecute(result);
        }
    }
    @Override
	protected void onStart() {
		// TODO Auto-generated method stub
		Log.v(TAG, "in onStart");
		super.onStart();
		if(netState==0){
			checkNetwork();
		}
	}
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
				.setMessage("为了与服务器通信以获取活动信息,请确保手机能够连接网络,是否现在设置?(由于网络打开时需要一定时间进行初始化连接," +
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
}
