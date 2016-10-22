/**
 *这是响应地图长按事件的覆盖物类 
 * 
 * @author JiaDebin
 * @version 1.0, 20/3/2012
 */

package hit.edu.cn;

import android.content.Context;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.MKSearch;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.Overlay;
import com.baidu.mapapi.Projection;

public class touchOverlay extends Overlay implements OnGestureListener {

	private GestureDetector gst;
	private Projection projection = null;
	private GeoPoint point = null;
	private MKSearch mksearch;
	private Context context;
	public touchOverlay(Context context, MKSearch inMksearch) {
		super();
		this.context=context;
		this.mksearch = inMksearch;
		gst = new GestureDetector(context, this);
		gst.setIsLongpressEnabled(true);
	}

	@Override
	public boolean onTouchEvent(MotionEvent mv, MapView mapView) {
		// TODO Auto-generated method stub
	    super.onTouchEvent(mv, mapView);
	    projection = mapView.getProjection();
	    gst.onTouchEvent(mv);   /*注意这个地方，如果不写这句话。则长按时会没有反应！！！*/
	    switch(mv.getAction()){
	    case MotionEvent.ACTION_DOWN:
//	    	Log.v(map_activity.TAG, "press");    /*调试用*/
	    	break;
	    case MotionEvent.ACTION_MOVE:
	    	break;
	    case MotionEvent.ACTION_UP:
//	    	Log.v(map_activity.TAG, "up");
	    	break;
	    default:
	    	break;
	    }	    
	    return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		Log.v(poi_activity.TAG, "long press");
    	Toast.makeText(this.context, "正在查询地点信息...", Toast.LENGTH_LONG).show(); 
    	if(projection!=null){
    		Log.v(poi_activity.TAG, "projection is ready");
    		point = projection.fromPixels((int)e.getX(), (int)e.getY());
    		Log.v(poi_activity.TAG, e.getX()+"  "+e.getY());
    		Log.v(poi_activity.TAG, point.getLatitudeE6()+"  "+point.getLongitudeE6());
    		mksearch.reverseGeocode(point);
    	}
    	else 
    		Log.v(map_activity.TAG, "projection is null");
	}
	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}
}
