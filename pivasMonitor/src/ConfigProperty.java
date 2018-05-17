import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.logging.Logger;

public class ConfigProperty {
    private static Logger logger = LogManager.getLogger(ConfigProperty.class);

    private static ConfigPropertyBean bean = new ConfigPropertyBean();

    public static String getIP() {
        return bean.IP;
    }

    public static String getDBURL() {
        return bean.DBURL;
    }

    public static String getDBUser() {
        return bean.DBUser;
    }

    public static String getDBPwd() {
        return bean.DBPwd;
    }

    public static Integer getRetryTime() {
        return bean.retryTime;
    }

    public static Integer getPivasFailMax() {
        return bean.pivasfailmax;
    }

    public static Integer getDemFailMax() {
        return bean.demfailmax;
    }

    public static Integer getMqFailMax() {
        return bean.mqfailmax;
    }

    public static Integer getBackUpModeWaitTime() {
        return bean.backupmodeWaitTime;
    }

    public static boolean initValue() {
        Properties p = new Properties();
        FileInputStream fis;
        String path = System.getProperty("user.dir");

        String url = path + File.separator + "config.properties"; // 获取位于工程根目录下的config.properties配置文件绝对路径
        try {
            fis = new FileInputStream(url);
            p.load(fis);
            fis.close();
        } catch (Exception e) {
            logger.severe("read file error,path:" + e.getMessage());
            return false;
        }

        bean.IP = p.getProperty("IP");
        if (null == bean.IP) {
            logger.severe("Invalid IP NULL");
            return false;
        }

        if (bean.IP.isEmpty()) {
            logger.severe("Invalid IP EMPTY");
            return false;
        }

        bean.DBURL = p.getProperty("DBURL");
        if (null == bean.DBURL) {
            logger.severe("Invalid DBURL NULL");
            return false;
        }

        if (bean.DBURL.isEmpty()) {
            logger.severe("Invalid DBURL EMPTY");
            return false;
        }

        bean.DBUser = p.getProperty("DBUSER");
        if (null == bean.DBUser) {
            logger.severe("Invalid DBUSER NULL");
            return false;
        }

        if (bean.DBUser.isEmpty()) {
            logger.severe("Invalid DBUSER EMPTY");
            return false;
        }

        bean.DBPwd = p.getProperty("DBPWD");
        if (null == bean.DBPwd) {
            logger.severe("Invalid DBPWD NULL");
            return false;
        }

        if (bean.DBPwd.isEmpty()) {
            logger.severe("Invalid DBPWD EMPTY");
            return false;
        }

        String strRetryTime = p.getProperty("RETRYTIME");
        String pivasfailStr = p.getProperty("PIVASMAXFAIL");
        String demfailStr = p.getProperty("DEMMAXFAIL");
        String mqfailStr = p.getProperty("MQMAXFAIL");
        String strBackupModeWaitTime = p.getProperty("BACKMODEWAITTIME");

        if (null == strBackupModeWaitTime) {
            logger.severe("Invalid strBackupModeWaitTime EMPTY");
            return false;
        }

        if (null == strRetryTime) {
            logger.severe("Invalid strRetryTime EMPTY");
            return false;
        }

        if (null == pivasfailStr) {
            logger.severe("Invalid pivasfailStr EMPTY");
            return false;
        }

        if (null == demfailStr) {
            logger.severe("Invalid demfailStr EMPTY");
            return false;
        }

        if (null == mqfailStr) {
            logger.severe("Invalid mqfailStr EMPTY");
            return false;
        }

        try {
            bean.pivasfailmax = Integer.valueOf(pivasfailStr);
            bean.demfailmax = Integer.valueOf(demfailStr);
            bean.mqfailmax = Integer.valueOf(mqfailStr);
            bean.retryTime = Integer.valueOf(strRetryTime);
            bean.backupmodeWaitTime = Integer.valueOf(strBackupModeWaitTime);
        } catch (java.lang.NumberFormatException e) {
            logger.severe("convert integer failed:" + e.getMessage());
            return false;
        }

        return true;
    }
}