import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class activityInfo {
	static List<activityBean> list=new ArrayList<activityBean>();
	/*用静态块加载，提高传输PoiBean函数的调用速度*/
	static{
		ConnectionDB dbtool;
		String sql;
		ResultSet result;
			try {
				dbtool = new ConnectionDB();
				
				sql="select * from activityinfo order by publishtime";
				result=dbtool.executeQuery(sql);
				
				if(result.first()){
					do{
						int id=result.getInt("id");
						String title=result.getString("title");
				    	String publishtime=result.getString("publishtime");
				    	int cutIndex=publishtime.lastIndexOf('.');
				    	publishtime=publishtime.substring(0, cutIndex);
				    	System.out.println("time is "+publishtime);
				    	
				    	String content=result.getString("content");
				    	activityBean bean=new activityBean(id, title, content, publishtime);
				    	list.add(0, bean);
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
	
	public int getActivityNum(String lastTime){
		int nowNum;
		System.out.println("Someone invoke me--getActivityNewNum");
		try {
			ConnectionDB dbtool2=new ConnectionDB();
			String sql2="select count(*) as rowCount from activityinfo where publishtime>'"+lastTime+"' ";
			ResultSet result2=dbtool2.executeQuery(sql2);
			/*
			 * 注意执行完查询，next()这句必须加上，使resultset移动到第一条记录处(默认在第一条之前)，
			 * 然后才能从中获得数据，否则会出现before start of result set的异常
			 */
			result2.next();   
			nowNum=result2.getInt("rowCount");
			System.out.println("Someone invoke me--getActivityNewNum:"+nowNum);
			if(lastTime.equals("2012-01-01 00:00:00")&&nowNum==list.size()){
				System.out.println("Someone invoke me--getActivityNewNum first time:"+nowNum);
				return nowNum;
			}
			if(nowNum>0){
				System.out.println("Someone invoke me--getActivityNewNum in second if:"+nowNum);
				list.clear();   //清空list
						sql2="select * from activityinfo where publishtime>'"+lastTime+"' order by publishtime ";
						result2=dbtool2.executeQuery(sql2);
						
						if(result2.first()){
							do{
								int id=result2.getInt("id");
								String title=result2.getString("title");
						    	String publishtime=result2.getString("publishtime");
						    	int cutIndex=publishtime.lastIndexOf('.');
						    	publishtime=publishtime.substring(0, cutIndex);
						    	System.out.println("time is "+publishtime);
						    	
						    	String content=result2.getString("content");
						    	activityBean bean=new activityBean(id, title, content, publishtime);
						    	list.add(0, bean);
								result2.next();
							}
							while(!result2.isAfterLast());
						}
						dbtool2.close();
						result2.close();
						return nowNum;
			}
			else{
				return nowNum;
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}
	public activityBean getActivityInfo(int num) { 
		System.out.println("Someone invoke me--getActivityInfo NO.:"+num);
		int size=list.size();
		if(num>0&&num<=size){
			return list.get(num-1);
		}
		return null;
	} 
}
