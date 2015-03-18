/*
OZMod - Java Sound Library
Copyright (C) 2012 by Igor Kravtchenko

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

Contact the author: igor@tsarevitch.org
*/

package ozmod;

import javax.sound.sampled.*;

import java.io.*;
import java.net.*;

/**
 * The OZMod kernel where output device are managed.
 * @author tsar
 */
public class OZMod {

    /**
     * Determines an error occured during an operation.
     */
    static public enum ERR
    {
        /** No error.*/ NOERR,
        /** The device has been already initialised.*/ ALREADYINIT,
        /** The method needs an output initialisation to proceed.*/ NEEDINIT,
        /** The file has not been found.*/ FILENOTFOUND,
        /** The format is invalid.*/ INVALIDFORMAT,
        /** The device saturate, no more operation is allowed.*/ DEVICESATURATE,
        /** A read error occured during the operation.*/ READERROR,
        /** The data are in a bad format.*/ BADFORMAT,
        /** Reserved for unknown error.*/ UNKNOWN
    }

    public OZMod()
    {
        int i, k;
        int len;

        Mixer.Info mixerInfo[] = AudioSystem.getMixerInfo();

        // Build OutputDevice

        len = mixerInfo.length;

        k = 0;
        for (i = 0; i < len; i++) {

            Mixer mixer = AudioSystem.getMixer( mixerInfo[i] );

            Line.Info infoLine[] = mixer.getSourceLineInfo();

            if (infoLine.length == 0)
                continue;

            k++;
        }

        outputMixer_ = new Mixer[k];

        k = 0;
        for (i = 0; i < len; i++) {

            Mixer mixer = AudioSystem.getMixer( mixerInfo[i] );

            Line.Info infoLine[] = mixer.getSourceLineInfo();

            if (infoLine.length == 0)
                continue;

            outputMixer_[k++] = mixer;
        }
        
        lastError_ = ERR.NOERR;
    }

    public void finalize()
    {
    }

    /**
     * Gets the number of output device
     * @return The number of output device.
     */
    public int getNbOutput()
    {
        return outputMixer_.length;
    }
    
    /**
     * Gets the name of an output device.
     * @param _i The zero indexed output device.
     * @return The name of the device.
     */
    public String getOutputName(int _i)
    {
        Mixer.Info info;
        info = outputMixer_[_i].getMixerInfo();
        return info.getName();
    }

    /**
     * Gets the description of an output device.
     * @param _i The zero indexed output device.
     * @return The description of the device.
     */
    public String getOutputDescription(int _i)
    {
        Mixer.Info info;
        info = outputMixer_[_i].getMixerInfo();
        return info.getDescription();
    }

    /**
     * Initializes the standard output device.
     * @return NOERR if no error occured.
     */
    public ERR initOutput()
    {
        return initOutput(0);
    }

    /**
     * Initializes an output device given its index. All output are 44100Hz, 16 bits, stereo.
     * @return NOERR if no error occured.
     */
    public ERR initOutput(int _deviceIndex)
    {
        if (initialised_ == true)
            return proceedError(ERR.ALREADYINIT);

        initialised_ = true;

        usedMixer_ = outputMixer_[_deviceIndex];

        return proceedError(ERR.NOERR);
    }
    
    /**
     * Gets a clips sound. The sound can be in WAV format, AIFF or AU.
     * @param _file The disk file of the sound.
     * @return The Sound instance or null if an error occured. If an error occured use getLastError() to determine the error.
     */
    public Sound getClipSound(File _file)
    {
        Sound sound = new Sound();
        sound.load(_file);
        if (sound.getClip() == null)
            return null;
        return sound;
    }

    /**
     * Swap the low and high bytes of a word.
     * @param _value The word to swap the bytes.
     * @return The swapped word.
     */
    public static short wordSwap(short _value)
    {
        short block1 = (short) (_value << 8);
        short block2 = (short) ((_value >> 8) & 0xff);
        return (short) (block1 | block2);
    }

    /**
     * Gets the last error occured. An error is usually directly returned by a method, but this method keeps a track of any error.
     * @return The last error occured. If NOERR is returned, no error happened during the last OZMOd call.
     */
    static final public ERR getLastError()
    {
        return lastError_;
    }
    
    static final ERR proceedError(ERR _err)
    {
        lastError_ = _err;
        return _err;
    }

    boolean initialised_ = false;
    static ERR lastError_;

    Mixer outputMixer_[];
    public static Mixer usedMixer_;
    
    static int g_squareTable[] = {
            255, 255, 255, 255, 255, 255, 255, 255,
            255, 255, 255, 255, 255, 255, 255, 255,
            255, 255, 255, 255, 255, 255, 255, 255,
            255, 255, 255, 255, 255, 255, 255, 255,
            -255, -255, -255, -255, -255, -255, -255, -255,
            -255, -255, -255, -255, -255, -255, -255, -255,
            -255, -255, -255, -255, -255, -255, -255, -255,
            -255, -255, -255, -255, -255, -255, -255, -255,
    };

}
