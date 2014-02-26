package com.xxx.galcon.screen.hud;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
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
import com.xxx.galcon.model.Move;
import com.xxx.galcon.model.Planet;
import com.xxx.galcon.model.Point;
import com.xxx.galcon.model.factory.MoveFactory;
import com.xxx.galcon.screen.Resources;
import com.xxx.galcon.screen.event.CancelDialogEvent;
import com.xxx.galcon.screen.event.MoveEvent;
import com.xxx.galcon.screen.event.SliderUpdateEvent;
import com.xxx.galcon.screen.widget.ActionButton;

public class ShipSelectionHud extends Group {

	private Slider slider;
	private Resources resources;
	private ActionButton okButton;

	private int max;
	private int shipsToSend = 0;
	private List<Planet> planetsInvolved;
	private int currentRound;

	public ShipSelectionHud(Planet fromPlanet, Planet toPlanet, int moveOffSetCount, int initialNumber,
			int currentRound, Resources resources) {
		this.resources = resources;
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
		AtlasRegion bgRegion = resources.gameBoardAtlas.findRegion("player_hud");
		Image backGround = new Image(new TextureRegionDrawable(bgRegion));
		backGround.setWidth(getWidth());
		backGround.setHeight(getHeight());
		addActor(backGround);
	}

	private void addCancelButton() {
		final ActionButton cancelButton = new ActionButton(resources.skin, "cancelButton", new Point(10,
				getHeight() * 0.1f));
		cancelButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				fire(new CancelDialogEvent());
			}
		});

		addActor(cancelButton);
	}

	private void addOkButton() {
		okButton = new ActionButton(resources.skin, "okButton", new Point(0, getHeight() * 0.1f));
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
		Drawable sliderBg = new TextureRegionDrawable(resources.menuAtlas.findRegion("slider_bg"));
		Drawable sliderKnob = new TextureRegionDrawable(resources.gameBoardAtlas.findRegion("ship"));

		sliderBg.setMinHeight(getHeight() * 0.2f);
		sliderBg.setMinWidth(getWidth() * 0.6f);
		sliderKnob.setMinHeight(getHeight() * 0.5f);
		sliderKnob.setMinWidth(getWidth() * 0.12f);
		resources.skin.add("default-horizontal", new SliderStyle(sliderBg, sliderKnob));

		slider = new Slider(0, max, 1, false, resources.skin);
		slider.setWidth(getWidth() * 0.6f);
		slider.setHeight(getHeight() * 0.8f);
		slider.setY(getHeight() * 0.05f);
		slider.setX(getWidth() * 0.2f);

		slider.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				shipsToSend = (int) slider.getValue();
				okButton.setDisabled(shipsToSend == 0);
				fire(new SliderUpdateEvent(shipsToSend));
			}
		});

		addActor(slider);
	}
}
