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

		final Player player = new Player();
		int rand = (int) (Math.random() * 1000);
		player.name = "me" + rand;
		try {
			gameAction.findUserInformation(new SetPlayerResultHandler(player), player.name);
			gameAction.requestHandleForUserName(new UIConnectionResultCallback<HandleResponse>() {

				@Override
				public void onConnectionResult(HandleResponse result) {
					player.handle = result.player.handle;
				}

				@Override
				public void onConnectionError(String msg) {
					// TODO Auto-generated method stub

				}
			}, player.name, "Handle" + rand);
		} catch (ConnectionException e) {
			e.printStackTrace();
		}
		new LwjglApplication(new GameLoop(player, gameAction), cfg);
	}
}
