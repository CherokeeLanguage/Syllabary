package com.cherokeelessons.syllabary.one.desktop;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.cherokeelessons.play.GameServices;
import com.cherokeelessons.play.Platform;
import com.cherokeelessons.syllabary.one.App;
import com.cherokeelessons.syllabary.one.SyllabaryApp;

public class DesktopLauncher {
	public static void main (String[] arg) {
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getDefaultScreenDevice();
		int width = (70 * gd.getDisplayMode().getWidth()) / 100;
		int height = (70 * gd.getDisplayMode().getHeight()) / 100;
//		width=2048/2;
//		height=1536/2;
		/*
		 * 720p
		 */
		width=1280;
		height=720;
		/*
		 * iphone 4
		 */
//		width=960;
//		height=640;
		/*
		 * generic tablet 7
		 */
//		width=1024;
//		height=600;
		
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.allowSoftwareMode = true;
		config.forceExit = true;
		config.height = height;
		config.width = width;
		config.audioDeviceBufferSize=8192;
		config.addIcon("icons/icon-128.png", FileType.Internal);
		config.addIcon("icons/icon-32.png", FileType.Internal);
		config.addIcon("icons/icon-16.png", FileType.Internal);
		new LwjglApplication(new SyllabaryApp(), config);
		App.services=new GameServices(App.CredentialsFolder, new Platform());
	}
}
