/**
 * 
 */
package com.dlgflw.agent.generator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
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
public class IntentGenerator {
	
	private static Logger logger = LoggerFactory.getLogger(IntentGenerator.class);
	
	private String intentDirectory;
	
	public IntentGenerator(String intentDir) {
		this.intentDirectory = intentDir;
	}
	
	

	public static void generate(String intentDirectory, LanguageTranslator translator) {
		List<String> enFileNames = new ArrayList<>();
		List<String> deFileNames = new ArrayList<>();
		List<String> noLangFileNames = new ArrayList<>();
		//read the entity folder
		try (Stream<Path> paths = Files.walk(Paths.get(intentDirectory))) {
			paths
	        .filter(Files::isRegularFile)
	        .forEach(e -> {
	        	//collect all
	        	File file = e.toFile();
	        	if(file.getName().contains("_en.json")) {
	        		enFileNames.add(file.getName());
	        	} else if(file.getName().contains("_de.json")) {
	        		deFileNames.add(file.getName());
	        	} else {
	        		noLangFileNames.add(file.getName());
	        	}
	        });
		} catch (IOException e) {
			logger.error("IOException in translateEntities {}", e.getMessage());
		}
		logger.info("enFiles size: {}", enFileNames.size());
		logger.info("deFiles size: {}", deFileNames.size());
		logger.info("noLangFiles size: {}", noLangFileNames.size());
		List<String> engFiles = getFilesWithOnlyDefaultLangName(enFileNames, deFileNames);
		logger.info("new en files list size: {}", engFiles.size());
		logger.info("Training phrases translation started...");
		generaeLocalizedUsersaysFiles(engFiles, intentDirectory, translator);
		logger.info("Training phrases translation completed...");
		List<String> requiredFilesToBeTranslated = removeDefaultFiles(noLangFileNames);
		logger.info("Intent responses translation started...");
		addLocalizedPhrasesForNoLangFiles(requiredFilesToBeTranslated, intentDirectory, translator);
		logger.info("Intent responses translation completed...");
	}

		
	private static List<String> removeDefaultFiles(List<String> noLangFileNames) {
		return noLangFileNames.stream()
				.filter(fn -> !fn.contains("Default"))
				.collect(Collectors.toList());
	}



	private static void addLocalizedPhrasesForNoLangFiles(List<String> noLangFileNames, String intentDir,
			LanguageTranslator translatorMock) {
		ObjectMapper mapper = new ObjectMapper();
		noLangFileNames.stream().forEach(fn -> {
			//read the file contents as JsonNode
			File intentFile = AgentUtility.getFileByNameFromDir(fn, intentDir);
			logger.debug("Current File >>{}", intentFile);
			File emptyLocalizedFile = AgentUtility.getEmptyLocalizedFile(intentFile);
			try {
				//Exception will occur if dialogflow usersays file structure changed from array 
				JsonNode intentNode = mapper.readValue(intentFile, JsonNode.class);
				//get first json object form responses array
				ObjectNode jsonInResponseArray = (ObjectNode) intentNode.get("responses").get(0);
				//from messages array
				ArrayNode msgsArrayNode = (ArrayNode)jsonInResponseArray.get("messages");
				logger.info("msgsArrayNode.size() - {}", msgsArrayNode.size());
				if(msgsArrayNode.size() > 1) {

					Iterator<JsonNode> iter1 = msgsArrayNode.elements();
					String localizedText = "";
					//iterate messages array to translate the default language speech text
					//Note: msgsArrayNode contains objects with languages, the below iteration is only for en
					 while (iter1.hasNext()) {
				            JsonNode entry = iter1.next();
				            String currentLang = entry.get("lang").asText();
							if(currentLang.equals("en")) {
								//if language is English get the speech text and translate it
								String tPhraseText = entry.get("speech").asText();
								if(!tPhraseText.equals("")) {
									//logger.info("speech{}", entry.get("speech"));
									localizedText = translatorMock.translateMessage(tPhraseText, 
											DialogflowConstants.FROM_LANG, DialogflowConstants.TO_LANG);
								}
								//localizedText = "bla bla bla bla bla";
								break;
				            }
				            
				        }
					 if(!localizedText.equals("")) {
						 
						 Iterator<JsonNode> iter2 = msgsArrayNode.elements();
						 //iterate again to set the translated text
						 while (iter2.hasNext()) {
							 ObjectNode entry = (ObjectNode) iter2.next();
							 String currentLang = entry.get("lang").asText();
							 if(currentLang.equals("de")) {
								 //set the translated speech text
								 entry.put("speech", localizedText);
								 break;
							 }
							 
						 }
					 }
				
				} else {
					 List<JsonNode> newMsgList = new ArrayList<>();
					 JsonNode jsonNode = msgsArrayNode.get(0);
					 newMsgList.add(jsonNode);
					 Iterator<String> fieldNames = jsonNode.fieldNames();
					 ObjectNode newJsonNode = mapper.createObjectNode();
					 while(fieldNames.hasNext()) {
						String fieldKey = fieldNames.next();
						String fieldVal = jsonNode.get(fieldKey).asText();
						if(fieldKey.equals("speech")) {
							if(!fieldVal.equals("")) {
								String localizedText = translatorMock.translateMessage(fieldVal, 
										DialogflowConstants.FROM_LANG, DialogflowConstants.TO_LANG);
								//String localizedText = "bla bla bla bla bla";
								newJsonNode.put(fieldKey, localizedText);
							}
						} else if(fieldKey.equals("lang")) {
							newJsonNode.put(fieldKey, DialogflowConstants.TO_LANG);
						}
						else 
						 newJsonNode.put(fieldKey, fieldVal);
					 }
					 msgsArrayNode.add(newJsonNode);
				}
				
				 //write to the new file
				 AgentUtility.writeToSameFile(emptyLocalizedFile, intentNode);
			} catch (IOException e) {
				logger.info("IOException > in generaeLocalizedUsersaysFiles {}", e.getMessage());
			} catch (ClassCastException cce) {
				logger.info("ClassCastException > JsonNode to ArrayNode convertion {}", cce.getMessage());
			}
		});
		
	}



