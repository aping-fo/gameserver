package com.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Debug {

	private static Log logger = LogFactory.getLog(Debug.class);
	
	public static void log(String message, Object ...args){
		if(logger.isDebugEnabled()){
			StackTraceElement e = Thread.currentThread().getStackTrace()[3];
			logger.debug(String.format("%s.%s(%s:%d): %s",e.getClassName(), e.getMethodName(), e.getFileName(),e.getLineNumber(),String.format(message, args)));
		}
	}
	
	public static void info(String message, Object ...args){
		if(logger.isInfoEnabled()){
			StackTraceElement e = Thread.currentThread().getStackTrace()[3];
			logger.info(String.format("%s.%s(%s:%d): %s",e.getClassName(), e.getMethodName(), e.getFileName(),e.getLineNumber(),String.format(message, args)));
		}
	}
	
	
	public static void test(boolean expression, String message, Object ...args){
		if(expression){
			log(message, args);
		}
	}
}
