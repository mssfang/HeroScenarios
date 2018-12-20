package main;

import java.util.ArrayList;

import bingSpellCheck.BingSpellCheck;
import luis.Luis;
import qNa.GetAnswer;
import speechSDK.RecognizeSpeech;
import speechSDK.tts.TTSSample;
import textAnalytics.DetectLanguage;
import textAnalytics.Documents;
import textTranslator.Translate;

public class ConversationAIPipelineSample {

	

	public static String detectLanguage(String text) {
		if (text == null || text.isEmpty()) return "";
		try {
            Documents documents = new Documents ();
            documents.add ("1", text);
            String response = DetectLanguage.GetLanguage(documents);
            System.out.println(DetectLanguage.prettify(response));
            ArrayList<String> detactLang = DetectLanguage.parseLanguage(response);
            
            for(String lang : detactLang) 
            	if (lang != null && !lang.isEmpty()) {
            		return lang;
            	}
        } catch (Exception e) {
            System.out.println(e);
        }
		
		return "";
	}
	
	public static String translateText(String text, String translateTo) {
		String translatedText = text;
		try {
			Translate translateRequest = new Translate();
			translatedText = Translate.getTranslatedText( translateRequest.Post(text, translateTo));
		} catch (Exception e) {
            System.out.println(e);
	    }
		return translatedText;
	}
	
	
	
	
	public static void main(String[] args) {
	
		
		
		
		System.out.println("This is a simple example of Conversation AI pipeline for HR contact informations,\n"
				+ "Please try to ask one of below questions: \n"
				+ "1, \"Call\" (ans: 777-777-7777) \n"
				+ "2, \"Phone\" (ans: 111-111-1111) \n"
				+ "3, \"Address\" (ans: Microsoft Way 1, Redmond, WA) \n"
				+ "4, \"Email\" (ans: lol@microsoft.com) \n");
		
				
		
	 /**
	  *  	1, Process/Filter Speech Stream
	  * */
		// Step 1: Speech Service (Speech To Text)
		String recognizedText  = RecognizeSpeech.sttService("61a42d71ad7b47a9b4b47d5b20ecc61f", "westus");
		System.out.println("\nStep 1, recognized text = " + recognizedText);
		
		// Step 2: Text Analytics (Language Detection)
		String detectLangResp = detectLanguage(recognizedText);
		System.out.println("\nStep 2, detect language = " + detectLangResp);
		
		// Step 3: Text Translator (Translate Text), [Question: Do we required to support other languages?]
		String enText = translateText(recognizedText, "en");
		System.out.println("\nStep 3, translate to english, text = " + enText);
		 
		// Step 4: Bing Spell Check (Spell Check) 
		//check(mkt, mode, text)
		System.out.println("\nStep 4: spelling check");
		try {
			BingSpellCheck.check("en-US", "proof", enText);
		} catch (Exception e) {
		    System.out.println(e);
		}
		
		// Step 5: Content Moderator (Explicit Content Rec) 
		System.out.println("\nStep 5, content moderator doesn't have appropriate sample to use for latest JAVA SDK");
		// don't know how to use it, there is no doc for latest JAVA SDK 1.0.2

		 
	 /**
	 *  	2, Retrieve Response
	 *  
	 *  	using QnA(https://www.qnamaker.ai/) and LUIS(https://www.luis.ai/home?force=1) to create Knowledge base, Intent and Entities;
	 **/
		// Step 6: LUIS (Recognize Intent)
		
		System.out.println("\nStep 6, LUIS");
		String luisResp = Luis.recognizeIntent("q", enText);
		System.out.println("luis raw respon = " + luisResp);
		String intent = Luis.getTopIntent(luisResp);
    	System.out.println("intent = " + intent);
    	
		// Step 7: QnA Maker (Retrieve Response)
		System.out.println("\nStep 7, QnA Maker");
    	String ans = GetAnswer.getAns(intent);    	
    	String topAns = GetAnswer.getTopAns(ans);
    	System.out.println("answer = " + topAns);
		 
		
	/**
	 *  	3, Generate Output
	 **/
		// Step 8: Text Translater (Translate Text)
    	System.out.println("\nStep 8, Text Translator");
    	String translatedTopAns = translateText(topAns, detectLangResp);
    	System.out.println("translated top ans is, " + translatedTopAns);
		// Step 9: Speech Service (Text To Speech)
    	System.out.println("\nStep 9, Text to Speech");
    	TTSSample.textToSpeech(translatedTopAns);
    	
    	
    	
		
		System.out.println("---------------- End of Conversation AI Pipeline --------------------");
	}

}
