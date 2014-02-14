package com.xxx.galcon.screen;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.xxx.galcon.UISkin;
import com.xxx.galcon.model.Move;
import com.xxx.galcon.model.Planet;
import com.xxx.galcon.model.Point;
import com.xxx.galcon.model.factory.MoveFactory;
import com.xxx.galcon.screen.event.CancelDialogEvent;
import com.xxx.galcon.screen.event.MoveEvent;
import com.xxx.galcon.screen.widget.ActionButton;

public class ShipSelectionHud extends Group {

	private Slider slider;
	private AssetManager assetManager;
	private UISkin skin;
	private ActionButton okButton;

	private int max;
	private int shipsToSend = 0;
	private List<Planet> planetsInvolved;
	private int currentRound;

	public ShipSelectionHud(Planet fromPlanet, Planet toPlanet, int moveOffSetCount, int initialNumber,
			int currentRound, AssetManager assetManager, UISkin skin) {
		this.assetManager = assetManager;
		this.skin = skin;
		this.setWidth(Gdx.graphics.getWidth());
		this.setHeight(Gdx.graphics.getHeight() * 0.1f);

		this.max = fromPlanet.numberOfShips - moveOffSetCount + initialNumber;
		this.shipsToSend = initialNumber;
		this.currentRound = currentRound;
		this.planetsInvolved = new ArrayList<Planet>();
		planetsInvolved.add(fromPlanet);
		planetsInvolved.add(toPlanet);

		createBackground();
		addOkButton();
		addCancelButton();
		addSlider();

		this.slider.setValue(initialNumber);
	}

	private void createBackground() {
		TextureAtlas gameBoardAtlas = assetManager.get("data/images/gameBoard.atlas", TextureAtlas.class);
		AtlasRegion bgRegion = gameBoardAtlas.findRegion("player_hud");
		Image backGround = new Image(new TextureRegionDrawable(bgRegion));
		backGround.setWidth(getWidth());
		backGround.setHeight(getHeight());
		addActor(backGround);
	}

	private void addCancelButton() {
		final ActionButton cancelButton = new ActionButton(skin, "cancelButton", new Point(10, getHeight() * 0.1f));
		cancelButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				fire(new CancelDialogEvent());
			}
		});

		addActor(cancelButton);
	}

	private void addOkButton() {
		okButton = new ActionButton(skin, "okButton", new Point(0, getHeight() * 0.1f));
		okButton.setX(getWidth() - okButton.getWidth() - 10);
		okButton.setDisabled(shipsToSend == 0);
		okButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Move move = MoveFactory.createMove(planetsInvolved, shipsToSend, currentRound);
				fire(new MoveEvent(move));
			}
		});

		addActor(okButton);
	}

	private void addSlider() {
		TextureAtlas menusAtlas = assetManager.get("data/images/menus.atlas", TextureAtlas.class);
		TextureAtlas gameBoardAtlas = assetManager.get("data/images/gameBoard.atlas", TextureAtlas.class);

		Drawable sliderBg = new TextureRegionDrawable(menusAtlas.findRegion("slider_bg"));
		Drawable sliderKnob = new TextureRegionDrawable(gameBoardAtlas.findRegion("ship"));

		sliderBg.setMinHeight(getHeight() * 0.2f);
		sliderBg.setMinWidth(getWidth() * 0.6f);
		sliderKnob.setMinHeight(getHeight() * 0.5f);
		sliderKnob.setMinWidth(getWidth() * 0.12f);
		skin.add("default-horizontal", new SliderStyle(sliderBg, sliderKnob));

		slider = new Slider(0, max, 1, false, skin);
		slider.setWidth(getWidth() * 0.6f);
		slider.setHeight(getHeight() * 0.8f);
		slider.setY(getHeight() * 0.05f);
		slider.setX(getWidth() * 0.2f);

		slider.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				shipsToSend = (int) slider.getValue();
				okButton.setDisabled(shipsToSend == 0);
			}
		});

		addActor(slider);
	}
}
