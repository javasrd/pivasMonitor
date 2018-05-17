import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class PostHeart {

    private static Logger logger = LogManager.getLogger(PostHeart.class);

    private String IP = "";

    private String port = "";

    private String domainName = "";

    public PostHeart(String ip, String port, String domainName) {
        this.IP = ip;
        this.port = port;
        this.domainName = domainName;
    }

    /**
     * 向对应地址发送心跳请求 0成功 1失败
     */
    public Integer sendHeart(String localIp) {

        try {
            //创建连接
            StringBuffer sb = new StringBuffer();
            sb.append("http://");
            sb.append(IP);
            sb.append(":");
            sb.append(port);
            sb.append(domainName);
            logger.info("Post url:" + sb.toString());

            URL url = new URL(sb.toString());
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Content-Type",
                    "application/json");

            connection.connect();

            //POST请求 
            OutputStreamWriter out = new OutputStreamWriter(
                    connection.getOutputStream(), "UTF-8"); // utf-8编码
            JSONObject obj = new JSONObject();
            obj.put("ip", localIp);
            out.append(obj.toString());
            out.flush();
            out.close();

            //读取响应 
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String lines;
            sb = new StringBuffer("");
            while ((lines = reader.readLine()) != null) {
                lines = new String(lines.getBytes(), "utf-8");
                sb.append(lines);
            }
            reader.close();

            // 断开连接 
            connection.disconnect();

            obj = new JSONObject(sb.toString());
            int ret = obj.getInt("ret");
            return ret;
        } catch (MalformedURLException e) {
            logger.severe("post json failed:" + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            logger.severe("post json failed:" + e.getMessage());
        } catch (IOException e) {
            logger.severe("post json failed:" + e.getMessage());
        } catch (JSONException e) {
            logger.severe("post json failed:" + e.getMessage());
        }

        return -1;
    }


} 