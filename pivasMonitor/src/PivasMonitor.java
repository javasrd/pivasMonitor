import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;


public class PivasMonitor {
    private static Logger logger = LogManager.getLogger(PivasMonitor.class);

    private final static int postNum = 2;

    private final static int totalNum = 3;

    private static int pivasFailCount = 0;

    private static int demFailCount = 0;

    private static int mqFailCount = 0;

    public interface ServiceIndex {
        Integer pivasIndex = 0;
        Integer demIndex = 1;
        Integer mqIndex = 2;
    }

    public static void main(String args[]) {

        //读取节点表
        boolean bRet = ConfigProperty.initValue();
        if (!bRet) {
            logger.severe("Init Value failed");
            return;
        }

        ActiveMQUtil activemqUtil = new ActiveMQUtil();

        do {

            clearFailInfo();

            //获取IP
            String ip = ConfigProperty.getIP();
            logger.info("Local ip[" + ip + "]");

            //读取节点表
            ServerNodeBean bean = OracleUtil.getServerNodeByIP(ip);
            if (bean == null) {
                logger.severe("Invalid ip,can not find node,waiting for retry");
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }

            //主机无须监控程序
            if (bean.getFlag() == 0) {
                logger.info("main server do not need monitor,waiting for retry");
                try {
                    Thread.sleep(ConfigProperty.getBackUpModeWaitTime() * 1000);
                } catch (InterruptedException e) {
                    logger.severe("sleep failed:" + e.getMessage());
                }
                continue;
            }

            logger.info("Start Monitor");

            //循环发送心跳
            ServerNodeBean mainBean = null;
            Boolean isChanged = false;
            while (true) {
                //获取相关信息
                mainBean = OracleUtil.getServerNodeByFlag(0);
                if (mainBean == null) {
                    logger.severe("cannot find back main");
                    break;
                }

                if (mainBean.getPort() == null) {
                    logger.severe("null port");
                    break;
                }

                String ports[] = mainBean.getPort().split(",");
                if (ports.length != totalNum) {
                    logger.severe("port num != " + totalNum);
                    break;
                }

                if (mainBean.getDomainName() == null) {
                    logger.severe("null domainname");
                    break;
                }

                String domainNames[] = mainBean.getDomainName().split(",");
                if (domainNames == null) {
                    logger.severe("null domainNames");
                    break;
                }

                if (domainNames.length != totalNum) {
                    logger.severe("domainNames num != " + totalNum);
                    break;
                }

                int index = 0;
                for (; index < postNum; ++index) {
                    PostHeart postHeart = new PostHeart(mainBean.getIP(), ports[index], domainNames[index]);
                    int postRet = postHeart.sendHeart(ip);
                    if (postRet != 0) {
                        logger.warning("send Ip[" + mainBean.getIP() + "] port[" + ports[index] + "] name[" + domainNames[index] + "]");
                        UpdateFailInfo(index);
                        if (isFailed()) {
                            isChanged = true;
                            break;
                        }
                    }
                }

                if (isFailed()) {
                    isChanged = true;
                    break;
                }

                StringBuffer sb = new StringBuffer();
                sb.append("tcp://");
                sb.append(mainBean.getIP());
                sb.append(":");
                sb.append(ports[postNum]);

                //MQ发送/接收
                int ret = activemqUtil.Init(sb.toString());
                if (ret != 0) {
                    logger.severe("activemqUtil connect failed");
                    UpdateFailInfo(ServiceIndex.mqIndex);
                    if (isFailed()) {
                        isChanged = true;
                        break;
                    }
                    continue;
                }

                sb = new StringBuffer("");
                sb.append("HEARTBEAT-");
                sb.append(ip);
                sb.append("-");

                SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date();
                String strDate = dateFormater.format(date);
                sb.append(strDate);

                ret = activemqUtil.SendMsg(sb.toString());
                if (ret != 0) {
                    logger.severe("send msg failed");
                    UpdateFailInfo(ServiceIndex.mqIndex);
                    if (isFailed()) {
                        isChanged = true;
                        break;
                    }
                }

                Message msg = activemqUtil.RecvMsg();
                if (msg != null) {
                    if (msg instanceof TextMessage) {
                        TextMessage textMessage = (TextMessage) msg;
                        String text = null;
                        try {
                            text = textMessage.getText();
                        } catch (JMSException e) {
                            logger.severe("get textmsg failed:" + e.getMessage());
                        }
                        logger.info("RECVMSG:" + text);
                    } else {
                        logger.info("RECVMSG:" + msg);
                    }
                }

                activemqUtil.Uninit();

                try {
                    Thread.sleep(ConfigProperty.getRetryTime() * 1000);
                } catch (InterruptedException e) {
                    logger.severe("Thread Interrupted:" + e.getMessage());
                }
            }

            if (!isChanged) {
                logger.warning("in exception,recontinue");
                continue;
            }

            logger.info("Heartbeat stop");
            logger.info(ip + " 1->0 BACK->MAIN");
            logger.info(mainBean.getIP() + " 0->1 MAIN->BACK");

            //修改主备节点FLAG
            OracleUtil.updateServerNode(ip, 0);
            OracleUtil.updateServerNode(mainBean.getIP(), 1);

        } while (true);
    }

    private static void UpdateFailInfo(Integer index) {
        if (ServiceIndex.pivasIndex == index) {
            //PIVAS
            ++pivasFailCount;
        } else if (ServiceIndex.demIndex == index) {
            //DEM
            ++demFailCount;
        } else if (ServiceIndex.mqIndex == index) {
            //MQ
            ++mqFailCount;
        }
        logger.info("Now fail info:pivasFailCount[" + pivasFailCount + "/" + ConfigProperty.getPivasFailMax() + "] demFailCount["
                + demFailCount + "/" + ConfigProperty.getDemFailMax() + "] mqFailCount[" + mqFailCount + "/" + ConfigProperty.getMqFailMax() + "]");
    }

    private static boolean isFailed() {
        if (pivasFailCount > ConfigProperty.getPivasFailMax()) {
            logger.warning("pivasFailCount[" + pivasFailCount + "] OVER failCount[" + ConfigProperty.getPivasFailMax() + "]");
            return true;
        }

        if (demFailCount > ConfigProperty.getDemFailMax()) {
            logger.warning("demFailCount[" + demFailCount + "] OVER failCount[" + ConfigProperty.getDemFailMax() + "]");
            return true;
        }

        if (mqFailCount > ConfigProperty.getMqFailMax()) {
            logger.warning("mqFailCount[" + mqFailCount + "] OVER failCount[" + ConfigProperty.getMqFailMax() + "]");
            return true;
        }

        return false;
    }

    public static void clearFailInfo() {
        pivasFailCount = 0;
        demFailCount = 0;
        mqFailCount = 0;
    }

}