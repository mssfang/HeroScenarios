package main;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

import com.microsoft.azure.cognitiveservices.language.luis.authoring.EndpointAPI;
import com.microsoft.azure.cognitiveservices.language.luis.authoring.LUISAuthoringClient;
import com.microsoft.azure.cognitiveservices.language.luis.authoring.LUISAuthoringManager;
import com.microsoft.azure.cognitiveservices.language.luis.authoring.models.ApplicationCreateObject;
import com.microsoft.azure.cognitiveservices.language.luis.authoring.models.ApplicationPublishObject;
import com.microsoft.azure.cognitiveservices.language.luis.authoring.models.BatchLabelExample;
import com.microsoft.azure.cognitiveservices.language.luis.authoring.models.CompositeEntityModel;
import com.microsoft.azure.cognitiveservices.language.luis.authoring.models.EnqueueTrainingResponse;
import com.microsoft.azure.cognitiveservices.language.luis.authoring.models.EntityLabelObject;
import com.microsoft.azure.cognitiveservices.language.luis.authoring.models.ExampleLabelObject;
import com.microsoft.azure.cognitiveservices.language.luis.authoring.models.HierarchicalEntityModel;
import com.microsoft.azure.cognitiveservices.language.luis.authoring.models.ModelTrainingInfo;
import com.microsoft.azure.cognitiveservices.language.luis.authoring.models.ProductionOrStagingEndpointInfo;
import com.microsoft.azure.cognitiveservices.language.luis.runtime.LuisRuntimeAPI;
import com.microsoft.azure.cognitiveservices.language.luis.runtime.LuisRuntimeManager;
import com.microsoft.azure.cognitiveservices.language.luis.runtime.models.EntityModel;
import com.microsoft.azure.cognitiveservices.language.luis.runtime.models.LuisResult;
import com.microsoft.azure.cognitiveservices.language.spellcheck.BingSpellCheckAPI;
import com.microsoft.azure.cognitiveservices.language.spellcheck.BingSpellCheckManager;
import com.microsoft.azure.cognitiveservices.language.spellcheck.BingSpellCheckOperations;
import com.microsoft.azure.cognitiveservices.language.spellcheck.models.SpellCheck;
import com.microsoft.azure.cognitiveservices.language.spellcheck.models.SpellCheckerOptionalParameter;
import com.microsoft.azure.cognitiveservices.language.spellcheck.models.SpellingFlaggedToken;
import com.microsoft.azure.cognitiveservices.language.spellcheck.models.SpellingTokenSuggestion;
import com.microsoft.azure.cognitiveservices.language.textanalytics.TextAnalytics;
import com.microsoft.azure.cognitiveservices.language.textanalytics.TextAnalyticsAPI;
import com.microsoft.azure.cognitiveservices.language.textanalytics.TextAnalyticsManager;
import com.microsoft.azure.cognitiveservices.language.textanalytics.models.AzureRegions;
import com.microsoft.azure.cognitiveservices.language.textanalytics.models.DetectLanguageOptionalParameter;
import com.microsoft.azure.cognitiveservices.language.textanalytics.models.DetectedLanguage;
import com.microsoft.azure.cognitiveservices.language.textanalytics.models.Input;
import com.microsoft.azure.cognitiveservices.language.textanalytics.models.LanguageBatchResult;
import com.microsoft.azure.cognitiveservices.language.textanalytics.models.LanguageBatchResultItem;
import com.microsoft.azure.cognitiveservices.vision.contentmoderator.ContentModeratorClient;
import com.microsoft.azure.cognitiveservices.vision.contentmoderator.ContentModeratorManager;
import com.microsoft.azure.cognitiveservices.vision.contentmoderator.TextModerations;
import com.microsoft.azure.cognitiveservices.vision.contentmoderator.models.AzureRegionBaseUrl;
import com.microsoft.azure.cognitiveservices.vision.contentmoderator.models.Classification;
import com.microsoft.azure.cognitiveservices.vision.contentmoderator.models.PII;
import com.microsoft.azure.cognitiveservices.vision.contentmoderator.models.Screen;
import com.microsoft.azure.cognitiveservices.vision.contentmoderator.models.ScreenTextOptionalParameter;
import com.microsoft.cognitiveservices.speech.CancellationDetails;
import com.microsoft.cognitiveservices.speech.CancellationReason;
import com.microsoft.cognitiveservices.speech.ResultReason;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechRecognitionResult;
import com.microsoft.cognitiveservices.speech.SpeechRecognizer;