	private static void generaeLocalizedUsersaysFiles(List<String> engFiles, String intentDir, LanguageTranslator translatorMock) {
		//read each file, keep it memory, add/update the message array & write to localized file
		ObjectMapper mapper = new ObjectMapper();
		engFiles.stream().forEach(fn -> {
			//read the file contents as JsonNode
			File intentFile = AgentUtility.getFileByNameFromDir(fn, intentDir);
			File emptyLocalizedFile = AgentUtility.getEmptyLocalizedFile(intentFile);
			try {
				//Exception will occur if dialogflow usersays file structure changed from array 
				ArrayNode intentNode = (ArrayNode) mapper.readValue(intentFile, JsonNode.class);
				Iterator<JsonNode> iter = intentNode.elements();
				//iterate each inner synonyms array
				 while (iter.hasNext()) {
			            ObjectNode entry = (ObjectNode) iter.next();
			            ArrayNode dataArray = (ArrayNode) entry.get("data");
			            //create new array node and replace this with the synonyms node
			            for(int i=0; i < dataArray.size(); i++) {
			            	ObjectNode jsonNode = (ObjectNode) dataArray.get(i);
							String tPhraseText = jsonNode.get("text").asText();
							if(!tPhraseText.equals("")) {
								String localizedText = translatorMock.translateMessage(tPhraseText, 
				            			DialogflowConstants.FROM_LANG, DialogflowConstants.TO_LANG);
								//String localizedText = "peep peep peep";
								jsonNode.put("text", localizedText);
							}
			            	
			            }
			            
			        }
				 //write to the new file
				 AgentUtility.writeToNewLocalizedFile(emptyLocalizedFile, intentNode);
			} catch (IOException e) {
				logger.info("IOException > in generaeLocalizedUsersaysFiles {}", e.getMessage());
			} catch (ClassCastException cce) {
				logger.info("ClassCastException > JsonNode to ArrayNode convertion {}", cce.getMessage());
			}
		});
		
	}



	/**
	 * @param enFiles
	 * @param deFiles
	 * @return {@code Set<File>} set of files for which there are not supported lang file available 
	 */
	private static List<String> getFilesWithOnlyDefaultLangName(List<String> enFiles, List<String> deFiles) {
		//filter the en files for which there is a de file
		return enFiles.stream()
				.filter(e -> (!deFiles.contains(e.replace("_en.json", "_de.json"))))
				.collect(Collectors.toList());
		
	}
	
	



	public static void main(String[] args) {
		String intentDirectory = "C:\\Bhabadyuti\\com.dlgflw\\src\\main\\resources\\agents\\HeatingSystemChatbot\\intents";
		generate(intentDirectory, new AwsTranslator());
	}

}
