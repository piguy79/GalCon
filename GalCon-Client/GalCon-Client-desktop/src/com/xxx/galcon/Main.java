package com.xxx.galcon;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.railwaygames.solarsmash.GameLoop;
import com.railwaygames.solarsmash.screen.widget.ShaderTextField;
import com.xxx.galcon.http.DesktopGameAction;
import com.xxx.galcon.http.DesktopInAppBillingAction;
import com.xxx.galcon.http.DesktopSocialAction;

public class Main {
	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "GalCon-Client";
		cfg.width = 450;
		cfg.height = 800;

		DesktopGameAction gameAction = new DesktopGameAction("localhost", 3000);
		DesktopSocialAction socialAction = new DesktopSocialAction();
		DesktopInAppBillingAction inAppBillAction = new DesktopInAppBillingAction();

		GameLoop loop = new GameLoop(gameAction, socialAction, inAppBillAction,
				new ShaderTextField.DefaultOnscreenKeyboard());
		new LwjglApplication(loop, cfg);
		loop.resize(480, 800);
	}
}
