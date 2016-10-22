/**
 *���Ǳ�Ӧ�õ��������� 
 * 
 * @author JiaDebin
 * @version 1.0, 20/3/2012
 */

package hit.edu.cn;


import activity.hit.edu.cn.MyActivitiesRefreshActivity;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import ar.hit.edu.cn.ARNavigatorActivity;

public class main_activity extends Activity {
	/*
	 * ������������ʱ��IP�����ǵ��Ե���ʵIP����10.0.2.2�����������ʱ��Ҫ����ʵIP���������Ҫ��ͬһ������
	 * 127.0.0.1��local host��������������,����������ARNavigator�ﶨ��һ����̬�����洢���URL
	 */
//	public static final String HOST="http://192.168.166.1:8080/axis2/services/";
	public static final String HOST="http://photo.hit.edu.cn/axis2/services/";
	private Intent intent=null;
	static final String TAG = "main_activity";
	Intent click_intent;
	private ImageButton ib_alarm;
	private ImageButton ib_map;
	private ImageButton ib_poi;
	private ImageButton ib_ar;
	private ImageButton ib_activity;
//	public static BMapManager mapManager;
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		AlertDialog.Builder builder;
		if (item.getItemId() == R.id.item_help_main) {
			builder=new Builder(this);
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.cancel();
				}
			});
			builder.setMessage(R.string.help);
			builder.setTitle("ʹ�ð���");
			builder.create().show();
		} else if (item.getItemId() == R.id.item_exit) {
			this.finish();
			/*
			 *�ҳ��������´�������ȫ�˳�����Ľ��̣�����service��Ȼ�����У����ҵ����ʱ֪ͨʱ�������Ϊ�����activity�����ر���
			 *���Ի��ǲ���ȫ�˳��ˣ������û��������һЩ�� 
			 
			ActivityManager activityMgr = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
			activityMgr.restartPackage(getPackageName());
			activityMgr.killBackgroundProcesses(getPackageName());
			android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(0);
			*/
		} else if (item.getItemId() == R.id.item_ablout) {
			builder=new Builder(this);
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.cancel();
				}
			});
			builder.setMessage(R.string.about);
			builder.setTitle("������");
			builder.create().show();
		}
		return super.onOptionsItemSelected(item);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflater=getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		
		return true;
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Log.v(TAG, "main destroy");
		super.onDestroy();
	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		Log.v(TAG, "main pause");
		super.onPause();
	}
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		Log.v(TAG, "main onRestart");
		super.onRestart();
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		Log.v(TAG, "main onResume");
		super.onResume();
	}
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		Log.v(TAG, "main onStop");
		super.onStop();
	}
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Log.v(TAG, "main create");

        intent = this.getIntent();  /*����ظ�����main����ʱ�Ƿ��ֱ�ӽ���POI��ͼ������*/
//    	if(intent==null)
//    		Log.v(TAG, "intent is null");
//    	else
//    		Log.v(TAG, "intent is not null");
//        mapManager = new BMapManager(getApplication());
//		mapManager.init("D52F4D689069CE4D8CC4271C2D4F7CA10D1B4F89", null);
        ib_alarm = (ImageButton)findViewById(R.id.ib_alarm);
        ib_map = (ImageButton)findViewById(R.id.ib_map);
        ib_poi = (ImageButton) findViewById(R.id.ib_poi);
        ib_ar = (ImageButton) findViewById(R.id.ib_ar);
        ib_activity = (ImageButton) findViewById(R.id.ib_activity);
        ib_alarm.setOnTouchListener(touch);
        ib_alarm.setOnClickListener(mylistener);
        ib_map.setOnTouchListener(touch);
        ib_map.setOnClickListener(mylistener);
        ib_poi.setOnTouchListener(touch);
        ib_poi.setOnClickListener(mylistener);
        ib_ar.setOnTouchListener(touch);
        ib_ar.setOnClickListener(mylistener);
        ib_activity.setOnTouchListener(touch);
        ib_activity.setOnClickListener(mylistener);
        
		if(intent!=null&&intent.getStringExtra("require_name")!=null){
			Intent nintent;
			if(alarm_activity.whether_from_myloc!=1){
				nintent = new Intent(main_activity.this, poi_activity.class);
				nintent.putExtra("require_name", "notification");
				nintent.putExtra("search_word", intent.getStringExtra("search_word"));
				nintent.putExtra("desc", intent.getStringExtra("desc"));
			}
			else{
				nintent = new Intent(main_activity.this, poi_activity.class);
				nintent.putExtra("require_name", "notification");
				nintent.putExtra("search_word", intent.getStringExtra("search_word"));
				nintent.putExtra("desc", intent.getStringExtra("desc"));
			}
			intent = null;
			startActivity(nintent);
		}
    }
    OnTouchListener touch = new OnTouchListener() {
    	public final float[] BT_SELECTED = new float[] {1,0,0,0,50,0,1,0,0,50,0,0,1,0,50,0,0,0,1,0};  
    	public final float[] BT_NOT_SELECTED = new float[] {1,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0,1,0};  
		
    	@Override
    	public boolean onTouch(View v, MotionEvent event) {  

			if (event.getAction() == MotionEvent.ACTION_DOWN) {

				v.getBackground().setColorFilter(

				new ColorMatrixColorFilter(BT_SELECTED));

				v.setBackgroundDrawable(v.getBackground());

			} 
			else if (event.getAction() == MotionEvent.ACTION_UP) {

				v.getBackground().setColorFilter(

				new ColorMatrixColorFilter(BT_NOT_SELECTED));

				v.setBackgroundDrawable(v.getBackground());
			}
			return false;
    	}  
	};
    OnClickListener mylistener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v.getId() == R.id.ib_alarm) {
				click_intent = new Intent(main_activity.this, alarm_activity.class);
				startActivity(click_intent);
			} else if (v.getId() == R.id.ib_map) {
				click_intent = new Intent(main_activity.this, map_activity.class);
				startActivity(click_intent);
			} else if (v.getId() == R.id.ib_poi) {
				click_intent = new Intent(main_activity.this, poi_activity.class);
				click_intent.putExtra("require_name", "main_activity");
				startActivity(click_intent);
			} else if (v.getId() == R.id.ib_ar) {
				click_intent = new Intent(main_activity.this, ARNavigatorActivity.class);
				click_intent.putExtra("require_name", "main_activity");
				startActivity(click_intent);
			} else if (v.getId() == R.id.ib_activity) {
				click_intent = new Intent(main_activity.this, MyActivitiesRefreshActivity.class);
				startActivity(click_intent);
			} else {
			}
		}
	};
}