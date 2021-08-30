package com.aigo.analysis.dispatcher;

import com.aigo.analysis.AigoAnalysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.cert.X509Certificate;
import java.util.zip.GZIPOutputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import timber.log.Timber;

/**
 * @Description: 默认实现的数据包发送器
 * @author: Eknow
 * @date: 2021/8/18 14:57
 */
public class DefaultPacketSender implements PacketSender {

    private static final String TAG = AigoAnalysis.tag(DefaultPacketSender.class);
    private long mTimeout = Dispatcher.DEFAULT_CONNECTION_TIMEOUT;
    private boolean mGzip = false;
    private static final String HTTP_PROTOCOL = "HTTPS";

    @Override
    public boolean send(Packet packet, PacketSenderCallback callback) {
        HttpURLConnection urlConnection = null;
        try {
            //忽略https证书
            URL url = new URL(packet.getTargetURL());
            if (HTTP_PROTOCOL.equals(url.getProtocol().toUpperCase())) {
                trustAllHosts();
                HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
                https.setHostnameVerifier(DO_NOT_VERIFY);
                urlConnection = https;
            } else {
                urlConnection = (HttpURLConnection) url.openConnection();
            }

            Timber.tag(TAG).v("Connection is open to %s", urlConnection.getURL().toExternalForm());
            Timber.tag(TAG).v("Sending: %s", packet);

            urlConnection.setConnectTimeout((int) mTimeout);
            urlConnection.setReadTimeout((int) mTimeout);

            if (packet.getPostData() != null) {
                // POST
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("charset", "utf-8");
                final String toPost = packet.getPostData().toString();
                if (mGzip) {
                    //开启 GZip 压缩请求数据
                    urlConnection.addRequestProperty("Content-Encoding", "gzip");
                    ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
                    GZIPOutputStream gzipStream = null;
                    try {
                        gzipStream = new GZIPOutputStream(byteArrayOS);
                        gzipStream.write(toPost.getBytes(Charset.forName("UTF8")));
                    } finally {
                        if (gzipStream != null) {
                            gzipStream.close();
                        }
                    }
                    OutputStream outputStream = null;
                    try {
                        outputStream = urlConnection.getOutputStream();
                        outputStream.write(byteArrayOS.toByteArray());
                    } finally {
                        if (outputStream != null) {
                            try {
                                outputStream.close();
                            } catch (IOException e) {
                                Timber.tag(TAG).d(e, "Failed to close output stream after writing gzipped POST data.");
                            }
                        }
                    }
                } else {
                    BufferedWriter writer = null;
                    try {
                        writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8"));
                        writer.write(toPost);
                    } finally {
                        if (writer != null) {
                            try {
                                writer.close();
                            } catch (IOException e) {
                                Timber.tag(TAG).d(e, "Failed to close output stream after writing POST data.");
                            }
                        }
                    }
                }
            } else {
                // GET
                urlConnection.setDoOutput(false);
            }

            int statusCode = urlConnection.getResponseCode();
            Timber.tag(TAG).v("Transmission finished (code=%d).", statusCode);
            final boolean successful = checkResponseCode(statusCode);
            String message = "";

            if (successful) {
                //请求成功
                InputStream inputStream = urlConnection.getInputStream();
                ByteArrayOutputStream baos = null;
                if (inputStream != null) {
                    try {
                        baos = new ByteArrayOutputStream();
                        int readLen;
                        byte[] bytes = new byte[1024];
                        while ((readLen = inputStream.read(bytes)) != -1) {
                            baos.write(bytes, 0, readLen);
                        }
                        String result = baos.toString();
                        message = result;
                        Timber.tag(TAG).v("result: %s", result);
                    } catch (Exception e) {
                        Timber.tag(TAG).d(e);
                    } finally {
                        inputStream.close();
                        if (baos != null) {
                            baos.close();
                        }
                    }
                }
            } else {
                //失败
                final StringBuilder errorReason = new StringBuilder();
                BufferedReader errorReader = null;
                try {
                    errorReader = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream()));
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        errorReason.append(line);
                    }
                } finally {
                    if (errorReader != null) {
                        try {
                            errorReader.close();
                        } catch (IOException e) {
                            Timber.tag(TAG).d(e);
                        }
                    }
                }
                message = errorReason.toString();
                Timber.tag(TAG).w("Transmission failed (code=%d, reason=%s)", statusCode, errorReason.toString());
            }
            if (callback != null) {
                if (successful) {
                    callback.onSuccess(message);
                } else {
                    callback.onError(statusCode, message);
                }
            }
            return successful;
        } catch (Exception e) {
            Timber.tag(TAG).e(e, "Transmission failed unexpectedly.");
            return false;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    @Override
    public void setTimeout(long timeout) {
        mTimeout = timeout;
    }

    @Override
    public void setGzipData(boolean gzip) {
        mGzip = gzip;
    }

    private static boolean checkResponseCode(int code) {
        return code == HttpURLConnection.HTTP_NO_CONTENT || code == HttpURLConnection.HTTP_OK;
    }

    /**
     * 覆盖java默认的证书验证，信任所有
     */
    public static void trustAllHosts() {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[]{};
            }

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }
        }};

        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置不验证主机
     */
    public final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };
}
