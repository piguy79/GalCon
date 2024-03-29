package com.railwaygames.solarsmash;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.robovm.apple.foundation.Foundation;
import org.robovm.apple.foundation.NSArray;
import org.robovm.apple.foundation.NSBundle;
import org.robovm.apple.foundation.NSData;
import org.robovm.apple.foundation.NSDataBase64EncodingOptions;
import org.robovm.apple.foundation.NSError;
import org.robovm.apple.foundation.NSNumberFormatter;
import org.robovm.apple.foundation.NSNumberFormatterBehavior;
import org.robovm.apple.foundation.NSNumberFormatterStyle;
import org.robovm.apple.foundation.NSSet;
import org.robovm.apple.foundation.NSString;
import org.robovm.apple.foundation.NSURL;
import org.robovm.apple.storekit.SKPayment;
import org.robovm.apple.storekit.SKPaymentQueue;
import org.robovm.apple.storekit.SKPaymentTransaction;
import org.robovm.apple.storekit.SKPaymentTransactionObserverAdapter;
import org.robovm.apple.storekit.SKPaymentTransactionState;
import org.robovm.apple.storekit.SKProduct;
import org.robovm.apple.storekit.SKProductsRequest;
import org.robovm.apple.storekit.SKProductsRequestDelegateAdapter;
import org.robovm.apple.storekit.SKProductsResponse;
import org.robovm.apple.storekit.SKRequest;

import com.railwaygames.solarsmash.http.InAppBillingAction;
import com.railwaygames.solarsmash.http.UIConnectionResultCallback;
import com.railwaygames.solarsmash.model.Inventory;
import com.railwaygames.solarsmash.model.InventoryItem;
import com.railwaygames.solarsmash.model.Order;

public class IOSInAppBillingAction implements InAppBillingAction {
	private static final String TAG = "IOSInAppBillingAction: ";

	private UIConnectionResultCallback<List<Order>> purchaseCallback;
	private Callback consumeCallback;

	private Map<String, SKProduct> skuToProduct = new HashMap<String, SKProduct>();

	public IOSInAppBillingAction() {
		SKPaymentTransactionObserverAdapter observer = new SKPaymentTransactionObserverAdapter() {
			@Override
			public void updatedTransactions(SKPaymentQueue queue, NSArray<SKPaymentTransaction> transactions) {
				Foundation.log(TAG + "Size: " + transactions.size());

				for (SKPaymentTransaction transaction : transactions) {
					switch (transaction.getTransactionState()) {
					case Purchasing:
						Foundation.log(TAG + "PURCHASING: " + transaction.getPayment().getProductIdentifier());
						break;
					case Purchased:
						Foundation.log(TAG + "PURCHASED: " + transaction.getPayment().getProductIdentifier());
						List<Order> orders = new ArrayList<Order>();
						orders.add(transactionToOrder(transaction));
						if (purchaseCallback == null) {
							return;
						}
						purchaseCallback.onConnectionResult(orders);
						break;
					case Failed:
						Foundation.log(TAG + "FAILED: " + transaction.getError().getLocalizedDescription());
						if (purchaseCallback == null) {
							return;
						}
						purchaseCallback.onConnectionError(transaction.getError().getLocalizedDescription());
						break;
					case Restored:
						Foundation.log(TAG + "RESTORED: " + transaction.description());
						break;
					default:
						break;
					}
				}
			}

			@Override
			public void removedTransactions(SKPaymentQueue queue, NSArray<SKPaymentTransaction> transactions) {
				for (SKPaymentTransaction transaction : transactions) {
					Foundation.log(TAG + "REMOVING: " + transaction.getPayment().getProductIdentifier());
				}

				if (consumeCallback != null) {
					consumeCallback.onSuccess("");
				}
			}
		};

		SKPaymentQueue.getDefaultQueue().addStrongRef(observer);
		SKPaymentQueue.getDefaultQueue().addTransactionObserver(observer);
	}

