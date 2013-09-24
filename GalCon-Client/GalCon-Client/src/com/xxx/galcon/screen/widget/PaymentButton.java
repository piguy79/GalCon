package com.xxx.galcon.screen.widget;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.xxx.galcon.ExternalActionWrapper;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.UIConnectionWrapper;
import com.xxx.galcon.http.UIConnectionResultCallback;
import com.xxx.galcon.model.InventoryItem;
import com.xxx.galcon.model.Player;

public class PaymentButton extends TextButton {
	
	public InventoryItem inventory;

	
	public PaymentButton(final InventoryItem inventory, Skin skin, String styleName, final UIConnectionResultCallback<Player> callback) {
		super(createTextFromInventory(inventory), skin, styleName);
		this.inventory = inventory;
		this.addListener(new InputListener(){
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				ExternalActionWrapper.purchaseCoins(inventory);
				//UIConnectionWrapper.addCoins(callback, GameLoop.USER.handle, inventory.numCoins, GameLoop.USER.usedCoins);
			}
		});
	}


	private static String createTextFromInventory(InventoryItem inventory2) {
		return inventory2.price + "  " + inventory2.numCoins + " Coins";
	}

	
}
