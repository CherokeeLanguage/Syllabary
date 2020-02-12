package com.cherokeelessons.syllabary.one.desktop;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.cherokeelessons.syllabary.one.SyllabaryApp;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.allowSoftwareMode=true;
		config.audioDeviceSimultaneousSources=16;
		config.forceExit=true;
		config.height=1242/2;
		config.width=2688/2;
		config.initialBackgroundColor=Color.WHITE;
		config.resizable=true;
		config.title="Cherokee Syllabary";
		
		config.addIcon("icons/icon-128.png", FileType.Internal);
		config.addIcon("icons/icon-32.png", FileType.Internal);
		config.addIcon("icons/icon-16.png", FileType.Internal);
		
		new LwjglApplication(new SyllabaryApp(), config);
	}
}
