package com.cherokeelessons.syllabary.one;

import org.robovm.apple.foundation.NSAutoreleasePool;
import org.robovm.apple.uikit.UIApplication;

import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import com.badlogic.gdx.backends.iosrobovm.IOSApplicationConfiguration;

public class IOSLauncher extends IOSApplication.Delegate {
    @Override
    protected IOSApplication createApplication() {
        IOSApplicationConfiguration config = new IOSApplicationConfiguration();
        config.allowIpod=true;
        config.orientationLandscape=true;
        config.orientationPortrait=false;
        config.displayScaleLargeScreenIfNonRetina=1.0f;
        config.displayScaleLargeScreenIfRetina=1.0f;
        config.displayScaleSmallScreenIfNonRetina=1.0f;
        config.displayScaleSmallScreenIfRetina=1.0f;
        config.preventScreenDimming=true;
        return new IOSApplication(new SyllabaryApp(), config);
    }

    public static void main(String[] argv) {
        NSAutoreleasePool pool = new NSAutoreleasePool();
        UIApplication.main(argv, null, IOSLauncher.class);
        pool.close();
    }
}