import qNa.GetAnswer;
import speechSDK.tts.AudioOutputFormat;
import speechSDK.tts.Gender;
import speechSDK.tts.TTSService;

public class ConversationAIPipelineSDK {
	
    static String versionId = "0.1";
    static UUID appId;
    static String appEndpoint;
    static String luisAuthoringKey;
    
	// Step 1: Speech Service (Speech To Text)
	private static String recognizeSpeech(String speechSubscriptionKey, String serviceRegion) {
		
		String recognizedSpeech = "";
		
		try {
	            int exitCode = 1;
	            SpeechConfig config = SpeechConfig.fromSubscription(speechSubscriptionKey, serviceRegion);
	            assert(config != null);
	            
	            SpeechRecognizer reco = new SpeechRecognizer(config);
	            assert(reco != null);

	            System.out.println("Say something...");

	            Future<SpeechRecognitionResult> task = reco.recognizeOnceAsync();
	            assert(task != null);

	            SpeechRecognitionResult result = task.get();
	            assert(result != null);

	            if (result.getReason() == ResultReason.RecognizedSpeech) {
	            	recognizedSpeech = result.getText();
	                System.out.println("We recognized: " + recognizedSpeech);
	                
	                exitCode = 0;
	            }
	            else if (result.getReason() == ResultReason.NoMatch) {
	                System.out.println("NOMATCH: Speech could not be recognized.");
	            }
	            else if (result.getReason() == ResultReason.Canceled) {
	                CancellationDetails cancellation = CancellationDetails.fromResult(result);
	                System.out.println("CANCELED: Reason=" + cancellation.getReason());

	                if (cancellation.getReason() == CancellationReason.Error) {
	                    System.out.println("CANCELED: ErrorCode=" + cancellation.getErrorCode());
	                    System.out.println("CANCELED: ErrorDetails=" + cancellation.getErrorDetails());
	                    System.out.println("CANCELED: Did you update the subscription info?");
	                }
	            }

	            reco.close();
	        } catch (Exception ex) {
	            System.out.println("Unexpected exception: " + ex.getMessage());

	            assert(false);
	            System.exit(1);
	        }
		 return recognizedSpeech;
	}
	
	// Step 2: Text Analytics (Language Detection)
	private static String detactFirstLanguage(String text) {
		String detactLan = "";
		TextAnalyticsAPI taAPI = TextAnalyticsManager.authenticate(AzureRegions.WESTCENTRALUS, "d3796a4ca0b9449eafd2e97827e76050");
		TextAnalytics ta = taAPI.textAnalytics();
		Input input = new Input();
		input.withId("1").withText(text);
		List<Input> documents = new ArrayList<>();
		documents.add(input);
		
		DetectLanguageOptionalParameter detectLanguageOptionalParameter = new DetectLanguageOptionalParameter();
		detectLanguageOptionalParameter.withDocuments(documents);
		LanguageBatchResult languageBatchResult = ta.detectLanguage(detectLanguageOptionalParameter);
		
		List<LanguageBatchResultItem> resp = languageBatchResult.documents();
		for (LanguageBatchResultItem LanguageBatchResultItem : resp) {
			List<DetectedLanguage> detectedLanguages = LanguageBatchResultItem.detectedLanguages();
			for (DetectedLanguage lang : detectedLanguages) {
				String langName = lang.iso6391Name();				
				if (langName != null && !langName.isEmpty()) {
					return langName;
				}
			}
		}
		return detactLan;
	}
	
