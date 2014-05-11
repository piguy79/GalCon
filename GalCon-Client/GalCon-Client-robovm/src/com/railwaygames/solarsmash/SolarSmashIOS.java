package com.railwaygames.solarsmash;

import static com.railwaygames.solarsmash.Config.HOST;
import static com.railwaygames.solarsmash.Config.PORT;
import static com.railwaygames.solarsmash.Config.PROTOCOL;
import static com.railwaygames.solarsmash.http.UrlConstants.FIND_GAMES_WITH_A_PENDING_MOVE;

import org.robovm.apple.coregraphics.CGRect;
import org.robovm.apple.foundation.NSAutoreleasePool;
import org.robovm.apple.foundation.NSData;
import org.robovm.apple.foundation.NSDate;
import org.robovm.apple.foundation.NSDictionary;
import org.robovm.apple.foundation.NSError.NSErrorPtr;
import org.robovm.apple.foundation.NSMutableURLRequest;
import org.robovm.apple.foundation.NSObject;
import org.robovm.apple.foundation.NSRange;
import org.robovm.apple.foundation.NSString;
import org.robovm.apple.foundation.NSURL;
import org.robovm.apple.foundation.NSURLConnection;
import org.robovm.apple.foundation.NSURLResponse;
import org.robovm.apple.uikit.UIApplication;
import org.robovm.apple.uikit.UIBackgroundFetchResult;
import org.robovm.apple.uikit.UIKeyboardType;
import org.robovm.apple.uikit.UILocalNotification;
import org.robovm.apple.uikit.UIReturnKeyType;
import org.robovm.apple.uikit.UITextAutocapitalizationType;
import org.robovm.apple.uikit.UITextAutocorrectionType;
import org.robovm.apple.uikit.UITextField;
import org.robovm.apple.uikit.UITextFieldDelegate;
import org.robovm.apple.uikit.UITextFieldDelegateAdapter;
import org.robovm.apple.uikit.UITextSpellCheckingType;
import org.robovm.bindings.crashlytics.Crashlytics;
import org.robovm.bindings.gpp.GPPURLHandler;
import org.robovm.objc.block.VoidBlock1;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import com.badlogic.gdx.backends.iosrobovm.IOSApplicationConfiguration;
import com.railwaygames.solarsmash.model.GameCount;
import com.railwaygames.solarsmash.screen.widget.ShaderTextField.OnscreenKeyboard;

public class SolarSmashIOS extends IOSApplication.Delegate implements OnscreenKeyboard {
	public static final String LOG_NAME = "GalCon";

	private IOSApplication iosApplication;
	private UITextFieldDelegate textDelegate;
	private UITextField textfield;
	private IOSGameAction gameAction;

	public static void main(String[] argv) {
		NSAutoreleasePool pool = new NSAutoreleasePool();
		UIApplication.main(argv, null, SolarSmashIOS.class);
		pool.close();
	}

	@Override
	protected IOSApplication createApplication() {
		IOSApplicationConfiguration config = new IOSApplicationConfiguration();
		config.orientationLandscape = false;
		config.orientationPortrait = true;
		config.allowIpod = true;

		IOSSocialAction socialAction = new IOSSocialAction();
		gameAction = new IOSGameAction(socialAction);
		IOSInAppBillingAction inAppBillingAction = new IOSInAppBillingAction();

		textDelegate = new UITextFieldDelegateAdapter() {
			@Override
			public boolean shouldChangeCharacters(UITextField textField, NSRange range, String string) {
				// "cheating" to detect backspace without overriding method
				if (range.length() > 0 && string.isEmpty()) {
					iosApplication.getInput().getInputProcessor().keyTyped((char) 8);
					return false;
				}

				char[] chars = new char[string.length()];
				string.getChars(0, string.length(), chars, 0);
				for (int i = 0; i < chars.length; i++) {
					iosApplication.getInput().getInputProcessor().keyTyped(chars[i]);
				}
				return false;
			}

			@Override
			public boolean shouldReturn(UITextField textField) {
				textField.resignFirstResponder();
				return false;
			}
		};

		iosApplication = new IOSApplication(new GameLoop(gameAction, socialAction, inAppBillingAction, this), config);
		return iosApplication;
	}

	@Override
	public boolean openURL(UIApplication application, NSURL url, String sourceApplication, NSObject annotation) {
		return GPPURLHandler.handleURL(url, sourceApplication, annotation);
	}
	
	@Override
	public void didBecomeActive(UIApplication application) {
		application.setApplicationIconBadgeNumber(0);
	}

