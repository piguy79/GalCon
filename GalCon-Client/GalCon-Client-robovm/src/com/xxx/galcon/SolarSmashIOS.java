package com.xxx.galcon;

import org.robovm.cocoatouch.foundation.NSAutoreleasePool;
import org.robovm.cocoatouch.uikit.UIApplication;

import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import com.badlogic.gdx.backends.iosrobovm.IOSApplicationConfiguration;

public class SolarSmashIOS extends IOSApplication.Delegate {
	@Override
	protected IOSApplication createApplication() {
		IOSApplicationConfiguration config = new IOSApplicationConfiguration();
		config.orientationLandscape = false;
		config.orientationPortrait = true;

		IOSGameAction gameAction = new IOSGameAction();
		IOSSocialAction socialAction = new IOSSocialAction();
		IOSInAppBillingAction inAppBillingAction = new IOSInAppBillingAction();

		return new IOSApplication(new GameLoop(gameAction, socialAction, inAppBillingAction), config);
	}

	public static void main(String[] argv) {
		NSAutoreleasePool pool = new NSAutoreleasePool();
		UIApplication.main(argv, null, SolarSmashIOS.class);
		pool.drain();
	}
}
