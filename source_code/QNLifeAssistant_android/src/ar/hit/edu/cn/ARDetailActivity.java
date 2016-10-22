/**
 *����ʵ�������ص���ϸ��Ϣ��ʾ������ 
 * 
 * @author JiaDebin
 * @version 1.0, 20/3/2012
 */

package ar.hit.edu.cn;

import android.app.Activity;

import hit.edu.cn.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.kobjects.base64.Base64;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ARDetailActivity extends Activity {
	
	private static final String TAG="ARDetailActivity";
	protected static final int TASK_DONE = 0;
	protected static final int TASK_CANCEL = 1;
	protected static final int TASK_UNDONE = 2;
	Handler handle;
	String id=null;
	private ImageView iv;
	private TextView tv_desc;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ar_detail);
		
		id = this.getIntent().getExtras().getString("ID");
		String name = this.getIntent().getExtras().getString("NAME");
		String desc = this.getIntent().getExtras().getString("DESC");
		
		((TextView) this.findViewById(R.id.tv_Name)).setText(name);
		tv_desc=((TextView) this.findViewById(R.id.tv_Desc));
		tv_desc.setText(desc);
		tv_desc.setMovementMethod(ScrollingMovementMethod.getInstance());
		iv=(ImageView) findViewById(R.id.iv);
		/*������ÿռ�С��500KB����ô��ʾ�û��ռ䲻��*/
		if(getAvailaleSize()<500){
			Toast lowStorageToast=Toast.makeText(this, "��⵽���ֻ��Ĵ洢�ռ䲻��,���Բ�����ͼƬ����(����ÿ�μ���ͼƬʱ��ķ�һЩ����)", 
					Toast.LENGTH_LONG);
			lowStorageToast.show();
		}
		wsAsyncTask wsTask = new wsAsyncTask(this);
	    wsTask.execute(id);
	    handle=new Handler(){

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				switch(msg.what){
				case TASK_DONE:
					Log.v(TAG, "task is done");
					iv.setImageBitmap((Bitmap)msg.obj);
//					((Bitmap)msg.obj).recycle();      
					/*
					 * ��������Ѿ���������,���Ϻ���쳣��ֹ,��Ϊ������
					 *�ͷ���̫����,log��ʾ��ʱCanvas����ʹ����
					 */
					break;
				case TASK_CANCEL:
					Log.v(TAG, "task is cancelled");
					iv.setImageResource(R.drawable.defaultimg);
					break;
				case TASK_UNDONE:
					Log.v(TAG, "task is undone");
					break;
				}
			}
	    	
	    };
		
	}

	/*��ȡ�ⲿ�洢���ÿռ�*/
	public static long getAvailaleSize(){
		File path = Environment.getExternalStorageDirectory();
		StatFs stat = new StatFs(path.getPath()); 
		long blockSize = stat.getBlockSize(); 
		long availableBlocks = stat.getAvailableBlocks();
		return (availableBlocks * blockSize)/1024;     //��KIB ��λ
		//(availableBlocks * blockSize)/1024 /1024  MIB��λ
		}
	/*
	 * ����������ͼƬ�Ĵ�С�����жϣ����õ����ʵ����ű���������2��1/2,3��1/3   
	 */
	static int computeSampleSize(BitmapFactory.Options options, int tarWidth, int tarHeight) {   
	    int realWidth = options.outWidth;   
	    int realHeight = options.outHeight;   
	    float candidateW = (float)realWidth / tarWidth;   
	    float candidateH = (float)realHeight / tarHeight;   
	    float candidate = Math.max(candidateW, candidateH);
	    candidate=Math.max(candidate, 1);
	    
	    /*���²�����Ϊ�˷�ֹ�������:���width��heightֻ��һ���ر��,��һ����realֵ�ӽ�,��ô�õ���candidate
	     * ֵ�ǲ�׼ȷ,�ǹ����,����Ҫ����,�ʵ������һ,���ڿ�͸߱ȽϽӽ�ʱ������Ч�������Է���*/
	    /*
	    if (candidate > 1) {   
	        if ((realWidth > tarWidth) && (realWidth / candidate) < tarWidth)   
	            candidate -= 1;   
	        if ((realHeight > tarHeight) && (realHeight / candidate) < tarHeight)   
	            candidate -= 1;   
	    } 
	    */  
	    Log.v(TAG, "realWidth:"+realWidth+" realHeight:" + realHeight + "candidate is "+candidate+
	    		" return " + Math.ceil(candidate)+"  "+(int) Math.ceil(candidate));   
	    return (int) Math.ceil(candidate);   
	}  


	private class wsAsyncTask extends AsyncTask<String, Integer, Bitmap> {
		//Ӧ����sd���ϵ�ͼƬ����Ŀ¼
		String dir=Environment.getExternalStorageDirectory()+"/QNLifrAssistant/ARNavigator/images";
		File mydir=null;
		File file=null;
		private String id;
		private Toast toast;
		private Bitmap img=null;
		private Drawable drawable=null;
		private ProgressDialog progress;
		
		/*
		 * ���ĳͼƬ�Ƿ��Ѿ���sdcard���ڵķ���
		 */
		boolean checkIfExist(String param){
			mydir=new File(dir);
    		Log.v(TAG, "mydir is "+dir);
			if(mydir.exists()){
				Log.v(TAG, "mydir exists!");
				file=new File(mydir, param+".jpg");
				if(file.exists()){
					Log.v(TAG, "file exists!");
					drawable = Drawable.createFromPath(dir+"/"+param+".jpg");

					if(drawable!=null){
						Log.v(TAG, "drawable is not null!");
						return true;
					}
					else{ 
						Log.v(TAG, "drawable is null!");
						return false;
					}
					
				}
			}
			return false;
		}
		
		/*
		 * ����ͼƬ��sd��
		 */
		void saveToSD(String param){
			boolean tag_save=false;
			if(!mydir.exists()){
				if(!mydir.mkdirs()){
					Log.v(TAG, "Cannot create the dir!");
				}
				else
					tag_save=true;
			}
			else{
				tag_save=true;
			}
			
			/*������ÿռ�С��500KB���򲻽��л���*/
			if(getAvailaleSize()<500){
				tag_save=false;
			}
			if(tag_save==true){
				//Ŀ¼�Ѿ����û��Ѿ�����
				File file=new File(mydir, param+".jpg");
				FileOutputStream fos;
				try {
					fos = new FileOutputStream(file);
					img.compress(Bitmap.CompressFormat.JPEG, 100, fos);
					Log.v(TAG, "save image to sdcard!");
					fos.flush();
					fos.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
		
		public wsAsyncTask(Context context){
			progress=new ProgressDialog(context);
			progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progress.setIndeterminate(false);
			progress.setCancelable(true);
//			progress.setCanceledOnTouchOutside(true);   //�����Ƿ���Ե����Ļֹͣ����,�������������,���Բ�����
			progress.setOnCancelListener(new OnCancelListener() {
				
				@Override
				public void onCancel(DialogInterface dialog) {
					// TODO Auto-generated method stub
					toast.setText("��ȡ��ͼƬ����");
					toast.show();
					wsAsyncTask.this.cancel(true);
				}
			});
			progress.setMessage("ͼƬ������...");
			progress.show();
			toast=Toast.makeText(context, "�����ؼ���ȡ��ͼƬ����(�����������״���ܲ�)", Toast.LENGTH_LONG);
		}
    	@Override
    	protected Bitmap doInBackground(String... params) {
    		// TODO Auto-generated method stub
    		id=params[0];
    		if((checkIfExist(id))==true){
    			img=((BitmapDrawable)drawable).getBitmap();
    			return img;
    		}
    		toast.show();
			Log.v(TAG, "load img from web service!");
    		
    		String wsMethodname="fileDownload";
    		SoapObject request=new SoapObject(ARNavigatorActivity.wsNamespace, wsMethodname);
    		request.addProperty("id", id);
    		SoapSerializationEnvelope envelop=new SoapSerializationEnvelope(SoapEnvelope.VER11);
    		envelop.bodyOut=request;
    		HttpTransportSE ht=new HttpTransportSE(ARNavigatorActivity.wsUrl);	
    		try {
    			Log.v(TAG, "before call");
    			ht.call(null, envelop);
    			Log.v(TAG, "after call");
    			if(envelop.getResponse()!=null){
    				Log.v(TAG, "response is not null");
    				Object result=envelop.getResponse();
    				Log.v(TAG, "has got response");
    				byte[] buffer=Base64.decode(result.toString());
    				BitmapFactory.Options options=new Options();
    				options.inJustDecodeBounds = true;   
    				    //���ô˷����õ�options�õ�ͼƬ�Ĵ�С   
    				BitmapFactory.decodeByteArray(buffer, 0, buffer.length, options);
    				    //�ҵ�Ŀ�����ڴ�Լ400*480 pixel�Ļ�������ʾ,���Ե���computeSampleSize�õ�ͼƬ���ŵı���   
    				options.inSampleSize = computeSampleSize(options, 450, 400);   
    				    //OK,�õ������ŵı��������ڿ�ʼ��ʽ����BitMap����   
    				options.inJustDecodeBounds = false;   
    				options.inDither=false;    /*������ͼƬ��������*/
    				img=BitmapFactory.decodeByteArray(buffer, 0, buffer.length, options);
    				
    	    		return img;
    			}
    			else{}
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} catch (XmlPullParserException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    		return null;
    	}
    	
    	
		@Override
		protected void onPostExecute(Bitmap result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			Log.v(TAG, "in on postExcute!");
			progress.dismiss();
			toast.cancel();
			if(result!=null){
				Message message=handle.obtainMessage();
				message.what=TASK_DONE;
				message.obj=result;
				message.sendToTarget();
				saveToSD(id);
			}
			else{
				Message message=handle.obtainMessage();
				message.what=TASK_UNDONE;
				message.sendToTarget();
			}
		}
		@Override
    	protected void onCancelled() {
    		// TODO Auto-generated method stub
    		super.onCancelled();
    		Log.v(TAG, "task is cancelled!");
    		Message message=handle.obtainMessage();
			message.what=TASK_CANCEL;
			message.sendToTarget();
    		
    	}
    }
}
