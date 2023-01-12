package com.zty.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

//操作数据库的公共类
     public class BaseDao {
         private static String driver;
         private static String url;
         private static String username;
         private static String password;

         //静态代码块，类加载的时候就初始化了
         // 只在类加载的时候执行一次
         static {
             // 读取proproties文件所需
             Properties properties = new Properties();
             // 通过类加载器读取对应的资源
             // 类加载器可以获取项目发布的实际地址
             InputStream is = BaseDao.class.getClassLoader().getResourceAsStream("db.properties");

             try {
                 // 解析properties文件
                 properties.load(is);
             } catch (IOException e) {
                 e.printStackTrace();
             }

             driver = properties.getProperty("driver");
             url = properties.getProperty("url");
             username = properties.getProperty("username");
             password = properties.getProperty("password");
         }

         //获取数据库的链接
         public static Connection getConnection(){
             // 提升作用域后再进行try catch
             Connection connection = null;
             try {
                 // 这一句话是Driver已经被初始化并注册到DriverManager中
                 // 在java中Class.forName()和ClassLoader都可以对类进行加载。
                 // ClassLoader就是遵循双亲委派模型最终调用启动类加载器的类加载器
                 // 实现的功能是“通过一个类的全限定名来获取描述此类的二进制字节流”
                 // 获取到二进制流后放到JVM中。
                 // Class.forName()方法实际上也是调用的CLassLoader来实现的。
                 Class.forName(driver);
                 connection = DriverManager.getConnection(url, username, password);
             } catch (Exception e) {
                 e.printStackTrace();
             }
             return connection;
         }

         //编写查询公共方法
         public static ResultSet execute(Connection connection, PreparedStatement preparedStatement, ResultSet resultSet, String sql, Object[] params) throws SQLException {
             // 这个方法在入参处初始化了preparedStatement, resultSet
             // 可以理解为引用类型
             // 最后修改后得到输出

             //预编译的sql，在后面直接执行就可以了
             preparedStatement = connection.prepareStatement(sql);

             for (int i = 0; i < params.length; i++) {
                 // 注意预编译的参数从1开始计数
                 //setObject,占位符从1开始，但是我们的数组是从0开始！
                 preparedStatement.setObject(i+1,params[i]);
             }

             resultSet = preparedStatement.executeQuery();
             return resultSet;
         }


         //编写增删改公共方法
         public static int execute(Connection connection,PreparedStatement preparedStatement,String sql,Object[] params) throws SQLException {
             preparedStatement = connection.prepareStatement(sql);

             for (int i = 0; i < params.length; i++) {
                 //setObject,占位符从1开始，但是我们的数组是从0开始！
                 preparedStatement.setObject(i+1,params[i]);
             }

             // updateRows表示发生变化的行数
             // 增删改统一用！
             int updateRows = preparedStatement.executeUpdate();
             return updateRows;
         }


         //释放资源
         public static boolean closeResource(Connection connection,PreparedStatement preparedStatement,ResultSet resultSet){
             boolean flag = true;

             // 手动释放resultSet， preparedStatement， connection
             // 这里是引用，外面调这个方法对外面也会生效！
             // 指向null的话，引用对象不再被指向，会被gc回收
             if (resultSet!=null){
                 try {
                     resultSet.close();
                     //GC回收
                     resultSet = null;
                 } catch (SQLException e) {
                     e.printStackTrace();
                     flag = false;
                 }
             }

             if (preparedStatement!=null){
                 try {
                     preparedStatement.close();
                     //GC回收
                     preparedStatement = null;
                 } catch (SQLException e) {
                     e.printStackTrace();
                     flag = false;
                 }
             }

             if (connection!=null){
                 try {
                     connection.close();
                     //GC回收
                     connection = null;
                 } catch (SQLException e) {
                     e.printStackTrace();
                     flag = false;
                 }
             }

             // 返回是否回收成功！
             return flag;
         }
     }
