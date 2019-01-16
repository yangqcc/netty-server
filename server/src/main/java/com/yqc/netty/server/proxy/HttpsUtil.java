package com.yqc.netty.server.proxy;


import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>title:</p>
 * <p>description:</p>
 *
 * @author yangqc
 * @date Created in 2019-01-09
 * @modified By yangqc
 */
public class HttpsUtil {

    public static final CloseableHttpResponse get(final String url, final Map<String, Object> params) throws IOException {
        StringBuilder sb = new StringBuilder();

        if (null != params && !params.isEmpty()) {
            int i = 0;
            for (String key : params.keySet()) {
                if (i == 0) {
                    sb.append("?");
                } else {
                    sb.append("&");
                }
                sb.append(key).append("=").append(params.get(key));
                i++;
            }
        }

        CloseableHttpClient httpClient = createSSLClientDefault();


        HttpGet get = new HttpGet(url + sb.toString());
        String result = "";
        CloseableHttpResponse response = httpClient.execute(get);
        return response;
        /*try {
            response = httpClient.execute(get);

            if (response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                if (null != entity) {
                    result = EntityUtils.toString(entity, "UTF-8");
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(HttpsUtil.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (null != response) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException ex) {
                    Logger.getLogger(HttpsUtil.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        return result;*/
    }

    public static final String post(final String url, final Map<String, Object> params) {
        CloseableHttpClient httpClient = createSSLClientDefault();
        HttpPost post = new HttpPost(url);

        CloseableHttpResponse response = null;

        if (null != params && !params.isEmpty()) {
            List<NameValuePair> nvpList = new ArrayList<NameValuePair>();
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                NameValuePair nvp = new BasicNameValuePair(entry.getKey(), entry.getValue().toString());
                nvpList.add(nvp);
            }
            post.setEntity(new UrlEncodedFormEntity(nvpList, Charset.forName("UTF-8")));
        }

        String result = "";

        try {
            response = httpClient.execute(post);

            if (response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                if (null != entity) {
                    result = EntityUtils.toString(entity, "UTF-8");
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(HttpsUtil.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (null != response) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException ex) {
                    Logger.getLogger(HttpsUtil.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        return result;
    }

    private static CloseableHttpClient createSSLClientDefault() {

        SSLContext sslContext;
        try {
            sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                //信任所有
                @Override
                public boolean isTrusted(X509Certificate[] xcs, String string) {
                    return true;
                }
            }).build();

            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);

            return HttpClients.custom().setSSLSocketFactory(sslsf).build();
        } catch (KeyStoreException ex) {
            Logger.getLogger(HttpsUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(HttpsUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (KeyManagementException ex) {
            Logger.getLogger(HttpsUtil.class.getName()).log(Level.SEVERE, null, ex);
        }

        return HttpClients.createDefault();
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Result:" + get("https://github.com/", null));
    }
}
