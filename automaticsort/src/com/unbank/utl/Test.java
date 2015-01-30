package com.unbank.utl;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.wltea.analyzer.cfg.Configuration;
import org.wltea.analyzer.cfg.DefaultConfig;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;
import org.wltea.analyzer.dic.Dictionary;
import org.wltea.analyzer.lucene.IKAnalyzer;

import com.unbank.lucene.production.CreateIndex;

public class Test {
	public static void main(String[] args) throws Exception {
		// //加载扩展词库
		// Configuration cfg = DefaultConfig.getInstance();
		// cfg.setUseSmart(true);
		// Dictionary.initial(cfg);
		//		
		// Dictionary dictionary = Dictionary.getSingleton();
		// List cidianList=new ArrayList();
		// cidianList.add("基础设施建设");
		// dictionary.addWords(cidianList);
		//		
		//		
		// // 创建分词对象 isMaxWordLength
		// Analyzer anal = new IKAnalyzer(true);
		// StringReader reader = new StringReader("你是我的小丫小苹果财政支出基础设施建设");
		// // 分词
		// TokenStream ts = anal.tokenStream("", reader);
		//		 
		// CharTermAttribute term = ts.getAttribute(CharTermAttribute.class);
		// // 遍历分词数据
		// String rc0 = "";
		// int count =0;
		// while (ts.incrementToken()) {
		// rc0 = rc0 + term.toString() + "|";
		// count++;
		//			
		// }
		// System.out.println(rc0);
		// System.out.println("dfs".toUpperCase());
		// JSONObject jsonObj = new JSONObject();
		// jsonObj.put("content", "");
		// jsonObj.put("id", "");
		// jsonObj.put("title", "");
		// System.out.println(jsonObj.toString());

		String[] arrStr = { "yours", "am", "I","赵建" };
		  sortStringArray(arrStr);
		  for (int i = 0; i < arrStr.length; i++) {
		   System.out.println(arrStr[i]);
		  }
	}

	public static void sortStringArray(String[] arrStr) {
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


	    }
}
