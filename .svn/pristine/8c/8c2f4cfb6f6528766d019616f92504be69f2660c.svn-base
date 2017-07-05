package com.tool.code;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class CodeGenerator {
	
	private static final String INSERT = "insert into xx(%s) value(%s);";
	private static final String UPDATE = "UPDATE XX SET %s where";
	private static final String TABLE = "a.";
	
	
	public static void main(String[] args) throws Exception {
		List<Code> sqls = new ArrayList<Code>();
		BufferedReader br = new BufferedReader(new FileReader("data/code.txt"));
		String line = br.readLine();
		while(line!=null){
			line = line.trim();
			if(line.isEmpty()){
				continue;
			}
			sqls.add(new Code(line));
			line = br.readLine();
		}
		br.close();
		//生成Class
		StringBuilder classContent = new StringBuilder();
		for(Code sql:sqls){
			classContent.append("private").append(" ")
			.append(dbType2JavaType(sql.type)).append(" ")
			.append(sql.name)
			.append(";");
			if(sql.comment!=null&&!sql.comment.isEmpty()){
				classContent.append("//").append(sql.comment);
			}
			classContent.append("\r\n");
		}
		System.out.println(classContent);
		//生成Insert语句
		
		StringBuilder insertItem = new StringBuilder();
		StringBuilder insertValue = new StringBuilder();
		for(Code sql:sqls){
			insertItem.append("`").append(sql.name).append("`,");
			insertValue.append(TABLE).append(sql.name).append(",");
		}
		insertItem.delete(insertItem.length()-1, insertItem.length());
		insertValue.delete(insertValue.length()-1, insertValue.length());
		System.out.println(String.format(INSERT, insertItem,insertValue));
		
		//生成Update语句
		StringBuilder updateSql = new StringBuilder();
		for(Code sql:sqls){
			updateSql.append("`").append(sql.name).append("`=:").append(TABLE).append(sql.name).append(",");
		}
		System.out.println(String.format(UPDATE, updateSql));
	}
	
	@SuppressWarnings("unused")
	private static String javaType2DbType(String javaType){
		if(javaType.equals("int")){
			return "int(10) unsigned NOT NULL";
		}else if(javaType.equals("float")){
			return "float NOT NULL";
		}else if(javaType.equals("long")){
			return "bigint(20) unsigned NOT NULL";
		}else if(javaType.equals("double")){
			return "double NOT NULL";
		}else if(javaType.equals("Date")){
			return "datetime NOT NULL";
		}else if(javaType.equals("String")){
			return "varchar(x) NOT NULL";
		}else if(javaType.equals("boolean")){
			return "tinyint(1) unsigned NOT NULL";
		}else{
			throw new RuntimeException("Unsupported type!"+javaType);
		}
	}
	
	private static String dbType2JavaType(String dbType){
		if(dbType.startsWith("int")){
			return "int";
		}else if(dbType.startsWith("float")){
			return "float";
		}else if(dbType.startsWith("double")){
			return "double";
		}else if(dbType.startsWith("bigint")){
			return "long";
		}else if(dbType.startsWith("datetime")){
			return "Date";
		}else if(dbType.startsWith("varchar")){
			return "String";
		}else if(dbType.startsWith("tinyint")){
			if(dbType.startsWith("tinyint(1)")){
				return "boolean";
			}else{
				return "int";
			}
		}else{
			throw new RuntimeException("Unsported dbType!"+dbType);
		}
	}
	
	

}
