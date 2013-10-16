package com.xxx.galcon;

import com.jirbo.adcolony.AdColonyVideoListener;
import com.xxx.galcon.http.GameAction;
import com.xxx.galcon.http.UIConnectionResultCallback;
import com.xxx.galcon.inappbilling.util.StoreResultCallback;
import com.xxx.galcon.model.Inventory;
import com.xxx.galcon.model.InventoryItem;
import com.xxx.galcon.model.Player;

public class ExternalActionWrapper {
	
	private static GameAction gameAction;

	private ExternalActionWrapper() {
		super();
	}

	public static void setGameAction(GameAction gameAction) {
		ExternalActionWrapper.gameAction = gameAction;
	}
	
	public static void showAd(AdColonyVideoListener listner){
		gameAction.showAd(listner);
	}
	
	public static void purchaseCoins(InventoryItem inventoryItem){
		gameAction.purchaseCoins(inventoryItem, new UIConnectionResultCallback<Player>() {

			@Override
			public void onConnectionResult(Player result) {
				GameLoop.USER.usedCoins = result.usedCoins;
				GameLoop.USER.coins = result.coins;
				GameLoop.USER.consumedOrders = result.consumedOrders;
				gameAction.consumeOrders(result.consumedOrders);
			}

			@Override
			public void onConnectionError(String msg) {
				// TODO Auto-generated method stub
				
			}
		});
	}

	public static void loadStoreInventory(final StoreResultCallback<Inventory> callback) {
		gameAction.loadAvailableInventory(new UIConnectionResultCallback<Inventory>() {
			
			@Override
			public void onConnectionResult(Inventory result) {
				gameAction.loadStoreInventory(result, callback);
				
			}
			
			@Override
			public void onConnectionError(String msg) {
				// Do something useful.
				
			}
		});		
	}

}
