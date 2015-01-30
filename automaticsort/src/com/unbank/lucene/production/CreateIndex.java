package com.unbank.lucene.production;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.cfg.Configuration;
import org.wltea.analyzer.dic.Dictionary;
import org.wltea.analyzer.lucene.IKAnalyzer;

import com.unbank.listener.TimerRun;
import com.unbank.utl.ConnectionFactory;
import com.unbank.utl.publicTools;

public class CreateIndex {
	// 索引路径
//	public static final String indexpath = ResourceBundle.getBundle("dbconfig").getString("indexpath");
//	public static final String indexpath = "d://index";
	// 是否开启智能分词 false 不开启  true 开启
	public static final boolean capacitySplit = true;
	// 日志
	public static Logger logger = Logger.getLogger(CreateIndex.class);
	public static void createIndex(String indexpath,int classid,String tablename) throws Exception {  
		
        File file = new File(indexpath);  
        FSDirectory directory = FSDirectory.open(file);  
        //用来创建索引  
        Analyzer analyzer = new IKAnalyzer(capacitySplit);
        IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_36,  analyzer);  
        IndexWriter writer = new IndexWriter(directory, conf);  
        //删除满足条件的索引
		writer.deleteDocuments(new Term("classid",classid+""));
		
		ResultSet rs = null;
		PreparedStatement ps = null;
		Connection con=null;
		
		try{
			TimerRun timerRun=new TimerRun();
			//有效词典
			Map<String,String> cidianMap=timerRun.getValidkeywordMap().get(classid);
			con = ConnectionFactory.getConnection();
			con.setAutoCommit(true);
			if (con.isClosed()) {
				throw new IllegalStateException("connection is close");
			}
			
			int i=0;
			String sSQL="select * from "+tablename;
			ps = con.prepareStatement(sSQL);
			rs = ps.executeQuery();
			while (rs.next()){
				Document doc1 = new Document();  
				String text =rs.getString("text");
		          
				// 创建分词对象 isMaxWordLength
				Analyzer anal = new IKAnalyzer(true);
				StringReader reader = new StringReader(text); 
				// 分词
				TokenStream ts = anal.tokenStream("", reader);
				 
				CharTermAttribute term = ts.getAttribute(CharTermAttribute.class);
				// 遍历分词数据
				String rc0 = "";
				int count =0;
				while (ts.incrementToken()) {
					if(cidianMap.get(term.toString())!=null){
						rc0 = rc0 + term.toString() + "|";
						count++;
					}
						
				}
				System.out.println(count+"========="+rc0);
				
				if(count>=10){
					Field f1 = new Field("content", rc0, Store.YES, Index.ANALYZED);    
			        Field f2 = new Field("doc_id", rs.getString("doc_id"), Store.YES, Index.ANALYZED);  
			        Field f3 = new Field("classid", classid+"", Store.YES, Index.ANALYZED);
			         
			        doc1.add(f1);  
			        doc1.add(f2); 
			        doc1.add(f3);
			          
			        writer.addDocument(doc1);  i++;
				}
		        
			}
			System.out.println("有效训练-----------"+i);
	        
			rs.close();
			ps.close();
			
			
		}catch(Exception e){
			publicTools.loggerException(logger, e);
		}finally{
			try {
				if(con!=null)
					con.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}	
				
          
        writer.close();//一定要关闭writer，这样索引才能写到磁盘上  
	}
       
	// 过滤html标签
	public static String delHTMLTag(String htmlStr) {
		Pattern p_script = Pattern.compile("<img[^>]*?[\\s\\S]*?\\/>", Pattern.CASE_INSENSITIVE);
		Matcher m_script = p_script.matcher(htmlStr);
		htmlStr = m_script.replaceAll(""); // 过滤script标签

		return htmlStr.trim(); // 返回文本字符串
	}
	public static boolean isNumeric(String str){ 
	    Pattern pattern = Pattern.compile("[0-9]*"); 
	    return pattern.matcher(str).matches();    
	 } 
    public static void main(String[] args) throws Exception {  
//    	String sSQL = "select * from ptf_doc_text_银行  order by doc_id limit 0,10000";
//    	String sSQL = "select * from ptf_doc_text_银行 p1 left join ptf_doc_state_银行 p2 on p1.doc_id=p2.doc_id where p2.state=1";
//    	createIndex("d://index",1,"fsdfdsfdsf");
//    	new IndexTest().loadcidian();
//    	System.out.println("AAA".toLowerCase());
    }  
  
    

}