	@Override
	public boolean didFinishLaunching(UIApplication application, NSDictionary<NSString, ?> launchOptions) {
		super.didFinishLaunching(application, launchOptions);
		Crashlytics.start("16b0d935ae5ad2229665b4beef8cc396294f878d");

		application.cancelAllLocalNotifications();
		application.setMinimumBackgroundFetchInterval(5 * 60);
		application.setApplicationIconBadgeNumber(0);

		return true;
	}

	@Override
	public void performFetch(final UIApplication application,
			final VoidBlock1<UIBackgroundFetchResult> completionHandler) {
		Gdx.app.log("FETCH", "Start fetch");
		application.cancelAllLocalNotifications();

		Preferences prefs = Gdx.app.getPreferences(Constants.GALCON_PREFS);
		String handle = prefs.getString(Constants.HANDLE, "");

		if (handle == null || handle.isEmpty()) {
			Gdx.app.log("FETCH", "No handle");
			completionHandler.invoke(UIBackgroundFetchResult.NoData);
			return;
		}

		IOSConfig config = new IOSConfig();
		NSMutableURLRequest request = new NSMutableURLRequest();
		request.setURL(new NSURL(config.getValue(PROTOCOL) + "://" + config.getValue(HOST) + ":"
				+ config.getValue(PORT) + FIND_GAMES_WITH_A_PENDING_MOVE + "?handle=" + handle));
		request.setHTTPMethod("GET");
		request.setTimeoutInterval(20);

		NSURLResponse.NSURLResponsePtr responsePtr = new NSURLResponse.NSURLResponsePtr();
		NSErrorPtr errorPtr = new NSErrorPtr();

		Gdx.app.log("FETCH", "Starting request");
		NSData data = NSURLConnection.sendSynchronousRequest$returningResponse$error$(request, responsePtr, errorPtr);
		Gdx.app.log("FETCH", "Request done");

		GameCount result = new GameCount();
		IOSGameAction.processResponse(result, data, errorPtr != null ? errorPtr.get() : null);

		if (result.errorMessage != null && result.errorMessage.length() > 0) {
			completionHandler.invoke(UIBackgroundFetchResult.Failed);
			return;
		}

		Gdx.app.log("FETCH", "Complete with result: " + (result.pendingGameCount + result.inviteCount));

		String pendingText = "";
		int pendingGamesCount = result.pendingGameCount;
		if (pendingGamesCount > 0) {
			if (pendingGamesCount == 1) {
				pendingText = "1 game is awaiting your move";
			} else {
				pendingText = pendingGamesCount + " games are awaiting your move";
			}
		}

		String inviteText = "";
		int inviteCount = result.inviteCount;
		if (inviteCount > 0) {
			if (inviteCount == 1) {
				inviteText = "1 pending invite";
			} else {
				inviteText = inviteCount + " pending invites";
			}
		}

		String text = pendingText;
		if (text.length() > 0 && inviteText.length() > 0) {
			text += " and ";
		}
		text += inviteText;

		if (text.length() > 0) {
			UILocalNotification notification = new UILocalNotification();
			notification.setFireDate(NSDate.date());
			notification.setApplicationIconBadgeNumber(result.pendingGameCount + result.inviteCount);
			notification.setAlertBody(text);
			notification.setAlertAction("Solar Smash");
			notification.setSoundName(UILocalNotification.DefaultSoundName());
			application.presentLocalNotificationNow(notification);
			Gdx.app.log("FETCH", "Done newData");
			completionHandler.invoke(UIBackgroundFetchResult.NewData);
			return;
		}

		completionHandler.invoke(UIBackgroundFetchResult.NoData);
	}

	/**
	 * Need to overlay the keyboard
	 */
	@Override
	public void show(boolean visible) {
		if (visible) {
			textfield = new UITextField(new CGRect(10, 10, 100, 50));
			// Setting parameters
			textfield.setKeyboardType(UIKeyboardType.Default);
			textfield.setReturnKeyType(UIReturnKeyType.Done);
			textfield.setAutocapitalizationType(UITextAutocapitalizationType.None);
			textfield.setAutocorrectionType(UITextAutocorrectionType.No);
			textfield.setSpellCheckingType(UITextSpellCheckingType.No);
			textfield.setHidden(true);
			// Text field needs to have at least one symbol - so we can use
			// backspace
			textfield.setText("x");

			textfield.becomeFirstResponder();
			textfield.setDelegate(textDelegate);

			iosApplication.getUIViewController().getView().addSubview(textfield);
		} else {
			if (textfield != null) {
				textfield.resignFirstResponder();
				textfield.removeFromSuperview();
				textfield = null;
			}
		}
	}
}
