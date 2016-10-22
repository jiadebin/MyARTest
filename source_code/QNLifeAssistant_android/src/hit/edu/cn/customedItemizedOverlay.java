/**
 *这是自定义地点覆盖物类 
 * 
 * @author JiaDebin
 * @version 1.0, 20/3/2012
 */

package hit.edu.cn;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import com.baidu.mapapi.ItemizedOverlay;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.OverlayItem;
import com.baidu.mapapi.Projection;

public class customedItemizedOverlay extends ItemizedOverlay<OverlayItem> {

	private Context context;
	private ArrayList<OverlayItem> itemList=new ArrayList<OverlayItem>();
	public customedItemizedOverlay(Drawable defaultMarker) {
		super(boundCenter(defaultMarker));
		// TODO Auto-generated constructor stub
	}

	public customedItemizedOverlay(Drawable marker, Context context){
		super(boundCenter(marker));
		this.context=context;
		
	}
	
	public void addItem(OverlayItem item){
		itemList.add(item);
		this.populate();
	}
	
	@Override
	protected boolean onTap(int index) {
		// TODO Auto-generated method stub
		setFocus(itemList.get(index));
		Toast.makeText(context, itemList.get(index).getSnippet(),	Toast.LENGTH_LONG).show();
		return true;
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean arg2) {
		// TODO Auto-generated method stub
		super.draw(canvas, mapView, arg2);
		Projection projection=mapView.getProjection();
		for(int index=this.size()-1; index>=0; index--){
			OverlayItem overlayItem=getItem(index);
			Point point=projection.toPixels(overlayItem.getPoint(), null);
			Paint paintText=new Paint();
			paintText.setColor(Color.RED);
			paintText.setTextSize(13);
			canvas.drawText(overlayItem.getTitle(),
					point.x+10, point.y-15, paintText);
		}
	}
	
	@Override
	protected OverlayItem createItem(int i) {
		// TODO Auto-generated method stub
		return itemList.get(i);
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return itemList.size();
	}

}
