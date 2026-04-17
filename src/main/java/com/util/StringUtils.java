package com.util;

import java.util.ArrayList;
import java.util.List;
/**
 * @author 王正权
 * 973598066@qq.com
 */
public class StringUtils {


	
	/**
	 * 按空白字符（空格、Tab 等）分割字符串；当字段被单引号或双引号包裹时，保留其内部空格。
	 * 例如："2 发生时间 '2011-11-03 00:00:02.0'"。
	 * @param line
	 * @return
	 */
	public static String[] splitLineWithSpace(String line){
		if(line==null){
			return new String[0];
		}
		List<String> list = new ArrayList<String>();
		int length = line.length();
		int pos = 0;
		while (pos < length) {
			while (pos < length && isSpace(line.charAt(pos))) {
				pos++;
			}
			if (pos >= length) {
				break;
			}
			char current = line.charAt(pos);
			if (isWrapper(current)) {
				char wrapper = current;
				pos++;
				int sectionStart = pos;
				while (pos < length && line.charAt(pos) != wrapper) {
					pos++;
				}
				if (pos >= length) {
					throw new IllegalArgumentException("存在未闭合的引号");
				}
				list.add(line.substring(sectionStart, pos));
				pos++;
				continue;
			}
			int sectionStart = pos;
			while (pos < length && !isSpace(line.charAt(pos))) {
				pos++;
			}
			list.add(line.substring(sectionStart, pos));
		}

		String[] arr=new String[list.size()];
		return list.toArray(arr);
	}
	
	private final static char[] wrappers=new char[]{'\'','\"'}; 
	/**
	 * 判断字符是否是单双引号
	 * @param chr
	 * @return
	 */
	private static boolean isWrapper(char chr){
		for(char c:wrappers){
			if(c==chr){
				return true;
			}
		}
		return false;
	}
	/**
	 * 是否是空白字符
	 * @param chr
	 * @return
	 */
	public static boolean isSpace(char chr){
		return (chr==' '|| chr=='\n' || chr=='\t' || chr=='\r' || chr=='\f');
	}
	
	public static boolean isInteger(String str){
		String regx="\\d+";
		return str.matches(regx);
	}
}
