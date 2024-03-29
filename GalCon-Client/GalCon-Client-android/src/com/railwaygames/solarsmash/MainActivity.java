package com.railwaygames.solarsmash;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.crashlytics.android.Crashlytics;
import com.facebook.Session;
import com.jirbo.adcolony.AdColony;
import com.jirbo.adcolony.AdColonyAd;
import com.jirbo.adcolony.AdColonyAdAvailabilityListener;
import com.jirbo.adcolony.AdColonyAdListener;
import com.jirbo.adcolony.AdColonyVideoAd;
import com.railwaygames.solarsmash.http.InAppBillingAction.Callback;
import com.railwaygames.solarsmash.http.SocialAction;
import com.railwaygames.solarsmash.http.UIConnectionResultCallback;
import com.railwaygames.solarsmash.inappbilling.util.IabHelper;
import com.railwaygames.solarsmash.inappbilling.util.IabHelper.QueryInventoryFinishedListener;
import com.railwaygames.solarsmash.inappbilling.util.IabResult;
import com.railwaygames.solarsmash.inappbilling.util.Purchase;
import com.railwaygames.solarsmash.inappbilling.util.SkuDetails;
import com.railwaygames.solarsmash.model.Inventory;
import com.railwaygames.solarsmash.model.InventoryItem;
import com.railwaygames.solarsmash.model.Order;
import com.railwaygames.solarsmash.screen.widget.ShaderTextField;

public class MainActivity extends AndroidApplication implements AdColonyAdListener, AdColonyAdAvailabilityListener {
	public static final String LOG_NAME = "GalCon";
	public static final int GOOGLE_PLUS_SIGN_IN_ACTIVITY_RESULT_CODE = 57029;
	public static final int FACEBOOK_SIGN_IN_ACTIVITY_RESULT_CODE = 57030;
	public static final int GOOGLE_PLUS_PUBLISH_ACTIVITY_RESULT_CODE = 57031;

	protected String mDebugTag = "MainActivity";
	protected boolean mDebugLog = true;

	final static String APP_ID = "appae5819628c4f43b5b7f9f9";
	final static String ZONE_ID = "vz510571feab514a8fa6";

	final static String ENCODED_APPLICATION_ID = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAk0bjlbWS+5lbngxfWgGR8Yh+ZKKvEWi2FEfWCxSNN+9ULjfOWL2O1qLcW07hL99l4a4u3eRwQejeaN63Dzcq1y5k9SSZCSVK6Qf+jpJEoduWyXa3m+GdxXRc9xuV+LjnzUMtQubtfYNbWK8oWFs3EOAl93FCEOOt+qz4If0cuMlX6Ew+HcU2lmCOmzS8F8bVyVQjGdMW52uCCVrwy6uLZNyl3NlHe1BGOlv6vyIiHUpPRhPrmQC9jPq+0HqOjQZHfQ0/ehdBTWhTfpSwRPfpJaSNciEx1feE9ChC+4qnu/GWCuQ70KTTcmVeImMx876VUurq/75eYTyO30Vr3H/JwQIDAQAB";

	private AndroidGameAction gameAction;
	private SocialAction socialAction;
	private AndroidInAppBillingAction inAppBillingAction;
	private GameLoop gameLoop;

	private IabHelper mHelper = null;
	private AlarmManager alarmMgr;

	private boolean adsEnabled = false;

	public MainActivity() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Crashlytics.start(this);

		AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
		cfg.hideStatusBar = true;
		cfg.useImmersiveMode = true;

		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

		socialAction = new AndroidSocialAction(this);
		gameAction = new AndroidGameAction(this, socialAction, connectivityManager);
		inAppBillingAction = new AndroidInAppBillingAction(this);

		gameLoop = new GameLoop(gameAction, socialAction, inAppBillingAction,
				new ShaderTextField.DefaultOnscreenKeyboard());
		initialize(gameLoop, cfg);

		PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, new Intent(
				"com.railwaygames.solarsmash.service.PingService"), 0);

		alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarmMgr.cancel(alarmIntent);
		alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 5 * 60 * 1000, 5 * 60 * 1000, alarmIntent);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		useImmersiveMode(true);

		gameLoop.refresh();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mHelper != null) {
			mHelper.dispose();
		}
		mHelper = null;
	}

	public void setupInAppBilling(final Callback callback) {
		String base64EncodedPublicKey = ENCODED_APPLICATION_ID;

		// Create the helper, passing it our context and the public key to
		// verify signatures with
		if (mHelper == null) {
			mHelper = new IabHelper(this, base64EncodedPublicKey);
			mHelper.enableDebugLogging(false);
		}

		mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
			public void onIabSetupFinished(IabResult result) {
				if (result.isSuccess()) {
					callback.onSuccess("");
				} else {
					Crashlytics.log(Log.ERROR, LOG_NAME, "Could not setup in-app billing: [" + result.getResponse()
							+ ", " + result.getMessage() + "]");
					if (result.getResponse() == IabHelper.IABHELPER_REMOTE_EXCEPTION) {
						callback.onFailure("retry");
					} else {
						callback.onFailure("In app billing not setup");
					}
				}
			}
		});
	}

	private void setupAdColony() {
		AdColony.configure(this, "version:0.9.8,store:google", APP_ID, ZONE_ID);
		AdColony.addAdAvailabilityListener(this);

		if (!AdColony.isTablet()) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		adsEnabled = true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (adsEnabled) {
			AdColony.pause();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		useImmersiveMode(true);
		if (adsEnabled) {
			AdColony.resume(this);
		}
	}

	public void displayAd() {
		if (adsEnabled) {
			AdColonyVideoAd ad = new AdColonyVideoAd(ZONE_ID).withListener(this);
			ad.show();
		}
	}

	public void purchaseCoins(final InventoryItem inventoryItem, final UIConnectionResultCallback<List<Order>> callback) {
		if (!mHelper.isSetupDone()) {
			inAppBillingAction.setup(new Callback() {

				@Override
				public void onSuccess(String msg) {
					purchaseCoins(inventoryItem, callback);
				}

				@Override
				public void onFailure(String msg) {
					callback.onConnectionError(msg);
				}
			});
		} else {
			mHelper.launchPurchaseFlow(this, inventoryItem.sku, 1001, new IabHelper.OnIabPurchaseFinishedListener() {

				@Override
				public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
					if (result.isSuccess()) {
						List<Order> orders = new ArrayList<Order>();
						orders.add(purchaseToOrder(purchase));
						callback.onConnectionResult(orders);
					} else if (result.getResponse() == IabHelper.IABHELPER_USER_CANCELLED) {
						callback.onConnectionResult(null);
					} else {
						callback.onConnectionError("Could not complete purchase");
					}
				}
			});
		}
	}

	public void loadStoreInventory(final Inventory inventoryResult, final UIConnectionResultCallback<Inventory> callback) {
		Gdx.app.log("GameAction", "Loading inventory");
		if (!mHelper.isSetupDone()) {
			inAppBillingAction.setup(new Callback() {

				@Override
				public void onSuccess(String msg) {
					loadStoreInventory(inventoryResult, callback);
				}

				@Override
				public void onFailure(String msg) {
					callback.onConnectionError(msg);
				}
			});
		} else {
			try {
				mHelper.queryInventoryAsync(true, inventoryResult.skus(), new QueryInventoryFinishedListener() {

					@Override
					public void onQueryInventoryFinished(IabResult result,
							com.railwaygames.solarsmash.inappbilling.util.Inventory inv) {
						if (result.isFailure()) {
							callback.onConnectionError("Unable to load inventory from Play Store.");
							return;
						}

						List<InventoryItem> mappedInventoryItems = new ArrayList<InventoryItem>();

						for (InventoryItem item : inventoryResult.inventory) {
							SkuDetails detail = inv.getSkuDetails(item.sku);
							if (detail == null) {
								Crashlytics.log(Log.WARN, "PlayStore", "No inventory item found for: " + item.sku);
								continue;
							}
							InventoryItem combinedItem = new InventoryItem(detail.getSku(), detail.getPrice(), detail
									.getTitle(), item.numCoins);

							if (inv.hasPurchase(detail.getSku())) {
								final Purchase purchase = inv.getPurchase(detail.getSku());
								combinedItem.unfulfilledOrder = new ArrayList<Order>() {
									{
										add(purchaseToOrder(purchase));
									}
								};
							}

							mappedInventoryItems.add(combinedItem);
						}

						Inventory inventory = new Inventory();
						inventory.inventory = mappedInventoryItems;
						callback.onConnectionResult(inventory);
					}
				});
			} catch (IllegalStateException e) {
				callback.onConnectionError("Could not get items");
			}
		}
	}

	public void consumeOrders(final List<Order> consumedOrders, final Callback callback) {
		List<Purchase> purchaseOrders = convertOrdersToPurchase(consumedOrders);

		if (!mHelper.isSetupDone()) {
			inAppBillingAction.setup(new Callback() {

				@Override
				public void onSuccess(String msg) {
					consumeOrders(consumedOrders, callback);
				}

				@Override
				public void onFailure(String msg) {
					callback.onFailure(msg);
				}
			});
		} else {
			mHelper.consumeAsync(purchaseOrders, new IabHelper.OnConsumeMultiFinishedListener() {
				@Override
				public void onConsumeMultiFinished(List<Purchase> purchases, List<IabResult> results) {
					for (IabResult result : results) {
						if (result.isFailure()) {
							callback.onFailure(result.getMessage());
							return;
						}
					}
					callback.onSuccess("");
				}
			});
		}
	}

	private List<Purchase> convertOrdersToPurchase(List<Order> consumedOrders) {
		List<Purchase> purchaseOrders = new ArrayList<Purchase>();
		for (Order order : consumedOrders) {
			try {
				Purchase purchase = new Purchase(IabHelper.ITEM_TYPE_INAPP, order.asJson().toString(), "");
				purchaseOrders.add(purchase);
			} catch (JSONException e) {
				Crashlytics.log(Log.ERROR, LOG_NAME, "Could not convert order to purchase: " + e.getMessage());
			}
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
		if (request == GOOGLE_PLUS_SIGN_IN_ACTIVITY_RESULT_CODE) {
			socialAction.onActivityResult(response);
		} else if (request == FACEBOOK_SIGN_IN_ACTIVITY_RESULT_CODE) {
			Session.getActiveSession().onActivityResult(this, MainActivity.FACEBOOK_SIGN_IN_ACTIVITY_RESULT_CODE,
					response, data);
			socialAction.onActivityResult(response);
		} else if (request == GOOGLE_PLUS_PUBLISH_ACTIVITY_RESULT_CODE) {
			socialAction.onActivityResult(request);
		} else if (mHelper != null && !mHelper.handleActivityResult(request, response, data)) {
			super.onActivityResult(request, response, data);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Session session = Session.getActiveSession();
		Session.saveSession(session, outState);
	}

	private Order purchaseToOrder(Purchase purchase) {
		Order order = new Order();

		order.orderId = purchase.getOrderId();
		order.packageName = purchase.getPackageName();
		order.purchaseTime = Long.toString(purchase.getPurchaseTime());
		order.purchaseState = Integer.toString(purchase.getPurchaseState());
		order.developerPayload = purchase.getDeveloperPayload();
		order.token = purchase.getToken();
		order.productId = purchase.getSku();
		order.platform = "android";

		return order;
	}

	@Override
	public void onAdColonyAdAvailabilityChange(boolean arg0, String arg1) {

	}

	@Override
	public void onAdColonyAdAttemptFinished(AdColonyAd arg0) {

	}

	@Override
	public void onAdColonyAdStarted(AdColonyAd arg0) {

	}

	public void shouldEnableAds(boolean enable) {
		if (enable && !adsEnabled) {
			setupAdColony();
		}
	}
}
