/**
 * 
 */
package com.dlgflw.agent.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.dlgflw.agent.constants.DialogflowConstants;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * @author SII068
 *
 */
public class AgentUtility {
	
	//Avoid public instantiation of this utility class
	private AgentUtility() {
	    throw new IllegalStateException("Utility class");
	  }
	
	
	/**
	 * Get string contents for agent.json form agent zip file
	 * @param zipFilePath
	 * @return
	 * @throws IOException
	 */
	public static String getAgentFileContents(String zipFilePath) throws IOException {
		try(ZipFile zipFile = new ZipFile(zipFilePath)) {
			 ZipEntry zipEntry = zipFile.getEntry(DialogflowConstants.AGENT_FILE);
			 InputStream inputStream = zipFile.getInputStream(zipEntry);
			 return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
		} 
	}
	
	/**
	 * Takes the current file and returns a new localized file  
	 * @param currentFileName
	 * @return File
	 */
	public static File getEmptyLocalizedFile(File currentFile) {
		String localeFileName = currentFile.getAbsolutePath().replace("_en.json", "_de.json");
		return new File(localeFileName);
	}
	
	public static void writeToNewLocalizedFile(File file, JsonNode jsonNode) throws JsonGenerationException, JsonMappingException, IOException {
		if(file.createNewFile()) {
			ObjectMapper mapper = new ObjectMapper();
			ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
			writer.writeValue(file, jsonNode);
		}
	}
	
	public static void writeToSameFile(File file, JsonNode jsonNode)
			throws JsonGenerationException, JsonMappingException, IOException {
		FileUtils.write(file, "", Charset.defaultCharset());
		ObjectMapper mapper = new ObjectMapper();
		ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
		writer.writeValue(file, jsonNode);
	}


	public static String getEntityDir(String unzipDir) {
		return unzipDir + File.separator + "entities";
	}
	
	public static String getIntentDir(String unzipDir) {
		return unzipDir + File.separator + "intents";
	}

	public static File getFileByNameFromDir(String fileName, String intentDir) {
		File intentDirectory = new File(intentDir);
		File[] matches = intentDirectory
				.listFiles((dir, name) -> 
						name.startsWith(fileName) && name.endsWith(".json"));
		return matches[0];
	}

}
