import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

public class LogManager {

    // 初始化LogManager
    static {
        // 读取配置文件
        ClassLoader cl = LogManager.class.getClassLoader();
        InputStream inputStream = null;
        String dir = System.getProperty("user.dir");
        if (null == dir) {
            dir = "";
        }

        File file = new File(dir + File.separator + "log.properties");
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        java.util.logging.LogManager logManager = java.util.logging.LogManager
                .getLogManager();
        try {
            // 重新初始化日志属性并重新读取日志配置。
            if (inputStream != null) {
                logManager.readConfiguration(inputStream);
            }
        } catch (SecurityException e) {
            System.err.println(e);
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    /**
     * 获取日志对象
     *
     * @param clazz
     * @return
     */
    public static Logger getLogger(Class clazz) {
        Logger logger = Logger
                .getLogger(clazz.getName());
        return logger;
    }

}