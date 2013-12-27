package com.xxx.galcon;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.WindowManager;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.jirbo.adcolony.AdColony;
import com.jirbo.adcolony.AdColonyVideoAd;
import com.jirbo.adcolony.AdColonyVideoListener;
import com.xxx.galcon.config.Configuration;
import com.xxx.galcon.http.SetConfigurationResultHandler;
import com.xxx.galcon.http.SocialAction;
import com.xxx.galcon.http.UIConnectionResultCallback;
import com.xxx.galcon.inappbilling.util.IabHelper;
import com.xxx.galcon.inappbilling.util.IabHelper.QueryInventoryFinishedListener;
import com.xxx.galcon.inappbilling.util.IabResult;
import com.xxx.galcon.inappbilling.util.Purchase;
import com.xxx.galcon.inappbilling.util.SkuDetails;
import com.xxx.galcon.model.Inventory;
import com.xxx.galcon.model.InventoryItem;
import com.xxx.galcon.model.Order;
import com.xxx.galcon.model.Player;
import com.xxx.galcon.service.PingService;

public class MainActivity extends AndroidApplication {
	public static final String LOG_NAME = "GalCon";
	public static final int SIGN_IN_ACTIVITY_RESULT_CODE = 57029;

	protected String mDebugTag = "MainActivity";
	protected boolean mDebugLog = true;

	final static String APP_ID = "appae5819628c4f43b5b7f9f9";
	final static String ZONE_ID = "vz592240fd26724b2a955912";

	final static String APPLICATION_CONFIG = "app";
	final static String ENCODED_APPLICATION_ID = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArSXCD3B6yYCKEeGA8y5q8G4Yc/XJCcg9QdFs+NIvE+YsTCSruh1sKKldOstcc6magpBjdGuNKMhSq+QiqN5irFbh3XcKoSiYR/5dX4J2bURxj1yI7H6yCwvAfBaw1xzhWyMJ8qUtj3FW8XejnWev5MgasrxCc2dNNBzJNCynOsreGhWVx+dlcqBITpv0ctMAb/gLw8MMFOFQ/r8+2Twl8RX+KOVjBrB3GelX7dUSAhynoBTgmyoC5qPId3pDlcwIKEt6iHJfP4bv7VBxhqOllATK5E8Ja2DIWPJQW9LSjkdQe1hXo/kv71pfAZj98691+PDCPxaUNmZzWER+KsbXMQIDAQAB";

	private AndroidGameAction gameAction;
	private SocialAction socialAction;

	private IabHelper mHelper;

	public MainActivity() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Crashlytics.start(this);

		setupAdColony();

		AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
		cfg.useGL20 = true;

		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

		socialAction = new AndroidSocialAction(this);
		gameAction = new AndroidGameAction(this, socialAction, connectivityManager);

		Configuration config = new Configuration();
		gameAction.findConfigByType(new SetConfigurationResultHandler(config), APPLICATION_CONFIG);

