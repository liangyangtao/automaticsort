package com.unbank.lucene.production;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.wltea.analyzer.cfg.Configuration;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;
import org.wltea.analyzer.dic.Dictionary;
import org.wltea.analyzer.lucene.IKAnalyzer;

import com.unbank.utl.publicTools;

public class Segment {
	// 日志
	public static Logger logger = Logger.getLogger(Segment.class);
	public static List<Map.Entry<String, Integer>> sortword(String text){
		List<Map.Entry<String, Integer>> wordFrenList=null;
		try{
			Map<String, Integer> wordsFren=new HashMap<String, Integer>();
	        IKSegmenter ikSegmenter = new IKSegmenter(new StringReader(text), true);
	        Lexeme lexeme;
	        while ((lexeme = ikSegmenter.next()) != null) {
	            if(lexeme.getLexemeText().length()>1){
	                if(wordsFren.containsKey(lexeme.getLexemeText())){
	                    wordsFren.put(lexeme.getLexemeText(),wordsFren.get(lexeme.getLexemeText())+1);
	                }else {
	                    wordsFren.put(lexeme.getLexemeText(),1);
	                }
	            }
	        }
	        
	        wordFrenList = new ArrayList<Map.Entry<String, Integer>>(wordsFren.entrySet());
	        Collections.sort(wordFrenList, new Comparator<Map.Entry<String, Integer>>() {
	            public int compare(Map.Entry<String, Integer> obj1, Map.Entry<String, Integer> obj2) {
	                return obj2.getValue() - obj1.getValue();
	            }
	        });
		}catch(Exception e){
			publicTools.loggerException(logger, e);
		}
        
        return wordFrenList;
    }
	public static List<String> getSegmentlist(String word){
		List<String> list=new ArrayList<String>();
		try{
			IKSegmenter ikSegmenter = new IKSegmenter(new StringReader(word), CreateIndex.capacitySplit);
	        Lexeme lexeme;
	        while ((lexeme = ikSegmenter.next()) != null) {
	        	String kword=lexeme.getLexemeText();
	        	list.add(kword);
	        }
		}catch(Exception e){
			publicTools.loggerException(logger, e);
		}
		
		return list;
		
	}
	public static List<String> getSegmentlist1( String s) throws Exception {
		
		List<String> list=new ArrayList<String>();
		Analyzer a = new IKAnalyzer();
		
		StringReader reader = new StringReader(s);
		TokenStream ts = a.tokenStream(s, reader);

		while (ts.incrementToken()) {
			//AttributeImpl ta = new AttributeImpl();
			CharTermAttribute ta = ts.getAttribute(CharTermAttribute.class);
			//TermAttribute ta = ts.getAttribute(TermAttribute.class);
			
			list.add(ta.toString());
		}
		return list;
	}
	public static void main(String args[]) throws Exception {
//        String text = "IKAnalyzer是一个开源的，基于java语言开发的轻量级的中文分词工具包。从2006年12月推出1.0版开始，IKAnalyzer已经推出 了3个大版本。最初，它是以开源项目 Lucene为应用主体的，结合词典分词和文法分析算法的中文分词组件。新版本的IKAnalyzer3.0则发展为 面向Java的公用分词组件，独立于Lucene项目，同时提供了对Lucene的默认优化实现。\n" +
//                "\n" +
//                "IKAnalyzer3.0特性:\n" +
//                "\n" +
//                "采用了特有的“正向迭代最细粒度切分算法“，具有60万字/秒的高速处理能力。\n" +
//                "\n" +
//                "采用了多子处理器分析模式，支持：英文字母（IP地址、Email、URL）、数字（日期，常用中文数量词，罗马数字，科学计数法），中文词汇（姓名、地名处理）等分词处理。\n" +
//                "\n" +
//                "优化的词典存储，更小的内存占用。支持用户词典扩展定义\n" +
//                "\n" +
//                "针对Lucene全文检索优化的查询分析器IKQueryParser(作者吐血推荐)；采用歧义分析算法优化查询关键字的搜索排列组合，能极大的提高Lucene检索的命中率。"+
//                "互联网金融和物联网是一次技术革命。";
//        List<Map.Entry<String, Integer>> wordFrenList=sortword(text);
//
//        for(int i=0;i<wordFrenList.size();i++){
//          Map.Entry<String,Integer> wordFrenEntry=wordFrenList.get(i);
//          System.out.println(wordFrenEntry.getKey()+"             的次数为"+wordFrenEntry.getValue());
//        }
		long start=System.currentTimeMillis();
		String teststr="你是我的小丫小苹果,怎么爱你都不嫌多";
		for(int i=0;i<100000;i++){
		List<String> list=getSegmentlist(teststr);
		}
//		for(int i=0;i<list.size();i++){
//			System.out.println(list.get(i));
//		}
		long end=System.currentTimeMillis();
		System.out.println("1耗时-----"+(end-start));
		
		start=System.currentTimeMillis();
		for(int i=0;i<100000;i++){
		List<String> list=getSegmentlist1(teststr);
		}
//		for(int i=0;i<list.size();i++){
//			System.out.println(list.get(i));
//		}
		end=System.currentTimeMillis();
		System.out.println("2耗时-----"+(end-start));
    }

}
