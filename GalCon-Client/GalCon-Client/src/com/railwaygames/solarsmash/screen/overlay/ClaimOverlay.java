package com.railwaygames.solarsmash.screen.overlay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.GameLoop;
import com.railwaygames.solarsmash.UIConnectionWrapper;
import com.railwaygames.solarsmash.http.UIConnectionResultCallback;
import com.railwaygames.solarsmash.model.GameBoard;
import com.railwaygames.solarsmash.screen.Resources;
import com.railwaygames.solarsmash.screen.event.ClaimVictoryEvent;
import com.railwaygames.solarsmash.screen.widget.CommonTextButton;
import com.railwaygames.solarsmash.screen.widget.ShaderLabel;

public class ClaimOverlay extends Overlay {
	
	ShaderLabel descLabel;
	private float buttonHeight;
	private float buttonWidth;
	private GameBoard gameBoard;
	
	private CommonTextButton claimButton ;

	public ClaimOverlay(Resources resources, GameBoard gameBoard) {
		super(resources);
		this.gameBoard = gameBoard;
		
		float width = Gdx.graphics.getWidth();
		float height = Gdx.graphics.getHeight();
		this.buttonHeight = height * 0.1f;
		this.buttonWidth = width * 0.45f;
		
		addDescriptionText();
		addClaimButton();
		addContinueButton();
	}

	private void addDescriptionText() {
		String description = "Claim Victory\n\nFrozen by fear, your enemy refuses to fight. Click 'Claim Victory' to win.";
		descLabel = new ShaderLabel(resources.fontShader, description, resources.skin, Constants.UI.DEFAULT_FONT);

		float y = (Gdx.graphics.getHeight() * 0.75f) - (descLabel.getHeight() / 2);
		descLabel.setBounds(0, y, Gdx.graphics.getWidth(), descLabel.getHeight());
		descLabel.setWrap(true);
		descLabel.setAlignment(Align.center, Align.center);
		addActor(descLabel);
	}
	
	private void addClaimButton() {
		float width = Gdx.graphics.getWidth();
		float height = Gdx.graphics.getHeight();
		
		claimButton = new CommonTextButton(resources.skin, "Claim Victory", buttonHeight, buttonWidth, resources.fontShader);
		claimButton.setX((width / 2) - (claimButton.getWidth() / 2));
		claimButton.setY(descLabel.getY() - (descLabel.getTextBounds().height + (height * 0.1f)));
		
		claimButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				UIConnectionWrapper.claimGame(claimVictoryCallBack, GameLoop.USER.handle, gameBoard.id);
			}
		});
		addActor(claimButton);
	}
	
	private void addContinueButton(){
		CommonTextButton continueButton = new CommonTextButton(resources.skin, "Continue Game", buttonHeight, buttonWidth, resources.fontShader);
		continueButton.setX(claimButton.getX());
		continueButton.setY(claimButton.getY() - (buttonHeight * 1.1f));
		
		continueButton.addListener(new ClickListener(){
			@Override
			public void clicked(InputEvent event, float x, float y) {
				remove();
			}
		});
		addActor(continueButton);
	}
	
	private UIConnectionResultCallback<GameBoard> claimVictoryCallBack = new UIConnectionResultCallback<GameBoard>() {
		public void onConnectionResult(GameBoard result) {
			fire(new ClaimVictoryEvent(true));
		};
		
		public void onConnectionError(String msg) {
			fire(new ClaimVictoryEvent(false));
		};
	};

}
