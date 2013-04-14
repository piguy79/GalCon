package com.xxx.galcon;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.xxx.galcon.http.DesktopGameAction;

public class Main {
	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "GalCon-Client";
		cfg.useGL20 = true;
		cfg.width = 540;
		cfg.height = 860;

		new LwjglApplication(new GameLoop("me" + Math.random() * 10000, new DesktopGameAction("localhost", 3000)), cfg);
	}
}
