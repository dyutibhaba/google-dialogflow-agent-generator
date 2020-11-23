/**
 * 
 */
package com.dlgflw.agent.translation;

/**
 * @author SII068
 *
 */
public interface LanguageTranslator {
	
	/**
	 * @param inputText
	 * @param fromLanguageCode
	 * @param toLanguageCode
	 * @return
	 */
	String translateMessage(String inputText, String fromLanguageCode, String toLanguageCode );

}
