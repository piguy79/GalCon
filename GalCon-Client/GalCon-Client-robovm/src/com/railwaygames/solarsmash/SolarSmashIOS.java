package com.railwaygames.solarsmash;

import org.robovm.apple.coregraphics.CGRect;
import org.robovm.apple.foundation.NSAutoreleasePool;
import org.robovm.apple.foundation.NSObject;
import org.robovm.apple.foundation.NSRange;
import org.robovm.apple.foundation.NSURL;
import org.robovm.apple.uikit.UIApplication;
import org.robovm.apple.uikit.UIKeyboardType;
import org.robovm.apple.uikit.UIReturnKeyType;
import org.robovm.apple.uikit.UITextAutocapitalizationType;
import org.robovm.apple.uikit.UITextAutocorrectionType;
import org.robovm.apple.uikit.UITextField;
import org.robovm.apple.uikit.UITextFieldDelegate;
import org.robovm.apple.uikit.UITextFieldDelegateAdapter;
import org.robovm.apple.uikit.UITextSpellCheckingType;
import org.robovm.bindings.gpp.GPPURLHandler;

import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import com.badlogic.gdx.backends.iosrobovm.IOSApplicationConfiguration;
import com.railwaygames.solarsmash.screen.widget.ShaderTextField.OnscreenKeyboard;

public class SolarSmashIOS extends IOSApplication.Delegate implements OnscreenKeyboard {
	public static final String LOG_NAME = "GalCon";

	private IOSApplication iosApplication;
	private UITextFieldDelegate textDelegate;
	private UITextField textfield;

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
		IOSGameAction gameAction = new IOSGameAction(socialAction);
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