	// Step 3: Text Translator (Translate Text) Missing SDK
	
	
	// Step 4: Bing Spell Check (Spell Check) 
	private static String spellCheck(String market, String mode, String text) {
		
		BingSpellCheckAPI  bingSpellCheckAPI  = BingSpellCheckManager.authenticate("a8c484ddb5974c39b7ada08956ba2205");
		BingSpellCheckOperations bingSpellCheckOperations = bingSpellCheckAPI.bingSpellCheckOperations();
		SpellCheckerOptionalParameter spellCheckerOptionalParameter  = new SpellCheckerOptionalParameter();
		
		spellCheckerOptionalParameter.withMarket(market).withMode(mode);
		SpellCheck spellCheck = bingSpellCheckOperations.spellChecker(text, spellCheckerOptionalParameter);
		
		List<SpellingFlaggedToken> spellingFlaggedTokens = spellCheck.flaggedTokens();		
		System.out.println("Spelling flagged tokens size = " + spellingFlaggedTokens.size());
		
		if (spellingFlaggedTokens.size() == 0) {
			return text;
		}
		
		StringBuilder sb = new StringBuilder(); 
		
		for (SpellingFlaggedToken spellingFlaggedToken : spellingFlaggedTokens) {
			
			
			
			System.out.println("token = " + spellingFlaggedToken.token());
			List<SpellingTokenSuggestion> suggestions = spellingFlaggedToken.suggestions();
			
			
			for (SpellingTokenSuggestion spellingTokenSuggestion : suggestions) {
				System.out.println("suggestion = " + spellingTokenSuggestion.suggestion()
							+ ", score = " + spellingTokenSuggestion.score());
				String sug = spellingTokenSuggestion.suggestion();
				
				
				
				if (sug != null && !sug.isEmpty()) {
					sb.append(sug).append(" ");
					break;
				}
			}
		}
		
		
		if (sb.length() > 0)
			sb.setLength(sb.length()- 1);
		
		System.out.println("sb = " + sb.toString());
		
		
		return sb.toString();
	}
	
	
	// Step 5: Content Moderator
	private static String contentModerator(String text) {
		if (text== null || text.isEmpty()) return "";
		ContentModeratorClient contentModeratorClient = ContentModeratorManager.authenticate(AzureRegionBaseUrl.WESTCENTRALUSAPICOGNITIVEMICROSOFTCOM, "ddc60ae683fa419aace95b3c07ecee68");
		TextModerations textModerations = contentModeratorClient.textModerations();
		
		ScreenTextOptionalParameter screenTextOptionalParameter = new ScreenTextOptionalParameter();
		screenTextOptionalParameter.withAutocorrect(true).withPII(true).withClassify(true);
		
		Screen screen = textModerations.screenText("text/plain", text.getBytes(), screenTextOptionalParameter);
		
				
		System.out.println("auto corrected text = " + screen.autoCorrectedText());
		System.out.println("language = " + screen.language());
		System.out.println("normalized text = " + screen.normalizedText());
		System.out.println("original text = " + screen.originalText());
		
		Classification classification = screen.classification();
		if (classification == null) {
			return "Not an appropriate sentences";
		} else {
			System.out.println("review recommended = " + classification.reviewRecommended());
			if (classification.reviewRecommended()) {
				return "Review Recommended: category1 score = " + classification.category1().score() + ", category2 score = " + classification.category2().score() + ", category3 score = " + classification.category3().score() ;
			}
		}
		PII pii = screen.pII();
		if (pii == null) {
			System.out.println("pii is NULL");
		} else {
			System.out.println("pii = (address) " + pii.address().toString() + ", (email) "+ pii.email().toString() + ", (ipa) "+ pii.iPA().toString() + ", (phone) "+ pii.phone().toString() + ", (ssn) "+ pii.sSN().toString());			
		}
		return screen.autoCorrectedText();
	}
	
	
	// Step 6: LUIS
	private static String luis(String text) {
		String luisAuthoringKey = "616f4ba49e514a7b8f202285d84b3699";
		try {
			LUISAuthoringClient authoringClient = LUISAuthoringManager.authenticate(EndpointAPI.US_WEST, luisAuthoringKey);			
			System.out.println("Result of run Luis Authoring = " + runLuisAuthoring(authoringClient));		
			LuisRuntimeAPI runtimeClient = LuisRuntimeManager
	                .authenticate(com.microsoft.azure.cognitiveservices.language.luis.runtime.EndpointAPI.US_WEST, luisAuthoringKey);
			return runLuisRuntimeSample(runtimeClient, text);
		} catch (Exception e) {
			System.out.println("Erorr : " + e.getMessage());
			e.printStackTrace();
		}
		return "";
	}
	
