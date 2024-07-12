package com.qlh.base;


import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class QlhHttp {

    /**
     * 默认超时设置
     */
    public static RequestConfig RC_3 = RequestConfig
            .custom()
            .setSocketTimeout(3000) // 读取超时
            .setConnectTimeout(1000) // 链接超时
            .setConnectionRequestTimeout(1000) // 从连接池获取链接失败
            .build();

    public static RequestConfig RC_10 = RequestConfig
            .custom()
            .setSocketTimeout(10000) // 读取超时
            .setConnectTimeout(2000) // 链接超时
            .setConnectionRequestTimeout(2000) // 从连接池获取链接失败
            .build();

    public static RequestConfig RC_60 = RequestConfig
            .custom()
            .setSocketTimeout(60000) // 读取超时
            .setConnectTimeout(3000) // 链接超时
            .setConnectionRequestTimeout(3000) // 从连接池获取链接失败
            .build();

    /**
     * 支持连接池, 线程安全, 懒加载
     */
    private static HttpClient getDefaultClient() {
        return QlhConcurrent.getSingletonObject("HttpClient", HttpClient.class, () -> QlhException.runtime(() -> {
            SSLContext sslContext = SSLContexts
                    .custom()
                    .loadTrustMaterial(null, (chain, authType) -> true) // 信任所有证书
                    .build();

            return HttpClients
                    .custom()
                    .setSSLContext(sslContext)
                    .setDefaultRequestConfig(RC_3)
                    .setRetryHandler(new DefaultHttpRequestRetryHandler(3, true))
                    .build();
        }));
    }

    private static HttpClient getDefaultPooledClient() {
        return QlhConcurrent.getSingletonObject("PooledHttpClient", HttpClient.class, () -> QlhException.runtime(() -> {
            SSLContext sslContext = SSLContexts
                    .custom()
                    .loadTrustMaterial(null, (chain, authType) -> true) // 信任所有证书
                    .build();
            SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.INSTANCE)
                    .register("https", sslConnectionSocketFactory).build();
            PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            cm.setMaxTotal(1000);
            cm.setDefaultMaxPerRoute(1000);
            return HttpClients
                    .custom()
                    .setConnectionManager(cm)
                    .setDefaultRequestConfig(RC_3)
                    .setRetryHandler(new DefaultHttpRequestRetryHandler(3, true))
                    .build();
        }));
    }

    public static HttpClient getClient(Request request) {
        if (request.getClient() != null)
            return request.getClient();
        return request.isUsePool() ? getDefaultPooledClient() : getDefaultClient();
    }

    public static Response get(Request request) {
        HttpClient client = getClient(request);

        StringBuilder urlBuilder = new StringBuilder(request.getUrl());
        if (request.getParameters().size() > 0) {
            if (urlBuilder.indexOf("?") == -1) {
                urlBuilder.append("?");
            }

            request.getParameters().entrySet().forEach(en -> {
                try {
                    urlBuilder.append("&")
                            .append(en.getKey()).append("=")
                            .append(URLEncoder.encode(en.getValue().toString(), request.getCharset()));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

        HttpGet httpGet = new HttpGet(urlBuilder.toString());
        setHttpRequestBase(httpGet, request);
        return QlhException.runtime(() -> response(client, request, client.execute(httpGet)));
    }

    private static Response post(Request request) {
        HttpClient client = getClient(request);

        HttpPost httpPost = new HttpPost(request.getUrl());
        setHttpRequestBase(httpPost, request);

        if (request.getParameters().size() > 0) {
            List<NameValuePair> parameters = new ArrayList<NameValuePair>(request.getParameters().size());
            request.getParameters().entrySet().forEach(en -> {
                parameters.add(new BasicNameValuePair(en.getKey().toString(), en.getValue().toString()));
            });
            httpPost.setEntity(new UrlEncodedFormEntity(parameters, Charset.forName(request.getCharset())));
        }
        return QlhException.runtime(() -> response(client, request, client.execute(httpPost)));
    }

    private static Response file(Request request) {
        HttpClient client = getClient(request);

        HttpPost httpPost = new HttpPost(request.getUrl());
        setHttpRequestBase(httpPost, request);

        if (request.getHeaders().size() > 0) {
            request.getHeaders().entrySet().forEach(en -> {
                httpPost.addHeader(en.getKey(), en.getValue());
            });
        }

        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        multipartEntityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

        if (request.getParameters().size() > 0) {
            request.getParameters().entrySet().forEach(en -> {
                multipartEntityBuilder.addTextBody(en.getKey().toString(),
                        en.getValue().toString(),
                        ContentType.MULTIPART_FORM_DATA);
            });
        }
        if (request.getStreams().size() > 0) {
            request.getStreams().entrySet().forEach(en -> {
                multipartEntityBuilder.addBinaryBody(en.getKey(),
                        en.getValue(),
                        ContentType.MULTIPART_FORM_DATA,
                        en.getKey());
            });
        }
        if (request.getFiles().size() > 0) {
            request.getFiles().entrySet().forEach(en -> {
                multipartEntityBuilder.addBinaryBody(en.getKey(),
                        en.getValue(),
                        ContentType.MULTIPART_FORM_DATA,
                        en.getKey());
            });
        }

        httpPost.setEntity(multipartEntityBuilder.build());
        return QlhException.runtime(() -> response(client, request, client.execute(httpPost)));
    }

    private static Response json(Request request) {
        HttpClient client = getClient(request);

        HttpPost httpPost = new HttpPost(request.getUrl());
        setHttpRequestBase(httpPost, request);
        if (request.getParameters().size() > 0) {
            httpPost.setEntity(new StringEntity(QlhJsonUtils.toJson(request.getParameters()), request.getCharset()));
        }
        if (request.getPostBody() != null) {
            httpPost.setEntity(new StringEntity(request.getPostBody(), request.getCharset()));
        }
        httpPost.addHeader(HTTP.CONTENT_TYPE, "application/json;charset=" + request.getCharset());
        return QlhException.runtime(() -> response(client, request, client.execute(httpPost)));
    }

    private static Response put(Request request) {
        HttpClient client = getClient(request);

        HttpPut httpPut = new HttpPut(request.getUrl());
        setHttpRequestBase(httpPut, request);
        if (request.getParameters().size() > 0) {
            httpPut.setEntity(new StringEntity(QlhJsonUtils.toJson(request.getParameters()), request.getCharset()));
        }
        if (request.getPostBody() != null) {
            httpPut.setEntity(new StringEntity(request.getPostBody(), request.getCharset()));
        }
        httpPut.addHeader(HTTP.CONTENT_TYPE, "application/json;charset=" + request.getCharset());
        return QlhException.runtime(() -> response(client, request, client.execute(httpPut)));
    }

    private static Response xml(Request request) {
        HttpClient client = getClient(request);

        HttpPost httpPost = new HttpPost(request.getUrl());
        setHttpRequestBase(httpPost, request);
        if (request.getParameters().size() > 0) {
            httpPost.setEntity(new StringEntity(QlhJsonUtils.toJson(request.getParameters()), request.getCharset()));
        }
        if (request.getPostBody() != null) {
            httpPost.setEntity(new StringEntity(request.getPostBody(), request.getCharset()));
        }
        httpPost.addHeader(HTTP.CONTENT_TYPE, "text/xml;charset=" + request.getCharset());

        try {
            return response(client, request, client.execute(httpPost));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private static void setHttpRequestBase(HttpRequestBase base, Request req) {
        try {
            log.info("YQHttp 请求地址: {}", base.getURI());
            log.info("YQHttp 请求参数: {}", QlhJsonUtils.toJson(req.getParameters()));
            if (QlhStringUtils.isNotBlank(req.getPostBody())) {
                log.info("YQHttp 请求Body: {}", req.getPostBody());
            }
        } catch (Exception e) {
            log.error("YQHttp打印日志错误", e);
        }
        if (req.getHeaders().size() > 0) {
            req.getHeaders().entrySet().forEach(en -> {
                base.addHeader(en.getKey(), en.getValue());
            });
        }
        base.setConfig(req.getRequestConfig());
    }

    private static Response response(HttpClient client, Request request, org.apache.http.HttpResponse resp) throws Exception {
        Response response = new Response();
        response.setStatus(resp.getStatusLine().getStatusCode());
        switch (request.getResponseType()) {
            case Request.RESPONSE_TYPE_STRING:
                response.setContent(EntityUtils.toString(resp.getEntity(), request.getCharset()));
                if (request.isLogFlag()) {
                    log.info("YQHttp 返回内容: {}", response.getContent());
                }
                break;
            case Request.RESPONSE_TYPE_BYTE:
                try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    QlhIoUtils.copy(resp.getEntity().getContent(), out);
                    response.setBytes(out.toByteArray());
                }
                break;
        }
        response.setHeaders(resp.getAllHeaders());
        EntityUtils.consume(resp.getEntity());
        ((CloseableHttpResponse) resp).close();
        return response;
    }

    @Data
    public static class Response {
        /**
         * http请求返回的字符串内容
         */
        private String content;
        private byte[] bytes;
        private Header[] headers;
        /**
         * HTTP code
         */
        private int status;

        public Header getHeader(String name) {
            if (headers != null) {
                for (Header header : headers) {
                    if (name.equals(header.getName())) {
                        return header;
                    }
                }
            }
            return null;
        }

        public <T> T toObject(Class<T> t) {
            return QlhJsonUtils.toObject(content, t);
        }

    }

    @Accessors(chain = true)
    @Data
    public static class Request {

        public final static int RESPONSE_TYPE_STRING = 1;
        public final static int RESPONSE_TYPE_BYTE = 2;
        public final static int RESPONSE_TYPE_STREAM = 3;
        /**
         * 自定义 HTTP client
         */
        private HttpClient client;
        /**
         * 请求地址
         */
        private String url;
        /**
         * 请求参数
         */
        private Map<Serializable, Object> parameters = new HashMap<>();
        /**
         * post body
         */
        private String postBody;
        /**
         * 超时参数
         */
        private RequestConfig requestConfig = RC_3;
        /**
         * 是否使用 HTTP 连接池
         */
        private boolean usePool = true;
        /**
         * http 编码, 默认 utf8
         */
        private String charset = "UTF-8";
        /**
         * 返回类型, 1: 字符串 (默认), 2:字节数组, 3: 输入流
         */
        private int responseType = RESPONSE_TYPE_STRING;
        private Map<String, InputStream> streams = new HashMap<>();
        private Map<String, File> files = new HashMap<>();
        /**
         * 请求头
         */
        private Map<String, String> headers = new HashMap<>();
        /**
         * 是否记录日志
         */
        private boolean logFlag = true;

        public Request() {
        }

        public Request(String url) {
            this.url = url;
        }


        public Request addParameter(String name, Object value) {
            this.parameters.put(name, value);
            return this;
        }

        public Request addHeader(String header, String value) {
            headers.put(header, value);
            return this;
        }

        /* ---------------- */
        public Response get() {
            return QlhHttp.get(this);
        }

        public Response json() {
            return QlhHttp.json(this);
        }

        public Response post() {
            return QlhHttp.post(this);
        }

        public Response put() {
            return QlhHttp.put(this);
        }

        public Response xml() {
            return QlhHttp.xml(this);
        }

        public Response file() {
            return QlhHttp.file(this);
        }
    }

    public static Request newRequest() {
        return new Request();
    }
}
