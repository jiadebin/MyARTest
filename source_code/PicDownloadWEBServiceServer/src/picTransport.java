
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
	/*用静态块加载，提高传输PoiBean函数的调用速度*/
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
			 * 注意执行完查询，next()这句必须加上，使resultset移动到第一条记录处(默认在第一条之前)，
			 * 然后才能从中获得数据，否则会出现before start of result set的异常
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
			/*本机测试时*/
//			FileInputStream fis=new FileInputStream("../webapps\\axis2\\WEB-INF/pojo/images/"+id+".jpg");
			/*服务器*/
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
	 * 向数据库插入POI信息的方法，只调用一次
	 */
	
	/*
	public void loadPoiData() throws ClassNotFoundException{

		dbtool=new ConnectionDB();
		String sql;
		ArrayList<poiBean> poiBeanList=new ArrayList<poiBean>();
		int i=0;
		
		poiBeanList.add(new poiBean(""+i, "主楼", "这是哈尔滨工业大学主楼", 1, 45.7457721233,126.6262936592));
		
		i++;
		poiBeanList.add(new poiBean(""+i, "图书馆", "这是哈尔滨工业大学一校区图书馆", 2, 45.74351906,126.62592887));
		
		i++;
		poiBeanList.add(new poiBean(""+i, "体育馆", "这是哈尔滨工业大学体育馆", 1, 45.74041843,126.62416934));
		
		i++;
		poiBeanList.add(new poiBean(""+i, "体育场", "这是哈尔滨工业大学体育场,范德萨将放宽点睡觉啊fks动静啊健康法律撒娇fks将考虑放卡里双方就算了咖啡加快gas考虑将公开沙拉酱风景山卡拉放假快乐撒风景ask浪费", 
				1, 45.73683500,126.62816047));
		
		i++;
		poiBeanList.add(new poiBean(""+i, "致知楼", "这是哈尔滨工业大学致知楼", 1, 45.74246764,126.62695884));
		
		i++;
		poiBeanList.add(new poiBean(""+i, "软件学院", "这是哈尔滨工业大学软件学院", 1, 45.74334740,126.62749528));
		
		
		i++;
		poiBeanList.add(new poiBean(""+i, "行政楼", "这是哈尔滨工业大学行政楼", 1, 45.74401259,126.62522077));
		
		i++;
		poiBeanList.add(new poiBean(""+i, "理学院", "这是哈尔滨工业大学理学院", 1, 45.74168443,126.62206649));
		
		i++;
		poiBeanList.add(new poiBean(""+i, "活动中心", "这是哈尔滨工业大学一校区活动中心", 2, 45.73719978,126.62517786));
		
		i++;
		poiBeanList.add(new poiBean(""+i, "游泳馆", "这是哈尔滨工业大学游泳馆", 1, 45.73659896,126.62625074));
		i++;
		poiBeanList.add(new poiBean(""+i, "中行、工商行", "这是哈尔滨工业大学中行、工商行支行", 2, 45.74059009,126.62680864));
		
		i++;
		poiBeanList.add(new poiBean(""+i, "建设银行", "这是哈尔滨工业大学建设银行支行", 2, 45.74180245,126.62809610));
		
		i++;
		poiBeanList.add(new poiBean(""+i, "学友书店", "这是哈尔滨工业大学学友书店", 2, 45.74201703,126.62755966));
		
		i++;
		poiBeanList.add(new poiBean(""+i, "二公寓", "这是哈尔滨工业大学一校区学生A02公寓", 2, 45.74127674,126.62987709));
		
		i++;
		poiBeanList.add(new poiBean(""+i, "十公寓", "这是哈尔滨工业大学学生A10公寓", 2, 45.74110507,126.62652969));
		
		i++;
		poiBeanList.add(new poiBean(""+i, "学士楼", "这是哈尔滨工业大学学士楼（食堂）", 2, 45.74167370,126.62743091));
		
		i++;
		poiBeanList.add(new poiBean(""+i, "学苑楼", "这是哈尔滨工业大学学苑楼（食堂）", 2, 45.74260711,126.62667989));
		
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