	private static boolean runLuisAuthoring(LUISAuthoringClient authoringClient) {
		
        try {
        	try {
				appId = authoringClient.apps().add(new ApplicationCreateObject()
						.withName("FlightExample")
						.withInitialVersionId(versionId)
						.withCulture("en-us")
						);
        	} catch (Exception ex) {
        		// TODO: can't find an appropriate function call to get the exist status of the app 
        		System.out.println("Exception is " + ex.getMessage());
        		return false;
        	}
        	
			System.out.println("Created Application " + appId.toString());
			
			String destinationName = "Destination";
			UUID destinationId = authoringClient.models().addEntity()
					.withAppId(appId)
					.withVersionId(versionId)
					.withName(destinationName)
					.execute();
			System.out.println("Created simple entity " + destinationName + " with ID " +
					destinationId.toString());
	
			
			String className = "Class";
	
			UUID classId = authoringClient.models().addHierarchicalEntity(appId, versionId, 
					new HierarchicalEntityModel()
			                .withName(className)
			                .withChildren(Arrays.asList("First", "Business", "Economy")));
	
			System.out.println("Created hierarchical entity " + className + " with ID " + classId.toString());
			
			
			 //=============================================================
	        // This will create the "Flight" composite entity including "Class" and "Destination"
	        System.out.println("Creating the \"Flight\" composite entity including \"Class\" and \"Destination\".");
	
	        String flightName = "Flight";
	        UUID flightId = authoringClient.models().addCompositeEntity(appId, versionId, new CompositeEntityModel()
	            .withName(flightName)
	            .withChildren(Arrays.asList(className, destinationName)));
	
	        System.out.println("Created composite entity " + flightName + "with ID " + flightId.toString());
			
			
	        //=============================================================
	        // This will create a new "FindFlights" intent including the following utterances
	        System.out.println("Creating a new \"FindFlights\" intent with two utterances");
	        String utteranceFindEconomyToMadrid = "find flights in economy to Madrid";
	        String utteranceFindFirstToLondon = "find flights to London in first class";
	        String intentName = "FindFlights";
	
	        UUID intendId = authoringClient.models().addIntent()
	            .withAppId(appId)
	            .withVersionId(versionId)
	            .withName(intentName)
	            .execute();
	
	        System.out.println("Created intent " + intentName + "with ID " + intendId.toString());
	
			
			
	        //=============================================================
	        // This will build an EntityLabel Object
	        System.out.println("Building an EntityLabel Object");
	
	        ExampleLabelObject exampleLabelObject1 = new ExampleLabelObject()
	            .withText(utteranceFindEconomyToMadrid)
	            .withIntentName(intentName)
	            .withEntityLabels(Arrays.asList(
	                getEntityLabelObject(utteranceFindEconomyToMadrid, "Flight", "economy to Madrid"),
	                getEntityLabelObject(utteranceFindEconomyToMadrid, "Destination", "Madrid"),
	                getEntityLabelObject(utteranceFindEconomyToMadrid, "Class", "economy")
	            ));
	        ExampleLabelObject exampleLabelObject2 = new ExampleLabelObject()
	            .withText(utteranceFindFirstToLondon)
	            .withIntentName(intentName)
	            .withEntityLabels(Arrays.asList(
	                getEntityLabelObject(utteranceFindFirstToLondon, "Flight", "London in first class in first class"),
	                getEntityLabelObject(utteranceFindFirstToLondon, "Destination", "London in first class"),
	                getEntityLabelObject(utteranceFindFirstToLondon, "Class", "first")
	            ));
	
	        List<BatchLabelExample> utterancesResult = authoringClient.examples()
	            .batch(appId, versionId, Arrays.asList(exampleLabelObject1, exampleLabelObject2));
	
	        System.out.println("Utterances added to the " + intentName + " intent");
	
	        
	
	        //=============================================================
	        // This will start training the application.
	        System.out.println("Training the application");
	
	        EnqueueTrainingResponse trainingResult = authoringClient.trains().trainVersion(appId, versionId);
	        boolean isTrained = trainingResult.status().equals("UpToDate");
	
	        while (!isTrained) {
	            Thread.sleep(1000);
	            List<ModelTrainingInfo> status = authoringClient.trains().getStatus(appId, versionId);
	            isTrained = true;
	            for (ModelTrainingInfo modelTrainingInfo : status) {
	                if (!modelTrainingInfo.details().status().equals("UpToDate") && !modelTrainingInfo.details().status().equals("Success")) {
	                    isTrained = false;
	                    break;
	                }
	            }
	        }
	
	        //=============================================================
	        // This will start publishing the application.
	        System.out.println("Publishing the application");
	        ProductionOrStagingEndpointInfo publishResult = authoringClient.apps().publish(appId, new ApplicationPublishObject()
	            .withVersionId(versionId)
	            .withIsStaging(false)
	            .withRegion("westus")
	        );
	
	        appEndpoint = publishResult.endpointUrl() + "?subscription-key=" + luisAuthoringKey + "&q=";
	
	        System.out.println("Your app is published. You can now go to test it on " + appEndpoint);
	
	        

	        return true;
	    } catch (Exception f) {
	        System.out.println(f.getMessage());
	        f.printStackTrace();
	    }
	    return false;
	}

	
	static EntityLabelObject getEntityLabelObject(String utterance, String entityName, String value) {
	    return new EntityLabelObject()
	        .withEntityName(entityName)
	        .withStartCharIndex(utterance.indexOf(value))
	        .withEndCharIndex(utterance.indexOf(value) + value.length());
	}
	