		initialize(new GameLoop(gameAction, socialAction, config), cfg);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		Intent intent = new Intent(this, PingService.class);
		startService(intent);
	}

	private UIConnectionResultCallback<Player> playerCallback = new UIConnectionResultCallback<Player>() {

		@Override
		public void onConnectionResult(Player result) {
			GameLoop.USER.coins = result.coins;
			GameLoop.USER.usedCoins = result.usedCoins;
			GameLoop.USER.watchedAd = result.watchedAd;
			gameAction.consumeOrders(result.consumedOrders);
		}

		@Override
		public void onConnectionError(String msg) {
			// TODO Auto-generated method stub

		}
	};

	private void consumeAnyPurchasedOrders() {
		UIConnectionWrapper.loadAvailableInventory(new UIConnectionResultCallback<Inventory>() {

			@Override
			public void onConnectionResult(Inventory result) {
				gameAction.loadStoreInventory(result, new UIConnectionResultCallback<Inventory>() {

					@Override
					public void onConnectionResult(final Inventory inventory) {
						mHelper.queryInventoryAsync(true, inventory.skus(), new QueryInventoryFinishedListener() {

							@Override
							public void onQueryInventoryFinished(IabResult result,
									com.xxx.galcon.inappbilling.util.Inventory inv) {

								List<Order> orders = new ArrayList<Order>();
								for (InventoryItem item : inventory.inventory) {
									if (inv.hasPurchase(item.sku)) {
										orders.add(new Order(inv.getPurchase(item.sku).getOriginalJson(), item.numCoins));
									}
								}

								UIConnectionWrapper.addCoinsForAnOrder(playerCallback, GameLoop.USER.handle, orders);

							}
						});
					}

					@Override
					public void onConnectionError(String msg) {
						// TODO Auto-generated method stub

					}
				});
			}

			@Override
			public void onConnectionError(String msg) {
				complain("Unable to load inventory from server.");

			}
		});
		List<String> skus = new ArrayList<String>();
		skus.add("android.test.purchased");

	}

	public void setupInAppBilling() {
		String base64EncodedPublicKey = ENCODED_APPLICATION_ID;

		// Create the helper, passing it our context and the public key to
		// verify signatures with
		mHelper = new IabHelper(this, base64EncodedPublicKey);

		// enable debug logging (for a production application, you should set
		// this to false).
		mHelper.enableDebugLogging(true);

		mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
			public void onIabSetupFinished(IabResult result) {

				if (!result.isSuccess()) {
					complain("Problem setting up in-app billing: " + result);
					return;
				} else {
					consumeAnyPurchasedOrders();
				}
			}
		});

	}

	private void setupAdColony() {
		AdColony.configure(this, "version:1.0,store:google", APP_ID, ZONE_ID);

		if (!AdColony.isTablet()) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
	}

	void complain(String message) {
		alert("Error: " + message);
	}

	void alert(String message) {
		AlertDialog.Builder bld = new AlertDialog.Builder(this);
		bld.setMessage(message);
		bld.setNeutralButton("OK", null);
		bld.create().show();
	}

	@Override
	protected void onPause() {
		super.onPause();
		AdColony.pause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		AdColony.resume(this);
	}

	public void displayAd(AdColonyVideoListener adListener) {
		AdColonyVideoAd ad = new AdColonyVideoAd();
		ad.show(adListener);
	}

	public void purchaseCoins(final InventoryItem inventoryItem, final UIConnectionResultCallback<Player> callback) {
		mHelper.launchPurchaseFlow(this, inventoryItem.sku, 1001, new IabHelper.OnIabPurchaseFinishedListener() {

			@Override
			public void onIabPurchaseFinished(IabResult result, Purchase info) {

				if (result.isSuccess()) {
					List<Order> orders = new ArrayList<Order>();
					orders.add(new Order(info.getOriginalJson(), inventoryItem.numCoins));

					UIConnectionWrapper.addCoinsForAnOrder(callback, GameLoop.USER.handle, orders);
				}
			}
		});
	}

	public void loadInventory(final Inventory inventory, final UIConnectionResultCallback<Inventory> callback) {

		List<String> skuDetail = new ArrayList<String>();

		for (InventoryItem item : inventory.inventory) {
			skuDetail.add(item.sku);
		}

		mHelper.queryInventoryAsync(true, skuDetail, new QueryInventoryFinishedListener() {

			@Override
			public void onQueryInventoryFinished(IabResult result, com.xxx.galcon.inappbilling.util.Inventory inv) {

				if (result.isFailure()) {
					complain("Unable to load inventory from Play Store.");
					return;
				}

				List<InventoryItem> mappedInventoryItems = new ArrayList<InventoryItem>();

				for (InventoryItem item : inventory.inventory) {
					SkuDetails detail = inv.getSkuDetails(item.sku);
					InventoryItem combinedItem = new InventoryItem(detail.getSku(), detail.getPrice(), detail
							.getTitle(), item.numCoins);
					mappedInventoryItems.add(combinedItem);
				}

				Inventory inventory = new Inventory();
				inventory.inventory = mappedInventoryItems;
				callback.onConnectionResult(inventory);
			}
		});
	}

	public void consumeOrders(final List<Order> consumedOrders) {
		List<Purchase> purchaseOrders = convertOrdersToPurchase(consumedOrders);

		mHelper.consumeAsync(purchaseOrders, new IabHelper.OnConsumeMultiFinishedListener() {
			@Override
			public void onConsumeMultiFinished(List<Purchase> purchases, List<IabResult> results) {
				gameAction.deleteConsumedOrders(new UIConnectionResultCallback<Player>() {
					@Override
					public void onConnectionResult(Player result) {
					}

					@Override
					public void onConnectionError(String msg) {
						// TODO Auto-generated method stub
					}
				}, GameLoop.USER.handle, consumedOrders);
			}
		});
	}

	private List<Purchase> convertOrdersToPurchase(List<Order> consumedOrders) {

		List<Purchase> purchaseOrders = new ArrayList<Purchase>();
		for (Order order : consumedOrders) {
			Purchase purchase = new Purchase(IabHelper.ITEM_TYPE_INAPP, order, "");
			purchaseOrders.add(purchase);
		}
		return purchaseOrders;
	}

	@Override
	protected void onStart() {
		super.onStart();

	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onActivityResult(int request, int response, Intent data) {
		if (request == SIGN_IN_ACTIVITY_RESULT_CODE) {
			socialAction.onActivityResult(response);
		} else if (mHelper != null && !mHelper.handleActivityResult(request, response, data)) {
			super.onActivityResult(request, response, data);
		}
	}
}
