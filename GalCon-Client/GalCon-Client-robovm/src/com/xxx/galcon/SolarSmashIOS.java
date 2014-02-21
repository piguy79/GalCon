package com.xxx.galcon;

import org.robovm.bindings.gpp.GPPURLHandler;
import org.robovm.cocoatouch.coregraphics.CGRect;
import org.robovm.cocoatouch.foundation.NSAutoreleasePool;
import org.robovm.cocoatouch.foundation.NSObject;
import org.robovm.cocoatouch.foundation.NSRange;
import org.robovm.cocoatouch.foundation.NSURL;
import org.robovm.cocoatouch.uikit.UIApplication;
import org.robovm.cocoatouch.uikit.UIKeyboardType;
import org.robovm.cocoatouch.uikit.UIReturnKeyType;
import org.robovm.cocoatouch.uikit.UITextAutocapitalizationType;
import org.robovm.cocoatouch.uikit.UITextAutocorrectionType;
import org.robovm.cocoatouch.uikit.UITextField;
import org.robovm.cocoatouch.uikit.UITextFieldDelegate;
import org.robovm.cocoatouch.uikit.UITextSpellCheckingType;

import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import com.badlogic.gdx.backends.iosrobovm.IOSApplicationConfiguration;
import com.xxx.galcon.screen.widget.ShaderTextField.OnscreenKeyboard;

public class SolarSmashIOS extends IOSApplication.Delegate implements OnscreenKeyboard {
	public static final String LOG_NAME = "GalCon";

	private IOSApplication iosApplication;
	private UITextFieldDelegate textDelegate;
	private UITextField textfield;

	@Override
	protected IOSApplication createApplication() {
		IOSApplicationConfiguration config = new IOSApplicationConfiguration();
		config.orientationLandscape = false;
		config.orientationPortrait = true;

		IOSSocialAction socialAction = new IOSSocialAction();
		IOSGameAction gameAction = new IOSGameAction(socialAction);
		IOSInAppBillingAction inAppBillingAction = new IOSInAppBillingAction();

		textDelegate = new UITextFieldDelegate.Adapter() {
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

	public static void main(String[] argv) {
		NSAutoreleasePool pool = new NSAutoreleasePool();
		UIApplication.main(argv, null, SolarSmashIOS.class);
		pool.drain();
	}

	@Override
	public boolean openURL(UIApplication application, NSURL url, String sourceApplication, NSObject annotation) {
		return GPPURLHandler.handleURL(url, sourceApplication, annotation);
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
