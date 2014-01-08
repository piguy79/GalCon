package com.xxx.galcon.screen;

import static com.xxx.galcon.Util.createShader;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.joda.time.DateTime;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.jirbo.adcolony.AdColonyVideoListener;
import com.xxx.galcon.Constants;
import com.xxx.galcon.ExternalActionWrapper;
import com.xxx.galcon.GameLoop;
import com.xxx.galcon.PartialScreenFeedback;
import com.xxx.galcon.UIConnectionWrapper;
import com.xxx.galcon.http.SetPlayerResultHandler;
import com.xxx.galcon.http.UIConnectionResultCallback;
import com.xxx.galcon.model.Inventory;
import com.xxx.galcon.model.InventoryItem;
import com.xxx.galcon.model.Player;
import com.xxx.galcon.screen.hud.HeaderHud;
import com.xxx.galcon.screen.overlay.DismissableOverlay;
import com.xxx.galcon.screen.overlay.Overlay;
import com.xxx.galcon.screen.overlay.TextOverlay;
import com.xxx.galcon.screen.widget.ScrollList;
import com.xxx.galcon.screen.widget.ShaderLabel;
import com.xxx.galcon.screen.widget.ShaderTextButton;
import com.xxx.galcon.screen.widget.WaitImageButton;

public class NoMoreCoinsDialog implements PartialScreenFeedback, UIConnectionResultCallback<Player> {
	private Stage stage;
	private Skin skin;
	private Array<Actor> actors = new Array<Actor>();

	private ShaderProgram fontShader;

	private ImageButton watchAd;
	private ImageButton timeRemaining;
	private ShaderTextButton timeRemainingText;
	private Group coinGroup;
	private ShaderLabel coinText;
	private ImageButton backButton;
	protected WaitImageButton waitImage;

	private Object returnValue;

	private TextureAtlas menusAtlas;

	private Inventory inventoryResult;

	public NoMoreCoinsDialog(Skin skin, AssetManager assetManager) {
		super();
		this.skin = skin;

		menusAtlas = assetManager.get("data/images/menus.atlas", TextureAtlas.class);
		fontShader = createShader("data/shaders/font-vs.glsl", "data/shaders/font-fs.glsl");
	}

