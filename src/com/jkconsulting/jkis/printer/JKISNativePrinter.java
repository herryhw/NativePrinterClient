package com.jkconsulting.jkis.printer;

import java.awt.Desktop;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;

import org.apache.log4j.PropertyConfigurator;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRPrintServiceExporter;
import net.sf.jasperreports.engine.export.JRPrintServiceExporterParameter;
import net.sf.jasperreports.engine.query.JsonQueryExecuterFactory;
import net.sf.jasperreports.view.JasperViewer;
/**
 * 打印程序
 * 实现报表打印及标签打印
 * @author hw
 *
 */
public class JKISNativePrinter {

	public static boolean isDebug = false; //是否测试
	
	
	public static void main(String[] args) {
		
		/**********正式*************/
		
		if(!isDebug){
			if(args.length>0){
				
				String url = args[0].substring(args[0].indexOf(":")+1);

				JKISNativePrinter printer = new JKISNativePrinter();

				 PropertyConfigurator.configure("log4j.properties");
				try {
					

					HttpsUtil.getInstance().init();

					Map<String,String> mapReq = CRequest.URLRequest(url);

					if(null!=mapReq.get("print") && "file".equals(mapReq.get("print"))){

						printer.printFile(mapReq,url);
					}else{
						printer.print(mapReq,url);
					}
					
				} catch (Exception e) {
					SysLog.error(e);
					e.printStackTrace();
				}
			}
		}else{
			/************测试***********/
			
			String url = args[0].substring(args[0].indexOf(":")+1);
					
			JKISNativePrinter printer = new JKISNativePrinter();
			
			try {
				
				SysLog.info("url:"+url);
				PropertyConfigurator.configure("log4j.properties"); 
				HttpsUtil.getInstance().init();
				Map<String,String> mapReq = CRequest.URLRequest(url);
				if(null!=mapReq.get("print") && "file".equals(mapReq.get("print"))){
					printer.printFile(mapReq,url);
				}else{
					printer.print(mapReq,url);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	
	/*
	 * 请求打印
	 * 1.请求模板
	 * 2.请求数据
	 * 3。填充模板
	 * 4。 调用打印机打印
	 */
	private void print(Map<String,String> mapReq,String url) throws Exception{
		
		 //url页面路径
		SysLog.info(CRequest.UrlPage(url));
		
		//2.请求模板

		InputStream isTemplate = HttpsUtil.getInstance().sendPostResponseStream(url+"&method=report",null);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];  
		int len;  
		while ((len = isTemplate.read(buffer)) > -1 ) {  
		    baos.write(buffer, 0, len);  
		}  
		baos.flush(); 
		
		isTemplate.close();
		
		final List<JasperPrint> jasperPrintArray = new ArrayList<JasperPrint>();
		
		//3.请求数据
		String condition =  CommUtils.deCode(mapReq.get("condition"));

		JSONObject obj = JSONObject.parseObject(condition);
		String op = (String) obj.get("op");
		SysLog.info("-->op:"+op);

		if(null!=op && !"".equals(op)){
			String param = obj.get(op).toString();
			SysLog.info("-->param:"+obj.toJSONString());
			obj.remove(op);
			JSONObject newObj = (JSONObject) obj.clone();
			JSONArray array = JSONArray.parseArray(param);
			for(Object pa : array){
				newObj.put(op, pa);
				System.out.println(newObj.toJSONString());
				
				String path = CRequest.replaceAccessTokenReg(url, "condition", newObj.toJSONString());
				SysLog.info("-->path:"+path);
				//4.填充模板
				InputStream streamTemp = new ByteArrayInputStream(baos.toByteArray());
				String json = requestInfo(path+"&method=info");
				
				if(null!=obj.get("decode")){
					if((Boolean)obj.get("decode")){
						json = CommUtils.deCode(json);
					}
				}else{
					json = CommUtils.deCode(json);
				}
				SysLog.info("-->run json2:"+json);
				Map<String, Object> params = new HashMap<String, Object>();
				params.put(JRParameter.IS_IGNORE_PAGINATION, false);
		        params.put(JsonQueryExecuterFactory.JSON_INPUT_STREAM,new ByteArrayInputStream(json.getBytes("UTF-8")));
		        
		        
		        JSONObject tbObj = JSONObject.parseObject(json);
		        JSONObject dataObj = tbObj.getJSONObject("data");
		        if(null!=dataObj){
		        	String tablesCount = dataObj.getString("tablescount");
		        	if(null!=tablesCount && !"".equals(tablesCount) && !"0".equals(tablesCount)){
		        		int count = Integer.parseInt(tablesCount);
						InputStream isArray[] = new InputStream[count];
						
						for(int i=0;i<count;i++){
							
							isArray[i] =new ByteArrayInputStream(json.getBytes("UTF-8"));
							params.put("SUB_STREAM_"+i,isArray[i]);
						}
						
						JasperPrint jasperPrint=JasperFillManager.fillReport(streamTemp, params);
						
						for(InputStream inS : isArray){
							inS.close();
						}
						if(isDebug){
							JasperViewer.viewReport(jasperPrint); //预览
						}
						jasperPrintArray.add(jasperPrint);
						
		        	}else{
		        		InputStream is=new ByteArrayInputStream(json.getBytes("UTF-8"));
		        		params.put("SUB_STREAM", is);
						JasperPrint jasperPrint=JasperFillManager.fillReport(streamTemp, params);
						if(isDebug){
							JasperViewer.viewReport(jasperPrint); //预览
						}
						jasperPrintArray.add(jasperPrint);
						
						if(null!=is){
							is.close();
							is=null;
						}
		        	}
		        }else{
		        	InputStream is=new ByteArrayInputStream(json.getBytes("UTF-8"));
		        	params.put("SUB_STREAM", is);
					JasperPrint jasperPrint=JasperFillManager.fillReport(streamTemp, params);
					if(isDebug){
						JasperViewer.viewReport(jasperPrint); //预览
					}
					jasperPrintArray.add(jasperPrint);
					
					if(null!=is){
						is.close();
						is=null;
					}
		        }
		        
		        if(null!=streamTemp){
					streamTemp.close();
					streamTemp = null;
				}
			}
			
		}else{
			
			InputStream streamTemp = new ByteArrayInputStream(baos.toByteArray());
			String json = requestInfo(url+"&method=info");
			if(null!=obj.get("decode")){
				if((Boolean)obj.get("decode")){
					json = CommUtils.deCode(json);
				}
			}else{
				json = CommUtils.deCode(json);
			}
			SysLog.info("-->run json1:"+json);
			Map<String, Object> params = new HashMap<String, Object>();
			params.put(JRParameter.IS_IGNORE_PAGINATION, false);
	        params.put(JsonQueryExecuterFactory.JSON_INPUT_STREAM,new ByteArrayInputStream(json.getBytes("UTF-8")));
	        
	        
	        JSONObject tbObj = JSONObject.parseObject(json);
	        JSONObject dataObj = tbObj.getJSONObject("data");
	        if(null!=dataObj){
	        	String tablesCount = dataObj.getString("tablescount");
	        	if(null!=tablesCount && !"".equals(tablesCount) && !"0".equals(tablesCount)){
	        		
	        		int count = Integer.parseInt(tablesCount);
					InputStream isArray[] = new InputStream[count];
					
					for(int i=0;i<count;i++){
						
						isArray[i] =new ByteArrayInputStream(json.getBytes("UTF-8"));
						params.put("SUB_STREAM_"+i,isArray[i]);
					}
					
					JasperPrint jasperPrint=JasperFillManager.fillReport(streamTemp, params);
					if(isDebug){
						JasperViewer.viewReport(jasperPrint); //预览
					}
					jasperPrintArray.add(jasperPrint);
					
					for(InputStream is : isArray){
						is.close();
					}
	        	}else{
	    	        InputStream is=new ByteArrayInputStream(json.getBytes("UTF-8"));
	    			params.put("SUB_STREAM", is);
	    			JasperPrint jasperPrint=JasperFillManager.fillReport(streamTemp, params);
	    			if(isDebug){
						JasperViewer.viewReport(jasperPrint); //预览
					}
	    			jasperPrintArray.add(jasperPrint);
	    			
	    			if(null!=is){
	    				is.close();
	    				is=null;
	    			}
	        	}
	        }else{
	        	 InputStream is=new ByteArrayInputStream(json.getBytes("UTF-8"));
	    			params.put("SUB_STREAM", is);
	    			JasperPrint jasperPrint=JasperFillManager.fillReport(streamTemp, params);
	    			if(isDebug){
						JasperViewer.viewReport(jasperPrint); //预览
					}
	    			jasperPrintArray.add(jasperPrint);
	    			
	    			if(null!=is){
	    				is.close();
	    				is=null;
	    			}
	        }
	        
			if(null!=streamTemp){
				streamTemp.close();
				streamTemp = null;
			}
			
		}
		
		if(!isDebug){
			//5.调用打印机打印
			PrintRequestAttributeSet printRequestAttributeSet = new HashPrintRequestAttributeSet(); 
	        //定位默认的打印服务
	        PrintService defaultService = PrintServiceLookup.lookupDefaultPrintService();    
//	        //显示打印对话框
//	        PrintService service = ServiceUI.printDialog(null, 200, 200, printerService,     
//	                defaultService, flavor, printRequestAttributeSet);
//	        MediaSize ms = new MediaSize(241,279.4f,Size2DSyntax.MM,MediaSizeName.ISO_A4);
	        
//	        MediaSizeName sizeName = MediaSize.ISO.A4.getMediaSizeName();
//	        
			JRPrintServiceExporter exporter = new JRPrintServiceExporter(); 
//			
//			printRequestAttributeSet.add(sizeName);
			String copy = mapReq.get("copy");
			if(null!=copy && !"".equals(copy)){
				printRequestAttributeSet.add(new Copies(Integer.parseInt(copy)));
			}
			
			exporter.setParameter(JRExporterParameter.JASPER_PRINT_LIST, jasperPrintArray);
			exporter.setParameter(JRPrintServiceExporterParameter.PRINT_SERVICE, defaultService);
			exporter.setParameter(JRPrintServiceExporterParameter.PRINT_REQUEST_ATTRIBUTE_SET, printRequestAttributeSet); 
			exporter.setParameter(JRPrintServiceExporterParameter.PRINT_SERVICE_ATTRIBUTE_SET, defaultService.getAttributes()); 
			exporter.setParameter(JRPrintServiceExporterParameter.DISPLAY_PAGE_DIALOG, Boolean.FALSE); 
			exporter.setParameter(JRPrintServiceExporterParameter.DISPLAY_PRINT_DIALOG, Boolean.FALSE); //设置是否弹出打印机选择框

			exporter.exportReport();
	        
		}
		
	}
	/*
	 * 下载远程文件，本地打印
	 */
	private void printFile(Map<String,String> mapReq,String url) throws Exception{
//		String json = requestInfo(url+"&method=info");
		InputStream inputStream = HttpsUtil.getInstance().sendPostResponseStream(url+"&method=file",null);
//		SysLog.info("-->input :"+(inputStream == null));
//		System.out.println("result:"+json);
		String condition = CommUtils.deCode(mapReq.get("condition"));
		JSONObject obj = JSONObject.parseObject(condition);
		
		if (Desktop.isDesktopSupported()){
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.PRINT))
            {
            	OutputStream os = null;
            	File f  = null;
                try {
                	
                	// 1K的数据缓冲
                    byte[] bs = new byte[1024];
                    // 读取到的数据长度
                    int len;
                    // 输出的文件流保存到本地文件
                    
                	File direct = new File("temp");
                	if(!direct.exists()){
                		direct.mkdir();
                	}
                	if(null!=obj.getString("filetype") && !"".equals(obj.getString("filetype"))){
                		f = new File(direct.getPath(),"file."+obj.getString("filetype"));
                	}else{
                		f = new File(direct.getPath(),"file.html");
                	}
                	
                	if(f.exists()){
                		f.delete();
                	}
                	f.createNewFile();
                	os = new FileOutputStream(f);
                    // 开始读取
                    while ((len = inputStream.read(bs)) > -1) {
                        os.write(bs, 0, len);
                    }
                	
                    if(f!=null && f.exists()){
                    	SysLog.info("--print--");
                    	desktop.print(f);
                    }
                } catch (IOException e) {
                	SysLog.error(e);
                    e.printStackTrace();
                }finally{
                	 // 完毕，关闭所有链接
                    try {
                        os.close();
                        inputStream.close();
                        
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            System.exit(0);
        }
		
	}
	
	
	public String requestInfo(String reportInfo) throws Exception{

		SysLog.info("-->requestInfo:"+reportInfo);

		return HttpsUtil.getInstance().sendPostResponseString(reportInfo, null);
	}
	
	
}
