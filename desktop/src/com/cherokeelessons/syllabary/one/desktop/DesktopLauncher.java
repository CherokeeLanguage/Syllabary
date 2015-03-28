package com.cherokeelessons.syllabary.one.desktop;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.cherokeelessons.syllabary.one.Syllabary;

public class DesktopLauncher {
	public static void main (String[] arg) {
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getDefaultScreenDevice();
		int width = (75 * gd.getDisplayMode().getWidth()) / 100;
		int height = (75 * gd.getDisplayMode().getHeight()) / 100;
		width=2048/2;
		height=1536/2;
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.allowSoftwareMode = true;
		config.forceExit = true;
		config.height = height;
		config.width = width;
//		config.addIcon("icons/icon-128.png", FileType.Internal);
//		config.addIcon("icons/icon-32.png", FileType.Internal);
//		config.addIcon("icons/icon-16.png", FileType.Internal);
		new LwjglApplication(new Syllabary(), config);
	}
}