	@Override
	public void loadInventory(final Inventory serverInventory, final UIConnectionResultCallback<Inventory> callback) {
		List<String> productIds = serverInventory.skus();

		List<NSString> products = new ArrayList<NSString>();
		for (int i = 0; i < productIds.size(); i++) {
			products.add(new NSString(productIds.get(i)));
		}

		SKProductsRequest productsRequest = new SKProductsRequest(new NSSet(products));
		productsRequest.setDelegate(new SKProductsRequestDelegateAdapter() {
			@Override
			public void didReceiveResponse(SKProductsRequest request, SKProductsResponse response) {
				Foundation.log(TAG + "IOS INVENTORY LOAD");
				NSArray<SKProduct> products = response.getProducts();

				skuToProduct = new HashMap<String, SKProduct>();
				for (SKProduct product : products) {
					skuToProduct.put(product.getProductIdentifier(), product);
				}

				List<InventoryItem> mappedInventoryItems = new ArrayList<InventoryItem>();

				Map<String, List<SKPaymentTransaction>> skuToTransaction = new HashMap<String, List<SKPaymentTransaction>>();
				NSArray<SKPaymentTransaction> unconsumedTransactions = SKPaymentQueue.getDefaultQueue()
						.getTransactions();

				for (SKPaymentTransaction unconsumedTransaction : unconsumedTransactions) {
					Foundation.log(TAG + "UNCONSUMED: " + unconsumedTransaction.getPayment().getProductIdentifier());
					if (unconsumedTransaction.getTransactionState() == SKPaymentTransactionState.Failed) {
						Foundation.log(TAG + "FINISHING FAILED: "
								+ unconsumedTransaction.getPayment().getProductIdentifier());
						SKPaymentQueue.getDefaultQueue().finishTransaction(unconsumedTransaction);

						continue;
					}

					String productId = unconsumedTransaction.getPayment().getProductIdentifier();

					List<SKPaymentTransaction> transactions;
					if (skuToTransaction.containsKey(productId)) {
						transactions = skuToTransaction.get(productId);
					} else {
						transactions = new ArrayList<SKPaymentTransaction>();
						skuToTransaction.put(productId, transactions);
					}
					transactions.add(unconsumedTransaction);
				}

				NSNumberFormatter formatter = new NSNumberFormatter();
				formatter.setFormatterBehavior(NSNumberFormatterBehavior._10_4);
				formatter.setNumberStyle(NSNumberFormatterStyle.Currency);

				for (InventoryItem serverItem : serverInventory.inventory) {
					SKProduct iosProduct = skuToProduct.get(serverItem.sku);
					if (iosProduct == null) {
						Foundation.log(TAG + "SKU not found in ios store: " + serverItem.sku);
						continue;
					}

					formatter.setLocale(iosProduct.getPriceLocale());
					String price = formatter.stringFromNumber$(iosProduct.getPrice());

					InventoryItem combinedItem = new InventoryItem(iosProduct.getProductIdentifier(), price, iosProduct
							.getLocalizedTitle(), serverItem.numCoins);

					if (skuToTransaction.containsKey(iosProduct.getProductIdentifier())) {
						Foundation.log(TAG + "Adding unfulfilled order to inventory: " + serverItem.sku);

						List<SKPaymentTransaction> transactions = skuToTransaction.get(iosProduct
								.getProductIdentifier());
						List<Order> orders = new ArrayList<Order>();

						for (SKPaymentTransaction transaction : transactions) {
							orders.add(transactionToOrder(transaction));
						}

						combinedItem.unfulfilledOrder = orders;
					}

					mappedInventoryItems.add(combinedItem);
				}

				Inventory inventory = new Inventory();
				inventory.inventory = mappedInventoryItems;
				callback.onConnectionResult(inventory);
			}

			@Override
			public void didFail(SKRequest request, NSError error) {
				Foundation.log(TAG + "INVENTORY FAIL: " + error.description());
				callback.onConnectionError(error.description());
			}
		});
		Foundation.log(TAG + "Calling start on in app billing.");
		productsRequest.start();
	}

	@Override
	public void setup(Callback callback) {
		callback.onSuccess("");
	}

	@Override
	public void consumeOrders(List<Order> orders, Callback callback) {
		String ids = "";
		NSArray<SKPaymentTransaction> pendingTransactions = SKPaymentQueue.getDefaultQueue().getTransactions();
		for (SKPaymentTransaction transaction : pendingTransactions) {
			ids += transaction.getPayment().getProductIdentifier() + " ";
		}

		Foundation.log(TAG + "CONSUMING: " + ids);
		this.consumeCallback = callback;

		if (ids.trim().isEmpty()) {
			consumeCallback.onSuccess("");
			return;
		}

		for (SKPaymentTransaction transaction : pendingTransactions) {
			SKPaymentQueue.getDefaultQueue().finishTransaction(transaction);
		}
	}

	@Override
	public void purchaseCoins(InventoryItem inventoryItem, UIConnectionResultCallback<List<Order>> callback) {
		Foundation.log("PURCHASE: " + inventoryItem.sku);
		Foundation.log("CANMAKEPAYMENTS: " + SKPaymentQueue.canMakePayments());
		this.purchaseCallback = callback;
		SKPayment payment = SKPayment.createFromProduct(skuToProduct.get(inventoryItem.sku));
		SKPaymentQueue.getDefaultQueue().addPayment(payment);
	}

	private Order transactionToOrder(SKPaymentTransaction transaction) {
		Foundation.log(TAG + "Converting transaction to order");
		Order order = new Order();

		NSURL receiptURL = NSBundle.getMainBundle().getAppStoreReceiptURL();
		NSData dataReceipt = (NSData) NSData.read(receiptURL);
		String receipt = dataReceipt.toBase64EncodedString(new NSDataBase64EncodingOptions(0L));

		order.orderId = transaction.getTransactionIdentifier();
		order.packageName = "";
		if (transaction.getTransactionDate() != null) {
			order.purchaseTime = Double.toString(transaction.getTransactionDate().getTimeIntervalSince1970());
		}
		order.purchaseState = transaction.getTransactionState().name();
		order.developerPayload = "";
		order.token = receipt;
		order.productId = transaction.getPayment().getProductIdentifier();
		order.platform = "ios";

		return order;
	}
}
