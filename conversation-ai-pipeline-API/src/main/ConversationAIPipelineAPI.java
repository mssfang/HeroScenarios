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

public class ConversationAIPipelineAPI {

	public static String detectLanguage(String text, String TEXT_ANALYSTICS, String host, String path) {
		
		if (text == null || text.isEmpty()) return "";
		
		try {
            Documents documents = new Documents ();
            documents.add ("1", text);
            DetectLanguage detectLanguageInstance = new DetectLanguage(TEXT_ANALYSTICS, host, path);
            String response = detectLanguageInstance.GetLanguage(documents);
            System.out.println(detectLanguageInstance.prettify(response));
            ArrayList<String> detactLang = detectLanguageInstance.parseLanguage(response);
            
            for(String lang : detactLang) 
            	if (lang != null && !lang.isEmpty()) {
            		return lang;
            	}
        } catch (Exception e) {
            System.out.println(e);
        }
		
		return "";
	}
	
	public static String translateText(String text, String translateTo, String subscriptionKey) {
		String translatedText = text;
		try {
			Translate translateRequest = new Translate(subscriptionKey);
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
        String SPEECH = System.getenv("Speech");
        System.out.println("\n---------------Step 1: Speech to text------(SDK)---------");
		String recognizedText  = RecognizeSpeech.sttService(SPEECH, "westus");
		System.out.println("recognized text = " + recognizedText);
		
		// Step 2: Text Analytics (Language Detection)
   		String TEXT_ANALYSTICS = System.getenv("TextAnalytics");
   		System.out.println("\n---------------Step 2: Text Analytics---------------");
   		String host = "https://westcentralus.api.cognitive.microsoft.com";
        String path = "/text/analytics/v2.0/languages";
		String detectLangResp = detectLanguage(recognizedText, TEXT_ANALYSTICS, host, path);
		System.out.println("detected language = " + detectLangResp);
		
		// Step 3: Text Translator (Translate Text)
   		System.out.println("\n---------------Step 3: Text Translator--------------");
		String TRANSLATOR = System.getenv("Translator");
		String enText = translateText(recognizedText, "en", TRANSLATOR);
		System.out.println("translate to english, text = " + enText);
		 
		// Step 4: Bing Spell Check (Spell Check) 
   		String SPELL_CHECK =  System.getenv("SpellCheck");
   		System.out.println("\n---------------Step 4: Spelling check---------------");
		try {
			String BING_SPELL_PATH = "/bing/v7.0/spellcheck";
			String BING_SPELL_HOST = "https://api.cognitive.microsoft.com";
			String BING_SPELL_SUBSCRIPTION_KEY = SPELL_CHECK;
			BingSpellCheck bingSpellCheck = new BingSpellCheck(BING_SPELL_SUBSCRIPTION_KEY, BING_SPELL_HOST, BING_SPELL_PATH);
			BingSpellCheck.check("en-US", "proof", enText);
		} catch (Exception e) {
		    System.out.println(e);
		}
		
		// Step 5: Content Moderator (Explicit Content Rec) 
   		System.out.println("\n---------------Step 5: content moderator---------------");

		 
	 /**
	 *  	2, Retrieve Response
	 *  
	 *  	using QnA(https://www.qnamaker.ai/) and LUIS(https://www.luis.ai/home?force=1) to create Knowledge base, Intent and Entities;
	 **/
		// Step 6: LUIS (Recognize Intent)
		String LUIS = System.getenv("LUIS");
		System.out.println("\n---------------Step 6: LUIS---------------");
		String luisResp = Luis.recognizeIntent("q", enText);
		System.out.println("luis raw respon = " + luisResp);
		String intent = Luis.getTopIntent(luisResp);
    	System.out.println("intent = " + intent);
    	
		// Step 7: QnA Maker (Retrieve Response)
    	System.out.println("\n---------------Step 7: QnA Maker-----------------------");	
    	String ans = GetAnswer.getAns(intent);    	
    	String topAns = GetAnswer.getTopAns(ans);
    	System.out.println("answer = " + topAns);
		 
	/**
	 *  	3, Generate Output
	 **/
		// Step 8: Text Translater (Translate Text)
    	System.out.println("\n---------------Step 8: Text Translator---------------");
    	String translatedTopAns = translateText(topAns, detectLangResp, TRANSLATOR);
    	System.out.println("translated top ans is, " + translatedTopAns);
		// Step 9: Speech Service (Text To Speech)
	    System.out.println("\n---------------Step 9: Text to Speech---------------");
    	TTSSample.textToSpeech(translatedTopAns, SPEECH);
    	
    	
    	
		
		System.out.println("---------------- End of Conversation AI Pipeline --------------------");
	}

}
