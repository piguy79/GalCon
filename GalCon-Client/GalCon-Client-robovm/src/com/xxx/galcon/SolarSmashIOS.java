package com.xxx.galcon;

import org.robovm.bindings.gpp.GPPURLHandler;
import org.robovm.cocoatouch.foundation.NSAutoreleasePool;
import org.robovm.cocoatouch.foundation.NSObject;
import org.robovm.cocoatouch.foundation.NSURL;
import org.robovm.cocoatouch.uikit.UIApplication;

import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import com.badlogic.gdx.backends.iosrobovm.IOSApplicationConfiguration;

public class SolarSmashIOS extends IOSApplication.Delegate {
	public static final String LOG_NAME = "GalCon";

	@Override
	protected IOSApplication createApplication() {
		IOSApplicationConfiguration config = new IOSApplicationConfiguration();
		config.orientationLandscape = false;
		config.orientationPortrait = true;

		IOSSocialAction socialAction = new IOSSocialAction();
		IOSGameAction gameAction = new IOSGameAction(socialAction);
		IOSInAppBillingAction inAppBillingAction = new IOSInAppBillingAction();

		return new IOSApplication(new GameLoop(gameAction, socialAction, inAppBillingAction), config);
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

}
