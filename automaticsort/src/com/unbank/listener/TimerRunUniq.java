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

public class TimerRunUniq extends TimerTask {
	private ServletContext context = null;

	// 日志
	public static Logger logger = Logger.getLogger(TimerRunUniq.class);
	public TimerRunUniq() {
		
	}
	public TimerRunUniq(ServletContext context) {
		this.context = context;
	}
	@Override
	public void run() { 
		//分类
		try {
			if(TimerRun.operuniqflag==false){
				Similarity.pancheng();
			}
		} catch (Exception e) {
			publicTools.loggerException(logger, e);
		} 
		
	}
	
	public static void main(String[] args) throws Exception {
		
		
	}
}