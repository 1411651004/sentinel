package com.gzf.sentinel.config;

import com.alibaba.druid.pool.DruidDataSourceFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * @program: JDBC工具类
 * @description:
 * @author: Gaozf
 * @create: 2020-08-12 11:16
 **/
public class DataSourceUtils {

    private static DataSource dataSource;
    private static ThreadLocal<Connection> tl = new ThreadLocal<Connection>();

    //在静态代码块中初始化连接池
    static {
        Properties properties = new Properties();
        try {
            //getResourceAsStream(String name) 在模块中查找指定名称的文件，返回该文件的输入流
            properties.load(DataSourceUtils.class.getClassLoader().getResourceAsStream("datasource.properties"));
            dataSource = DruidDataSourceFactory.createDataSource(properties);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 直接可以获取一个连接池
    public static DataSource getDataSource() {
        return dataSource;
    }

    // 获取连接对象
    public static Connection getConnection() throws SQLException {

        Connection con = tl.get();
        if (con == null) {
            con = dataSource.getConnection();
            tl.set(con);
        }
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
            tl.remove();// 从线程绑定中移除
        }
    }

    public static void closeStatement(Statement st) throws SQLException {
        if (st != null) {
            st.close();
        }
    }

    public static void closeResultSet(ResultSet rs) throws SQLException {
        if (rs != null) {
            rs.close();
        }
    }

}