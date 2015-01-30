
package com.unbank.cxfinterface;

import java.io.UnsupportedEncodingException;

import javax.jws.WebService;

import com.unbank.lucene.production.Comparesimilarity;

/**

 */
@WebService
public class AutoclassinterfaceImpl implements Autoclassinterface {

	/**
	 * 新闻分类提取关键词
	 */
	public String comparesimilarity(String newsStr) {

		return Comparesimilarity.comparesimilarity(newsStr);
	}

	
}
