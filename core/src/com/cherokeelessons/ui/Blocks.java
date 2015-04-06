package com.cherokeelessons.ui;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.Align;

public class Blocks<T extends Actor> extends WidgetGroup {
	private int width;
	private int height;
	private List<T> cells;
	private float step_width=0;
	private float step_height=0;
	public Blocks(int width, int height) {
		this.width=width;
		this.height=height;
		int size = this.width * this.height;
		cells = new ArrayList<>(size);
		for (int ix=0; ix<size; ix++) {
			cells.add(null);
		}
	}
	public void setActorAt(int ix, int iy, T actor) {
		int index = ix+iy*height;
		T prevActor = cells.get(index);
		if (prevActor!=null) {
			prevActor.remove();
		}
		if (actor!=null) {
			this.addActor(actor);
		}
		cells.set(index, actor);
		sizeChanged();
	}
	@Override
	protected void sizeChanged() {
		super.sizeChanged();
		step_width = getWidth()/width;
		step_height = getHeight()/height;
		for (int ix=0; ix<width; ix++) {
			for (int iy=0; iy<height; iy++) {
				Actor actor = cells.get(ix+iy*height);
				if (actor==null) {
					continue;
				}
				float pos_x = ix*step_width;
				float pos_y = iy*step_height*this.height;
				actor.setWidth(step_width);
				actor.setHeight(step_height);
				actor.setPosition(pos_x, pos_y, Align.center);
			}
		}
	}
	public T getActorAt(int x, int y) {
		int index = x+y*height;
		return cells.get(index);
	}
}