	  /**
     * Main function which runs the runtime part of the sample.
     *
     * @param runtimeClient instance of the LUIS Runtime API client
     * @return true if sample runs successfully
     */
    public static String runLuisRuntimeSample(LuisRuntimeAPI runtimeClient, String text) {
        try {
            appId = UUID.fromString("f0865180-ff5d-4f12-a720-96182d666a7c");            
            //=============================================================
            // This will execute a LUIS prediction for a "find second class flight to new york" utterance
            String query = text;
            LuisResult predictionResult = runtimeClient.predictions().resolve()
                .withAppId(appId.toString())
                .withQuery(query)
                .execute();
            
            System.out.println("Executing query: " + query);            
            String intentEntities = "";
            
            if (predictionResult != null && predictionResult.topScoringIntent() != null) {
                System.out.format("Detected intent \"%s\" with the score %f%%\n", predictionResult.topScoringIntent().intent(), predictionResult.topScoringIntent().score() * 100);
                predictionResult.entities();
                if (predictionResult.entities() != null && predictionResult.entities().size() > 0) {
                	StringBuilder sb = new StringBuilder();
                	sb.append(predictionResult.topScoringIntent().intent());
                	for (EntityModel entityModel : predictionResult.entities()) {
                		sb.append(".").append(entityModel.type()).append(".").append(entityModel.entity());
                		System.out.format("\tFound entity \"%s\" with type %s\n", entityModel.entity(), entityModel.type());
                    }
                	System.out.println("sb = " + sb.toString());
                	intentEntities = sb.toString();
                } else {
                    System.out.println("\tNo entities were found.");
                }
            } else {
                System.out.println("Intent not found.");
            }
            return intentEntities;
        } catch (Exception f) {
            System.out.println(f.getMessage());
            f.printStackTrace();
        }
        return "";
    }
	
	
    // Step 7: QnA Maker (Retrieve Response)   Missing Java SDK
	// Step 8: Text Translater (Translate Text)  Missing Translator SDK in Maven repo
	// Step 9: Text to Speech (Still using API) 
	public static void textToSpeech(String textToSynthesize) {
		 String outputFormat = AudioOutputFormat.Riff24Khz16BitMonoPcm;
	     String deviceLanguage = "en-US";
	     String genderName = Gender.Male;
	     String voiceName = "Microsoft Server Speech Text to Speech Voice (en-US, Guy24KRUS)";

	     try{
	     	byte[] audioBuffer = TTSService.Synthesize(textToSynthesize, outputFormat, deviceLanguage, genderName, voiceName);
	     	
	     	// write the pcm data to the file
	     	String outputWave = ".\\output.pcm";
	     	File outputAudio = new File(outputWave);
	     	FileOutputStream fstream = new FileOutputStream(outputAudio);
	         fstream.write(audioBuffer);
	         fstream.flush();
	         fstream.close();
	         
	         
	         // specify the audio format 
	        	AudioFormat audioFormat = new AudioFormat(
	        			AudioFormat.Encoding.PCM_SIGNED,
	            		24000,
	            		16,
	            		1,
	            		1 * 2,
	            		24000,
	            		false);
	        	
	            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(outputWave));
	            
	            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class,
	                    audioFormat, AudioSystem.NOT_SPECIFIED);
	            SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem
	                    .getLine(dataLineInfo);
	            sourceDataLine.open(audioFormat);
	            sourceDataLine.start();
	            System.out.println("start to play the wave:");
	            /*
	             * read the audio data and send to mixer
	             */
	            int count;
	            byte tempBuffer[] = new byte[4096];
	            while ((count = audioInputStream.read(tempBuffer, 0, tempBuffer.length)) >0) {
	                    sourceDataLine.write(tempBuffer, 0, count);
	            }
	     
