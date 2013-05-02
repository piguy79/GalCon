package com.xxx.galcon;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.xxx.galcon.http.ConnectionException;
import com.xxx.galcon.http.DesktopGameAction;
import com.xxx.galcon.http.SetPlayerResultHandler;
import com.xxx.galcon.http.UIConnectionResultCallback;
import com.xxx.galcon.model.HandleResponse;
import com.xxx.galcon.model.Player;

public class Main {
	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "GalCon-Client";
		cfg.useGL20 = true;
		cfg.width = 480;
		cfg.height = 800;

		DesktopGameAction gameAction = new DesktopGameAction("localhost", 3000);

		Player player = new Player();
		player.name = "me" + Math.random() * 10000;
		try {
			gameAction.findUserInformation(new SetPlayerResultHandler(player), player.name);
			gameAction.requestHandleForUserName(new UIConnectionResultCallback<HandleResponse>() {

				@Override
				public void onConnectionResult(HandleResponse result) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onConnectionError(String msg) {
					// TODO Auto-generated method stub

				}
			}, player.name, "MyHandle");
		} catch (ConnectionException e) {
			e.printStackTrace();
		}
		new LwjglApplication(new GameLoop(player, gameAction), cfg);
	}
}
