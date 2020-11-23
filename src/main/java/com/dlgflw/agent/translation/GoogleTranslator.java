/**
 * 
 */
package com.dlgflw.agent.translation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author SII068
 *
 */
public class GoogleTranslator implements LanguageTranslator {
	
	private static Logger logger = LoggerFactory.getLogger(GoogleTranslator.class);

	
	public String translateMessage(String inputText, String fromLanguageCode, String toLanguageCode ) {
		logger.info("inputText >> {}", inputText);
		logger.info("fromLanguageCode: {}, toLanguageCode: {}", fromLanguageCode, toLanguageCode);
		StringBuilder response;
		StringBuilder outputText = new StringBuilder();
		try {
			String urlStr = "https://translate.googleapis.com/translate_a/single" + "?client=gtx&sl=" + fromLanguageCode
					+ "&tl=" + toLanguageCode + "&dt=t&q=" + URLEncoder.encode(inputText, "UTF-8");
			logger.info("Google Translate API: {}", urlStr);
			URL url = new URL(urlStr);
			response = new StringBuilder();
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			// con.setRequestProperty("User-Agent", "Mozilla/5.0");
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			JSONArray jSONArray = new JSONArray(response.toString());
			JSONArray jSONArray1 = new JSONArray(jSONArray.get(0).toString());
			for(int i=0; i< jSONArray1.length(); i++) {
				JSONArray jSONArray2 = new JSONArray(jSONArray1.get(i).toString());
				outputText.append(jSONArray2.get(0).toString());
			}
			
			logger.info("outputText>> {}", outputText);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return outputText.toString();
	}
	
	public static void main(String[] args) {
		GoogleTranslator translator = new GoogleTranslator();
		String output = translator.translateMessage("Ok great. How can i help you ?", "en", "de");
		System.out.println(output);
	}

}
