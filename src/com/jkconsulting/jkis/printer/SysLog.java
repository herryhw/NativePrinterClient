package com.jkconsulting.jkis.printer;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.log4j.Logger;

public class SysLog {

	public static Logger log = Logger.getLogger(SysLog.class);
	
	
	/**
     * 打印信息
     * 
     * @param obj
     */
    public static void info(Object obj) {
        try{
            /*** 获取输出信息的代码的位置 ***/
            String location = "";
            StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
            location = stacks[2].getClassName() + "." + stacks[2].getMethodName()
                    + "(" + stacks[2].getLineNumber() + ")";
            /*** 是否是异常  ***/
            if (obj instanceof Exception) {
                Exception e = (Exception) obj;
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw, true));
                String str = sw.toString();
                log.info(location + str);
            } else {
                log.info(location + obj.toString());
                if(JKISNativePrinter.isDebug){
            		System.out.println("debug info:"+obj.toString());
            	}
            }
        }catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /**
     * 打印错误
     * 
     * @param obj
     */
    public static void error(Object obj) {
        try{
            /*** 获取输出信息的代码的位置 ***/
            String location = "";
            StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
            location = stacks[2].getClassName() + "." + stacks[2].getMethodName()
                    + "(" + stacks[2].getLineNumber() + ")";
            
            /*** 是否是异常  ***/
            if (obj instanceof Exception) {
                Exception e = (Exception) obj;
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw, true));
                String str = sw.toString();
                log.error(location + str);
            } else {
                log.error(location + obj.toString());
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
