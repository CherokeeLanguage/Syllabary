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

import java.io.*;
import javax.sound.sampled.*;

/**
 * A sound is merely a sample encoded in a certain manner.
 */
public class Sound {

    /**
     * Determines the type of pulse of a sound value.
     */
    public enum PULSE_TYPE
    {
        PCM,
        UPCM,
        ULAW,
        ALAW,
    };

    Sound()
    {
    }

    /**
     * Loads a sound from the disk. The sound can be in WAV format, AIFF or AU.
     * @param _file Filename of the sound.
     * @return NOERR if no error occured.
     */
    OZMod.ERR load(File _file)
    {
        AudioInputStream stream;
        try {
            stream = AudioSystem.getAudioInputStream(_file);
        }
        catch(IOException e) {
            return OZMod.proceedError(OZMod.ERR.FILENOTFOUND);
        }
        catch(UnsupportedAudioFileException e) {
            return OZMod.proceedError(OZMod.ERR.INVALIDFORMAT);
        }

        file_ = _file;

        try {
            clip_ = AudioSystem.getClip(OZMod.usedMixer_.getMixerInfo());
            clip_.open(stream);
        }
        catch(LineUnavailableException e) {
            return OZMod.proceedError(OZMod.ERR.DEVICESATURATE);
        }
        catch(IOException e) {
            return OZMod.proceedError(OZMod.ERR.DEVICESATURATE);
        }

        AudioFormat format = stream.getFormat();
        nbChannels_ = format.getChannels();
        frequency_ = (int) format.getSampleRate();
        nbBits_ = format.getSampleSizeInBits();
        formatDesc_ = format.toString();

        return OZMod.proceedError(OZMod.ERR.NOERR);
    }

    /**
     * Gets the number of channel(s) of the sound. 
     * @return 1 for mono, 2 for stereo
     */
    public int getNbChannels()
    {
        return nbChannels_;
    }

    /**
     * Gets the number of bits of each sample of the sound. 
     * @return Typically 8 or 16.
     */
    public int getNbBitsPerSample()
    {
        return nbBits_;
    }

    /**
     * Gets the frequency of the sound. 
     * @return The frequency.
     */
    public int getFrequency()
    {
        return frequency_;
    }

    /**
     * Gets the format description of the sound under the form of a string.
     * @return The format description into a string.
     */
    public String getFormatDescription()
    {
        return formatDesc_;
    }

    /**
     * Plays the sound in loop or not. 
     * @param _loop true to play the sound in loop, false otherwhise.
     */
    public void play(boolean _loop)
    {
        if (_loop == false)
            clip_.start();
        else
            clip_.loop(Clip.LOOP_CONTINUOUSLY);
    }

    /**
     * Sets loop points if you choose to replay the song in loop. 
     * @param _start Start loop in number of frames of samples.
     * @param _end End loop in number of frames of samples.
     */
    public void setLoop(int _start, int _end)
    {
        clip_.setLoopPoints(_start, _end);
    }

    /**
     * Duplicate the sound. Warning this also duplicate samples of the song and so is not memory friendly. However, this is the only possible native Java method. 
     * @return The duplicated Sound instance.
     */
    public Sound duplicate()
    {
        Sound sound = new Sound();
        sound.load(file_);
        return sound;
    }

    /**
     * Gets the native Java Clip instance to manipulate the sound at your will. This is at your own risk. 
     * @return The native Java Clip.
     */
    public Clip getClip()
    {
        return clip_;
    }

    Clip clip_;
    int nbChannels_;
    int frequency_;
    int nbBits_;
    PULSE_TYPE pulseType_;
    String formatDesc_;
    File file_;
}
