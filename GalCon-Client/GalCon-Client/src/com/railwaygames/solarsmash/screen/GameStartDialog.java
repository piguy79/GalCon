package com.railwaygames.solarsmash.screen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.railwaygames.solarsmash.Constants;
import com.railwaygames.solarsmash.GameLoop;
import com.railwaygames.solarsmash.config.ConfigResolver;
import com.railwaygames.solarsmash.config.Configuration;
import com.railwaygames.solarsmash.screen.event.GameStartEvent;
import com.railwaygames.solarsmash.screen.event.PracticeStartEvent;
import com.railwaygames.solarsmash.screen.event.SocialGameStartEvent;
import com.railwaygames.solarsmash.screen.widget.CommonCoinButton;
import com.railwaygames.solarsmash.screen.widget.CommonTextButton;
import com.railwaygames.solarsmash.screen.widget.OKCancelDialog;
import com.railwaygames.solarsmash.screen.widget.ShaderLabel;

public class GameStartDialog extends OKCancelDialog {

	private float height;
	private float width;
	private int selectedMapKey;
	private Array<Actor> actors;
	public CommonCoinButton practiceButton;
	public CommonCoinButton randomPlay;

	public GameStartDialog(Resources resources, float width, float height, Stage stage, int selectedMapKey) {
		super(resources, width, height, stage, Type.CANCEL);
		this.height = height;
		this.width = width;
		this.selectedMapKey = selectedMapKey;

		create();
	}
	
	@Override
	public void addActor(final Actor actor){
		super.addActor(actor);
		if(actors == null){
			actors = new Array<Actor>();
		}
		actors.add(actor);
	}

	private void create() {
		createStartGameLabel();
		createPracticeButton();
		if(ConfigResolver.getRankForXp(GameLoop.getUser().xp).level >= 2){
			createRandomButton();
			createSocialButton();
		}else{
			createMultiplayerUnlockLabel();
		}
		
	}

	private void createMultiplayerUnlockLabel() {
		ShaderLabel unlockLabel = new ShaderLabel(resources.fontShader, "Unlock Multiplayer by reaching Rank 2 (200xp)", resources.skin,
				Constants.UI.SMALL_FONT, Color.WHITE);
		unlockLabel.setY(getHeight() * 0.33f);
		unlockLabel.setX(getWidth() * 0.05f);
		unlockLabel.setWidth(getWidth() * 0.9f);
		unlockLabel.setAlignment(Align.center);
		unlockLabel.setWrap(true);

		addActor(unlockLabel);
		
	}

	private void createSocialButton() {
		CommonTextButton socialPlay = new CommonTextButton(resources.skin, "friend", height * 0.2f, width * 0.8f,
				resources.fontShader);
		socialPlay.setX((getWidth() / 2) - (socialPlay.getWidth() / 2));
		socialPlay.setY(getHeight() * 0.08f);

		socialPlay.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				fire(new SocialGameStartEvent(selectedMapKey));
			}
		});

		addActor(socialPlay);
	}

	private void createRandomButton() {
		randomPlay = new CommonCoinButton(resources.skin, "random opponent", height * 0.2f,
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
	}

	private void createPracticeButton() {
		practiceButton = new CommonCoinButton(resources.skin, "battle simulation", height * 0.2f,
				width * 0.8f, resources.fontShader);
		practiceButton.setX((getWidth() / 2) - (practiceButton.getWidth() / 2));
		practiceButton.setY(getHeight() * 0.58f);

		practiceButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				fire(new PracticeStartEvent(selectedMapKey));
			}
		});

		addActor(practiceButton);
	}

	private void createStartGameLabel() {
		ShaderLabel startGameLabel = new ShaderLabel(resources.fontShader, "Start a New Game", resources.skin,
				Constants.UI.DEFAULT_FONT, Color.BLACK);
		startGameLabel.setY(getHeight() - (startGameLabel.getTextBounds().height * 2.2f));
		startGameLabel.setWidth(getWidth());
		startGameLabel.setAlignment(Align.center);

		addActor(startGameLabel);
	}
	
	public void fade() {
		actors.add(cancelButton);
		GraphicsUtils.fadeOut(actors, new Runnable() {
			
			@Override
			public void run() {
				getOverlay().remove();	
				remove();
			}
		}, 1);
	}
}
