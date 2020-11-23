/**
 * 
 */
package com.dlgflw.agent.validation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dlgflw.agent.generator.EntityGenerator;
import com.dlgflw.agent.generator.IntentGenerator;
import com.dlgflw.agent.translation.AwsTranslator;
import com.dlgflw.agent.util.ZipUnzipUtility;
import com.dlgflw.agent.util.AgentUtility;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * @author SII068
 *
 */
public class AgentValidator {
	
	private static Logger logger = LoggerFactory.getLogger(AgentValidator.class);
	
	/**
	 * This method can contain the rules to validate an agnet zip file
	 * i.e., if the required files/folders are available
	 * @return
	 */
	public static boolean validateAgent() {
		return true;
	}
	
	/**
	 * Check if the agent file has supported languages available
	 */
	public static boolean processAgent() {
		
		
		try (Stream<Path> paths = Files.walk(Paths.get("src/main/resources/agents/"))) {
			ZipUnzipUtility zipUtility = new ZipUnzipUtility();
			ObjectMapper mapper = new ObjectMapper();
		    paths
		        .filter(Files::isRegularFile)
		        .peek(e -> logger.info("Reading agent: {}", e.toFile().getName()))
		        .forEach(e -> {
		        	File file = e.toFile();
		        	try {
		        		String contents = AgentUtility.getAgentFileContents(file.getAbsolutePath());
		        		ArrayNode supportedLangs = (ArrayNode) mapper.readTree(contents).get("supportedLanguages");
		        		
		        		if(supportedLangs.size() < 1) {
		        			logger.error("No supported language found for agent file {}, please add supported language in the agent.", file.getName());
		        		} else {
		        			String zipFileDir = file.getAbsoluteFile().getParent();
							String zipFileName = file.getName();
							String fileWithoutExtension = FilenameUtils.removeExtension(zipFileName);
							String unzipDir = zipFileDir + "\\" + fileWithoutExtension;
							logger.info("zipFileDir = {}", zipFileDir);
							logger.info("zipFileName = {}", zipFileName);
							logger.info("unzipDir = {}", unzipDir);
							if(new File(unzipDir).mkdir()) {
								//unzip the agent file in the unzipDir
								zipUtility.unzipAgentFile(zipFileDir , zipFileName , unzipDir);
								//start translating the entity entries
								EntityGenerator.generate(AgentUtility.getEntityDir(unzipDir), new AwsTranslator());
								IntentGenerator.generate(AgentUtility.getIntentDir(unzipDir), new AwsTranslator());
								logger.info("Preparing to zip the agent...");
								Path outPath = Paths.get("src/main/resources/output/");
								String zipFolder = outPath.toFile().getAbsolutePath();
								String targetZipFile = zipFolder+ File.separator +zipFileName;
								zipUtility.zipAgentFolder(unzipDir, targetZipFile);
							}
		        		}
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
		        });
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		return false;
	}

}