	            sourceDataLine.drain();
	            sourceDataLine.close();
	            audioInputStream.close();
	            
	     }catch(Exception e){
	     	e.printStackTrace();
	     }
	}
	
	
    public static void main(String[] args) {

    	Map<String, String> languageMap = new HashMap<String, String>();
    	languageMap.put("en", "en-US");

        System.out.println("This is a simple example of Conversation AI pipeline,\n"
                + "Please try to ask one of below questions: \n"
//                + "1, \"Call\" (ans: 777-777-7777) \n"
//                + "2, \"Phone\" (ans: 111-111-1111) \n"
//                + "3, \"Address\" (ans: Microsoft Way 1, Redmond, WA) \n"
//                + "4, \"Email\" (ans: lol@microsoft.com) \n"
//                
        		+ "1, \"find flights to London in first class\" (ans: london in first class, testing testing)"
        		
        		);
   	 /**
   	  *  	1, Process/Filter Speech Stream
   	  * */
   		// Step 1: Speech Service (Speech To Text)
   		String recognizedText  = recognizeSpeech("f54ba74e83904bd593ec228acde2d22b", "westus");
   		System.out.println("\nStep 1, recognized text = " + recognizedText);
   		
   		// Step 2: Text Analytics (Language Detection)
   		String detectLangResp = detactFirstLanguage(recognizedText);
   		System.out.println("\nStep 2, detect language = " + detectLangResp);
   		
//   		// Step 3: Text Translator (Translate Text)        // Missing Translator SDK in Maven repo
//        String enText = translateText(recognizedText, "en");
//   		System.out.println("\nStep 3, translate to english, text = " + enText);
   		
//   		// Step 4: Bing Spell Check (Spell Check) 
   		System.out.println("\nStep 4: Spelling check");
//   		String enText = "find flights to London in first class";
   		String correctedText = spellCheck(languageMap.get(detectLangResp), "proof", recognizedText);
   		System.out.println("corrected text = " + correctedText);
   
   		// Step 5: Content Moderator (Explicit Content Rec) 
   		System.out.println("\nStep 5, content moderator");
//   		String moderatedText =  contentModerator("These are all UK phone numbers, the last two being Microsoft UK support numbers: +44 870 608 4000 or 0344 800 2400 or 0800 820 3300. dumb!");
   		String moderatedText =  contentModerator(correctedText);
   		System.out.println("content moderated text = " + moderatedText);

   	 /**
   		 *  	2, Retrieve Response
   		 *  
   		 *  	using QnA(https://www.qnamaker.ai/) to create Knowledge base
   		 **/
   			// Step 6: LUIS (Recognize Intent)   			
   			System.out.println("\nStep 6, LUIS");
//   			String question = luis("find flights to London in first class");
   			String question = luis(moderatedText);

   			// Step 7: QnA Maker (Retrieve Response)   Missing Java SDK
   			System.out.println("\nStep 7, QnA Maker");	
			String ans = GetAnswer.getAns(question);    			
   	    	String topAns = GetAnswer.getTopAns(ans);
   	    	System.out.println("answer = " + topAns);
		
   		/**
   		 *  	3, Generate Output
   		 **/
   			// Step 8: Text Translater (Translate Text)    	    	    		 // Missing Translator SDK in Maven repo
   	    	System.out.println("\nStep 8, Text Translator");
//   	    	String translatedTopAns = translateText(topAns, detectLangResp);
//   	    	System.out.println("translated top ans is, " + translatedTopAns);
   			
   			// Step 9: Speech Service (Text To Speech)						// Missing SDK API, there are some classes are missing but found AudioInputStrean class
    		System.out.println("\nStep 9, Text to Speech");
    		textToSpeech(topAns);
   			
   			System.out.println("---------------- End of Conversation AI Pipeline --------------------");
   		
   		
   		
   		
    }
}