package com.jkconsulting.jkis.printer;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * 通用工具
 * */
public class CommUtils {
	private static final String UTF_8 = "utf-8";
	public static final String RESPONSE_CODE = "responseCode";
	public static final int OK_CODE = 200;
	public static final int APP_MANTAIN_CODE = 104;

	public static final int PAGE_SIZE = 10;
	/**
	 * 请求参数值两次utf-8解码
	 * @param	String inText	请求参数值
	 * @return	String
	 * @author	feng.lin
	 * */
	public static String deCode(String inText) throws UnsupportedEncodingException {
//		inText = inText.replaceAll(" ", "%20");
		return URLDecoder.decode(URLDecoder.decode(inText, UTF_8), UTF_8);
	}
	/**
	 * 响应数据值两次utf-8编码
	 * @param	String inText	响应数据值
	 * @return	String
	 * @author	feng.lin
	 * */
	public static String enCode(String inText) throws UnsupportedEncodingException {
		char[] tempArray = inText.toCharArray();
		StringBuffer sb = new StringBuffer();
		for (char temp : tempArray) {
			if (Character.isWhitespace(temp)) {
				sb.append(" ");
			} else {
				sb.append(URLEncoder.encode(URLEncoder.encode(String.valueOf(temp), UTF_8), UTF_8));
			}

		}
		return sb.toString();
	}
	/**
	 * 验证邮箱格式
	 * @param	String email	电子邮箱	
	 * @return	boolean
	 * @author	feng.lin
	 * */
	public static boolean isEmailFormat(String email) {
		Pattern pattern = Pattern.compile(
				"^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$");
		Matcher matcher = pattern.matcher(email);
		return matcher.matches();
	}
	
	public static String getRealPath(){
		String realPath = JKISNativePrinter.class.getClassLoader().getResource("").getFile();
		File file = new File(realPath);
		realPath = file.getAbsolutePath();
		
		try {
			realPath = URLDecoder.decode(realPath,"utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return realPath;
	}
	
	public static String getAppPath(Class<?> cls) {
        // 检查用户传入的参数是否为空
        if (cls == null)
            throw new java.lang.IllegalArgumentException("参数不能为空！");

        ClassLoader loader = cls.getClassLoader();
        // 获得类的全名，包括包名
        String clsName = cls.getName();
        // 此处简单判定是否是Java基础类库，防止用户传入JDK内置的类库
        if (clsName.startsWith("java.") || clsName.startsWith("javax.")) {
            throw new java.lang.IllegalArgumentException("不要传送系统类！");
        }
        // 将类的class文件全名改为路径形式
        String clsPath = clsName.replace(".", "/") + ".class";

        // 调用ClassLoader的getResource方法，传入包含路径信息的类文件名
        java.net.URL url = loader.getResource(clsPath);
        // 从URL对象中获取路径信息
        String realPath = url.getPath();
        // 去掉路径信息中的协议名"file:"
        int pos = realPath.indexOf("file:");
        if (pos > -1) {
            realPath = realPath.substring(pos + 5);
        }
        // 去掉路径信息最后包含类文件信息的部分，得到类所在的路径
        pos = realPath.indexOf(clsPath);
        realPath = realPath.substring(0, pos - 1);
        // 如果类文件被打包到JAR等文件中时，去掉对应的JAR等打包文件名
        if (realPath.endsWith("!")) {
            realPath = realPath.substring(0, realPath.lastIndexOf("/"));
        }
        java.io.File file = new java.io.File(realPath);
        realPath = file.getAbsolutePath();

        try {
            realPath = java.net.URLDecoder.decode(realPath, "utf-8");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return realPath;
    }// getAppPath定义结束
	
}
