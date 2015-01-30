package com.unbank.listener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TimerTask;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.servlet.ServletContext;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.wltea.analyzer.cfg.Configuration;
import org.wltea.analyzer.cfg.DefaultConfig;
import org.wltea.analyzer.dic.Dictionary;

import com.unbank.lucene.production.CreateIndex;
import com.unbank.uniq.Similarity;
import com.unbank.utl.ConnectionFactory;
import com.unbank.utl.PoolManager;
import com.unbank.utl.publicDao;
import com.unbank.utl.publicTools;

public class TimerRun extends TimerTask {
	private ServletContext context = null;
	//操作分类标识
	public static boolean operflag=false;
	//操作去重标识
	public static boolean operuniqflag=false;
	private static Map<String,Object> parametersMap=null;
	
	private static List<Integer> classlist=null;
	private static Map<Integer,Map<String,Object>> classMap=null;
	private static Map<Integer,List<Map<String,Object>>> keywordMap=null;
	private static Map<Integer,Map<String,String>> validkeywordMap=null;
	
	//关键词分类
	private static List<Map<String,Object>> wordclasslist=null;
	// 日志
	public static Logger logger = Logger.getLogger(TimerRun.class);
	public TimerRun() {
		
	}
	public TimerRun(ServletContext context) {
		this.context = context;
	}
	
	public static Map<String, Object> getParametersMap() {
		return parametersMap;
	}
	public static Map<Integer, Map<String, Object>> getClassMap() {
		return classMap;
	}
	

	public static Map<Integer, List<Map<String, Object>>> getKeywordMap() {
		return keywordMap;
	}
	public static Map<Integer, Map<String, String>> getValidkeywordMap() {
		return validkeywordMap;
	}
	
	public static List<Map<String, Object>> getWordclasslist() {
		return wordclasslist;
	}
	
