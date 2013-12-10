package com.xxx.galcon;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.xxx.galcon.config.Configuration;
import com.xxx.galcon.http.DesktopGameAction;
import com.xxx.galcon.http.DesktopSocialAction;
import com.xxx.galcon.http.SetConfigurationResultHandler;

public class Main {
	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "GalCon-Client";
		cfg.useGL20 = true;
		cfg.width = 480;
		cfg.height = 800;

		DesktopGameAction gameAction = new DesktopGameAction("localhost", 3000);
		DesktopSocialAction socialAction = new DesktopSocialAction();

		final Configuration config = new Configuration();

		gameAction.findConfigByType(new SetConfigurationResultHandler(config), "app");

		new LwjglApplication(new GameLoop(gameAction, socialAction, config), cfg);
	}
}
