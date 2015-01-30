package com.unbank.lucene.production;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;
import org.wltea.analyzer.dic.Dictionary;
import org.wltea.analyzer.lucene.IKAnalyzer;

import com.unbank.listener.TimerRun;
import com.unbank.utl.publicDao;
import com.unbank.utl.publicTools;

import net.sf.json.JSONObject;

public class Comparesimilarity {
	// 日志
	public static Logger logger = Logger.getLogger(Comparesimilarity.class);
	private static IndexReader reader = null;//一般都是单例的
	public static synchronized IndexSearcher getSearcher(String indexpath) {
		   try {
		      if(reader==null) {
		    	  File file = new File(indexpath);
		    	  Directory mdDirectory = FSDirectory.open(file);
		    	  reader = IndexReader.open(mdDirectory);
		      } else {
		          IndexReader tr = IndexReader.openIfChanged(reader);
		          if(tr!=null) {
		             reader.close();
		             reader = tr;
		          }
		      }
		      return new IndexSearcher(reader);
		   } catch (Exception e) {
			   publicTools.loggerException(logger, e);
		   } 
		   return null;
	}
	public static String comparesimilarity(String newstr){
		IndexSearcher searcher = null;
		
		String id="";
		String title="";
		String content="";
		
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("errcode", "0");
		jsonObj.put("class", "-1");
		jsonObj.put("keyword", "");
		try {
			if(TimerRun.operflag==true){
				jsonObj.put("errcode", "1002");
				jsonObj.put("class", "");
				jsonObj.put("keyword", "");
			}
			
			TimerRun timerRun=new TimerRun();
			publicDao pd=new publicDao();
			String indexpath=(String)timerRun.getParametersMap().get("trainuindexpath");
			searcher = getSearcher(indexpath);
			
			//解析传入数据
			try{
				JSONObject demoJson = new JSONObject(newstr);
				title=demoJson.getString("title");
				content=demoJson.getString("content");
				id=demoJson.getString("id");
			}catch(Exception e){
				jsonObj.put("errcode", "1004");
				jsonObj.put("class", "");
				jsonObj.put("keyword", "");
				logger.error("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
				logger.error(e.toString());
	        	StackTraceElement[] este=e.getStackTrace();
	        	for(int i=0;i<este.length;i++){
	        		logger.error(este[i]);
	        	}
			}
			
			//当title或content为空字符串时查询数据库
			if(content.trim().equals("")||title.trim().equals("")){
				Map map=pd.getRsMap("form2", "select p1.text,p2.crawl_title from ptf_crawl_text p1 left join ptf_crawl p2 on p1.crawl_id=p2.crawl_id where p1.crawl_id="+id);
				if(map!=null){
					title=map.get("crawl_title")+"";
					content=map.get("text")+"";
				}else{
					jsonObj.put("errcode", "1003");
					jsonObj.put("class", "");
					jsonObj.put("keyword", "");
					return jsonObj.toString();
				}
				
			}
			String classid="";
			
			//使用关键词分类
			if(classid.equals("")){
				ok:
				for(int i=0;i<timerRun.getWordclasslist().size();i++){
					Map<String,Object> classmap=timerRun.getWordclasslist().get(i);
					int cid=(Integer)classmap.get("id");
					String strategytype=(String)classmap.get("strategytype");
					//第一种分词方式
					if(strategytype.equals("2")){
						List<Map<String, Object>> keywordlist=timerRun.getKeywordMap().get(cid);
						
						for(int f=0;f<keywordlist.size();f++){
				        	Map<String, Object> map=keywordlist.get(f);
				        	
				        	String type=(String)map.get("type");
				        	//找词
				        	if(type.equals("1")){
				        		if(map.get("keyword").equals(""))break;
				        		
				        		String[] keys=((String)map.get("keyword")).split("\\&");
				        		boolean flag=true;
				        		//标题中查找
				        		if(!keys[0].trim().equals("")){
				        			String key[]=keys[0].split("\\-");
				        			//包含词
				        			if(!key[0].trim().equals("")){
				        				String k1[]=key[0].split("\\+");
				        				
					        			ok1:
										for(int l=0;l<k1.length;l++){
											if(k1[l].indexOf(";")!=-1){
												//多个词满足一个
												String k2[]=k1[l].split(";");
												boolean orflag=true;
												for(int n=0;n<k2.length;n++){
													if(title.indexOf(k2[n])!=-1){
														orflag=false;
														break;
													}
												}
												if(orflag){
													flag=false;
							        				break ok1;
												}
											}else{
												if(title.indexOf(k1[l])==-1){
													flag=false;
							        				break ok1;
												}
											}
										}
				        			}
				        			//不包含词
				        			if(key.length==2&&!key[1].trim().equals("")){
				        				String k1[]=key[1].split("\\+");
					        			ok1:
										for(int l=0;l<k1.length;l++){
											if(k1[l].indexOf(";")!=-1){
												//多个词满足一个
												String k2[]=k1[l].split(";");
												boolean orflag=false;
												for(int n=0;n<k2.length;n++){
													if(title.indexOf(k2[n])!=-1){
														orflag=true;
														break;
													}
												}
												if(orflag){
													flag=false;
							        				break ok1;
												}
											}else{
												if(title.indexOf(k1[l])!=-1){
													flag=false;
							        				break ok1;
												}
											}
										}
				        			}
				        			
				        		}
				        		//内容中查找
				        		if(keys.length==2&&!keys[1].trim().equals("")){
				        			String key[]=keys[1].split("\\-");
				        			//包含词
				        			if(!key[0].trim().equals("")){
				        				String k1[]=key[0].split("\\+");
					        			ok1:
										for(int l=0;l<k1.length;l++){
											if(k1[l].indexOf(";")!=-1){
												//多个词满足一个
												String k2[]=k1[l].split(";");
												boolean orflag=true;
												for(int n=0;n<k2.length;n++){
													if(content.indexOf(k2[n])!=-1){
														orflag=false;
														break;
													}
												}
												if(orflag){
													flag=false;
							        				break ok1;
												}
											}else{
												if(content.indexOf(k1[l])==-1){
													flag=false;
							        				break ok1;
												}
											}
										}
				        			}
				        			//不包含词
				        			if(key.length==2&&!key[1].trim().equals("")){
				        				String k1[]=key[1].split("\\+");
					        			ok1:
										for(int l=0;l<k1.length;l++){
											if(k1[l].indexOf(";")!=-1){
												//多个词满足一个
												String k2[]=k1[l].split(";");
												boolean orflag=false;
												for(int n=0;n<k2.length;n++){
													if(content.indexOf(k2[n])!=-1){
														orflag=true;
														break;
													}
												}
												if(orflag){
													flag=false;
							        				break ok1;
												}
											}else{
												if(content.indexOf(k1[l])!=-1){
													flag=false;
							        				break ok1;
												}
											}
										}
				        			}
				        		}
				        		
				        		if(flag){
				        			classid=cid+"";
				        			break ok;
				        		}
				        	}
				        	//正则表达式
				        	if(type.equals("2")){
				        		if(map.get("keyword").equals(""))break;
				        		
				        		String[] keys=((String)map.get("keyword")).split("\\&");
				        		boolean flag=true;
				        		//在标题中查找
				        		if(!keys[0].trim().equals("")){
				        			String key[]=keys[1].split("\\-");
				        			//包含词
				        			if(!key[0].trim().equals("")){
				        				StringBuffer titlesb=new StringBuffer(".*");
					        			String k1[]=key[0].split("\\+");
					        			
										for(int l=0;l<k1.length;l++){
											if(k1[l].indexOf(";")!=-1){
												String k2[]=k1[l].split(";");
												titlesb.append("(");
												for(int n=0;n<k2.length;n++){
													titlesb.append(k2[n]+"|");
												}
												titlesb.delete(titlesb.length()-1, titlesb.length());
												titlesb.append(")"+"\\W{0,12}");
											}else{
												titlesb.append(k1[l]+"\\W{0,12}");
											}
										}
					        			titlesb.delete(titlesb.length()-8, titlesb.length());
					        			titlesb.append(".*");
					        			if(!title.matches(titlesb.toString())){
					        				flag=false;
					        			}
				        			}
				        			//不包含词
				        			if(key.length==2&&!key[1].trim().equals("")){
				        				String k1[]=key[1].split("\\+");
					        			ok1:
										for(int l=0;l<k1.length;l++){
											if(k1[l].indexOf(";")!=-1){
												//多个词满足一个
												String k2[]=k1[l].split(";");
												boolean orflag=false;
												for(int n=0;n<k2.length;n++){
													if(title.indexOf(k2[n])!=-1){
														orflag=true;
														break;
													}
												}
												if(orflag){
													flag=false;
							        				break ok1;
												}
											}else{
												if(title.indexOf(k1[l])!=-1){
													flag=false;
							        				break ok1;
												}
											}
										}
				        			}
				        		}
				        		//在内容中查找
				        		if(keys.length==2&&!keys[1].trim().equals("")){
				        			String key[]=keys[1].split("\\-");
				        			//包含词
				        			if(!key[0].trim().equals("")){
				        				StringBuffer contentsb=new StringBuffer(".*");
						        		String k1[]=key[0].split("\\+");
					        			
										for(int l=0;l<k1.length;l++){
											if(k1[l].indexOf(";")!=-1){
												String k2[]=k1[l].split(";");
												contentsb.append("(");
												for(int n=0;n<k2.length;n++){
													contentsb.append(k2[n]+"|");
												}
												contentsb.delete(contentsb.length()-1, contentsb.length());
												contentsb.append(")"+"\\W{0,12}");
											}else{
												contentsb.append(k1[l]+"\\W{0,12}");
											}
										}
										contentsb.delete(contentsb.length()-8, contentsb.length());
										contentsb.append(".*");
					        			if(!content.matches(contentsb.toString())){
					        				flag=false;
					        			}
				        			}
				        			//不包含词
				        			if(key.length==2&&!key[1].trim().equals("")){
				        				String k1[]=key[1].split("\\+");
					        			ok1:
										for(int l=0;l<k1.length;l++){
											if(k1[l].indexOf(";")!=-1){
												//多个词满足一个
												String k2[]=k1[l].split(";");
												boolean orflag=false;
												for(int n=0;n<k2.length;n++){
													if(content.indexOf(k2[n])!=-1){
														orflag=true;
														break;
													}
												}
												if(orflag){
													flag=false;
							        				break ok1;
												}
											}else{
												if(content.indexOf(k1[l])!=-1){
													flag=false;
							        				break ok1;
												}
											}
										}
				        			}
				        		}
				        		
				        		if(flag){
				        			classid=cid+"";
				        			break ok;
				        		}
				        	}
				        }
					}
			        
			        
				}
			}
			//在关键词分类找不到分类，使用训练集分类
			if(classid.equals("")){
				Analyzer analyzer = new IKAnalyzer(CreateIndex.capacitySplit);
				
				//清楚特殊符号
				content=handlespecialcharact(content);
				//查询条件
				BooleanQuery boolQuery = new BooleanQuery();
				boolQuery.setMaxClauseCount(100000);//查询条件长度
				String[] fieldName = { "content" }; 
				QueryParser queryParser = new MultiFieldQueryParser(Version.LUCENE_36, fieldName, analyzer);
				Query q2 = queryParser.parse(content);
				boolQuery.add(q2, BooleanClause.Occur.MUST);
						
				ScoreDoc[] docs=searcher.search(boolQuery,2).scoreDocs;
						
				
				float score=0;
				
				for(int i=0;i<docs.length;i++) { 
					Document doc = searcher.doc(docs[i].doc);
					classid=doc.get("classid");
					score=docs[i].score;
					break;//只取第一个
				}
				
				if(!classid.equals("")&&score!=0){
					float thresholdvalue=(Float)timerRun.getClassMap().get(Integer.parseInt(classid)).get("thresholdvalue");
					if(score<thresholdvalue){
						classid="";
					}
				}
			}
			
			if(!classid.equals("")){
				Map<String, String> validkeyword=timerRun.getValidkeywordMap().get(Integer.parseInt(classid));
				int count=0;
				
				//去标题和内容关键词的重词
				Map<String,String> quchong=new HashMap<String,String>();
				List<String> keywordlist=new ArrayList<String>();
				
				//标题提取关键词
				IKSegmenter ikSegmenter = new IKSegmenter(new StringReader(title), false);
		        Lexeme lexeme;
		        while ((lexeme = ikSegmenter.next()) != null&&count<5) {
		        	String kword=lexeme.getLexemeText();
		        	if(validkeyword.get(kword)!=null&&quchong.get(kword)==null){
		        		quchong.put(kword, "");
		        		keywordlist.add(kword.toUpperCase());
			        	count++;
			        }
		        }
		        
		        //内容提取关键词
		        List<Map.Entry<String, Integer>> wordFrenList=Segment.sortword(content);
			    for(int i=0;i<wordFrenList.size()&&count<5;i++){
			        Map.Entry<String,Integer> wordFrenEntry=wordFrenList.get(i);
//			          	System.out.println(wordFrenEntry.getKey()+"             的次数为"+wordFrenEntry.getValue());
			        if(validkeyword.get(wordFrenEntry.getKey())!=null&&quchong.get(wordFrenEntry.getKey())==null){
			        	quchong.put(wordFrenEntry.getKey(), "");
			        	keywordlist.add(wordFrenEntry.getKey().toUpperCase());
			        	count++;
			        }
			    }
			    
			    //长词包含短词去重
			    quchong.clear();
			    String[] keywordarray=sortStringArray(keywordlist);
			    
			    for(int i=0;i<keywordarray.length;i++){
			    	boolean flag=true;
			    	for(int j=i+1;j<keywordarray.length;j++){
			    		if(keywordarray[j].indexOf(keywordarray[i])!=-1){
			    			flag=false;
			    			break;
			    		}
			    	}
			    	if(flag)
			    		quchong.put(keywordarray[i], "");
			    }
			    
			    StringBuffer keyword=new StringBuffer("");
			    for(int i=0;i<keywordlist.size();i++){
			    	if(quchong.get(keywordlist.get(i))!=null)
			    		keyword.append(keywordlist.get(i)+",");
			    }
			    if(keywordlist.size()>0){
			    	keyword.delete(keyword.length()-1, keyword.length());
			    }
			    
			    jsonObj.put("keyword", keyword.toString());
			    jsonObj.put("class", classid);
			}
		} catch (Exception e) {e.printStackTrace();
			jsonObj.put("errcode", "1001");
			jsonObj.put("class", "");
			jsonObj.put("keyword", "");
			publicTools.loggerException(logger, e);
		}finally{
			logger.info(id+"-----------"+jsonObj.toString());
			if(searcher!=null)try {searcher.close();} catch (IOException e) {}
		}
		return jsonObj.toString();
	}
	public static String[] sortStringArray(List<String> keywordlist) {
		String[] arrStr=new String[keywordlist.size()];
		for(int i=0;i<keywordlist.size();i++){
			arrStr[i]=keywordlist.get(i);
		}
	     String temp;
	     for (int i = 0; i < arrStr.length; i++) {
	    	 for (int j = arrStr.length - 1; j > i; j--) {
	    		 if (arrStr[i].length() > arrStr[j].length()) {
	    			 temp = arrStr[j];
	    			 arrStr[j] = arrStr[i];
	    			 arrStr[i] = temp;
	    		 }
	    	 }
	     }
	     return arrStr;
	}
	public static String handlespecialcharact(String record){
//		String[] strs="+,-,&&,||,!,(,),{,},[,],^,\",~,*,?,:,".split(",");
		
//		for(int i=0;i<strs.length;i++){
//			record=record.replaceAll("["+strs[i]+"]", " ");
//		}
		String record1=record+"";
		record1=record1.replaceAll("[+]", " ");
		record1=record1.replaceAll("[-]", " ");
		record1=record1.replaceAll("&&", " ");
		record1=record1.replaceAll("\\|", " ");
		record1=record1.replaceAll("[!]", " ");
		record1=record1.replaceAll("[(]", " ");
		record1=record1.replaceAll("[)]", " ");
		record1=record1.replaceAll("[{]", " ");
		record1=record1.replaceAll("[}]", " ");
		record1=record1.replaceAll("\\[", " ");
		record1=record1.replaceAll("\\]", " ");
		record1=record1.replaceAll("\\^", " ");
		record1=record1.replaceAll("\"", " ");
		record1=record1.replaceAll("\\~", " ");
		record1=record1.replaceAll("\\*", " ");
		record1=record1.replaceAll("\\?", " ");
		record1=record1.replaceAll("\\:", " ");
		return record1;
	}
	public static void main(String[] args){
		
	}
}
