package com.aigo.analysis.dispatcher;

import com.aigo.analysis.AigoAnalysis;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

/**
 * @Description: 默认请求发送，后续抽空优化
 * @author: Eknow
 * @date: 2021/5/17 18:15
 */
public class DefaultPacketSender {

    private static final String TAG = AigoAnalysis.tag(DefaultPacketSender.class);

    private static final int CONNECT_TIMEOUT = 30 * 1000;
    private static final int READ_TIMEOUT = 30 * 1000;
    private static ExecutorService executorService = Executors.newFixedThreadPool(5);

    public static void get(final Packet packet, final DefaultPacketSender.OnRequestCallBack callBack) {
        executorService.execute(() -> getRequest(packet, callBack));
    }

    public static void post(final Packet packet, final DefaultPacketSender.OnRequestCallBack callBack) {
        executorService.execute(() -> postRequest(packet, callBack));
    }

    private static void getRequest(Packet packet, DefaultPacketSender.OnRequestCallBack callBack) {
        boolean isSuccess = false;
        String message = "";
        int statusCode = -1;

        HttpURLConnection connection = null;
        InputStream inputStream = null;
        ByteArrayOutputStream baos = null;
        try {
            URL url = new URL(packet.getTargetURL());
            connection = (HttpURLConnection) url.openConnection();

            Timber.tag(TAG).v("Connection is open to %s", connection.getURL().toExternalForm());
            Timber.tag(TAG).v("Sending: %s", packet);

            // 设定请求的方法为"POST"，默认是GET
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            // User-Agent  IE9的标识
//            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0;");
//            connection.setRequestProperty("Accept-Language", "zh-CN");
//            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("charset", "utf-8");
            /*
             * 当我们要获取我们请求的http地址访问的数据时就是使用connection.getInputStream().read()方式时我们就需要setDoInput(true)，
             * 根据api文档我们可知doInput默认就是为true。我们可以不用手动设置了，如果不需要读取输入流的话那就setDoInput(false)。
             * 当我们要采用非get请求给一个http网络地址传参 就是使用connection.getOutputStream().write() 方法时我们就需要setDoOutput(true), 默认是false
             */
            // 设置是否从httpUrlConnection读入，默认情况下是true;
            connection.setDoInput(true);
            // 设置是否向httpUrlConnection输出，如果是post请求，参数要放在http正文内，因此需要设为true, 默认是false;
            //connection.setDoOutput(true);//Android  4.0 GET时候 用这句会变成POST  报错java.io.FileNotFoundException
            connection.setUseCaches(false);
            connection.connect();//

            statusCode = connection.getResponseCode();
            Timber.tag(TAG).v("Transmission finished (code=%d).", statusCode);
            isSuccess = checkResponseCode(statusCode);
            if (isSuccess) {
                inputStream = connection.getInputStream();
                baos = new ByteArrayOutputStream();
                int readLen;
                byte[] bytes = new byte[1024];
                while ((readLen = inputStream.read(bytes)) != -1) {
                    baos.write(bytes, 0, readLen);
                }
                String result = baos.toString();
                Timber.tag(TAG).v("result: %s", result);

                message = result;
            } else {
                final StringBuilder errorReason = new StringBuilder();
                BufferedReader errorReader = null;
                try {
                    errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        errorReason.append(line);
                    }
                } finally {
                    if (errorReader != null) {
                        try {
                            errorReader.close();
                        } catch (IOException e) {
                            Timber.tag(TAG).d(e, "Failed to close the error stream.");
                        }
                    }
                }
                Timber.tag(TAG).w("Transmission failed (code=%d, reason=%s)", statusCode, errorReason.toString());
                message = errorReason.toString();
            }
        } catch (MalformedURLException e) {
            message = e.getMessage();
            e.printStackTrace();
        } catch (IOException e) {
            message = e.getMessage();
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                message = e.getMessage();
                e.printStackTrace();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        if (callBack != null) {
            if (isSuccess) {
                callBack.onSuccess(message);
            } else {
                callBack.onError(statusCode, message);
            }
        }
    }

    private static void postRequest(Packet packet, DefaultPacketSender.OnRequestCallBack callBack) {
        boolean isSuccess = false;
        String message = "";
        int statusCode = -1;

        HttpURLConnection connection = null;
        InputStream inputStream = null;
        ByteArrayOutputStream baos = null;
        try {
            URL url = new URL(packet.getTargetURL());
            connection = (HttpURLConnection) url.openConnection();

            Timber.tag(TAG).v("Connection is open to %s", connection.getURL().toExternalForm());
            Timber.tag(TAG).v("Sending: %s", packet);

            connection.setRequestMethod("POST");
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            // User-Agent  IE9的标识
//            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0;");
//            connection.setRequestProperty("Accept-Language", "zh-CN");
//            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("charset", "utf-8");
            /*
             * 当我们要获取我们请求的http地址访问的数据时就是使用connection.getInputStream().read()方式时我们就需要setDoInput(true)，
             * 根据api文档我们可知doInput默认就是为true。我们可以不用手动设置了，如果不需要读取输入流的话那就setDoInput(false)。
             * 当我们要采用非get请求给一个http网络地址传参 就是使用connection.getOutputStream().write() 方法时我们就需要setDoOutput(true), 默认是false
             */
            // 设置是否从httpUrlConnection读入，默认情况下是true;
            connection.setDoInput(true);
            // 设置是否向httpUrlConnection输出，如果是post请求，参数要放在http正文内，因此需要设为true, 默认是false;
            connection.setDoOutput(true);
            connection.setUseCaches(false);

            // set  params three way  OutputStreamWriter
            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
            // 发送请求params参数
            out.write(packet.getPostData().toString());
            out.flush();
            connection.connect();

            statusCode = connection.getResponseCode();
            Timber.tag(TAG).v("Transmission finished (code=%d).", statusCode);
            isSuccess = checkResponseCode(statusCode);
            if (isSuccess) {
                // 会隐式调用connect()
                inputStream = connection.getInputStream();
                baos = new ByteArrayOutputStream();
                int readLen;
                byte[] bytes = new byte[1024];
                while ((readLen = inputStream.read(bytes)) != -1) {
                    baos.write(bytes, 0, readLen);
                }
                String result = baos.toString();
                Timber.tag(TAG).v("result: %s", result);

                message = result;
            } else {
                final StringBuilder errorReason = new StringBuilder();
                BufferedReader errorReader = null;
                try {
                    errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        errorReason.append(line);
                    }
                } finally {
                    if (errorReader != null) {
                        try {
                            errorReader.close();
                        } catch (IOException e) {
                            Timber.tag(TAG).d(e, "Failed to close the error stream.");
                        }
                    }
                }
                Timber.tag(TAG).w("Transmission failed (code=%d, reason=%s)", statusCode, errorReason.toString());
                message = errorReason.toString();
            }

        } catch (MalformedURLException e) {
            message = e.getMessage();
            e.printStackTrace();
        } catch (IOException e) {
            message = e.getMessage();
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                message = e.getMessage();
                e.printStackTrace();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        if (callBack != null) {
            if (isSuccess) {
                callBack.onSuccess(message);
            } else {
                callBack.onError(statusCode, message);
            }
        }
    }

    private static boolean checkResponseCode(int code) {
        return code == HttpURLConnection.HTTP_NO_CONTENT || code == HttpURLConnection.HTTP_OK;
    }

    public interface OnRequestCallBack {
        void onSuccess(String json);
        void onError(int errorCode, String errorMsg);
    }
}
