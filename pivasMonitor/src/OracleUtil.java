import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class OracleUtil {
    private static Logger logger = LogManager.getLogger(OracleUtil.class);

    public static ServerNodeBean getServerNodeByIP(String ip) {
        List<String> paramList = new ArrayList<String>();
        paramList.add("IP");

        List<String> valueList = new ArrayList<String>();
        valueList.add(ip);

        List<ServerNodeBean> beanList = getServerNodeListByParam(paramList, valueList);
        if (beanList == null) {
            return null;
        }

        if (beanList.size() == 0) {
            return null;
        }

        ServerNodeBean bean = new ServerNodeBean();
        bean = beanList.get(0);
        return bean;
    }

    public static ServerNodeBean getServerNodeByFlag(Integer flag) {
        List<String> paramList = new ArrayList<String>();
        paramList.add("FLAG");

        List<String> valueList = new ArrayList<String>();
        valueList.add(flag.toString());

        List<ServerNodeBean> beanList = getServerNodeListByParam(paramList, valueList);
        if (beanList == null) {
            return null;
        }

        if (beanList.size() <= 0) {
            return null;
        }

        ServerNodeBean bean = new ServerNodeBean();
        bean = beanList.get(0);
        return bean;
    }

    public static void updateServerNode(String ip, Integer flag) {
        Connection con = null;// 创建一个数据库连接
        PreparedStatement pre = null;// 创建预编译语句对象，一般都是用这个而不用Statement

        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");// 加载Oracle驱动程序
            String url = ConfigProperty.getDBURL();
            String user = ConfigProperty.getDBUser();// 用户名,系统默认的账户名
            String password = ConfigProperty.getDBPwd();// 你安装时选设置的密码
            con = DriverManager.getConnection(url, user, password);// 获取连接
            logger.info("Connect db:" + url + "ok");

            //pre = con.prepareStatement("UPDATE SM_SERVER_NODE SET FLAG=? WHERE IP=?");// 实例化预编译语句
            pre = con.prepareStatement("UPDATE SYS_SERVER_NODE SET FLAG=? WHERE IP=?");// 实例化预编译语句
            pre.setString(1, flag.toString());
            pre.setString(2, ip);
            pre.executeUpdate();
        } catch (Exception e) {
            logger.severe("database connect error:" + e.getMessage());
        } finally {
            try {
                if (pre != null)
                    pre.close();
                if (con != null)
                    con.close();
                logger.info("database disconnected");
            } catch (Exception e) {
                logger.severe("database disconnect error:" + e.getMessage());
            }
        }
    }

    private static List<ServerNodeBean> getServerNodeListByParam(List<String> paramList, List<String> valueList) {
        Connection con = null;// 创建一个数据库连接
        PreparedStatement pre = null;// 创建预编译语句对象，一般都是用这个而不用Statement
        ResultSet result = null;// 创建一个结果集对象
        List<ServerNodeBean> retList = new ArrayList<ServerNodeBean>();
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");// 加载Oracle驱动程序
            String url = ConfigProperty.getDBURL();
            String user = ConfigProperty.getDBUser();// 用户名,系统默认的账户名
            String password = ConfigProperty.getDBPwd();// 你安装时选设置的密码
            con = DriverManager.getConnection(url, user, password);// 获取连接
            logger.info("Connect db:" + url + " OK");

            StringBuffer sql = new StringBuffer();// 预编译语句，“？”代表参数
            //sql.append("select * from SM_SERVER_NODE");
            sql.append("select * from SYS_SERVER_NODE");
            if (paramList.size() > 0) {
                sql.append(" WHERE");
            }
            for (String param : paramList) {
                sql.append(" ");
                sql.append(param);
                sql.append("=?");
                sql.append(" and");
            }
            String strSql = sql.substring(0, sql.length() - 4);
            pre = con.prepareStatement(strSql);// 实例化预编译语句
            if (valueList.size() > 0) {
                int index = 0;
                for (; index < valueList.size(); ++index) {
                    pre.setString(index + 1, valueList.get(index));
                }
            }

            result = pre.executeQuery();// 执行查询，注意括号中不需要再加参数
            while (result.next()) {
                logger.info("ID:" + result.getInt("id") + " IP:" + result.getString("IP") + " NAME:" + result.getString("NAME") + " FLAG:"
                        + result.getInt("FLAG") + " PORT:" + result.getString("PORT") + " DOMAINNAME:" + result.getString("DOMAINNAME"));
                ServerNodeBean bean = new ServerNodeBean();
                bean.setID(result.getInt("id"));
                bean.setIP(result.getString("IP"));
                bean.setName(result.getString("NAME"));
                bean.setFlag(result.getInt("FLAG"));
                bean.setPort(result.getString("PORT"));
                bean.setDomainName(result.getString("DOMAINNAME"));
                retList.add(bean);
            }
        } catch (Exception e) {
            logger.severe("database connect error:" + e.getMessage());
            return null;
        } finally {
            try {
                // 逐一将上面的几个对象关闭，因为不关闭的话会影响性能、并且占用资源
                // 注意关闭的顺序，最后使用的最先关闭
                if (result != null)
                    result.close();
                if (pre != null)
                    pre.close();
                if (con != null)
                    con.close();
                logger.info("database disconnected");
            } catch (Exception e) {
                logger.severe("database disconnect error:" + e.getMessage());
            }
        }

        return retList;
    }
}