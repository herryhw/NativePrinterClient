package com.jkconsulting.jkis.printer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class HttpsUtil {

	
	private static String KEY_STORE_FILE="keystore";  
	private static String KEY_STORE_PASS="keypass";  
	private static String TRUST_STORE_FILE="truststore";  
	private static String TRUST_STORE_PASS="trustpass";
	
	private static String CONFIG_FOLD_NAME="config";
	private static String CONFIG_FILE="config.properties";
	
	private String keystore = null;
	private String keypass=null;
	private String truststore = null;
	private String trustpass=null;
	
	private String path = null;
	
	private static SSLContext sslContext;  
	
	private static final class Instance{
		private static final HttpsUtil instance = new HttpsUtil();
	}
	public final static HttpsUtil getInstance(){
		return Instance.instance;
	}
	/**
	 * 初始化配置文件
	 * @throws FileNotFoundException 
	 */
	public void init(){
		InputStream is  = null;
		try{
			
			path = CONFIG_FOLD_NAME+File.separator;
			
			is = new FileInputStream(new File(path+CONFIG_FILE));
			
			Properties prop = new Properties();
			prop.load(is);
			keystore = prop.getProperty(KEY_STORE_FILE);
			keypass = prop.getProperty(KEY_STORE_PASS);
			truststore = prop.getProperty(TRUST_STORE_FILE);
			trustpass = prop.getProperty(TRUST_STORE_PASS);
			
		}catch(Exception e){
//			e.printStackTrace();
		}finally {
			if(is!=null){
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				is = null;
			}
		}
	
	}
	
	 /** 
     * 向指定URL发送GET方法的请求 
     *  
     * @param url 
     *            发送请求的URL 
     * @param param 
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。 
     * @return URL 所代表远程资源的响应结果 
     *  
     */  
    public String sendGet(String url, String param) {  
    	url = url.replaceAll(" ", "%20");
        String result = "";  
        BufferedReader in = null;  
        try {  
            String urlNameString = url + "?" + param;  
            URL realUrl = new URL(urlNameString);  
            // 打开和URL之间的连接  
            HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();  
            // 打开和URL之间的连接  
            if(connection instanceof HttpsURLConnection){  
                ((HttpsURLConnection)connection)  
                .setSSLSocketFactory(getSSLContext().getSocketFactory());  
            }  
  
            // 设置通用的请求属性  
            connection.setRequestProperty("accept", "*/*");  
            connection.setRequestProperty("connection", "Keep-Alive");  
            connection.setRequestProperty("user-agent",  
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");  
            // 建立实际的连接  
            connection.connect();  
            // 获取所有响应头字段  
            Map<String, List<String>> map = connection.getHeaderFields();  
            // 遍历所有的响应头字段  
            for (String key : map.keySet()) {  
                System.out.println(key + "--->" + map.get(key));  
            }  
            // 定义 BufferedReader输入流来读取URL的响应  
  
            if(connection.getResponseCode()==200){  
                in = new BufferedReader(new InputStreamReader(  
                        connection.getInputStream()));  
            }else{  
                in = new BufferedReader(new InputStreamReader(  
                        connection.getErrorStream()));  
            }  
            String line;  
            while ((line = in.readLine()) != null) {  
                result += line;  
            }  
  
        } catch (Exception e) {  
            System.out.println("发送GET请求出现异常！" + e);  
            e.printStackTrace();  
        }  
        // 使用finally块来关闭输入流  
        finally {  
            try {  
                if (in != null) {  
                    in.close();  
                }  
            } catch (Exception e2) {  
                e2.printStackTrace();  
            }  
        }  
        return result;  
    }  
  
    /**  
     * 向指定 URL 发送POST方法的请求  
     *   
     * @param url  
     *            发送请求的 URL  
     * @param param  
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。  
     * @return 所代表远程资源的响应结果  
     */  
    public InputStream sendPostResponseStream(String url, String param) {
    	url = url.replaceAll(" ", "%20");
        PrintWriter out = null;  
//        BufferedReader in = null;
        InputStream is=null;
        
        try {  
            URL realUrl = new URL(url);  //编码转换
//            URI uri = new URI(realUrl.getProtocol(), realUrl.getUserInfo(), realUrl.getHost(), realUrl.getPort(), realUrl.getPath(), realUrl.getQuery(), realUrl.getRef());
//            realUrl = uri.toURL();
            // 打开和URL之间的连接  
            HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();  
            if(conn instanceof HttpsURLConnection){  
                ((HttpsURLConnection)conn)  
                .setSSLSocketFactory(getSSLContext().getSocketFactory());  
            }  
            // 设置通用的请求属性  
            conn.setRequestProperty("accept", "*/*");  
            conn.setRequestProperty("connection", "Keep-Alive");  
            conn.setRequestProperty("user-agent",  
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");  
            // 发送POST请求必须设置如下两行  
            conn.setDoOutput(true);  
            conn.setDoInput(true);  
            // 获取URLConnection对象对应的输出流  
            out = new PrintWriter(conn.getOutputStream());  
            // 发送请求参数  
            out.print(param);  
            // flush输出流的缓冲  
            out.flush();  
            SysLog.info("-->sendPostResponseStream response code:"+conn.getResponseCode());
            // 定义BufferedReader输入流来读取URL的响应  
            if(conn.getResponseCode()==200){  
            	is = conn.getInputStream();
            }else{  
            	  is = conn.getErrorStream();
            }  
        } catch (Exception e) {  
            System.out.println("发送 POST 请求出现异常！"+e);  
            e.printStackTrace();  
        }  
        //使用finally块来关闭输出流、输入流  
        finally{  
            try{  
                if(out!=null){  
                    out.close();  
                }   
            }catch(Exception ex){  
                ex.printStackTrace();  
            }  
        }  
        return is;  
    }  
    
    
    /**  
     * 向指定 URL 发送POST方法的请求  
     *   
     * @param url  
     *            发送请求的 URL  
     * @param param  
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。  
     * @return 所代表远程资源的响应结果  
     */  
    public String sendPostResponseString(String url, String param) {
    	url = url.replaceAll(" ", "%20");
        PrintWriter out = null;  
        String result="";
        BufferedReader in = null;
        
        try {  
            URL realUrl = new URL(url);  //编码转换
            URI uri = new URI(realUrl.getProtocol(), realUrl.getUserInfo(), realUrl.getHost(), realUrl.getPort(), realUrl.getPath(), realUrl.getQuery(), realUrl.getRef());
            realUrl = uri.toURL();
            // 打开和URL之间的连接  
            HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();  
            if(conn instanceof HttpsURLConnection){  
                ((HttpsURLConnection)conn)  
                .setSSLSocketFactory(getSSLContext().getSocketFactory());  
            }  
//            System.out.println("-->sendPostResponseString:"+url+",param:"+param);
            // 设置通用的请求属性  
            conn.setRequestProperty("accept", "*/*");  
            conn.setRequestProperty("connection", "Keep-Alive");  
            conn.setRequestProperty("user-agent",  
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");  
            // 发送POST请求必须设置如下两行  
            conn.setDoOutput(true);  
            conn.setDoInput(true);  
            // 获取URLConnection对象对应的输出流  
            out = new PrintWriter(conn.getOutputStream());  
            // 发送请求参数  
            out.print(param);  
            // flush输出流的缓冲  
            out.flush();  
            SysLog.info("-->sendPostResponseString response code:"+conn.getResponseCode());
            // 定义BufferedReader输入流来读取URL的响应  
            if(conn.getResponseCode()==200){  
                in = new BufferedReader(  
                        new InputStreamReader(conn.getInputStream()));
            }else{  
                in = new BufferedReader(  
                        new InputStreamReader(conn.getErrorStream()));
            }  
            String line="";  
            while ((line = in.readLine()) != null) {  
                result += line;  
            }  
        } catch (Exception e) {  
            System.out.println("发送 POST 请求出现异常！"+e);  
            e.printStackTrace();  
        }  
        //使用finally块来关闭输出流、输入流  
        finally{  
            try{  
                if(out!=null){  
                    out.close();  
                }  
                if(in!=null){  
                    in.close();  
                }  
            }catch(IOException ex){  
                ex.printStackTrace();  
            }  
        }  
        return result;  
    } 
  
    private SSLContext getSSLContext(){  
        long time1=System.currentTimeMillis();  
        if(sslContext==null){  
            try {  
                KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");  
                kmf.init(getkeyStore(),keypass.toCharArray());  
                KeyManager[] keyManagers = kmf.getKeyManagers();  
  
                TrustManagerFactory trustManagerFactory=TrustManagerFactory.getInstance("SunX509");  
                trustManagerFactory.init(getTrustStore());  
                TrustManager[]  trustManagers= trustManagerFactory.getTrustManagers();  
  
                sslContext = SSLContext.getInstance("TLS");  
                sslContext.init(keyManagers, trustManagers, new SecureRandom());  
                HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {  
                    @Override  
                    public boolean verify(String hostname, SSLSession session) {  
                        return true;  
                    }  
                });  
            } catch (NoSuchAlgorithmException e) {  
                e.printStackTrace();  
            } catch (UnrecoverableKeyException e) {  
                e.printStackTrace();  
            } catch (KeyStoreException e) {  
                e.printStackTrace();  
            } catch (KeyManagementException e) {  
                e.printStackTrace();  
            }  
        }  
        long time2=System.currentTimeMillis();  
        System.out.println("SSLContext 初始化时间："+(time2-time1));  
        return sslContext;  
    }  
  
   /*
    * 获取密钥文件
	*/
    private  KeyStore getkeyStore(){  
        KeyStore keySotre=null;  
        try {  
            keySotre = KeyStore.getInstance("PKCS12");  
            FileInputStream fis = new FileInputStream(new File(path+File.separator+keystore));
//            FileInputStream fis = new FileInputStream(new File(this.getClass().getResource(path+KEY_STORE_FILE).getFile()));
            keySotre.load(fis, keypass.toCharArray());  
            fis.close();  
        } catch (KeyStoreException e) {  
            e.printStackTrace();  
        } catch (FileNotFoundException e) {  
            e.printStackTrace();  
        } catch (NoSuchAlgorithmException e) {  
            e.printStackTrace();  
        } catch (CertificateException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
        return keySotre;  
    }  
    private KeyStore getTrustStore(){  
        KeyStore trustKeyStore=null;  
        FileInputStream fis=null;  
        try {  
            trustKeyStore=KeyStore.getInstance("JKS");  
            fis = new FileInputStream(new File(path+truststore));
//            fis = new FileInputStream(new File(this.getClass().getResource(path+TRUST_STORE_FILE).getFile()));
            trustKeyStore.load(fis, trustpass.toCharArray());  
        } catch (FileNotFoundException e) {  
            e.printStackTrace();  
        } catch (KeyStoreException e) {  
            e.printStackTrace();  
        } catch (NoSuchAlgorithmException e) {  
            e.printStackTrace();  
        } catch (CertificateException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }finally{  
            try {
            	if(fis!=null){
            		fis.close();
            		fis=null;
            	}
			} catch (IOException e) {
				e.printStackTrace();
			}  
        }  
        return trustKeyStore;  
    }  
}
