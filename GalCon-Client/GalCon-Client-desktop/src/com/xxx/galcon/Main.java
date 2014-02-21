package com.xxx.galcon;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.xxx.galcon.http.DesktopGameAction;
import com.xxx.galcon.http.DesktopInAppBillingAction;
import com.xxx.galcon.http.DesktopSocialAction;
import com.xxx.galcon.screen.widget.ShaderTextField;

public class Main {
	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "GalCon-Client";
		cfg.useGL20 = true;
		cfg.width = 480;
		cfg.height = 800;

		DesktopGameAction gameAction = new DesktopGameAction("localhost", 3000);
		DesktopSocialAction socialAction = new DesktopSocialAction();
		DesktopInAppBillingAction inAppBillAction = new DesktopInAppBillingAction();

		new LwjglApplication(new GameLoop(gameAction, socialAction, inAppBillAction,
				new ShaderTextField.DefaultOnscreenKeyboard()), cfg);
	}
}
