package com.railwaygames.solarsmash.screen.widget;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.GameLoop;
import com.railwaygames.solarsmash.screen.Resources;

public class CoinInfoDisplay {
	
	private Image coinImage;
	private ShaderLabel coinAmountText;
	private Resources resources;
	private Image originalCoin;
	
	public CoinInfoDisplay(final Resources resources, final Image originalCoin){
		this.resources = resources;
		this.originalCoin = originalCoin;
		createCoinImage();
		createCoinAmountLabel();
	}

	private void createCoinAmountLabel() {
		coinAmountText = new ShaderLabel(resources.fontShader, "" + GameLoop.USER.coins, resources.skin, Constants.UI.LARGE_FONT, Color.WHITE);
		coinAmountText.setBounds(coinImage.getX() - (coinAmountText.getTextBounds().width * 1.1f), (coinImage.getY() + (coinImage.getHeight() / 2)) - (coinAmountText.getTextBounds().height / 2), coinAmountText.getTextBounds().width, Gdx.graphics.getHeight() * 0.1f);
		coinAmountText.setColor(255, 255, 255, 0);
		getCoinAmountText().addAction(Actions.fadeOut(0));
		getCoinAmountText().addAction(Actions.sequence(Actions.fadeIn(0.5f),Actions.delay(0.75f), Actions.run(new Runnable() {
			
			@Override
			public void run() {
				getCoinAmountText().setText("" + (GameLoop.USER.coins - 1));
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

}
