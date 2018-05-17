import java.util.logging.Logger;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

public class ActiveMQUtil {
    private ActiveMQConnectionFactory connectionFactory = null;

    private Connection connection = null;

    private Session session = null;

    private Destination destination = null;

    private final String queueName = "heartbeat";

    private static Logger logger = LogManager.getLogger(ActiveMQUtil.class);

    private MessageProducer producer = null;

    private MessageConsumer consumer = null;

    public ActiveMQUtil() {

    }

    public Integer Init(String url) {
        try {
            connectionFactory = new ActiveMQConnectionFactory(url);

            connection = connectionFactory.createConnection();
            connection.start();

            // 创建Session，参数解释：
            // 第一个参数是否使用事务:当消息发送者向消息提供者（即消息代理）发送消息时，消息发送者等待消息代理的确认，没有回应则抛出异常，消息发送程序负责处理这个错误。
            // 第二个参数消息的确认模式：
            // AUTO_ACKNOWLEDGE ： 指定消息提供者在每次收到消息时自动发送确认。消息只向目标发送一次，但传输过程中可能因为错误而丢失消息。
            // CLIENT_ACKNOWLEDGE ： 由消息接收者确认收到消息，通过调用消息的acknowledge()方法（会通知消息提供者收到了消息）
            // DUPS_OK_ACKNOWLEDGE ： 指定消息提供者在消息接收者没有确认发送时重新发送消息（这种确认模式不在乎接收者收到重复的消息）。
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            // 创建目标，就创建主题也可以创建队列
            destination = session.createQueue(queueName);

            // 创建消息生产者
            producer = session.createProducer(destination);

            // 设置持久化，DeliveryMode.PERSISTENT和DeliveryMode.NON_PERSISTENT
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

            // 创建消息消费者  
            consumer = session.createConsumer(destination);
        } catch (javax.jms.JMSException e) {
            logger.severe("Init ActiveMQ failed:" + e.getMessage());
            return -1;
        }

        return 0;
    }

    public Integer SendMsg(String text) {
        TextMessage message;
        try {
            message = session.createTextMessage(text);
            producer.send(message);
        } catch (JMSException e) {
            logger.severe("SendMsg[" + text + "] failed:" + e.getMessage());
            return -1;
        }

        return 0;
    }

    public Message RecvMsg() {
        Message message = null;
        try {
            message = consumer.receive(1000);
        } catch (JMSException e) {
            logger.severe("RecvMsg " + e.getMessage());
            return null;
        }

        return message;
    }

    public void Uninit() {
        try {
            producer.close();
            consumer.close();

            session.close();
            connection.close();
        } catch (JMSException e) {
            logger.severe("Uninit failed: " + e.getMessage());
        }

    }

}