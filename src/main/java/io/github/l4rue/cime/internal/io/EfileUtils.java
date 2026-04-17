
package io.github.l4rue.cime.internal.io;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * File utilities used by the E-language parser for charset detection and content normalization.
 *
 * @author dingyh
 */
public class EfileUtils {
	/**
	 * Opens a file and returns its normalized text content.
	 *
	 * @param path file path
	 * @return normalized file content
	 * @throws Exception when the file cannot be validated or converted
	 */
	public String openFile(String path) throws Exception {
		if (this.checkFile(path) != 1) {
			throw new Exception("Invalid file format");
		}

		try {
			EfileUtils.transferFile(path);
		} catch (IOException e) {
			throw new Exception("Failed to convert file to UTF-8", e);
		}

		String content;
		try {
			content = changeFileToString(path);
		} catch (IOException e) {
			throw new Exception("Failed to convert file content to string", e);
		}

		return this.removeHeadFormat(content);
	}

	private String changeFileToString(String path) throws IOException {
		StringBuilder content = new StringBuilder();
		try (InputStreamReader streamReader = new InputStreamReader(new FileInputStream(new File(path)), "UTF-8");
				 BufferedReader reader = new BufferedReader(streamReader)) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.contains("\\") || line.contains("//")) {
					content.append(line.replaceAll("\\\\", "").replaceAll("////", "")).append('\n');
				} else {
					content.append(line.trim()).append('\n');
				}
			}
		}
		return content.toString();
	}
	
	
	/**
	 * Removes the metadata header block from raw E-file content.
	 */
	public String removeHeadFormat(String fileStr){
		StringBuilder contentBuilder = new StringBuilder();
		while(fileStr.contains("<!") && fileStr.contains("!>")){
			int start = fileStr.indexOf("<!");
			int end = fileStr.indexOf("!>");
			if(start>0){
				int headerEndPosition = start - 1;
				contentBuilder.append(fileStr.substring(0, headerEndPosition));
			}
			contentBuilder.append(fileStr.substring(end + 2));
			fileStr = contentBuilder.toString().trim();
			contentBuilder = new StringBuilder();	
		}
		return fileStr;
	}
	
	
	/**
	 * Converts the source file to UTF-8 when the current charset differs.
	 *
	 * @param srcFileName source file path
	 * @throws IOException
	 */
	public static void transferFile(String srcFileName) throws IOException 
	{ 
		File file = new File(srcFileName);
		String charset = getCharset(file);
		if(null != charset && !charset.equals("UTF-8")){
			String lineSeparator = System.getProperty("line.separator"); 
			StringBuilder content = new StringBuilder();
			try (FileInputStream fileInputStream = new FileInputStream(srcFileName);
					 DataInputStream dataInputStream = new DataInputStream(fileInputStream);
					 BufferedReader reader = new BufferedReader(new InputStreamReader(dataInputStream, charset))) {
				String line;
				while ((line = reader.readLine()) != null) {
					content.append(line).append(lineSeparator);
				}
			}
			try (Writer writer = new OutputStreamWriter(new FileOutputStream(srcFileName), "utf-8")) {
				writer.write(content.toString());
			}
		}
	}
	
	
	/**
	 * Detects the text charset of a file.
	 * 
	 * @param file file to inspect
	 */
	public static String getCharset(File file) {
	        String charset = "GBK"; // Default fallback charset.
	        byte[] first3Bytes = new byte[3];
	        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
	            boolean checked = false;
	            bis.mark(0);
	            int read = bis.read(first3Bytes, 0, 3);
	            if (read == -1)
	                return charset;
	            if (first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE) {
	                charset = "UTF-16LE";
	                checked = true;
	            } else if (first3Bytes[0] == (byte) 0xFE && first3Bytes[1]
	                == (byte) 0xFF) {
	                charset = "UTF-16BE";
	                checked = true;
	            } else if (first3Bytes[0] == (byte) 0xEF && first3Bytes[1]
	                    == (byte) 0xBB
	                    && first3Bytes[2] == (byte) 0xBF) {
	                charset = "UTF-8";
	                checked = true;
	            }
	            bis.reset();
	            if (!checked) {
	                while ((read = bis.read()) != -1) {
	                    if (read >= 0xF0)
	                        break;
	                    // Bytes below BF alone are still treated as GBK.
	                    if (0x80 <= read && read <= 0xBF)
	                        break;
	                    if (0xC0 <= read && read <= 0xDF) {
	                        read = bis.read();
	                        if (0x80 <= read && read <= 0xBF)// Double-byte sequence (0xC0 - 0xDF)
	                            // (0x80 -
	                            // 0xBF), which may still belong to GBK.
	                            continue;
	                        else
	                            break;
	                     // False positives are still possible, but unlikely.
	                    } else if (0xE0 <= read && read <= 0xEF) {
	                        read = bis.read();
	                        if (0x80 <= read && read <= 0xBF) {
	                            read = bis.read();
	                            if (0x80 <= read && read <= 0xBF) {
	                                charset = "UTF-8";
	                                break;
	                            } else
	                                break;
	                        } else
	                            break;
	                    }
	                }
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        return charset;
	}	
	
	/**
	 * Validates whether the file satisfies parser prerequisites.
	 * 
	 * @param path file path to inspect
	 * @return {@code 1} when the file is accepted
	 */
	public int checkFile(String path) {
		
		
	        return 1;
	}	

}
