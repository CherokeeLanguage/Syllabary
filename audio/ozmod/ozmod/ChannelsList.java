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

class ChannelsList {

    static final int MAX_NB_CHANNELS = 128;

    ChannelsList()
    {
        chansList_ = new Channel[MAX_NB_CHANNELS];
    }

     void addChannel(Channel _chan)
     {
         for (int i = 0; i < MAX_NB_CHANNELS; i++) {
             if (chansList_[i] == null) {
                 chansList_[i] = _chan;
                 _chan.ramp = Channel.RAMP_IN;
                 _chan.rampVolume = 0;
                 _chan.rampStep = (1.0f / (5.0f / 1000.0f)) / 44100.0f; // ramp in 5ms, experimental
                 break;
             }
         }
     }

     void removeChannel(Channel _chan)
     {
         for (int i = 0; i < MAX_NB_CHANNELS; i++) {
             if (chansList_[i] == _chan) {
                 chansList_[i] = null;
                 _chan.ramp = Channel.RAMP_NONE;

                 for (int j = 0; j < MAX_NB_CHANNELS; j++) {
                     if (chansList_[j] == null) {
                         chansList_[j] = new Channel();
                         chansList_[j].audio = _chan.audio;
                         chansList_[j].frequency = _chan.frequency;
                         chansList_[j].pos = _chan.pos;
                         chansList_[j].step = _chan.step;
                         chansList_[j].leftFactor = _chan.leftFactor;
                         chansList_[j].rightFactor = _chan.rightFactor;
                         chansList_[j].ramp = Channel.RAMP_OUT;
                         chansList_[j].vol = _chan.vol;
                         chansList_[j].rampVolume = _chan.rampVolume;
                         chansList_[j].rampStep = _chan.rampStep;
                         break;
                     }
                 }
                 break;
             }
         }
     }

     void purgeChannel(Channel _chan)
     {
         for (int i = 0; i < MAX_NB_CHANNELS; i++) {
             if (chansList_[i] == _chan) {
                 chansList_[i] = null;
                 _chan.ramp = Channel.RAMP_NONE;
                 break;
             }
         }
     }
     
     boolean isPresent(Channel _chan)
     {
         for (int i = 0; i < MAX_NB_CHANNELS; i++) {
             if (chansList_[i] == _chan)
                 return true;
         }
         return false;
     }
     
     void empty()
     {
         for (int i = 0; i < MAX_NB_CHANNELS; i++)
             chansList_[i] = null;
     }

