package com.xxx.galcon.screen.signin;

import static com.xxx.galcon.Util.createShader;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.PartialScreenFeedback;
import com.xxx.galcon.config.Configuration;
import com.xxx.galcon.http.GameAction;
import com.xxx.galcon.http.InAppBillingAction;
import com.xxx.galcon.http.InAppBillingAction.Callback;
import com.xxx.galcon.http.UIConnectionResultCallback;
import com.xxx.galcon.model.Inventory;
import com.xxx.galcon.model.InventoryItem;
import com.xxx.galcon.model.Order;
import com.xxx.galcon.model.Player;
import com.xxx.galcon.screen.Action;
import com.xxx.galcon.screen.overlay.DismissableOverlay;
import com.xxx.galcon.screen.overlay.Overlay;
import com.xxx.galcon.screen.overlay.TextOverlay;
import com.xxx.galcon.screen.widget.WaitImageButton;

/**
 * Generic loading screen. Load anything that is needed prior to the main menu.
 */
public class LoadingScreen implements PartialScreenFeedback {

	private WaitImageButton waitImage;
	private Skin skin;
	private Stage stage;

	private InAppBillingAction inAppBillingAction;
	private GameAction gameAction;
	private String returnResult = null;

	private ShaderProgram fontShader;
	private TextureAtlas menusAtlas;

	private List<Order> ordersToConsume = null;

	public LoadingScreen(Skin skin, GameAction gameAction, InAppBillingAction inAppBillingAction,
			AssetManager assetManager) {
		this.inAppBillingAction = inAppBillingAction;
		this.gameAction = gameAction;
		this.skin = skin;

		menusAtlas = assetManager.get("data/images/menus.atlas", TextureAtlas.class);
		fontShader = createShader("data/shaders/font-vs.glsl", "data/shaders/font-fs.glsl");
	}

	@Override
	public void show(final Stage stage, float width, float height) {
		this.stage = stage;

		waitImage = new WaitImageButton(skin);
		float buttonWidth = .25f * (float) width;
		waitImage.setWidth(buttonWidth);
		waitImage.setHeight(buttonWidth);
		waitImage.setX(width / 2 - buttonWidth / 2);
		waitImage.setY(height / 2 - buttonWidth / 2);
		stage.addActor(waitImage);

		waitImage.start();

		gameAction.findConfigByType(configCallback, "app");
	}

	private UIConnectionResultCallback<Configuration> configCallback = new UIConnectionResultCallback<Configuration>() {
		@Override
		public void onConnectionResult(Configuration result) {
			GameLoop.CONFIG = result;
			inAppBillingAction.setup(setupCallback);
		}

		@Override
		public void onConnectionError(String msg) {
			final Overlay ovrlay = new DismissableOverlay(menusAtlas, new TextOverlay(
					"Could not connect.\n\nTouch to retry", menusAtlas, skin, fontShader), new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					gameAction.findConfigByType(configCallback, "app");
				}
			});
			stage.addActor(ovrlay);
		}
	};

	private Callback setupCallback = new Callback() {

		@Override
		public void onSuccess(String msg) {
			consumeAnyPurchasedOrders();
		}

		@Override
		public void onFailure(String msg) {
			waitImage.stop();
			if (msg.equals("retry")) {
				final Overlay ovrlay = new DismissableOverlay(menusAtlas, new TextOverlay(
						"Could not connect.\n\nTouch to retry", menusAtlas, skin, fontShader), new ClickListener() {
					@Override
					public void clicked(InputEvent event, float x, float y) {
						inAppBillingAction.setup(setupCallback);
					}
				});

				stage.addActor(ovrlay);
			} else {
				final Overlay ovrlay = new DismissableOverlay(menusAtlas, new TextOverlay(
						"In-app purchases\nare not present\nand disabled.\n\nTouch to continue.", menusAtlas, skin,
						fontShader), new ClickListener() {
					@Override
					public void clicked(InputEvent event, float x, float y) {
						consumeAnyPurchasedOrders();
					}
				});
				stage.addActor(ovrlay);
			}
		}
	};

	@Override
	public void hide() {
		// TODO Auto-generated method stub

	}

	@Override
	public void render(float delta) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object getRenderResult() {
		return returnResult;
	}

	@Override
	public void resetState() {
		returnResult = null;
	}

	@Override
	public boolean hideTitleArea() {
		return false;
	}

	private void consumeAnyPurchasedOrders() {
		gameAction.loadAvailableInventory(new UIConnectionResultCallback<Inventory>() {

			@Override
			public void onConnectionResult(Inventory inventoryResult) {
				inAppBillingAction.loadInventory(inventoryResult, new UIConnectionResultCallback<Inventory>() {

					@Override
					public void onConnectionResult(final Inventory inventory) {

						List<Order> orders = new ArrayList<Order>();
						for (InventoryItem item : inventory.inventory) {
							if (item.unfulfilledOrder != null) {
								orders.add(item.unfulfilledOrder);
							}
						}

						if (!orders.isEmpty()) {
							ordersToConsume = orders;
							gameAction.addCoinsForAnOrder(playerCallback, GameLoop.USER.handle, orders);
						} else {
							doneLoad();
						}
					}

					@Override
					public void onConnectionError(String msg) {
						retryAddCoins(msg);
					}
				});
			}

			@Override
			public void onConnectionError(String msg) {
				retryAddCoins(msg);
			}
		});
	}

	private void doneLoad() {
		waitImage.stop();
		ordersToConsume = null;
		returnResult = Action.DONE;
	}

	private UIConnectionResultCallback<Player> playerCallback = new UIConnectionResultCallback<Player>() {
		@Override
		public void onConnectionResult(Player player) {
			GameLoop.USER = player;
			doneLoad();
		};

		@Override
		public void onConnectionError(String msg) {
			retryAddCoins(msg);
		}
	};

	private void retryAddCoins(String msg) {
		waitImage.stop();

		final Overlay ovrlay = new DismissableOverlay(menusAtlas, new TextOverlay(msg, menusAtlas, skin, fontShader),
				new ClickListener() {
					@Override
					public void clicked(InputEvent event, float x, float y) {
						gameAction.addCoinsForAnOrder(playerCallback, GameLoop.USER.handle, ordersToConsume);
					}
				});

		stage.addActor(ovrlay);
	}
}