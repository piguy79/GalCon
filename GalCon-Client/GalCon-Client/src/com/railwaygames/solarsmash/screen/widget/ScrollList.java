package com.railwaygames.solarsmash.screen.widget;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.esotericsoftware.tablelayout.Cell;
import com.railwaygames.solarsmash.Constants;

public abstract class ScrollList<Item> extends ScrollPane {

	private Skin skin;

	public ScrollList(Skin skin) {
		super(new Table());
		this.skin = skin;
		getWidget().top();
		this.setScrollingDisabled(true, false);
		this.setFadeScrollBars(false);
	}
	

	@Override
	public Table getWidget() {
		return (Table) super.getWidget();
	}

	protected float getRowHeight() {
		return Gdx.graphics.getHeight() * 0.15f;
	}

	protected float getRowWidth() {
		return Gdx.graphics.getWidth();
	}

	public Cell addRow(Actor actor) {
		Cell cell = getWidget().add(actor);
		getWidget().row();
		return cell;
	}
	
	public void clearRows(){
		getWidget().clearChildren();
	}

	public void addRow(Item item, ClickListener clickListener) {
		float width = getRowWidth();
		float rowHeight = getRowHeight();

		Image imageRow = new Image(skin, Constants.UI.CELL_BG);
		imageRow.setHeight(rowHeight);
		imageRow.setWidth(width);
		imageRow.setAlign(Align.center);

		Group group = new Group();
		group.addActor(imageRow);
		group.addListener(clickListener);
		group.setHeight(rowHeight);
		group.setWidth(width);

		buildCell(item, group);

		getWidget().add(group).width(width).height(rowHeight);
		getWidget().row();
	}

	@Override
	public void layout() {
		super.layout();

		for (Cell cell : getWidget().getCells()) {
			cell.setWidgetHeight(400);
		}
		getWidget().layout();
	}

	public abstract void buildCell(Item item, Group group);

}
