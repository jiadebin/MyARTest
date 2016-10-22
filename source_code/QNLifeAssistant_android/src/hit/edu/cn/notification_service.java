/**
 *这是提供定时功能的后台服务类 
 * 
 * @author JiaDebin
 * @version 1.0, 20/3/2012
 */

package hit.edu.cn;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class notification_service extends Service{

	static final String TAG = "SERVICE";
	Notification notification = null;
	NotificationManager nmanager = null;
	private static final int notification_id = 1234567;
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		nmanager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		show();
	}
		
	public void show() {
		// TODO Auto-generated method stub
		Intent next_intent = new Intent(getApplicationContext(), main_activity.class);
		
		next_intent.putExtra("require_name", "notification");
		
		/*此处有两种情况：从数据库中选择或者直接选择了类别*/
		next_intent.putExtra("search_word", ((alarm_activity.whether_from_myloc==1)?alarm_activity.myloc_name:alarm_activity.alarm_search));
		next_intent.putExtra("desc", alarm_activity.thing_desc);
		PendingIntent pend = PendingIntent.getActivity(getApplicationContext(), 0, next_intent, 0);
		notification = new Notification(R.drawable.pop, "LifeAssistant提醒", System.currentTimeMillis());
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		
		notification.ledARGB = 0xff00ff00; // LED灯的颜色，绿灯
		notification.ledOnMS = 300; // LED灯显示的毫秒数，300毫秒
		notification.ledOffMS = 1000; // LED灯关闭的毫秒数，1000毫秒
		notification.flags |= Notification.FLAG_SHOW_LIGHTS; // 必须加上这个标志,而且注意符号是|=竖扛不能少，否则无效
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.setLatestEventInfo(getApplicationContext(), "助手提示", "您有预设事件，请查看", pend);
		nmanager.notify(notification_id, notification);
		
	}
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
