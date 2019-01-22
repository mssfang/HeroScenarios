//
//Copyright (c) Microsoft. All rights reserved.
//Licensed under the MIT license. See LICENSE.md file in the project root for full license information.
//
//<code>
package speechSDK;

import java.util.concurrent.Future;
import com.microsoft.cognitiveservices.speech.*;

/**
* Quickstart: recognize speech using the Speech SDK for Java.
*/
public class RecognizeSpeech {
	
	public static String sttService(String speechSubscriptionKey, String serviceRegion) {
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
	             System.out.println("We recognized: " + result.getText());
	             exitCode = 0;
	             return result.getText();
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
	         
	         System.exit(exitCode);
	         return "";
	     } catch (Exception ex) {
	         System.out.println("Unexpected exception: " + ex.getMessage());

	         assert(false);
//	         System.exit(1);
	         return "";
	     }
	}
}