     void mix(int _nbSamp, byte[] _buf)
     {
         for (int i = 0; i < MAX_NB_CHANNELS; i++) {
             Channel chan = chansList_[i];
             if (chan == null)
                 continue;
           
             AudioData audio = chan.audio;
             byte data[] = audio.pcm_;

             int off = 0;
             
             if (audio.nbChans_ == 1)
             {
                 for (int j = 0; j < _nbSamp; j++) {
                     int posToRead = (int) (Math.floor(chan.pos));
                     float level = chan.pos - posToRead;

                     short pcm;
                     short nextpcm;
                     if (audio.nbBits_ == 8) {
                         pcm = (short) (data[posToRead] << 8);
                         nextpcm = (short) (data[posToRead+1] << 8);
                     }
                     else {
                         pcm = (short) (data[posToRead*2+0] & 0xff);
                         pcm |= (short) (data[posToRead*2+1] << 8);
                         nextpcm = (short) (data[posToRead*2+2] & 0xff);
                         nextpcm |= (short) (data[posToRead*2+3] << 8);
                     }

                     pcm = (short) (pcm * (1.0f-level));
                     pcm += (short) (nextpcm*level);           
                     pcm *= chan.vol * volume_;

                     if (chan.ramp == Channel.RAMP_IN) {
                         chan.rampVolume += chan.rampStep;
                         if (chan.rampVolume >= 1) {
                             chan.rampVolume = 1;
                             chan.ramp = Channel.RAMP_NONE;
                         }
                         pcm *= chan.rampVolume;
                     }
                     else if (chan.ramp == Channel.RAMP_OUT) {
                         chan.rampVolume -= chan.rampStep;
                         if (chan.rampVolume <= 0) {
                             chan.rampVolume = 0;
                             purgeChannel(chan);
                             continue;
                         }
                         pcm *= chan.rampVolume;
                     }

                     int b1L = _buf[off+0];
                     int b2L = _buf[off+1];
                     b1L &= 0xff;
                     b2L &= 0xff;
                     short curL = (short) ((b1L << 8) | b2L);

                     int b1R = _buf[off+2];
                     int b2R = _buf[off+3];
                     b1R &= 0xff;
                     b2R &= 0xff;
                     short curR = (short) ((b1R << 8) | b2R);

                     int resL = (int) (pcm * chan.leftFactor) + curL;
                     if (resL < -32768)
                         resL = -32768;
                     if (resL > 32767)
                         resL = 32767;

                     int resR = (int) (pcm * chan.rightFactor) + curR;
                     if (resR < -32768)
                         resR = -32768;
                     if (resR > 32767)
                         resR = 32767;

                     _buf[off+0] = (byte) (resL >> 8);
                     _buf[off+1] = (byte) (resL & 0xff);
                     _buf[off+2] = (byte) (resR >> 8);
                     _buf[off+3] = (byte) (resR & 0xff);

                     off += 4;
                     if (off == _buf.length)
                         off = 0;

                     chan.pos += chan.step;

                     if (audio.endLoop_ == 0) {
                         if ( (int) (Math.floor(chan.pos)) >= audio.endFrame_) {
                             chansList_[i] = null;
                             break;
                         }
                     }
                     else {
                         if (chan.pos >= audio.endLoop_) {
                             while(chan.pos >= audio.endLoop_)
                                 chan.pos -= audio.endLoop_ - audio.startLoop_;
                         }
                     }
                 }
             }
             else if (audio.nbChans_ == 2)
             {
                 for (int j = 0; j < _nbSamp; j++) {
                     int posToRead = (int) (Math.floor(chan.pos));
                     float level = chan.pos - posToRead;

                     short pcmL, pcmR;
                     short nextpcmL, nextpcmR;
                     if (audio.nbBits_ == 8) {
                         pcmL = (short) (data[posToRead*2] << 8);
                         pcmR = (short) (data[posToRead*2+1] << 8);
                         nextpcmL = (short) (data[posToRead*2+2] << 8);
                         nextpcmR = (short) (data[posToRead*2+3] << 8);
                     }
                     else {
                         pcmL = (short) (data[posToRead*4+0] & 0xff);
                         pcmL |= (short) (data[posToRead*4+1] << 8);
                         pcmR = (short) (data[posToRead*4+2] & 0xff);
                         pcmR |= (short) (data[posToRead*4+3] << 8);
                         nextpcmL = (short) (data[posToRead*4+4] & 0xff);
                         nextpcmL |= (short) (data[posToRead*4+5] << 8);
                         nextpcmR = (short) (data[posToRead*4+6] & 0xff);
                         nextpcmR |= (short) (data[posToRead*4+7] << 8);
                     }

                     if (chan.ramp == Channel.RAMP_IN) {
                         chan.rampVolume += chan.rampStep;
                         if (chan.rampVolume >= 1) {
                             chan.rampVolume = 1;
                             chan.ramp = Channel.RAMP_NONE;
                         }
                         pcmL *= chan.rampVolume;
                         pcmR *= chan.rampVolume;
                     }
                     else if (chan.ramp == Channel.RAMP_OUT) {
                         chan.rampVolume -= chan.rampStep;
                         if (chan.rampVolume <= 0) {
                             chan.rampVolume = 0;
                             purgeChannel(chan);
                             continue;
                         }
                         pcmL *= chan.rampVolume;
                         pcmR *= chan.rampVolume;
                     }
                     
                     pcmL = (short) (pcmL * (1.0f-level));
                     pcmL += (short) (nextpcmL*level);           
                     pcmL *= chan.vol * volume_;

                     pcmR = (short) (pcmR * (1.0f-level));
                     pcmR += (short) (nextpcmR*level);           
                     pcmR *= chan.vol * volume_;
                     
                     int b1L = _buf[off+0];
                     int b2L = _buf[off+1];
                     b1L &= 0xff;
                     b2L &= 0xff;
                     short curL = (short) ((b1L << 8) | b2L);

                     int b1R = _buf[off+2];
                     int b2R = _buf[off+3];
                     b1R &= 0xff;
                     b2R &= 0xff;
                     short curR = (short) ((b1R << 8) | b2R);

                     int resL = (int) (pcmL * chan.leftFactor) + curL;
                     if (resL < -32768)
                         resL = -32768;
                     if (resL > 32767)
                         resL = 32767;

                     int resR = (int) (pcmR * chan.rightFactor) + curR;
                     if (resR < -32768)
                         resR = -32768;
                     if (resR > 32767)
                         resR = 32767;

                     _buf[off+0] = (byte) (resL >> 8);
                     _buf[off+1] = (byte) (resL & 0xff);
                     _buf[off+2] = (byte) (resR >> 8);
                     _buf[off+3] = (byte) (resR & 0xff);

                     off += 4;
                     if (off == _buf.length)
                         off = 0;

                     chan.pos += chan.step;

                     if (audio.endLoop_ == 0) {
                         if ( (int) (Math.floor(chan.pos)) >= audio.endFrame_) {
                             chansList_[i] = null;
                             break;
                         }
                     }
                     else {
                         if (chan.pos >= audio.endLoop_) {
                             while(chan.pos >= audio.endLoop_)
                                 chan.pos -= audio.endLoop_ - audio.startLoop_;
                         }
                     }
                 }
             }
         }
     }

     Channel chansList_[];
     Channel rampOutChans_[];
     float volume_ = 0.2f;
 }
