package com.cherokeelessons.syllabary.one.desktop;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.OverlayLayout;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.lwjgl.opengl.Display;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Graphics.GraphicsType;
import com.badlogic.gdx.Input.TextInputListener;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.cherokeelessons.play.GameServices;
import com.cherokeelessons.play.Platform;
import com.cherokeelessons.syllabary.one.App;
import com.cherokeelessons.syllabary.one.App.PlatformTextInput;
import com.cherokeelessons.syllabary.one.SyllabaryApp;

public class DesktopLauncher implements PlatformTextInput  {
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
		 * iPad
		 */
//		width=1024;
//		height=768;
		/*
		 * iPhone 4 (3.5")
		 */
//		width=960;
//		height=640;
		/*
		 * iPhone 5 (4")
		 */
//		width=1136;
//		height=640;
		/*
		 * iPhone 6 (4.7")
		 */
//		width=1334;
//		height=750;
		/*
		 * iPhone 6 Plus (5.5")
		 */
//		width=1104;//2208;
//		height=621;//1242;
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
//		config.audioDeviceBufferSize=8192;
		config.addIcon("icons/icon-128.png", FileType.Internal);
		config.addIcon("icons/icon-32.png", FileType.Internal);
		config.addIcon("icons/icon-16.png", FileType.Internal);
		SyllabaryApp coreapp = new SyllabaryApp();
		coreapp.pInput=new DesktopLauncher();
		new LwjglApplication(coreapp, config);
		App.services=new GameServices(App.CredentialsFolder, new Platform());
	}
	
	@Override
	public void getTextInput(final TextInputListener listener,
			final String title, final String text, final String hint) {
		final InputProcessor savedInput = Gdx.input.getInputProcessor();
		Gdx.input.setInputProcessor(null);

		GraphicsType t = Gdx.graphics.getType();
		Gdx.app.log(this.getClass().getName(), t.name());

		Window[] windows = Window.getWindows();
		Gdx.app.log(this.getClass().getName(), "Found " + windows.length
				+ " windows.");
		for (Window win : windows) {
			Gdx.app.log(this.getClass().getName(), win.getClass().getName());
		}

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {

				JPanel panel = new JPanel(new FlowLayout());

				@SuppressWarnings("serial")
				JPanel textPanel = new JPanel() {
					public boolean isOptimizedDrawingEnabled() {
						return false;
					};
				};

				textPanel.setLayout(new OverlayLayout(textPanel));
				panel.add(textPanel);

				final JTextField textField = new JTextField(20);
				textField.setText(text);
				textField.setAlignmentX(0.0f);
				textPanel.add(textField);

				final JLabel placeholderLabel = new JLabel(hint);
				placeholderLabel.setForeground(Color.GRAY);
				placeholderLabel.setAlignmentX(0.0f);
				textPanel.add(placeholderLabel, 0);

				textField.getDocument().addDocumentListener(
						new DocumentListener() {

							@Override
							public void removeUpdate(DocumentEvent arg0) {
								this.updated();
							}

							@Override
							public void insertUpdate(DocumentEvent arg0) {
								this.updated();
							}

							@Override
							public void changedUpdate(DocumentEvent arg0) {
								this.updated();
							}

							private void updated() {
								if (textField.getText().length() == 0)
									placeholderLabel.setVisible(true);
								else
									placeholderLabel.setVisible(false);
							}
						});

				JOptionPane pane = new JOptionPane(panel,
						JOptionPane.QUESTION_MESSAGE,
						JOptionPane.OK_CANCEL_OPTION, null, null, null);

				pane.setInitialValue(null);
				pane.setComponentOrientation(JOptionPane.getRootFrame()
						.getComponentOrientation());

				Border border = textField.getBorder();
				placeholderLabel.setBorder(new EmptyBorder(border
						.getBorderInsets(textField)));

				JFrame taskbar = new JFrame(title);
				taskbar.setUndecorated(true);
				taskbar.setVisible(true);
				taskbar.setLocationRelativeTo(null);
				JDialog dialog = pane.createDialog(taskbar, title);

				pane.selectInitialValue();

				dialog.addWindowFocusListener(new WindowFocusListener() {

					@Override
					public void windowLostFocus(WindowEvent arg0) {
					}

					@Override
					public void windowGainedFocus(WindowEvent arg0) {
						textField.requestFocusInWindow();
					}
				});

				dialog.setVisible(false);

				int x = Display.getX() + Display.getWidth() / 2;
				int y = Display.getY() + Display.getHeight() / 2;
				dialog.setLocation(x, y);

				Gdx.app.log(this.getClass().getName(),
						"setting dialog options: " + x + "x" + y);
				dialog.setModalityType(Dialog.ModalityType.TOOLKIT_MODAL);
				dialog.setAlwaysOnTop(true);

				Gdx.app.log(this.getClass().getName(), "showing dialog");
				dialog.setVisible(true);

				dialog.dispose();

				Object selectedValue = pane.getValue();

				if (selectedValue != null
						&& (selectedValue instanceof Integer)
						&& ((Integer) selectedValue).intValue() == JOptionPane.OK_OPTION) {
					listener.input(textField.getText());
					Gdx.input.setInputProcessor(savedInput);
				} else {
					listener.canceled();
					Gdx.input.setInputProcessor(savedInput);
				}

			}
		});
	}	
}
