package com.cherokeelessons.syllabary.screens;

import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Scaling;
import com.cherokeelessons.syllabary.one.App;
import com.cherokeelessons.ui.GameBoard;
import com.cherokeelessons.ui.UI;

public class GameScreen extends ChildScreen {
	private GameBoard gameboard;

	public GameScreen(Screen caller) {
		super(caller);
	}

	@Override
	public void show() {
		super.show();
		gameboard = UI.getGameBoard(stage, App.getManager());

		for (int iy = 0; iy < GameBoard.height; iy++) {
			for (int ix = 0; ix < GameBoard.width; ix++) {
				int letter = new Random().nextInt('Ᏼ' - 'Ꭰ') + 'Ꭰ';
				int font = new Random().nextInt(4);
				String glyph = Integer.toHexString(letter).toLowerCase();
				String path = "images/glyphs/" + glyph + "_" + font + ".png";
				FileHandle file = Gdx.files.internal(path);
				Texture syl_text = new Texture(file);
				syl_text.setFilter(TextureFilter.Linear, TextureFilter.Linear);
				final Image img = new Image(syl_text) {
					@Override
					public void act(float delta) {
						if (needsLayout()) {
							return;
						}
						super.act(delta);
					}
				};
				img.setScaling(Scaling.fit);
				img.setColor(new Color(new Random().nextFloat(), new Random()
						.nextFloat(), new Random().nextFloat(), 1f));
				img.addListener(new ClickListener() {
					@Override
					public boolean touchDown(InputEvent event, float x,
							float y, int pointer, int button) {
						img.setColor(new Color(new Random().nextFloat(),
								new Random().nextFloat(), new Random()
										.nextFloat(), 1f));
						int swapx = new Random().nextInt(GameBoard.width);
						int swapy = new Random().nextInt(GameBoard.height);
						Image swap = gameboard.getImageAt(swapx, swapy);
						if (swap == null) {
							return false;
						}

						gameboard.getCellAt(swapx, swapy);
						swap.addAction(Actions.moveTo(img.getX(), img.getY(),
								1f));
						img.addAction(Actions.moveTo(swap.getX(), swap.getY(),
								1f));
						return true;
					}

				});
				gameboard.setImageAt(ix, iy, img);
				float stageHeight = stage.getHeight();
				img.layout();
				Action moveToHidden = Actions.moveBy(0, stageHeight);
				Action makeVisible = Actions.visible(true);
				Action moveToZero = Actions.moveBy(0, -stageHeight, 2f);
				SequenceAction sequence = Actions.sequence(moveToHidden,
						makeVisible, moveToZero);
				img.addAction(sequence);
				img.setVisible(false);
			}
		}
	}

	@Override
	public void hide() {
		super.hide();
		gameboard.dispose();
	}

	private float maxTime = 10f;
	private float total_elapsed = 0f;
	private float challenge_elapsed = 0f;
	private float update_prev = 0f;

	@Override
	public void act(float delta) {
		total_elapsed += delta;
		challenge_elapsed += delta;
		if (challenge_elapsed - update_prev > 1f) {
			while (challenge_elapsed - update_prev > 1f) {
				update_prev += 1f;
			}
			gameboard.setRemaining(challenge_elapsed / maxTime, 1f);
		}
		super.act(delta);
	}
}