	public static List<Integer> getClasslist() {
		return classlist;
	}
	@Override
	public void run() { 
		//分类
		try {
			operflag=true;
			//加载系统参数
			loadparameters();
			//加载分类主表
			loadclassinfor();
			//加载关键词词典表
			loadkeyworddictionary();
			//初始化数据
			initdata();
		} catch (Exception e) {
			publicTools.loggerException(logger, e);
		} finally {
			operflag=false;
		}
		//去重
		try{
			
			operuniqflag=true;
			//休眠一分钟以免冲突
			Thread.sleep(60000);
			Similarity.loaddata();
			
		}catch(Exception e){
			publicTools.loggerException(logger, e);
		}finally{
			operuniqflag=false;
		}
	}
	/**
	 * 加载系统参数*/
	public void loadparameters(){
		Connection conn=null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		ConnectionFactory dbConFactory = new ConnectionFactory();
		PoolManager dmgr = PoolManager.getInstance();
		Statement qtstmt = null;
		Map<String,Object> parametersMap1=new HashMap<String,Object>();
		
	    try{
		    
			conn = dbConFactory.getConnection("form1");
			conn.setAutoCommit(true);
			ps = conn.prepareStatement("select * from parameters where id=1");
			rs = ps.executeQuery();
			if (rs.next()) {
				String trainuindexpath=rs.getString("trainuindexpath");
				parametersMap1.put("trainuindexpath", trainuindexpath);
			}
			parametersMap=parametersMap1;
			ps.close();
		    rs.close();
		}catch(Exception e){
			publicTools.loggerException(logger, e);
		}finally{
			if(conn!=null)try {conn.close();} catch (SQLException e) {}
		}
		
	}
	/**
	 * 加载分类主表*/
	public void loadclassinfor(){
		Connection conn=null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		ConnectionFactory dbConFactory = new ConnectionFactory();
		PoolManager dmgr = PoolManager.getInstance();
		Statement qtstmt = null;
		Map<Integer,Map<String,Object>> classMap1=new HashMap<Integer,Map<String,Object>>();
		List<Integer> classlist1=new ArrayList<Integer>();
		List<Map<String,Object>> wordclasslist1= new ArrayList<Map<String,Object>>();
	    try{
		    
			conn = dbConFactory.getConnection("form1");
			conn.setAutoCommit(true);
			ps = conn.prepareStatement("select * from class where state=1 or state=2");
			rs = ps.executeQuery();
			while (rs.next()) {
				int id=rs.getInt("id");
				String classname=rs.getString("classname");
				String traintable=rs.getString("traintable");
				float thresholdvalue=rs.getFloat("thresholdvalue");
				String state=rs.getString("state");
				String strategytype=rs.getString("strategytype");
				Map<String,Object> map = new HashMap<String,Object>();
				map.put("id", id);
				map.put("classname", classname);
				map.put("traintable", traintable);
				map.put("thresholdvalue", thresholdvalue);
				map.put("strategytype", strategytype);
				map.put("state", state);
				
				classlist1.add(id);
				classMap1.put(id, map);
				
				if(strategytype.equals("2")){
					wordclasslist1.add(map);
				}
				
			}
			
			classMap=classMap1;
			classlist=classlist1;
			wordclasslist=wordclasslist1;
			ps.close();
		    rs.close();
		}catch(Exception e){
			publicTools.loggerException(logger, e);
		}finally{
			if(conn!=null)try {conn.close();} catch (SQLException e) {}
		}
		
	}
	/**
	 * 加载关键词词典表*/
	public void loadkeyworddictionary(){
		Connection conn=null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		ConnectionFactory dbConFactory = new ConnectionFactory();
		PoolManager dmgr = PoolManager.getInstance();
		Statement qtstmt = null;
		Map<Integer,List<Map<String,Object>>> keywordMap1=new HashMap<Integer,List<Map<String,Object>>>();
		Map<Integer,Map<String,String>> validkeywordMap1=new HashMap<Integer,Map<String,String>>();
	    try{
		    
			conn = dbConFactory.getConnection("form1");
			conn.setAutoCommit(true);
			ps = conn.prepareStatement("select * from keyworddictionary");
			rs = ps.executeQuery();
			while (rs.next()) {
				String keyword=rs.getString("keyword");
				int classid=rs.getInt("classid");
				String type=rs.getString("type");
				List<Map<String,Object>> list = null;
				if(keywordMap1.get(classid)==null){list=new ArrayList<Map<String,Object>>();keywordMap1.put(classid, list);}
				else{ list=keywordMap1.get(classid);}
				
				Map<String,String> map=null;
//				if(classid==3){
//					System.out.println(keyword);
//				}
				if(validkeywordMap1.get(classid)==null){map=new HashMap<String,String>();validkeywordMap1.put(classid, map);}
				else{ map=validkeywordMap1.get(classid);}
				
				Map<String,Object> l=new HashMap<String,Object>();
				l.put("keyword", keyword);
				l.put("type", type);
				list.add(l);
				
				String keys[]=keyword.split("\\&");
				for(int i=0;i<keys.length;i++){
					if(!keys[i].trim().equals("")){
						String k1[]=keys[i].split("\\+");
						for(int j=0;j<k1.length;j++){
							if(k1[j].indexOf(";")!=-1){
								String k2[]=k1[j].split(";");
								for(int k=0;k<k2.length;k++){
									map.put(k2[k], "");
								}
							}else{
								map.put(k1[j], "");
							}
						}
					}
					
				}
				
			}
			
			keywordMap=keywordMap1;
			validkeywordMap=validkeywordMap1;
			ps.close();
		    rs.close();
			
			ps.close();
		    rs.close();
		}catch(Exception e){
			publicTools.loggerException(logger, e);
		}finally{
			if(conn!=null)try {conn.close();} catch (SQLException e) {}
		}
		
	}
	/**
	 * 初始化数据*/
	public void initdata(){
		publicDao pd=new publicDao();
	    try{
	    	String indexpath=(String)parametersMap.get("trainuindexpath");
	    	
	    	//加载扩展词库
			Configuration cfg = DefaultConfig.getInstance();
			cfg.setUseSmart(true); 
			Dictionary.initial(cfg);
			
			Dictionary dictionary = Dictionary.getSingleton();
			List<String> words=new ArrayList<String>();
			for(int i=0;i<classlist.size();i++){
				int id=classlist.get(i);
				List<Map<String,Object>> cidianList=keywordMap.get(id);
				for(int j=0;j<cidianList.size();j++){
					Map<String,Object> m=cidianList.get(j);
					String keys[]=((String)m.get("keyword")).split("\\&");
					for(int k=0;k<keys.length;k++){
						if(!keys[k].trim().equals("")){
							String k1[]=keys[k].split("\\+");
							for(int l=0;l<k1.length;l++){
								if(k1[l].indexOf(";")!=-1){
									String k2[]=k1[l].split(";");
									for(int n=0;n<k2.length;n++){
										words.add(k2[n]);
									}
								}else{
									words.add(k1[l]);
								}
							}
						}
						
					}
				}
				
			}
			dictionary.addWords(words);
			Thread.sleep(1000);
			for(int i=0;i<classlist.size();i++){
				int id=classlist.get(i);
				Map<String,Object> map=classMap.get(id);
				String state=(String)map.get("state");
				if(state.equals("2")){
					String tablename=(String)classMap.get(id).get("traintable");
			    	CreateIndex.createIndex(indexpath, id, tablename);
			    	pd.doSql("form1", "update class set state=1 where id="+id);
				}
			}
		}catch(Exception e){
			publicTools.loggerException(logger, e);
		}finally{
			
		}
		
	}
	public static void main(String[] args) throws Exception {
		
		
	}
}