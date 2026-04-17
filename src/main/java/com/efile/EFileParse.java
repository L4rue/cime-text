package com.efile;

import java.io.File;
import java.util.List;

public interface EFileParse {

	
	/**
	 * 解析E语言文件总入口
	 * 
	 * @param file
	 * @return
	 * @throws Exception List<ETable>
	 * @author 王正权
	 * @Date Dec 11, 2012
	 */
	public List<ETable> parseFile(File file)  throws  Exception;

	/**
	 * 解析E语言文件入口（带配置）
	 *
	 * @param file 文件
	 * @param options 解析配置
	 * @return List<ETable>
	 * @throws Exception 解析异常
	 */
	public List<ETable> parseFile(File file, ParseOptions options) throws Exception;
		
	
}
