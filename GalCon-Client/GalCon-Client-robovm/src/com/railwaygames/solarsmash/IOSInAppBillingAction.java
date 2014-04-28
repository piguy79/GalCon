package com.railwaygames.solarsmash;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.robovm.apple.foundation.NSArray;
import org.robovm.apple.foundation.NSError;
import org.robovm.apple.foundation.NSSet;
import org.robovm.apple.foundation.NSString;
import org.robovm.apple.storekit.SKPaymentTransaction;
import org.robovm.apple.storekit.SKProduct;
import org.robovm.apple.storekit.SKProductsRequest;
import org.robovm.apple.storekit.SKProductsRequestDelegate;
import org.robovm.apple.storekit.SKProductsResponse;
import org.robovm.apple.storekit.SKRequest;
import org.robovm.bindings.inapppurchase.InAppPurchaseListener;
import org.robovm.bindings.inapppurchase.InAppPurchaseManager;

import com.badlogic.gdx.Gdx;
import com.railwaygames.solarsmash.http.InAppBillingAction;
import com.railwaygames.solarsmash.http.UIConnectionResultCallback;
import com.railwaygames.solarsmash.model.Inventory;
import com.railwaygames.solarsmash.model.InventoryItem;
import com.railwaygames.solarsmash.model.Order;

public class IOSInAppBillingAction implements InAppBillingAction {
	private static final String TAG = "IOSInAppBillingAction";
	private InAppPurchaseManager iapManager;

	public IOSInAppBillingAction() {
		iapManager = new InAppPurchaseManager(new InAppPurchaseListener() {
			@Override
			public void productsReceived(SKProduct[] products) {
				Map<String, SKProduct> appStoreProducts = new HashMap<String, SKProduct>();

				for (int i = 0; i < products.length; i++) {
					appStoreProducts.put(products[i].getProductIdentifier().toString(), products[i]);
				}
			}

			@Override
			public void productsRequestFailed(SKRequest request, NSError error) {
				// Something went wrong. Possibly no Internet connection.
			}

			@Override
			public void transactionCompleted(SKPaymentTransaction transaction) {
				// Purchase successfully completed.
				// Get the product identifier and award the product to the user.
				String productId = transaction.getPayment().getProductIdentifier().toString();
				if (productId.equals("com.business.game.consumable")) {
					// awardProduct1();
				} else if (productId.equals("com.business.game.nonconsumable")) {
					// awardProduct2();
				}
			}

			@Override
			public void transactionFailed(SKPaymentTransaction transaction) {
				// Something went wrong. Possibly no Internet connection.
			}

			@Override
			public void transcationRestored(SKPaymentTransaction transaction) {
				// Purchase successfully restored.
				// Get the product identifier and award the product to the user.
				// This is only useful for non-consumable products.
				String productId = transaction.getPayment().getProductIdentifier().toString();
				if (productId.equals("com.business.game.nonconsumable")) {
					// awardProduct2();
				}
			}
		});
	}

	@Override
	public void loadInventory(final Inventory serverInventory, final UIConnectionResultCallback<Inventory> callback) {
		List<String> productIds = serverInventory.skus();

		List<NSString> products = new ArrayList<NSString>();
		for (int i = 0; i < productIds.size(); i++) {
			products.add(new NSString(productIds.get(i)));
		}

		Gdx.app.log(TAG, "SERVER PRODUCTS: " + products.toString());
		SKProductsRequest productsRequest = new SKProductsRequest(new NSSet(products));
		productsRequest.setDelegate(new SKProductsRequestDelegate.Adapter() {
			@Override
			public void receivedResponse(SKProductsRequest request, SKProductsResponse response) {
				NSArray<SKProduct> products = response.getProducts();

				Map<String, SKProduct> productMap = new HashMap<String, SKProduct>();
				Gdx.app.log(TAG, "Number of ios products: " + products.size());
				for (SKProduct product : products) {
					Gdx.app.log(TAG, "Product id: " + product.getProductIdentifier());
					productMap.put(product.getProductIdentifier(), product);
				}

				List<InventoryItem> mappedInventoryItems = new ArrayList<InventoryItem>();

				for (InventoryItem serverItem : serverInventory.inventory) {
					SKProduct iosProduct = productMap.get(serverItem.sku);
					if (iosProduct == null) {
						Gdx.app.log(TAG, "SKU not found in ios store: " + serverItem.sku);
						continue;
					}

					InventoryItem combinedItem = new InventoryItem(iosProduct.getProductIdentifier(), iosProduct
							.getPrice().stringValue(), iosProduct.getLocalizedTitle(), serverItem.numCoins);

					// TODO: this currently does not load unconsumed items

					mappedInventoryItems.add(combinedItem);
				}

				Inventory inventory = new Inventory();
				inventory.inventory = mappedInventoryItems;
				callback.onConnectionResult(inventory);
			}

			@Override
			public void requestFailed(SKRequest request, NSError error) {
				callback.onConnectionError(error.description());
			}
		});
		productsRequest.start();
	}

	@Override
	public void setup(Callback callback) {
		callback.onSuccess("");
	}

	@Override
	public void consumeOrders(List<Order> orders, Callback callback) {
		callback.onSuccess("");
	}

	@Override
	public void purchaseCoins(InventoryItem inventoryItem, UIConnectionResultCallback<List<Order>> callback) {
		callback.onConnectionResult(null);
	}
}
