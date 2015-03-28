package com.cherokeelessons.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Scaling;
import com.cherokeelessons.syllabary.one.App;

public class GameBoard extends Table implements Disposable {
	public static final int height = 5;
	public static final int width = 7;
	private Texture bar = null;
	private List<Cell<Stack>> cell;
	private Label challenge_latin;
	private Image challenge_pic;
	private Cell<Stack> challenge_stack;
	private Cell<Actor> leftBottomCell;
	
	private Cell<Actor> leftTopCell;
	private TextButton mainMenu;
	private TextButton mute;

	private TextButton pause;

	private Texture question = null;
	private Label score;
	public GameBoard() {
		question = new Texture(Gdx.files.internal("images/misc/003f_4.png"));
		question.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		
		setFillParent(true);
		setTransform(true);
		defaults().expandX().fill();
		
		Texture t = App.getManager().get(UI.BG, Texture.class);
		TextureRegion tr = new TextureRegion(t);
		TiledDrawable background = new TiledDrawable(tr);
		Table btable = new Table();
		btable.setFillParent(true);
		btable.setBackground(background);
		btable.addAction(Actions.alpha(.5f));
		addActor(btable);

		LabelStyle ls = UI.getLs();
		TextButtonStyle tbs = UI.getTbs();

		final Table blocks = new Table();
		blocks.defaults().pad(8);
		cell = new ArrayList<>();
		Image i = new Image();
		for (int iy = 0; iy < height; iy++) {
			blocks.row();
			for (int ix = 0; ix < width; ix++) {
				cell.add(blocks.stack(i).expand().fill());
			}
		}
		for (int iy = 0; iy < height; iy++) {
			for (int ix = 0; ix < width; ix++) {
				int letter = new Random().nextInt('Ᏼ' - 'Ꭰ') + 'Ꭰ';
				int font = new Random().nextInt(4);
				String glyph = Integer.toHexString(letter).toLowerCase();
				String path = "images/glyphs/" + glyph + "_" + font
						+ ".png";
				FileHandle file = Gdx.files.internal(path);
				Texture syl_text = new Texture(file);
				syl_text.setFilter(TextureFilter.Linear,
						TextureFilter.Linear);
				final Image img = new Image(syl_text);
				img.setScaling(Scaling.fit);
				img.setColor(new Color(new Random().nextFloat(),
						new Random().nextFloat(), new Random().nextFloat(),
						1f));
				img.addListener(new ClickListener() {
					@Override
					public boolean touchDown(InputEvent event, float x,
							float y, int pointer, int button) {
						img.setColor(new Color(new Random().nextFloat(),
								new Random().nextFloat(), new Random()
										.nextFloat(), 1f));
						return true;
					}

				});
				getStackAt(ix, iy).add(img);
			}
		}

		score = new Label("000000000", ls);
		challenge_latin = new Label("GWA", ls);
		challenge_pic = new Image(question);
		challenge_pic.setScaling(Scaling.fit);
		mainMenu = new TextButton("Menu", tbs);
		mute = new TextButton("Mute", tbs);
		pause = new TextButton("Pause", tbs);

		row();
		final Table leftColumn = new Table() {
			@Override
			public void layout() {
				super.layout();
				float col_height = getHeight();
				float blocks_height = blocks.getHeight();
				if (blocks_height>0) {
					if (col_height-blocks_height>6*24) {
						if (bar==null) {
							bar = new Texture(Gdx.files.internal("images/misc/bar.png"));
							bar.setFilter(TextureFilter.Linear, TextureFilter.Linear);
							leftTopCell.setActor(new Image(bar)).center();
							leftBottomCell.setActor(new Image(bar)).center();
						}
					} else {
						leftTopCell.clearActor();
						leftBottomCell.clearActor();
						if (bar!=null) {
							bar.dispose();
							bar=null;
						}
					}
				}
			}				
		};
		add(leftColumn).fill().expand();
		leftColumn.defaults().pad(0).space(0).center();
		leftColumn.row();
		leftTopCell = leftColumn.add(new Actor()).center().expandY();
		leftColumn.row();
		leftColumn.add(blocks).pad(20);
		leftColumn.row();
		leftBottomCell = leftColumn.add(new Actor()).center().expandY();
		
		Table rightColumn = new Table();
		rightColumn.defaults().pad(0).space(0).center();
		add(rightColumn).right();
		rightColumn.row();
		rightColumn.add(score);
		rightColumn.row();
		rightColumn.add().expandY();
		rightColumn.row();
		rightColumn.add(challenge_latin);
		rightColumn.row();
		challenge_stack = rightColumn.stack(challenge_pic);
		rightColumn.row();
		rightColumn.add().expandY();
		rightColumn.row();
		rightColumn.add(mute).fillX();
		rightColumn.row();
		rightColumn.add(pause).fillX();
		rightColumn.row();
		rightColumn.add(mainMenu).fillX();
	}
	@Override
	public void dispose() {
		if (question!=null) {
			question.dispose();
			question=null;
		}
		if (bar!=null) {
			bar.dispose();
			bar=null;
		}
	}
	@Override
	protected void finalize() throws Throwable {
		dispose();
		super.finalize();
	}

	public String getChallenge_latin() {
		return challenge_latin.getText().toString().intern();
	}

	public Stack getStackAt(int x, int y) {
		x%=width;
		y%=height;
		if (x<0) x=width+x;
		if (y<0) y=height+y;
		return cell.get(x + y * width).getActor();
	}
	
	public void setChallenge_latin(String challenge_latin) {
		this.challenge_latin.setText(challenge_latin.intern());
	}
}