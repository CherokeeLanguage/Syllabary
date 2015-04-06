package com.cherokeelessons.syllabary.one.desktop;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.cherokeelessons.syllabary.one.SyllabaryApp;

public class DesktopLauncher {
	public static void main (String[] arg) {
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getDefaultScreenDevice();
		int width = (70 * gd.getDisplayMode().getWidth()) / 100;
		int height = (70 * gd.getDisplayMode().getHeight()) / 100;
		width=2048/2;
		height=1536/2;
//		width=1280;
//		height=720;
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.allowSoftwareMode = true;
		config.forceExit = true;
		config.height = height;
		config.width = width;
//		config.addIcon("icons/icon-128.png", FileType.Internal);
//		config.addIcon("icons/icon-32.png", FileType.Internal);
//		config.addIcon("icons/icon-16.png", FileType.Internal);
		new LwjglApplication(new SyllabaryApp(), config);
	}
}
