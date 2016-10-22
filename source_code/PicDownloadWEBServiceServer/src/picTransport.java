
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import sun.misc.BASE64Encoder;

public class picTransport {
	
	private static Map<Integer, Object> hmap=new HashMap<Integer, Object>();
	/*�þ�̬����أ���ߴ���PoiBean�����ĵ����ٶ�*/
	static{
		ConnectionDB dbtool;
		String sql;
		ResultSet result;
			try {
				dbtool = new ConnectionDB();
				sql="select * from poiinfo";
				result=dbtool.executeQuery(sql);
				if(result.first()){
					do{
						poiBean b=new poiBean();
						b.setId(result.getInt("id"));
						b.setName(result.getString("name"));
						b.setType(result.getInt("type"));
						b.setDescription(result.getString("description"));
						b.setLatitude(result.getDouble("latitude"));
						b.setLongitude(result.getDouble("longitude"));					
						
						hmap.put(b.getId(), b);
						result.next();
					}
					while(!result.isAfterLast());
				}
				dbtool.close();
				result.close();
			}catch(Exception e){
				e.printStackTrace();
			}finally{
			}
	}
	
	public int getPoiNum(){
		System.out.println("Someone invoke me--getPoiNum");
		try {
			ConnectionDB dbtool2=new ConnectionDB();
			String sql2="select count(*) as rowCount from poiinfo";
			ResultSet result2=dbtool2.executeQuery(sql2);
			/*
			 * ע��ִ�����ѯ��next()��������ϣ�ʹresultset�ƶ�����һ����¼��(Ĭ���ڵ�һ��֮ǰ)��
			 * Ȼ����ܴ��л�����ݣ���������before start of result set���쳣
			 */
			result2.next();   
			return result2.getInt("rowCount");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}
	
	public poiBean getPoiBean(int id) { 
		System.out.println("Someone invoke me--getPoiBean");
		
		return (poiBean) hmap.get(id);
 } 
	
	public String fileDownload(String id) throws ClassNotFoundException{
			System.out.println("Someone invoke me--fileDownload");
		String base64=null;
		try {
			System.out.println(new File(".").getAbsolutePath());
			/*��������ʱ*/
//			FileInputStream fis=new FileInputStream("../webapps\\axis2\\WEB-INF/pojo/images/"+id+".jpg");
			/*������*/
			FileInputStream fis=new FileInputStream("./webapps\\axis2\\WEB-INF/pojo/images/"+id+".jpg");
			ByteArrayOutputStream baos=new ByteArrayOutputStream();
			byte[] buffer=new byte[1024];
			int count=0;
			while((count=fis.read(buffer))>0){
				baos.write(buffer, 0, count);
			}
			base64=new BASE64Encoder().encode(baos.toByteArray());
			fis.close();
			baos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return base64;
	}
	
	/*
	 * �����ݿ����POI��Ϣ�ķ�����ֻ����һ��
	 */
	
	/*
	public void loadPoiData() throws ClassNotFoundException{

		dbtool=new ConnectionDB();
		String sql;
		ArrayList<poiBean> poiBeanList=new ArrayList<poiBean>();
		int i=0;
		
		poiBeanList.add(new poiBean(""+i, "��¥", "���ǹ�������ҵ��ѧ��¥", 1, 45.7457721233,126.6262936592));
		
		i++;
		poiBeanList.add(new poiBean(""+i, "ͼ���", "���ǹ�������ҵ��ѧһУ��ͼ���", 2, 45.74351906,126.62592887));
		
		i++;
		poiBeanList.add(new poiBean(""+i, "������", "���ǹ�������ҵ��ѧ������", 1, 45.74041843,126.62416934));
		
		i++;
		poiBeanList.add(new poiBean(""+i, "������", "���ǹ�������ҵ��ѧ������,���������ſ��˯����fks������������������fks�����Ƿſ���˫�������˿��ȼӿ�gas���ǽ�����ɳ�����羰ɽ�����żٿ������羰ask�˷�", 
				1, 45.73683500,126.62816047));
		
		i++;
		poiBeanList.add(new poiBean(""+i, "��֪¥", "���ǹ�������ҵ��ѧ��֪¥", 1, 45.74246764,126.62695884));
		
		i++;
		poiBeanList.add(new poiBean(""+i, "���ѧԺ", "���ǹ�������ҵ��ѧ���ѧԺ", 1, 45.74334740,126.62749528));
		
		
		i++;
		poiBeanList.add(new poiBean(""+i, "����¥", "���ǹ�������ҵ��ѧ����¥", 1, 45.74401259,126.62522077));
		
		i++;
		poiBeanList.add(new poiBean(""+i, "��ѧԺ", "���ǹ�������ҵ��ѧ��ѧԺ", 1, 45.74168443,126.62206649));
		
		i++;
		poiBeanList.add(new poiBean(""+i, "�����", "���ǹ�������ҵ��ѧһУ�������", 2, 45.73719978,126.62517786));
		
		i++;
		poiBeanList.add(new poiBean(""+i, "��Ӿ��", "���ǹ�������ҵ��ѧ��Ӿ��", 1, 45.73659896,126.62625074));
		i++;
		poiBeanList.add(new poiBean(""+i, "���С�������", "���ǹ�������ҵ��ѧ���С�������֧��", 2, 45.74059009,126.62680864));
		
		i++;
		poiBeanList.add(new poiBean(""+i, "��������", "���ǹ�������ҵ��ѧ��������֧��", 2, 45.74180245,126.62809610));
		
		i++;
		poiBeanList.add(new poiBean(""+i, "ѧ�����", "���ǹ�������ҵ��ѧѧ�����", 2, 45.74201703,126.62755966));
		
		i++;
		poiBeanList.add(new poiBean(""+i, "����Ԣ", "���ǹ�������ҵ��ѧһУ��ѧ��A02��Ԣ", 2, 45.74127674,126.62987709));
		
		i++;
		poiBeanList.add(new poiBean(""+i, "ʮ��Ԣ", "���ǹ�������ҵ��ѧѧ��A10��Ԣ", 2, 45.74110507,126.62652969));
		
		i++;
		poiBeanList.add(new poiBean(""+i, "ѧʿ¥", "���ǹ�������ҵ��ѧѧʿ¥��ʳ�ã�", 2, 45.74167370,126.62743091));
		
		i++;
		poiBeanList.add(new poiBean(""+i, "ѧԷ¥", "���ǹ�������ҵ��ѧѧԷ¥��ʳ�ã�", 2, 45.74260711,126.62667989));
		
		for(int j=0; j<=i; j++){
			poiBean bean=poiBeanList.get(j);
			sql="insert into poiinfo(id, name, type, description, latitude, longitude) values("+bean.getId()+
			",'"+bean.getName()+"',"+bean.getType()+",'"+bean.getDescription()+"',"+bean.getPoint().latitude+
			","+bean.getPoint().longitude+")";
			int reault=dbtool.executeUpdate(sql);
			if(reault==0)
				System.out.println("inert error!");
		}
		dbtool.close();
	}
	
	
	*/
 

}
