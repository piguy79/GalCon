package com.xxx.galcon.screen.widget;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.xxx.galcon.ExternalActionWrapper;
import com.xxx.galcon.http.UIConnectionResultCallback;
import com.xxx.galcon.model.InventoryItem;
import com.xxx.galcon.model.Player;

public class PaymentButton extends ShaderTextButton {

	public InventoryItem inventory;

	public PaymentButton(ShaderProgram shader, final InventoryItem inventory, Skin skin, String styleName,
			final UIConnectionResultCallback<Player> callback) {
		super(shader, createTextFromInventory(inventory), skin, styleName);
		this.inventory = inventory;
		this.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				ExternalActionWrapper.purchaseCoins(inventory);
			}
		});
	}

	private static String createTextFromInventory(InventoryItem inventory2) {
		return inventory2.price + "  " + inventory2.numCoins + " Coin[s]";
	}

}