	private void createBackButton(final Stage stage, final float width, final float height) {
		int buttonHeight = (int) (Gdx.graphics.getHeight() * (HeaderHud.HEADER_HEIGHT_RATIO * 0.88f));
		backButton = new ImageButton(skin, "backButton");
		backButton.setX(10);
		backButton.setY(height - buttonHeight - 5);
		backButton.setWidth(buttonHeight);
		backButton.setHeight(buttonHeight);

		backButton.addListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				startHideSequence(Action.BACK);
			}
		});
		actors.add(backButton);
		stage.addActor(backButton);
	}

	private void createLayout(Stage stage, Inventory stock) {
		final float height = Gdx.graphics.getHeight();
		final float width = Gdx.graphics.getWidth();

		if (GameLoop.USER.usedCoins != -1) {
			timeRemaining = new ImageButton(skin, Constants.UI.GRAY_BUTTON);
			timeRemainingText = new ShaderTextButton(fontShader, findTimeRemaining(), skin,
					Constants.UI.GRAY_BUTTON_TEXT);
			timeRemaining.setBounds(0, height * 0.8f, width, height * 0.1f);
			timeRemainingText.setX(0);
			timeRemainingText.setY(timeRemaining.getHeight() * 0.35f);
			timeRemainingText.setWidth(width);
			timeRemaining.addActor(timeRemainingText);
			stage.addActor(timeRemaining);
			actors.add(timeRemaining);
		} else {
			addCoinImageGroup();
		}

		Actor inAppBillingTable = createInAppBillingButtons(stock.inventory);
		actors.add(inAppBillingTable);
		stage.addActor(inAppBillingTable);

		createBackButton(stage, width, height);
	}

	private String coinString(Integer coins) {
		if (coins == null) {
			return "";
		}
		return coins == 1 ? "Coin" : "Coins";
	}

	private Actor createInAppBillingButtons(List<InventoryItem> inventory) {
		ScrollList<InventoryItem> scrollList = new ScrollList<InventoryItem>(skin) {
			@Override
			public void buildCell(InventoryItem item, Group group) {
				float width = group.getWidth();
				float rowHeight = group.getHeight();

				String itemText = item.price + "  " + item.numCoins + " " + coinString(item.numCoins);

				ShaderLabel coinLabel = new ShaderLabel(fontShader, itemText, skin, Constants.UI.DEFAULT_FONT);
				coinLabel.setAlignment(Align.center);
				coinLabel.setWidth(width);
				coinLabel.setY(rowHeight * 0.4f);
				group.addActor(coinLabel);
			}
		};
		final float height = Gdx.graphics.getHeight();
		final float width = Gdx.graphics.getWidth();
		scrollList.setBounds(0, 0, width, height * 0.78f);

		if (GameLoop.USER.usedCoins != -1L) {
			watchAd = new ImageButton(skin, Constants.UI.GREEN_BUTTON);
			watchAd.setWidth(width * 0.5f);
			watchAd.setHeight(watchAd.getWidth() * 0.3f);
			ShaderTextButton watchAdText = new ShaderTextButton(fontShader, "Watch Ad", skin,
					Constants.UI.GREEN_BUTTON_TEXT);
			watchAdText.setY(watchAd.getHeight() / 2 - watchAdText.getHeight() * 0.3f);
			watchAdText.setWidth(watchAd.getWidth());

			ShaderTextButton watchAdTextDesc = new ShaderTextButton(fontShader, "(To cut wait time by 50%)", skin,
					Constants.UI.GREEN_BUTTON_TEXT_SMALL);
			watchAdTextDesc.setY(watchAd.getHeight() / 2 - watchAdTextDesc.getHeight() * 0.9f);
			watchAdTextDesc.setWidth(watchAd.getWidth());

			watchAd.addActor(watchAdText);
			watchAd.addActor(watchAdTextDesc);
			watchAdText.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					if (GameLoop.USER.usedCoins != -1 && !GameLoop.USER.watchedAd) {
						ExternalActionWrapper.showAd(new AdColonyVideoListener() {

							@Override
							public void onAdColonyVideoStarted() {
							}

							@Override
							public void onAdColonyVideoFinished() {
								watchAd.setDisabled(true);
								GameLoop.USER.watchedAd = true;
								UIConnectionWrapper.reduceTimeUntilCoins(new SetPlayerResultHandler(GameLoop.USER),
										GameLoop.USER.handle, GameLoop.USER.timeRemainingForNewcoins(),
										GameLoop.USER.usedCoins);
							}
						});
					}
				}
			});
			scrollList.addRow(watchAd).width(watchAd.getWidth()).height(watchAd.getHeight());
		}

		Collections.sort(inventory, new Comparator<InventoryItem>() {
			@Override
			public int compare(InventoryItem o1, InventoryItem o2) {
				if (o1.numCoins < o2.numCoins) {
					return -1;
				} else if (o1.numCoins > o2.numCoins) {
					return 1;
				}
				return 0;
			}
		});

		for (final InventoryItem item : inventory) {
			if (item.isAvailable()) {
				scrollList.addRow(item, new ClickListener() {
					@Override
					public void clicked(InputEvent event, float x, float y) {
						ExternalActionWrapper.purchaseCoins(item);
					}
				});
			}
		}

		return scrollList;
	}

	private void addCoinImageGroup() {
		final float height = Gdx.graphics.getHeight();
		final float width = Gdx.graphics.getWidth();

		coinGroup = new Group();
		coinGroup.setBounds(0, height * 0.8f, width, height * 0.1f);

		ImageButton coinImage = new ImageButton(skin, Constants.UI.COIN);
		float coinSize = coinGroup.getHeight() * 0.95f;
		coinImage.setWidth(coinSize);
		coinImage.setHeight(coinSize);
		coinImage.setY((coinGroup.getHeight() - coinSize) / 2);

		coinGroup.addActor(coinImage);

		coinText = new ShaderLabel(fontShader, GameLoop.USER.coins.toString(), skin, Constants.UI.DEFAULT_FONT);
		coinText.setAlignment(Align.right, Align.right);
		float yMidPoint = coinImage.getY() + coinImage.getHeight() / 2;
		float coinTextWidth = coinText.getWidth() * 4;
		coinText.setBounds(width * 0.5f - coinImage.getWidth() / 2 - coinTextWidth, yMidPoint - coinText.getHeight()
				/ 2, coinTextWidth, coinText.getHeight());
		coinImage.setX(coinText.getX() + coinText.getWidth() + 0.05f * width);
		coinGroup.addActor(coinText);

		actors.add(coinGroup);
		stage.addActor(coinGroup);
	}

	private String findTimeRemaining() {
		DateTime timeRemaining = GameLoop.USER.timeRemainingUntilCoinsAvailable();

		if (timeRemaining == null) {
			return GameLoop.USER.coins + " " + coinString(GameLoop.USER.coins) + " available!";
		}
		return Constants.timeRemainingFormat.format(timeRemaining.toDate());
	}

	@Override
	public void render(float delta) {
		if (timeRemainingText != null) {
			timeRemainingText.setText(findTimeRemaining());
		}

		if (coinText != null) {
			coinText.setText(GameLoop.USER.coins.toString());
		}
	}

	@Override
	public void hide() {

	}

	private void startHideSequence(final String retVal) {
		GraphicsUtils.hideAnimated(actors, retVal.equals(Action.BACK), new Runnable() {
			@Override
			public void run() {
				returnValue = retVal;
			}
		});
	}

	@Override
	public Object getRenderResult() {
		return returnValue;
	}

	@Override
	public void resetState() {
		returnValue = null;
	}

	@Override
	public void onConnectionResult(Player result) {
		GameLoop.USER = result;
	}

	@Override
	public void onConnectionError(String msg) {

	}

	@Override
	public void show(Stage stage, float width, float height) {
		actors.clear();
		this.stage = stage;

		waitImage = new WaitImageButton(skin);
		float buttonWidth = .25f * (float) width;
		waitImage.setWidth(buttonWidth);
		waitImage.setHeight(buttonWidth);
		waitImage.setX(width / 2 - buttonWidth / 2);
		waitImage.setY(height / 2 - buttonWidth / 2);
		stage.addActor(waitImage);

		if (inventoryResult == null) {
			waitImage.start();
			ExternalActionWrapper.loadStoreInventory(inventoryCallback);
		} else {
			inventoryCallback.onConnectionResult(inventoryResult);
		}
	}

	@Override
	public boolean hideTitleArea() {
		return true;
	}

	private UIConnectionResultCallback<Inventory> inventoryCallback = new UIConnectionResultCallback<Inventory>() {

		@Override
		public void onConnectionResult(Inventory result) {
			waitImage.stop();

			inventoryResult = result;
			createLayout(stage, result);
		}

		@Override
		public void onConnectionError(String msg) {
			waitImage.stop();

			final Overlay ovrlay = new DismissableOverlay(menusAtlas, 0.8f,
					new TextOverlay(msg, menusAtlas, skin, fontShader), new ClickListener() {
						@Override
						public void clicked(InputEvent event, float x, float y) {
							ExternalActionWrapper.loadStoreInventory(inventoryCallback);
						}
					});

			stage.addActor(ovrlay);
		}
	};
}
