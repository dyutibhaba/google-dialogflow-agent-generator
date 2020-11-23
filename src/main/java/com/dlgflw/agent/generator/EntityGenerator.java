package com.dlgflw.agent.generator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dlgflw.agent.constants.DialogflowConstants;
import com.dlgflw.agent.translation.AwsTranslator;
import com.dlgflw.agent.translation.LanguageTranslator;
import com.dlgflw.agent.util.AgentUtility;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author SII068
 *
 */
public class EntityGenerator {
	
	private static Logger logger = LoggerFactory.getLogger(EntityGenerator.class);
	
	/**
	 * @param entityFolderPath
	 * @param translatorMock
	 */
	public static void generate(String entityFolderPath, LanguageTranslator translatorMock) {
		//read the entity folder
		try (Stream<Path> paths = Files.walk(Paths.get(entityFolderPath))) {
			paths
	        .filter(Files::isRegularFile)
	        .forEach(e -> {
	        	//iterate each file in entity folder
	        	File file = e.toFile();
	        	if(file.getName().contains("_en")) {
	        		try {
						ObjectMapper mapper = new ObjectMapper();
						//create JsonNode from the file
						JsonNode readValue = mapper.readValue(file, JsonNode.class);
						//create an ArrayNode to hold the entity file contents i.e., an array of objects
						ArrayNode entityNode = (ArrayNode) readValue;
						
						File localizedFile = AgentUtility.getEmptyLocalizedFile(file);
						Iterator<JsonNode> iter = entityNode.elements();
						//iterate each inner synonyms array
						 while (iter.hasNext()) {
					            ObjectNode entry = (ObjectNode) iter.next();
					            ArrayNode synonyms = (ArrayNode) entry.get("synonyms");
					            //create new array node and replace this with the synonyms node
					            ArrayNode newArrayNode = mapper.createArrayNode();
					            for(int i=0; i < synonyms.size(); i++) {
					            	String synText = synonyms.get(i).textValue();
					            	String localizedText = translatorMock.translateMessage(synText, 
					            			DialogflowConstants.FROM_LANG, DialogflowConstants.TO_LANG);
					            	//String localizedText = "done done done";
					            	newArrayNode.add(localizedText);
					            }
					            entry.putArray("synonyms").addAll(newArrayNode);
					            
					        }
						 //write to the new file
						 AgentUtility.writeToNewLocalizedFile(localizedFile, entityNode);
						
					} catch (IOException e1) {
						logger.error("IOException in translateEntities {}", e1.getMessage());
					}
	        	}
	        });
		} catch (IOException e) {
			logger.error("IOException in translateEntities {}", e.getMessage());
		}
		
	}
	
	
	public static void main(String[] args) {
		generate(DialogflowConstants.ENTITY_DIR, new AwsTranslator());
	}
	 

}
