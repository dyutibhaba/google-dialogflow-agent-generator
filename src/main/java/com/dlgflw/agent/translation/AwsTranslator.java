package com.dlgflw.agent.translation;

import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.translate.AmazonTranslate;
import com.amazonaws.services.translate.AmazonTranslateClient;
import com.amazonaws.services.translate.model.TranslateTextRequest;
import com.amazonaws.services.translate.model.TranslateTextResult;

public class AwsTranslator implements LanguageTranslator {
	
	AmazonTranslate awsTranslate;

	@Override
	public String translateMessage(String inputText, String fromLanguageCode, String toLanguageCode) {
		
		TranslateTextRequest request = new TranslateTextRequest()
                .withText(inputText)
                .withSourceLanguageCode(fromLanguageCode)
                .withTargetLanguageCode(toLanguageCode);
        TranslateTextResult result = getAwsTranslateInstance().translateText(request);
        System.out.println("Translated text: " + result.getTranslatedText());
        return " "+result.getTranslatedText()+" ";
	}
	
	
	private AmazonTranslate getAwsTranslateInstance()  {
		if(awsTranslate == null) {
			AWSCredentialsProviderChain defaultAWSCredentialsProviderChain = new AWSCredentialsProviderChain(
					new SystemPropertiesCredentialsProvider(),
					new EnvironmentVariableCredentialsProvider(),
					new ProfileCredentialsProvider()
					);
			
			// Create an Amazon Translate client
			awsTranslate = AmazonTranslateClient.builder()
					.withCredentials(defaultAWSCredentialsProviderChain)
					.withRegion("ap-south-1")
					.build();
		}
		return awsTranslate;
	}
	
	public static void main(String[] args) {
		new AwsTranslator().translateMessage(" We will use gas", "en", "de");
	}

}
