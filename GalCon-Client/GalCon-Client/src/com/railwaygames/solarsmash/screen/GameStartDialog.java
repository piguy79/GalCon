package com.railwaygames.solarsmash.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.screen.event.GameStartEvent;
import com.railwaygames.solarsmash.screen.event.PracticeStartEvent;
import com.railwaygames.solarsmash.screen.event.SocialGameStartEvent;
import com.railwaygames.solarsmash.screen.widget.CommonCoinButton;
import com.railwaygames.solarsmash.screen.widget.OKCancelDialog;
import com.railwaygames.solarsmash.screen.widget.ShaderLabel;

public class GameStartDialog extends OKCancelDialog {

	private float height;
	private float width;
	private int selectedMapKey;

	public GameStartDialog(Resources resources, float width, float height, Stage stage, int selectedMapKey) {
		super(resources, width, height, stage, Type.CANCEL);
		this.height = height;
		this.width = width;
		this.selectedMapKey = selectedMapKey;

		create();
	}

	private void create() {
		ShaderLabel startGameLabel = new ShaderLabel(resources.fontShader, "Start a New Game", resources.skin,
				Constants.UI.DEFAULT_FONT, Color.BLACK);
		startGameLabel.setY(getHeight() - (startGameLabel.getTextBounds().height * 2.1f));
		startGameLabel.setWidth(getWidth());
		startGameLabel.setAlignment(Align.center);

		addActor(startGameLabel);

		CommonCoinButton practiceButton = new CommonCoinButton(resources.skin, "battle simulation", height * 0.2f,
				width * 0.8f, resources.fontShader);
		practiceButton.setX((getWidth() / 2) - (practiceButton.getWidth() / 2));
		practiceButton.setY(getHeight() * 0.62f);

		practiceButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				fire(new PracticeStartEvent(selectedMapKey));
			}
		});

		addActor(practiceButton);

		CommonCoinButton randomPlay = new CommonCoinButton(resources.skin, "random opponent", height * 0.2f,
				width * 0.8f, resources.fontShader);
		randomPlay.setX((getWidth() / 2) - (randomPlay.getWidth() / 2));
		randomPlay.setY(getHeight() * 0.33f);

		randomPlay.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				fire(new GameStartEvent(selectedMapKey));
			}
		});

		addActor(randomPlay);

		CommonCoinButton socialPlay = new CommonCoinButton(resources.skin, "friend", height * 0.2f, width * 0.8f,
				resources.fontShader);
		socialPlay.setX((getWidth() / 2) - (socialPlay.getWidth() / 2));
		socialPlay.setY(getHeight() * 0.05f);

		socialPlay.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				fire(new SocialGameStartEvent(selectedMapKey));
			}
		});

		addActor(socialPlay);
	}
}
