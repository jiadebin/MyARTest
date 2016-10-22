import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


	 class ConnectionDB {
		private  Connection conn;
		private Statement stmt;
		private ResultSet rst;
		public  ConnectionDB() throws ClassNotFoundException{
			String url = "jdbc:mysql://localhost:3306/hitarnavigator";
			String username = "root";
			/*本机数据库密码*/
//			String password = "jia";
//			String password = "root";
			/*图片库服务器数据库密码*/
			String password = "zhangjiacheng";
			try{
				Class.forName("org.gjt.mm.mysql.Driver");  //要求JVM查找并加载指定的类，也就是说JVM会执行该类的静态代码段
				conn = (Connection) DriverManager.getConnection(url, username, password);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		public ResultSet executeQuery(String sql){
			try {
				stmt = conn.createStatement();
				rst = stmt.executeQuery(sql);
				return rst;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
			
		}
		public int executeUpdate(String sql){
			try {
				stmt = conn.createStatement();
			    int col = stmt.executeUpdate(sql);
				return col;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return 0;
		}
		public  void close(){
			if(conn!=null){
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}