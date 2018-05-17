public class ConfigPropertyBean {
    public ConfigPropertyBean() {
        IP = "";
        DBURL = "";
        DBUser = "";
        DBPwd = "";
        retryTime = 10;
        pivasfailmax = 0;
        demfailmax = 0;
        mqfailmax = 0;
        backupmodeWaitTime = 0;
    }

    public String IP;
    public String DBURL;
    public String DBUser;
    public String DBPwd;
    public Integer retryTime;
    public Integer pivasfailmax;
    public Integer demfailmax;
    public Integer mqfailmax;
    public Integer backupmodeWaitTime;
}