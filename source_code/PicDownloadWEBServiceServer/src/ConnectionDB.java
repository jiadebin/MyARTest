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
			/*�������ݿ�����*/
//			String password = "jia";
//			String password = "root";
			/*ͼƬ����������ݿ�����*/
			String password = "zhangjiacheng";
			try{
				Class.forName("org.gjt.mm.mysql.Driver");  //Ҫ��JVM���Ҳ�����ָ�����࣬Ҳ����˵JVM��ִ�и���ľ�̬�����
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