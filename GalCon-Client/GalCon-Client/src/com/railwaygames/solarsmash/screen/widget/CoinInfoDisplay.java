package com.railwaygames.solarsmash.screen.widget;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.ParallelAction;
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.GameLoop;
import com.railwaygames.solarsmash.screen.GraphicsUtils;
import com.railwaygames.solarsmash.screen.Resources;

public class CoinInfoDisplay {

	private Image coinImage;
	private ShaderLabel coinAmountText;
	private Resources resources;
	private Image originalCoin;

	public CoinInfoDisplay(final Resources resources, final Image originalCoin) {
		this.resources = resources;
		this.originalCoin = originalCoin;
		createCoinImage();
		createCoinAmountLabel();
	}

	private void createCoinAmountLabel() {
		coinAmountText = new ShaderLabel(resources.fontShader, "" + GameLoop.getUser().coins, resources.skin,
				Constants.UI.LARGE_FONT, Color.WHITE);
		coinAmountText.setBounds(coinImage.getX() - (coinAmountText.getTextBounds().width * 1.1f),
				(coinImage.getY() + (coinImage.getHeight() / 2)) - (coinAmountText.getTextBounds().height / 2),
				coinAmountText.getTextBounds().width, Gdx.graphics.getHeight() * 0.1f);
		coinAmountText.setColor(255, 255, 255, 0);
		getCoinAmountText().addAction(Actions.fadeOut(0));
		getCoinAmountText().addAction(
				Actions.sequence(Actions.fadeIn(0.5f), Actions.delay(0.75f), Actions.run(new Runnable() {

					@Override
					public void run() {
						getCoinAmountText().setText("" + (GameLoop.getUser().coins - 1));
					}
				})));

	}

	private void createCoinImage() {
		coinImage = new Image(resources.skin, Constants.UI.COIN_IMAGE);
		Vector2 pos = originalCoin.localToStageCoordinates(new Vector2(0, 0));
		coinImage.setX(pos.x);
		coinImage.setY(pos.y);
		coinImage.setSize(originalCoin.getWidth(), originalCoin.getHeight());
		coinImage.setOrigin(coinImage.getWidth() / 2, coinImage.getHeight() / 2);
	}

	public ShaderLabel getCoinAmountText() {
		return coinAmountText;
	}

	public Image getCoinImage() {
		return coinImage;
	}

	public void animate(Runnable runnable) {
		float baseDuration = 1f;
		float percentageFromBottom = coinImage.getY() / Gdx.graphics.getHeight();
		float floatUpHeight = Gdx.graphics.getHeight() * 0.25f;

		float fallHeight = coinImage.getY() + floatUpHeight + coinImage.getHeight();
		float distanceAcross = (Gdx.graphics.getWidth() - coinImage.getX()) * 0.5f;
		ParallelAction arc = GraphicsUtils.arcMovement(baseDuration + (baseDuration * percentageFromBottom),
				distanceAcross, floatUpHeight, fallHeight);
		RepeatAction rotate = Actions.forever(Actions.rotateBy(360, 0.75f));

		getCoinImage().addAction(
				Actions.parallel(rotate, Actions.sequence(Actions.delay(0.8f), arc, Actions.run(runnable))));

	}

}
