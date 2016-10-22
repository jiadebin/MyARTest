/**
 *这是这是添加事件界面类 
 * 
 * @author JiaDebin
 * @version 1.0, 20/3/2012
 */

package hit.edu.cn;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dbtool.hit.edu.cn.DBTool;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class alarm_activity extends Activity implements OnClickListener, DialogInterface.OnClickListener{

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflater=getMenuInflater();
		inflater.inflate(R.menu.alarm_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.item_help_alarm) {
			AlertDialog.Builder builder=new Builder(this);
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.cancel();
				}
			});
			builder.setMessage(R.string.alarm_help);
			builder.setTitle("使用帮助");
			builder.create().show();
		}
		return super.onOptionsItemSelected(item);
	}
	public static Intent intent = null;
	public static final String TAG = "alarm";
	private InputMethodManager imm;  
	static String alarm_search;
	static String myloc_name;
	static int whether_from_myloc;   /*表示是否从数据库中选择地点*/
	
	static String thing_desc = "";
	private Button btn_myloc;
	private Button btn_ok;
	private Button btn_cancel;
	private EditText desc;
	public EditText hour;
	public EditText minute;
	private Spinner spinner;
	private List<String> words; 
	private ArrayAdapter<String> adapter;
	private final static String[] PLACES = new String[]{"请选择", "超市", "KTV", "饭店", "书店", "理发店", "图书馆"};
	
	int hour_set;
	int min_set;
	
	CharSequence[] items;
	AlertDialog.Builder builder;
	AlertDialog dialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_activity);
		whether_from_myloc=0;   
		imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		spinner = (Spinner) findViewById(R.id.spinner);
		words = new ArrayList<String>();
		for(int i=0; i<PLACES.length; i++)
			words.add(PLACES[i]);
		adapter = new ArrayAdapter<String>(getApplication(), android.R.layout.simple_spinner_item, words);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				alarm_search = PLACES[arg2];
				arg0.setVisibility(View.VISIBLE);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		desc = (EditText) findViewById(R.id.description);
		hour = (EditText) findViewById(R.id.hour);
		minute = (EditText) findViewById(R.id.minute);
		
		hour.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				hour.selectAll();
			}
		});
		minute.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				minute.selectAll();
			}
		});
		btn_cancel = (Button) findViewById(R.id.btn_cancel);
		btn_cancel.setOnClickListener(this);
		btn_ok = (Button) findViewById(R.id.btn_ok);
		btn_ok.setOnClickListener(this);
		btn_myloc=(Button) findViewById(R.id.btn_myLoc);
		btn_myloc.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				loadMylocData();    
				builder = new AlertDialog.Builder(alarm_activity.this);   
				builder.setTitle("选择一个地点");   
				builder.setSingleChoiceItems(items, -1, alarm_activity.this);   
				dialog=builder.create(); 
				dialog.show();
			}
		});
	}
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btn_cancel) {
			desc.setText("");
			hour.setText("0");
			minute.setText("0");
		} else if (v.getId() == R.id.btn_ok) {
			if(desc.hasFocus())
				imm.hideSoftInputFromWindow(desc.getWindowToken(), 0);  //隐藏输入键盘
			if(hour.hasFocus())
				imm.hideSoftInputFromWindow(hour.getWindowToken(), 0);  //隐藏输入键盘
			if(minute.hasFocus())
				imm.hideSoftInputFromWindow(minute.getWindowToken(), 0);  //隐藏输入键盘
			thing_desc = desc.getText().toString();
			hour_set = Integer.parseInt(hour.getText().toString());
			min_set = Integer.parseInt(minute.getText().toString());
			if(thing_desc.equals("")){
				Toast.makeText(getApplicationContext(), "您还未填写描述信息！", Toast.LENGTH_LONG).show();
			}
			if(hour_set==0&&min_set==0){
				Toast.makeText(getApplicationContext(), "您还未设置时间！", Toast.LENGTH_LONG).show();
			}
			AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			intent = new Intent(getBaseContext(), notification_service.class);
			PendingIntent pend = PendingIntent.getService(getApplicationContext(), 0, intent, 0);
			Date t = new Date();
			t.setTime(System.currentTimeMillis()+60*60*1000*hour_set+60*1000*min_set);
			alarm.set(AlarmManager.RTC_WAKEUP, t.getTime(), pend);
			Toast.makeText(getApplicationContext(), "设置成功", Toast.LENGTH_SHORT).show();
			alarm_activity.this.finish();
		}
	}

	private boolean netStateCheck()
    {
		
		boolean state = false;
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		cm.getActiveNetworkInfo();
		if(cm.getActiveNetworkInfo()!=null){
			state = cm.getActiveNetworkInfo().isAvailable();
			Log.v(TAG, "main onStart");
		}
		if(!state)
		{	
			Builder b = new AlertDialog.Builder(this).setTitle("网络未连接")
			.setMessage("由于搜索地点时需要联网,\n是否现在对网络进行设置？");            
			b.setPositiveButton("是", new DialogInterface.OnClickListener() 
			{                
				public void onClick(DialogInterface dialog, int whichButton) {                    
					Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
					startActivity(intent);
					
					/*
					Intent mIntent = new Intent("/");                    
					ComponentName comp = new ComponentName(   
					"com.android.settings", 
					"com.android.settings.WirelessSettings"); 
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
	@Override
	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
		whether_from_myloc=1;
		Toast.makeText(this, "您选择了:"+items[which], Toast.LENGTH_SHORT).show(); 
		myloc_name=items[which].toString();
		dialog.dismiss();
	}
	
	void loadMylocData() {
		// TODO Auto-generated method stub
		DBTool dbtool=new DBTool(this);
		Cursor cursor=dbtool.select();
		if(cursor.getCount()>0){
			ArrayList<String> nameList=new ArrayList<String>();
			cursor.moveToFirst();
			while(!cursor.isAfterLast()){
				nameList.add(cursor.getString(cursor.getColumnIndex(DBTool.LOCATION_NAME)));
				cursor.moveToNext();
			}
			items=nameList.toArray(new CharSequence[nameList.size()]);
		}
		else
			Toast.makeText(this, "您还没有创建过地点哦~", Toast.LENGTH_LONG).show();
		cursor.close();
		dbtool.close();
	}
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		netStateCheck();
		super.onStart();
	}
}
