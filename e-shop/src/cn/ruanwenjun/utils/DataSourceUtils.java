package cn.ruanwenjun.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class DataSourceUtils {
	//  C3P0 连接池创建数据源。配置文件方式（c3p0-config.xml）
	private static DataSource dataSource = new ComboPooledDataSource();

	private static ThreadLocal<Connection> tl = new ThreadLocal<Connection>();

	// 直接可以获取一个连接池
	public static DataSource getDataSource() {
		return dataSource;
	}

	// 获取连接对象
	public static Connection getConnection() throws SQLException {
    // 1. 从ThreadLocal中获取当前线程绑定的连接
    Connection con = tl.get();
    // 2. 判断当前线程是否已有绑定的连接：若无，则从连接池获取并绑定
    if (con == null) {
        con = dataSource.getConnection(); // 从C3P0连接池拿连接（复用而非新建）
        tl.set(con); // 将连接绑定到当前线程的ThreadLocal副本
    }
    // 3. 返回当前线程的专属连接（保证线程内所有操作共用一个连接）
    return con;
}

	// 开启事务
	public static void startTransaction() throws SQLException {
		Connection con = getConnection();
		if (con != null) {
			con.setAutoCommit(false);
		}
	}

	// 事务回滚
	public static void rollback() throws SQLException {
		Connection con = getConnection();
		if (con != null) {
			con.rollback();
		}
	}

	// 提交并且 关闭资源及从ThreadLocall中释放
	public static void commitAndRelease() throws SQLException {
		Connection con = getConnection();
		if (con != null) {
			con.commit(); // 事务提交
			con.close();// 关闭资源
			tl.remove();// 从线程绑定中移除
		}
	}

	// 关闭资源方法
	public static void closeConnection() throws SQLException {
		Connection con = getConnection();
		if (con != null) {
			con.close();
		}
	}
	//释放 Statement 资源
	public static void closeStatement(Statement st) throws SQLException {
		if (st != null) {
			st.close();
		}
	}
	//释放结果集资源
	public static void closeResultSet(ResultSet rs) throws SQLException {
		if (rs != null) {
			rs.close();
		}
	}

}
