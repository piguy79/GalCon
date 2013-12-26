package com.xxx.galcon.screen.ship.selection;

import static com.xxx.galcon.Util.createShader;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.xxx.galcon.Constants;
import com.xxx.galcon.UISkin;
import com.xxx.galcon.model.Move;
import com.xxx.galcon.model.Planet;
import com.xxx.galcon.model.Point;
import com.xxx.galcon.model.factory.MoveFactory;
import com.xxx.galcon.screen.event.CancelDialogEvent;
import com.xxx.galcon.screen.event.MoveEvent;
import com.xxx.galcon.screen.widget.ActionButton;
import com.xxx.galcon.screen.widget.Dialog;
import com.xxx.galcon.screen.widget.ShaderLabel;


public class MoveDialog extends Dialog {
	
	private UISkin skin;
	protected ShaderLabel counter;
	private ShaderLabel initialCount;
	private ShaderProgram fontShader;
	protected int shipsToSend = 0;
	private int max;
	
	private ActionButton cancelButton;
	private ActionButton okButton;
	
	protected Slider slider;
	
	private List<Planet> planetsInvolved;
	
	private int currentRound;
	

	public MoveDialog(Planet fromPlanet,Planet toPlanet, int moveOffSetCount, int max, AssetManager assetManager, float width, float height, UISkin skin, int currentRound) {
		super(assetManager, width, height);
		this.skin = skin;
		this.max = max;
		this.currentRound = currentRound;
		this.planetsInvolved = new ArrayList<Planet>();
		planetsInvolved.add(fromPlanet);
		planetsInvolved.add(toPlanet);

		
		fontShader = createShader("data/shaders/font-vs.glsl", "data/shaders/font-fs.glsl");
		counter = new ShaderLabel(fontShader, shipsToSend + "", skin, Constants.UI.DEFAULT_FONT_BLACK);
		initialCount = new ShaderLabel(fontShader, (fromPlanet.numberOfShips - moveOffSetCount) + "", skin, Constants.UI.DEFAULT_FONT_BLACK);
		
		addSlider();
		addCounter();
		addCancelButton();
		addOkButton();
	}
	

	private void addSlider() {
		TextureAtlas menusAtlas = assetManager.get("data/images/menus.atlas", TextureAtlas.class);
		TextureAtlas gameBoardAtlas = assetManager.get("data/images/gameBoard.atlas", TextureAtlas.class);
		
		Drawable sliderBg = new TextureRegionDrawable(menusAtlas.findRegion("slider_bg"));
		Drawable sliderKnob =  new TextureRegionDrawable(gameBoardAtlas.findRegion("ship"));
		
		sliderBg.setMinHeight(getHeight() * 0.2f);
		sliderBg.setMinWidth(getWidth() * 0.8f);
		sliderKnob.setMinHeight(getHeight() * 0.3f);
		sliderKnob.setMinWidth(getWidth() * 0.1f);
		
		skin.add("default-horizontal", new SliderStyle(sliderBg, sliderKnob));
		
		slider = new Slider(0, max, 1, false, skin);
		slider.setWidth(getWidth() * 0.8f);
		slider.setY(slider.getY() + (getHeight() * 0.4f));
		slider.setX(slider.getX() + (getWidth() * 0.08f));
		
		slider.addListener(new ChangeListener() {
			
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				counter.setText((int)slider.getValue() + "");
				shipsToSend = (int)slider.getValue();
				initialCount.setText("" + (max - shipsToSend));
			}
		});
		
		addActor(slider);
		
	}
	
	private void addCounter(){
		Table countTable = new Table();

		countTable.add(initialCount);
		countTable.add(new ShaderLabel(fontShader, " >> ", skin, Constants.UI.DEFAULT_FONT_BLACK));
		countTable.add(counter);
		
		countTable.setX(countTable.getX() + (getWidth() * 0.5f));
		countTable.setY(countTable.getY() + (getHeight() * 0.8f));
		
		addActor(countTable);
	}
	
	private void addCancelButton(){
		cancelButton = new ActionButton(skin,"cancelButton", getWidth() * 0.12f, getWidth() * 0.12f, new Point(getX(), getY()));
		cancelButton.setColor(0, 0, 0, 0);
		cancelButton.addListener(new ClickListener(){@Override
		public void clicked(InputEvent event, float x, float y) {
			fire(new CancelDialogEvent());
			hide();
		}});
		
		addActor(cancelButton);
	}
	
	private void addOkButton(){
		okButton =  new ActionButton(skin,"okButton", getWidth() * 0.12f, getWidth() * 0.12f, new Point(getX() + (getWidth() * 0.87f), getY()));
		okButton.setColor(0, 0, 0, 0);
		
		okButton.addListener(new ClickListener(){@Override
		public void clicked(InputEvent event, float x, float y) {
			Move move = MoveFactory.createMove(planetsInvolved, shipsToSend, currentRound);
			fire(new MoveEvent(move));
		}});
		addActor(okButton);
	}
	
	public void displayButtons(){
		cancelButton.addAction(Actions.fadeIn(0.4f));
		okButton.addAction(Actions.fadeIn(0.4f));
	}
	
	public void show(final Point point){
		addAction(Actions.sequence(new RunnableAction(){@Override
		public void run() {
			showParent(point, 0.4f);
		}}, Actions.delay(0.4f), new RunnableAction(){@Override
		public void run() {
			displayButtons();
		}}));
	}
	
	
	public void hide(){
		cancelButton.addAction(Actions.fadeOut(0.4f));
		okButton.addAction(Actions.sequence(Actions.fadeOut(0.4f), new RunnableAction(){@Override
		public void run() {
			hideParent();
		}}));
	}
	
	public void showParent(Point point, float duration){
		super.show(point, duration);
	}
	
	public void hideParent(){
		super.hide(0.4f);
	}

}
