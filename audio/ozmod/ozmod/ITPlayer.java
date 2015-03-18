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

import java.util.*;
import java.io.*;
import javax.sound.sampled.*;
import java.lang.*;

/**
 * A Class to replay IT file.
 */
public class ITPlayer extends Thread
{
    static final int IT_MINPERIOD = 40;
    static final int IT_MAXPERIOD = 10000;
    
    int g_ITperiod[] = { 1712, 1616, 1524, 1440, 1356, 1280, 1208, 1140, 1076, 1016, 960, 907 };

    byte g_fineSineData[] = {
        0,  2,  3,  5,  6,  8,  9, 11, 12, 14, 16, 17, 19, 20, 22, 23,
        24, 26, 27, 29, 30, 32, 33, 34, 36, 37, 38, 39, 41, 42, 43, 44,
        45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 56, 57, 58, 59,
        59, 60, 60, 61, 61, 62, 62, 62, 63, 63, 63, 64, 64, 64, 64, 64,
        64, 64, 64, 64, 64, 64, 63, 63, 63, 62, 62, 62, 61, 61, 60, 60,
        59, 59, 58, 57, 56, 56, 55, 54, 53, 52, 51, 50, 49, 48, 47, 46,
        45, 44, 43, 42, 41, 39, 38, 37, 36, 34, 33, 32, 30, 29, 27, 26,
        24, 23, 22, 20, 19, 17, 16, 14, 12, 11,  9,  8,  6,  5,  3,  2,
        0, -2, -3, -5, -6, -8, -9,-11,-12,-14,-16,-17,-19,-20,-22,-23,
        -24,-26,-27,-29,-30,-32,-33,-34,-36,-37,-38,-39,-41,-42,-43,-44,
        -45,-46,-47,-48,-49,-50,-51,-52,-53,-54,-55,-56,-56,-57,-58,-59,
        -59,-60,-60,-61,-61,-62,-62,-62,-63,-63,-63,-64,-64,-64,-64,-64,
        -64,-64,-64,-64,-64,-64,-63,-63,-63,-62,-62,-62,-61,-61,-60,-60,
        -59,-59,-58,-57,-56,-56,-55,-54,-53,-52,-51,-50,-49,-48,-47,-46,
        -45,-44,-43,-42,-41,-39,-38,-37,-36,-34,-33,-32,-30,-29,-27,-26,
        -24,-23,-22,-20,-19,-17,-16,-14,-12,-11, -9, -8, -6, -5, -3, -2,
    };
    
    PipeIn loader_;
    
    String songName_;
    int nbOrders_;
    int nbInstrus_;
    int nbSamples_;
    int nbPatterns_;
    int hdrFlags_;
    int hdrSpecial_;
    
    int mixVol_;
    int initialSpeed_;
    int initialTempo_;
    int panSeparation_;
    int pitchMIDI_;
    boolean bSampleMode_;
    
    int chnsPan_[] = new int[64];
    int chnsVol_[] = new int[64];
    int orders_[];
    
    Instru nullInstru_;
    Instru[] instrus_;
    
    Sample[] samples_;
    
    Pattern patterns_[];
    
    boolean isRunning_ = false;
    Timer timer_ = new Timer();
    byte pcm_[];
    int frequency_;
    SourceDataLine line_ = null;
    
    float fltTimerRate_;
    int intTimerRate_;
    int tick_;
    int speed_;
    int tempo_;
    
    int nbVCs_ = 0;
    
    int songRow_;
    int songPos_;

    Voice voices_[] = new Voice[128];
    ChannelsList chansList_;
    
    int chanRemap_[] = new int[64];
    
    float globalVol_, globalVolBAK_;
    float localVol_;
    float vol_; // localVol unaffected by GAudio's global volume

    boolean bLinearSlide_  = false;
    boolean bOldEffects_ = false;
    
    boolean done_ = false;
    boolean loopable_ = false;
    
    static final int ITCOMMAND_NONE = 0;
    static final int ITCOMMAND_VOLSLIDEUP = 1;
    static final int ITCOMMAND_VOLSLIDEDOWN = 2;
    static final int ITCOMMAND_CHANVOLSLIDEUP = 3;
    static final int ITCOMMAND_CHANVOLSLIDEDOWN = 4;
    static final int ITCOMMAND_GLOBVOLSLIDEUP = 5;
    static final int ITCOMMAND_GLOBVOLSLIDEDOWN = 6;
    static final int ITCOMMAND_PORTAUP = 7;
    static final int ITCOMMAND_PORTADOWN = 8;
    static final int ITCOMMAND_PORTATO = 9;
    static final int ITCOMMAND_TREMOR = 10;
    static final int ITCOMMAND_ARPEGGIO = 11;
    static final int ITCOMMAND_VIBRATO = 12;
    
    static final int IT_NNA_CUT  = 0;
    static final int IT_NNA_CONT = 1;
    static final int IT_NNA_OFF  = 2;
    static final int IT_NNA_FADE = 3;
        
    static final int IT_DCT_DISABLE      = 0;
    static final int IT_DCT_NOTE         = 1;
    static final int IT_DCT_SAMPLE       = 2;
    static final int IT_DCT_INSTRUMENT   = 3;

    static final int IT_DCA_CUT  = 0;
    static final int IT_DCA_OFF  = 1;
    static final int IT_DCA_FADE = 2;
    
    class NodePoint {
            int y = 0;
            int x = 0;
        };

    class Envelope {
        Envelope()
        {
            flags = 0;
        }

        int calc(int pt[], int pos[], boolean bKeyOff[], boolean bFadeOut[], boolean bGotLoop[], boolean bFinished[])
        {
            int x0, y0, x1, y1, actuY;
            NodePoint point[];
            
            if ( (flags & 1) == 0 )
                return -9999;

            int of = pt[0];
            point = nodePoints;

            if ( of == nbPoints - 1) {
                x1 = 1;
                y0 = y1 = point[of].y;
            }
            else {
                x1 = point[of+1].x - point[of+0].x;
                y0 = point[of+0].y;
                y1 = point[of+1].y;
                if (x1 == 0) {
                    x1 = 1;
                    y1 = y0;
                }
            }

            actuY = (int)( (float) ( (y1 - y0) * pos[0]) / x1 + y0 );
        //  printf("%ld\n", actuY);
        //    System.out.println(actuY);

        //  int p = *pos + point->x;
        //  if (*bKeyOff == true)
        //  {
//              p = p;
        //  }
            //printf("%ld\n", *bKeyOff);
            pos[0]++;

            if (bGotLoop != null) bGotLoop[0] = false;
            if ( ((flags&2) != 0) && (pos[0]-1) >= (nodePoints[loopEnd].x - point[of+0].x) ) {
                // normal loop
                pt[0] = loopStart;
                pos[0] = 0;
                if (bGotLoop != null)
                    bGotLoop[0] = true;
            }
            else if ( (flags&4)!=0 && pos[0]-1 >= (nodePoints[sustainLoopEnd].x - point[of+0].x) && bKeyOff[0] == false) {
                // sustain loop
                pt[0] = sustainLoopStart;
                pos[0] = 0;
            }
            else if (pos[0]-1 >= x1) {
                if (pt[0] >= nbPoints-1) {
                    pos[0] = x1;
                    if (bFadeOut != null) bFadeOut[0] = true;
                    if (bFinished != null) bFinished[0] = true;
                }
                else {
                    pt[0]++;
                    pos[0] = 0;
                }
            }

            return actuY;
        }

        int       flags = 0;
        int       nbPoints = 0;
        int       loopStart = 0;
        int       loopEnd = 0;
        int       sustainLoopStart = 0;
        int       sustainLoopEnd = 0;
        NodePoint nodePoints[] = new NodePoint[26]; // 25+1
    };

    class NoteTable {
            int   note = 0;
            int   numSample = 0;
        };

    class Instru {
            int     nna = 0;
            int     dct = 0;
            int     dca = 0;
            float   fadeOut = 0;
            int     pps = 0;
            int     ppc = 0;
            float   globalVol = 0;
            int     defaultPan = 0;
            int     rv = 0;
            int     rp = 0;
            String  name;
            int     ifc = 0;
            int     ifr = 0;
            int     mch = 0;
            int     mpr = 0;
            int     midiBank = 0;
            NoteTable noteTable[] = new NoteTable[120];
            
            Envelope  volEnv = new Envelope();
            Envelope  panEnv = new Envelope();
            Envelope  pitchEnv = new Envelope();
        };

    class Sample {
//            Sample();
  //          ~Sample();

            String  name;
            float   globalVol = 0;
            int     flags = 0;
            float   defaultVol = 0;
            int     cvt = 0;
            float   defaultPan = 0;
            int     len = 0;
            int     loopStart = 0;
            int     loopEnd = 0;
            int     c5Speed = 0;
            int     sustainLoopStart = 0;
            int     sustainLoopEnd = 0;
            int     vibSpeed = 0;
            int     vibDepth = 0;
            int     vibRate = 0;
            int     vibType = 0;
            boolean bGotPan = false;
            AudioData audioData = new AudioData();
        };

    
        class Note {
            
                Note()
                {
                    note = 0;
                    numInstru = -1;
                    vol = -10.0f;
                    pan = -10.0f;
                    extraCommand = 0;
                    command = 0;
                    commandVal = 0;
                }

                int     note;
                int     numInstru;
                float   vol, pan;
                int     extraCommand;
                int     command, commandVal;
            };

            class Column {
                Note notes[];
            };

            class Pattern {
                int         nbRows;
                Column      columns[] = new Column[64];
            };

    class Voice {
   //         Voice();
     //       ~Voice();
  //          void        updateSoundWithEffect();
  //          void        updateSoundWithEffect2();
   //         void        updateSoundWithEffect3();
    //        void        doVibrato();
     //       void        updateFadeOut();
      //      void        soundUpdate();
       //     void        evalue_DCommand(int command);
        //    void        evalue_NCommand(int command);
         //   void        evalue_WCommand(int command);
          //  void        slidePeriod(int slide);
       //     void        tryToFree();

        void slidePeriod(int slide)
        {
            if (slide == 0)
                return;

            if (bLinearSlide_ == false)
                period_ += slide;
            else
                period_ *= Math.pow(2.0, slide / 768.0 );

            if (period_ < IT_MINPERIOD)
                period_ = IT_MINPERIOD;
            else if (period_ > IT_MAXPERIOD)
                period_ = IT_MAXPERIOD;

            periodBAK_ = period_;
        }

        void updateSoundWithEffect()
        {
            switch(command_) {

            case ITCOMMAND_VOLSLIDEUP:
                vol_ += (lastDCommandParam_ >> 4) / 64.0f;
                if (vol_ > 1.0f) vol_ = 1.0f;
                volBAK_ = vol_;
                break;

            case ITCOMMAND_VOLSLIDEDOWN:
                vol_ -= (lastDCommandParam_ &0x0f) / 64.0f;
                if (vol_ < 0.0f) vol_ = 0.0f;
                volBAK_ = vol_;
                break;

            case ITCOMMAND_CHANVOLSLIDEUP:
                chanVol_ += (lastNCommandParam_ >> 4) / 64.0f;
                if (chanVol_ > 1.0f) chanVol_ = 1.0f;
                break;

            case ITCOMMAND_CHANVOLSLIDEDOWN:
                chanVol_ -= (lastNCommandParam_ &0x0f) / 64.0f;
                if (chanVol_ < 0.0f) chanVol_ = 0.0f;
                break;

            case ITCOMMAND_GLOBVOLSLIDEUP:
                globalVol_ += (lastWCommandParam_ >> 4) / 128.0f;
                if (globalVol_ > 1.0f) globalVol_ = 1.0f;
                break;

            case ITCOMMAND_GLOBVOLSLIDEDOWN:
                globalVol_ -= (lastWCommandParam_ &0x0f) / 128.0f;
                if (globalVol_ < 0.0f) globalVol_ = 0.0f;
                break;

            case ITCOMMAND_PORTAUP:
                slidePeriod( -lastEFG_ * 4);
                break;

            case ITCOMMAND_PORTADOWN:
                slidePeriod(lastEFG_ * 4);
                break;

            case ITCOMMAND_PORTATO:
                if (period_ < dstPeriod_) {
                    slidePeriod(lastEFG_ * 4);
                    if (period_ > dstPeriod_) period_ = (float) dstPeriod_;
                    periodBAK_ = period_;
                }
                else if (period_ > dstPeriod_) {
                    slidePeriod(-lastEFG_ * 4);
                    if (period_ < dstPeriod_) period_ = (float) dstPeriod_;
                    periodBAK_ = period_;
                }
                break;
            }
        }

        void updateSoundWithEffect2()
        {
            int on, off, pos;
            int periodOffset;
            int seek;

            switch(command_) {

            case ITCOMMAND_TREMOR:
                on = lastI_ >> 4;
                off = lastI_ &0xf;
                pos = tremorCounter_ % (on + off + 1);
                if (pos < on) vol_ = volBAK_;
                else vol_ = 0;
                //(pos < on) ? vol_ = volBAK_ : vol_ = 0;
                tremorCounter_++;
                break;

            case ITCOMMAND_VIBRATO:
                seek = (vibCounter_ << 2) & 0xff;

                switch(vibType_) {
                default:
                    periodOffset = g_fineSineData[seek] << 2;
                }

                periodOffset *= vibDepth_;
                periodOffset >>= 7;

                if (bOldEffects_ == true)
                    periodOffset <<= 2;
                else periodOffset <<= 1;

                if (bLinearSlide_ == false)
                    period_ = periodBAK_ + periodOffset;
                else
                    period_ = periodBAK_ * (float) Math.pow(2.0, periodOffset / 768.0 );
            
                vibCounter_ += vibSpeed_;
                break;
            }
        }

        void updateSoundWithEffect3()
        {
            if (samplePlaying_ == null)
                return;

            switch(command_) {

            case ITCOMMAND_ARPEGGIO:
                switch(arpeggioCounter_ % 3) {
                case 0:
                    period_ = (float)getPeriod(note_ + (lastJ_ >> 4), samplePlaying_.c5Speed);
                    break;
                case 1:
                    period_ = (float)getPeriod(note_ + (lastJ_ & 0x0f), samplePlaying_.c5Speed);
                    break;
                case 2:
                    period_ = (float)getPeriod(note_, samplePlaying_.c5Speed);
                    break;
                }
                arpeggioCounter_++;
                break;
            }
        }

        void doVibrato()
        {
            int periodOffset;
            int seek;

            if (samplePlaying_ == null)
                return;

            int depth = samplePlaying_.vibDepth;
            if (depth == 0)
                return;

            int speed = samplePlaying_.vibRate;
            int rate = samplePlaying_.vibSpeed;
            int vibType = samplePlaying_.vibType;

            seek = (vibCounter_s_ ) & 0xff;

            switch(vibType) {

            default:
            case 0:
                // sinus (default)
                periodOffset = g_fineSineData[seek] << 2;
                break;
            case 1:
                // ramp down
                periodOffset = (((seek+128)%255)-128) << 1;
                break;
            case 2:
                // square
                periodOffset = OZMod.g_squareTable[seek>>2];
                break;
            case 4:
                //ramp up
                periodOffset = (-((seek+128)%255)+128) << 1;
                break;
            }

            periodOffset *= vibCount_s_ >> 8;
            periodOffset >>= 8;

        //  printf("%ld\n", periodOffset);

            if ( (bGotVibrato_ == true) || (bGotVibratoU_ == true) )
                period_ *= Math.pow(2.0, periodOffset / 768.0 );
            else {
                if (command_ == ITCOMMAND_ARPEGGIO)
                    period_ *= Math.pow(2.0, periodOffset / 768.0 );
                else
                    period_ = periodBAK_ * (float) Math.pow(2.0, periodOffset / 768.0 );
            }

            vibCount_s_ += speed << 1;
            if ( (vibCount_s_ >> 8) > depth)
                vibCount_s_ = depth << 8;

            vibCounter_s_ += rate;
        }

        void updateFadeOut()
        {
            if (actuInstru_ == null || samplePlaying_ == null)
                return;

            if (tick_ < noteDelay_)
                return;

            noteDelay_ = 0;

            if (bKeyOff_[0] == true && (actuInstru_.volEnv.flags&1)==0)
                bFadeOut_[0] = true;
            else if (bKeyOff_[0] == true && bGotLoop_[0] == true)
                bFadeOut_[0] = true;

            if (bFadeOut_[0] == true)
                fadeVol_ -= actuInstru_.fadeOut;
        }
        
        void soundUpdate()
        {
            int i;
            int freq;
            float vol, pan;
            int cutoff;

            if (samplePlaying_ == null)
                return;

            if (tick_ < noteDelay_)
                return;
            noteDelay_ = 0;

            float periodBAK = period_;
            if (bGotFilterEnv_ == false)
                period_ *= Math.pow(2.0, (envPitch_ / 768.0) );
            freq = (int)( 14317056 / period_ );
            freq = (int)( (3579546 << 2) / period_ );
            period_ = periodBAK;

        //  printf("%ld \n", (int)period_ );

        //  if (samplePlaying_->gasound[numVoice_]->nbChans_ == 2)
//              freq *= 2;

            noteCut_--;
            if (noteCut_ <= -1) {
                chansList_.removeChannel(sndchan_);
                //sndchan_.stop();
//              if (samplePlaying_)
//                  samplePlaying_->gasound[numVoice_]->stop();
                return;
            }

            if (bGotRetrigger_ == true) {
                int nbTicks = lastQ_ & 0xf;
                if (nbTicks == 0) nbTicks = 1;
                if ( ((tick_ - 1) % nbTicks == 0) && (tick_ != 1) ) {
                    bNeedToBePlayed_ = true;
                    int fv = lastQ_ >> 4;
                    if (fv == 1) vol_ -= 1 / 64.0f;
                    else if (fv == 2) vol_ -= 2 / 64.0f;
                    else if (fv == 3) vol_ -= 4 / 64.0f;
                    else if (fv == 4) vol_ -= 8 / 64.0f;
                    else if (fv == 5) vol_ -= 16 / 64.0f;
                    else if (fv == 6) vol_ *= 2.0f / 3.0f;
                    else if (fv == 7) vol_ *= 0.5f;
                    else if (fv == 9) vol_ += 1 / 64.0f;
                    else if (fv == 0xa) vol_ += 1 / 64.0f;
                    else if (fv == 0xb) vol_ += 2 / 64.0f;
                    else if (fv == 0xc) vol_ += 3 / 64.0f;
                    else if (fv == 0xd) vol_ += 4 / 64.0f;
                    else if (fv == 0xe) vol_ *= 3.0f / 2.0f;
                    else if (fv == 0xf) vol_ *= 2.0f;
                    if (vol_ < 0.0f) vol_ = 0.0f;
                    else if (vol_ > 1.0f) vol_ = 1.0f;
                }
            }

            vol = vol_ * samplePlaying_.globalVol * chanVol_ * actuInstru_.globalVol * globalVol_ * localVol_ * fadeVol_ * envVol_;
            finalVol_ = vol;

            pan = pan_ + envPan_ * (1.0f - Math.abs(pan_) );
            if (pan < -1.0f) pan = -1.0f;
            else if (pan > 1.0f) pan = 1.0f;
        //  printf("%ld \n", (int)(pan*100) );

            if (bGotFilterEnv_ == false) {
                cutoff = cutOff_;
            }
            else {
                cutoff = (int) envPitch_;
            }

        //  printf("%ld \n", cutoff );

            if (bNeedToBePlayed_ == false) {
                sndchan_.frequency = freq;
                sndchan_.step = freq / (float) (frequency_);
                sndchan_.setPan(pan);
                sndchan_.vol = vol;

 //               if (cutoff != -1) {
   //                 sndchan_.setCutOff(cutoff, resonance_);
     //               sndchan_.useCutOff(GATRUE);
     //           }
     //           else
       //             sndchan_.useCutOff(GAFALSE);

//              SamplePlaying->gaSound[numVoice]->DS_checkForLoopAndGetPos();
                return;
            }

            //sndchan_.stop();
            chansList_.removeChannel(sndchan_);

            //sndchan_.bind( samplePlaying_->soundData, childClass_->soundList_ );
            chansList_.removeChannel(sndchan_);
            sndchan_.audio = samplePlaying_.audioData;
            chansList_.addChannel(sndchan_);
            
            sndchan_.frequency = freq;
            sndchan_.step = freq / (float) (frequency_);
            sndchan_.vol = vol;
            sndchan_.setPan(pan);
            sndchan_.pos = (float)( sampleOffset_ << 8);

//            if (cutoff != -1) {
 //               sndchan_.setCutOff(cutoff, resonance_);
  //              sndchan_.useCutOff(GATRUE);
   //         }
   //         else
    //            sndchan_.useCutOff(GAFALSE);

            //sndchan_.play();

            bNeedToBePlayed_ = false;
            return;
        }
        
        void tryToFree()
        {
          if (status_ == 0)
              return;

            if (samplePlaying_ != null) {
                boolean stat = chansList_.isPresent(sndchan_);
                if (stat == false) {
                    if (status_ != 1)
                        status_ = 0;
                    samplePlaying_ = null;
                    return;
                }
            }

            if (envVol_ <= 0.0f && bEnvFinished_[0] == true) {
                if (samplePlaying_ != null) {
                    //sndchan_.stop();
                    chansList_.removeChannel(sndchan_);
                    samplePlaying_ = null;
                }
                if (status_ != 1)
                    status_ = 0;
                return;
            }

            if (fadeVol_ <= 0) {
                if (samplePlaying_ != null) {
//                    sndchan_.stop();
                    chansList_.removeChannel(sndchan_);
                    samplePlaying_ = null;
                }
                if (status_ != 1)
                    status_ = 0;
            }
        }

        void evalue_DCommand(int commandParam)
        {
            if (commandParam != 0)
                lastDCommandParam_ = commandParam;

            int param = lastDCommandParam_;

            int x = param >> 4;
            int y = param &0xf;

            if (y==0 || (y==0x0f && x!=0) ) {
                // VOL SLIDE UP
                if (y == 0) {
                    // normal
                    command_ = ITCOMMAND_VOLSLIDEUP;
                }
                else {
                    // fine vslide
                    vol_ += x / 64.0f;
                    if (vol_ > 1.0f) vol_ = 1.0f;
                    volBAK_ = vol_;
                }
            }
            else if (x==0 || (x==0x0f && y!=0) ) {
                // VOL SLIDE DOWN
                if (x == 0) {
                    // normal
                    command_ = ITCOMMAND_VOLSLIDEDOWN;
                }
                else {
                    // fine vslide
                    vol_ -= y / 64.0f;
                    if (vol_ < 0.0f) vol_ = 0.0f;
                    volBAK_ = vol_;
                }
            }
        }

        void evalue_NCommand(int commandParam)
        {
            if (commandParam != 0)
                lastNCommandParam_ = commandParam;

            int param = lastNCommandParam_;

            int x = param >> 4;
            int y = param &0xf;

            if (y==0 || (y==0x0f && x!=0) ) {
                // VOL SLIDE UP
                if (y == 0) {
                    // normal
                    command_ = ITCOMMAND_CHANVOLSLIDEUP;
                }
                else {
                    // fine vslide
                    chanVol_ += x / 64.0f;
                    if (chanVol_ > 1.0f) chanVol_ = 1.0f;
                }
            }
            else if (x==0 || (x==0x0f && y!=0) ) {
                // VOL SLIDE DOWN
                if (x == 0) {
                    // normal
                    command_ = ITCOMMAND_CHANVOLSLIDEDOWN;
                }
                else {
                    // fine vslide
                    chanVol_ -= y / 64.0f;
                    if (chanVol_ < 0.0f) chanVol_ = 0.0f;
                }
            }
        }

        void evalue_WCommand(int commandParam)
        {
            if (commandParam != 0)
                lastWCommandParam_ = commandParam;

            int param = lastWCommandParam_;

            int x = param >> 4;
            int y = param &0xf;

            if (y==0 || (y==0x0f && x!=0) ) {
                // VOL SLIDE UP
                if (y == 0) {
                    // normal
                    command_ = ITCOMMAND_GLOBVOLSLIDEUP;
                }
                else {
                    // fine vslide
                    globalVol_ += x / 128.0f;
                    if (globalVol_ > 1.0f) globalVol_ = 1.0f;
                }
            }
            else if (x==0 || (x==0x0f && y!=0) ) {
                // VOL SLIDE DOWN
                if (x == 0) {
                    // normal
                    command_ = ITCOMMAND_GLOBVOLSLIDEDOWN;
                }
                else {
                    // fine vslide
                    globalVol_ -= y / 64.0f;
                    if (globalVol_ < 0.0f) globalVol_ = 0.0f;
                }
            }
        }

        
        int         numVoice_;
        int         id_;
        Instru      actuInstru_;
        Sample      samplePlaying_;
        float       period_, periodBAK_;
        float       vol_, volBAK_;
        float       chanVol_;
        float       pan_;
        float       finalVol_;
        boolean     bNeedToBePlayed_;
        int         numInstru_;

        int         tremorCounter_;
        int         arpeggioCounter_;

        Channel     sndchan_ = new Channel();

        int         status_;
        float       fadeVol_;
        float       envVol_, envPan_;
        float       envPitch_;
        boolean     bKeyOff_[] = new boolean[1];
        boolean     bFadeOut_[] = new boolean[1];
        boolean     bGotLoop_[] = new boolean[1];
        boolean     bEnvFinished_[] = new boolean[1];

        int         envVolPos_[] = new int[1];
        int         envVolPoint_[] = new int[1];
        int         envPanPos_[] = new int[1];
        int         envPanPoint_[] = new int[1];
        int         envPitchPos_[] = new int[1];
        int         envPitchPoint_[] = new int[1];
     
        int         nna_;
        int         sampleOffset_;

        int         cutOff_; // -1 = no resonance/cuttof
        int         resonance_; 

        Instru      instru_;

        int         atPos_;
        int         atRow_;

        int         command_;
        int         note_;
        int         lastNote_;
        int         lastDCommandParam_;
        int         lastNCommandParam_;
        int         lastWCommandParam_;
        int         lastEFG_;
        int         lastH_;
        int         lastI_;
        int         lastJ_;
        int         lastO_;
        int         lastQ_;
        int         lastS_;
        int         lastU_;

        int         noteDelay_;

        boolean     bGotTremor_;
        boolean     bGotArpeggio_;
        boolean     bGotLoopBack_;
        boolean     bGotRetrigger_;
        int         loopBackLoc_;
        int         nbLoopBack_;
        int         noteCut_;

        boolean     bGotVibrato_, bGotVibratoU_;
        int         vibCounter_;
        int         vibType_;
        int         vibDepth_, vibSpeed_;

        int         vibCounter_s_;
        int         vibCount_s_;

        int         dstPeriod_;

        boolean     bUpdateNote_;

        boolean     bGotFilterEnv_;
    };

    int srcbuffer[];
    int srcpos[];
    int offsrcpos = 0;
    int srcrembits;

    int shr(int val, int shift)
    {
        for (int i = 0; i < shift; i++) {
            val >>= 1;
        val &= 0x7fffffff;
        }
        return val;
    }

    int readBits(int b)
    {
        int value;
        if (b<=srcrembits) {
            value = srcpos[offsrcpos] & ((1<<b)-1);
            int a = srcpos[offsrcpos];
            a = shr(a, b);
            srcpos[offsrcpos] = a;
            srcrembits -= b;
        }
        else {
            int nbits = b - srcrembits;
            value = srcpos[offsrcpos]; offsrcpos++;
            value |= ((srcpos[offsrcpos] & ((1<<nbits)-1)) << srcrembits);
            int a = srcpos[offsrcpos];
            a = shr(a, nbits);
            srcpos[offsrcpos] = a;
            srcrembits = 32-nbits;
        }
        return value;
    }

    void readblock(PipeIn in)  // gets block of compressed data from file
    {
        int size;
        size = in.readUShort();
        srcbuffer = new int[(size>>2)+20];

        for (int i = 0; i < srcbuffer.length; i++)
            srcbuffer[i] = 0;

        byte allbuf[] = new byte[size+40];
        for (int i = 0; i < allbuf.length; i++)
            allbuf[i] = 0;
        for (int i = 0; i < size; i++) {
            int val = in.readUByte();
            allbuf[i] = (byte) val;
        }

        int fsize = (size >> 2) + 2;
        int p = 0;
        for (int i = 0; i < fsize; i++) {
            int val;
            int b1 = allbuf[p++];
            int b2 = allbuf[p++];
            int b3 = allbuf[p++];
            int b4 = allbuf[p++];
            b1 &= 0xff;
            b2 &= 0xff;
            b3 &= 0xff;
            b4 &= 0xff;
            val = (b4 << 24) | (b3 << 16) | (b2 << 8) | b1;
            srcbuffer[i] = val;
        }

        srcpos = srcbuffer;
        srcrembits = 32;
        offsrcpos = 0;
    }

    FileOutputStream oss;
    
    int decompress8(PipeIn in, byte out[], int len, boolean it215)
    {
        int blklen;      // length of compressed data block in samples
        int blkpos;      // position in block
        int width;       // actual "bit width"
        int value;       // value read from file to be processed
        int d1, d2;     // integrator buffers (d2 for it2.15)

        int outoff = 0;

        for (int i = 0; i < len; i++)
            out[i] = 0;

        // now unpack data till the dest buffer is full
        while (len != 0)
        {
            // read a new block of compressed data and reset variables

            readblock(in);
            blklen=(len<0x8000)?len:0x8000;
            blkpos=0;

            width=9;  // start with width of 9 bits
            d1=d2=0;  // reset integrator buffers

            // now uncompress the data block
            while (blkpos<blklen)
            {
                value = readBits(width) & 0xffff; // read bits

                if (width<7) // method 1 (1-6 bits)
                {
                    if (value==(1<<(width-1))) // check for "100..."
                    {
                        value = readBits(3)+1;                // yes . read new width;
                        width = (byte) ((value<width)? value:value+1);  // and expand it
                        continue;                             // ... next value
                    }
                }
                else if (width<9) // method 2 (7-8 bits)
                {
                    int border = (0xFF>>(9-width)) - 4;  // lower border for width chg
                    border &= 0xff;

                    if (value>border && value <=(border+8))
                    {
                        value-=border;                        // convert width to 1-8
                        width = (byte) ((value<width)?value:value+1);  // and expand it
                        continue;                             // ... next value
                    }
                }
                else if (width==9) // method 3 (9 bits)
                {
                    if ((value & 0x100) != 0) // bit 8 set?
                    {
                        width= (value+1)&0xff; // new width...
                        continue;             // ... and next value
                    }
                }
                else // illegal width, abort
                {
                    return 0;
                }

                // now expand value to signed byte
                int v; // sample value
                if (width<8)
                {
                    int shift=8-width;
                    v = (value<<shift);
                    byte bb = (byte) v;
                    bb >>= shift;
                    v = bb;
                }
                else
                    v = (byte) value;

                // integrate upon the sample values
                d1+=v;
                d2+=d1; 
               /* 
                byte eof[] = { 0xd, 0xa };
                String vv = Integer.toString(v, 10);
                byte bba[] = vv.getBytes();
                try {
                    oss.write(bba);
                    oss.write(eof);
                }
                catch(IOException e) {   
                }
                */
                // ... and store it into the buffer
                out[outoff++] = it215 ? (byte)(d2) : (byte)(d1);
                blkpos++;
            }

            // now subtract block lenght from total length and go on
            len-=blklen;
        }

        return 1;
    }

    int decompress16(PipeIn in, byte out[], int len, boolean it215)
    {
        int blklen;      // length of compressed data block in samples
        int blkpos;      // position in block
        int width;       // actual "bit width"
        int value;      // value read from file to be processed
        short d1, d2;     // integrator buffers (d2 for it2.15)

        int outoff = 0;

        // now unpack data till the dest buffer is full
        while (len != 0)
        {

            // read a new block of compressed data and reset variables

            readblock(in);
            blklen=(len<0x4000)?len:0x4000; // 0x4000 samples => 0x8000 bytes again
            blkpos=0;

            width=17; // start with width of 17 bits
            d1=d2=0;  // reset integrator buffers

            // now uncompress the data block
            while (blkpos<blklen)
            {
                value = readBits(width); // read bits

                if (width<7) // method 1 (1-6 bits)
                {
                    if (value==(1<<(width-1))) // check for "100..."
                    {
                        value = readBits(4)+1;                // yes . read new width;
                        width = (value<width)?value:value+1;  // and expand it
                        continue;                             // ... next value
                    }
                }
                else if (width<17) // method 2 (7-16 bits)
                {
                    int border = (0xFFFF>>(17-width)) - 8;  // lower border for width chg

                    if (value>border && value <=(border+16))
                    {
                        value-=border;                        // convert width to 1-8
                        width = (value<width)?value:value+1;  // and expand it
                        continue;                             // ... next value
                    }
                }
                else if (width==17) // method 3 (17 bits)
                {
                    if ((value&0x10000)!=0) // bit 16 set?
                    {
                        width=(value+1)&0xff; // new width...
                        continue;             // ... and next value
                    }
                }
                else // illegal width, abort
                {
                    return 0;
                }

                // now expand value to signed word
                int v; // sample value
                if (width<16)
                {
                    int shift=16-width;
                    v = (value<<shift);
                    short ss = (short) v;
                    ss >>= shift;
                    v = ss;
                }
                else
                    v = (short) value;

                // integrate upon the sample values
                d1+=v;
                d2+=d1;
                
                // ... and store it into the buffer
                short tostore = it215 ? d2 : d1;
                //out.writeShort(tostore);

                out[outoff++] = (byte)(tostore >> 8);
                out[outoff++] = (byte)(tostore & 0xff);

                blkpos++;

            }

            // now subtract block lenght from total length and go on
            len-=blklen;

        }

        return 1;
    }

    int findFreeVC()
    {
        int i, j;
        for (i = 0; i < nbVCs_; i++) {
            if (voices_[i].status_ == 0)
                return i;
        }

        // Find the virtual channel with the lowest volume
        float minVol = 9999.0f;
        j = -1;
        for (i = 0; i < nbVCs_; i++) {
            if (voices_[i].finalVol_ < minVol && voices_[i].status_ != 1) {
                j = i;
                minVol = voices_[i].finalVol_;
            }
        }
        if (j == -1) // normally should never happen
            return -1;

        //            voices_[j].sndchan_.stop();
        //            voices_[j].samplePlaying_ = NULL;

        chansList_.removeChannel(voices_[j].sndchan_);
        voices_[j].samplePlaying_ = null;

        return j;
    }

    public ITPlayer()
    {
        songPos_ = 0;
        songRow_ = 0;
        nbVCs_ = 0;
        bLinearSlide_  = false;
     }
    
    /**
     * Loads the IT.
     * @param _input An instance to a PipeIn Class to read data from disk or URL.
     * @return NOERR if no error occured.
     */
    public OZMod.ERR load(PipeIn _input)
    {
        int orgNbInstrus;
        int byt;
        int wor;
    
        loader_ = _input;
        loader_.readContent();
        
        if (nbVCs_ == 0)
            nbVCs_ = 80;
        else if (nbVCs_ > 128)
            nbVCs_ = 128;
        
        byte id[] = new byte[4];
        _input.read(id, 0, 4);
        String format = new String(id).substring(0, 4);
        if (format.compareTo("IMPM") != 0)
            return OZMod.proceedError(OZMod.ERR.BADFORMAT);        
            
        byte songName[] = new byte[26];
        _input.read(songName, 0, 26);
        songName_ = new String(songName).substring(0, 25);
        
        _input.forward(2);
        
        nbOrders_ = _input.readUShort();
        nbInstrus_ = _input.readUShort();
        orgNbInstrus = nbInstrus_;
        nbSamples_ = _input.readUShort();
        nbPatterns_ = _input.readUShort();
        int cwt;
        cwt = _input.readUShort();
        
        int version;
        version = _input.readUShort();
        hdrFlags_ = _input.readUShort();
        if ( (hdrFlags_ & 4) == 0) {
            bSampleMode_ = true;
            nbInstrus_ = nbSamples_;
        }

        hdrSpecial_ = _input.readUShort();
        byt = _input.readUByte();
        globalVol_ = byt / 128.0f;
        globalVolBAK_ = globalVol_;

        mixVol_ = _input.readUByte();
        initialSpeed_ = _input.readUByte();
        initialTempo_ = _input.readUByte();
        panSeparation_ = _input.readUByte();
        pitchMIDI_ = _input.readUByte();
        
        _input.forward(2 + 4 + 4);                // MsgLgth + Message Offset + Reserved
        
        for (int i = 0; i < 64; i++) {
            int pan;
            pan = _input.readUByte();
            if (pan > 64)
                pan = 32; // fix, if the pan is not valid, assume it's just centered
            chnsPan_[i] = pan;
        }
        for (int i = 0; i < 64; i++) {
            int vol = _input.readUByte();
            chnsVol_[i] = vol;
        }
        
        orders_ = new int[nbOrders_];
        for (int i = 0; i < nbOrders_; i++) {
            int order = _input.readUByte();
            orders_[i] = order;
        }
            
        nullInstru_ = new Instru();
        nullInstru_.globalVol = 1.0f;
        for (int i = 0; i < 120; i++) {
            nullInstru_.noteTable[i] = new NoteTable();
            nullInstru_.noteTable[i].numSample = -1;
        }

        instrus_ = new Instru[nbInstrus_];
        if (bSampleMode_ == true)
            _input.forward(orgNbInstrus * 4);
        else {
            for (int i = 0; i < nbInstrus_; i++) {
                Instru instru = new Instru();
                instrus_[i] = instru;
                
                int instruOffset;
                instruOffset = _input.readInt();
                
                int oldSeek;
                oldSeek = _input.tell();
                
                _input.seek(instruOffset);
                
                _input.forward(4 + 12 + 1); // 'IMPI' + DOS filename + 00h
                instru.nna = _input.readUByte();
                instru.dct = _input.readUByte();
                instru.dca = _input.readUByte();
                wor = _input.readUShort();
                instru.fadeOut = wor / 1024.0f;
                instru.pps = _input.readUByte();
                instru.ppc = _input.readUByte();
                byt = _input.readUByte();
                instru.globalVol = byt / 128.0f;
                instru.defaultPan = _input.readUByte();
                instru.rv = _input.readUByte();
                instru.rp = _input.readUByte();
                _input.forward(4);
                byte instruName[] = new byte[26];
                _input.read(instruName, 0, 26);
                instru.name = new String(instruName).substring(0, 25);

                instru.ifc = _input.readUByte();
                instru.ifr = _input.readUByte();
                instru.mch = _input.readUByte();
                instru.mpr = _input.readUByte();
                instru.midiBank = _input.readUShort();
                
                for (int j = 0; j < 120; j++) {
                    NoteTable note = new NoteTable();
                    note.note = _input.readUByte();
                    note.numSample = _input.readUByte();
                    instru.noteTable[j] = note;
                 }
             
                Envelope volEnv = instru.volEnv;
                volEnv.flags = _input.readUByte();
                volEnv.nbPoints = _input.readUByte();
                volEnv.loopStart = _input.readUByte();
                volEnv.loopEnd = _input.readUByte();
                volEnv.sustainLoopStart = _input.readUByte();
                volEnv.sustainLoopEnd = _input.readUByte();
                for (int j = 0; j < 25; j++) {
                    NodePoint nodePoint = new NodePoint();
                    volEnv.nodePoints[j] = nodePoint;
                    int val;
                    val = _input.readByte();
                    nodePoint.y = val;
                    val = _input.readUShort();
                    nodePoint.x = val;
                }
                _input.forward(1);
              
                Envelope panEnv = instru.panEnv;
                panEnv.flags = _input.readUByte();
                panEnv.nbPoints = _input.readUByte();
                panEnv.loopStart = _input.readUByte();
                panEnv.loopEnd = _input.readUByte();
                panEnv.sustainLoopStart = _input.readUByte();
                panEnv.sustainLoopEnd = _input.readUByte();
                for (int j = 0; j < 25; j++) {
                    NodePoint nodePoint = new NodePoint();
                    panEnv.nodePoints[j] = nodePoint;
                    int val;
                    val = _input.readByte();
                    nodePoint.y = val;
                    val = _input.readUShort();
                    nodePoint.x = val;
                }
                _input.forward(1);
           
                Envelope pitchEnv = instru.pitchEnv;
                pitchEnv.flags = _input.readUByte();
                pitchEnv.nbPoints = _input.readUByte();
                pitchEnv.loopStart = _input.readUByte();
                pitchEnv.loopEnd = _input.readUByte();
                pitchEnv.sustainLoopStart = _input.readUByte();
                pitchEnv.sustainLoopEnd = _input.readUByte();
                for (int j = 0; j < 25; j++) {
                    NodePoint nodePoint = new NodePoint();
                    pitchEnv.nodePoints[j] = nodePoint;
                    int val;
                    val = _input.readByte();
                    nodePoint.y = val;
                    val = _input.readUShort();
                    nodePoint.x = val;
                }
                _input.forward(1);

                _input.seek(oldSeek);
             }
        }

        // read samples
        samples_ = new Sample[nbSamples_];
        for (int i = 0; i < nbSamples_; i++) {
            Sample samp = new Sample();
            samples_[i] = samp;
            
            if (bSampleMode_ == true) {
                Instru instru = new Instru();
                instrus_[i] = instru;
                instru.globalVol = 1.0f;
                for (int j = 0; j < 120; j++) {
                    instru.noteTable[j] = new NoteTable();
                    instru.noteTable[j].note = j;
                    instru.noteTable[j].numSample = i + 1;
                }
            }
            
            int sampleOffset;
            sampleOffset = _input.readInt();
            
            int oldSeek = _input.tell();
            _input.seek(sampleOffset);
            
            _input.forward(4 + 12 + 1); // 'IMPS' + DOS filename + 00h
            byt = _input.readUByte();
            samp.globalVol = byt / 64.0f;
            samp.flags = _input.readUByte();
            byt = _input.readUByte();
            samp.defaultVol = byt / 64.0f;
            
            byte sampName[] = new byte[26];
            _input.read(sampName, 0, 26);
            samp.name = new String(sampName).substring(0, 25);
            
            samp.cvt = _input.readUByte();
            byt = _input.readUByte();
            
            if ( (byt & 0x80) != 0) {
                samp.defaultPan = ( (byt&0x7f) - 32) / 32.0f;
                samp.bGotPan = true;
            }

            samp.len = _input.readInt();
            if (samp.len == 0) {
                _input.seek(oldSeek);
                continue;
            }
            
            samp.loopStart = _input.readInt();
            samp.loopEnd = _input.readInt();
            samp.c5Speed = _input.readInt();
            samp.sustainLoopStart = _input.readInt();
            samp.sustainLoopEnd = _input.readInt();
            
            int nbBits;
            int nbChans = 1;
            if ( (samp.flags & 2) != 0)
                nbBits = 16;
            else
                nbBits = 8;

            samp.c5Speed /= 2;
            
            int sampPos;
            sampPos = _input.readInt();
            int toRead = samp.len;
            if (nbBits == 16)
                toRead *= 2;
            if ( (samp.flags & 4) != 0) {
                toRead *= 2;
                nbChans = 2;
            }
            
            int seekBAK = _input.tell();
            _input.seek(sampPos);
            
            byte sampData[];
            
 //           try {
   //             oss = new FileOutputStream(new File("c:/test2.txt"));
    //        }
    //        catch(FileNotFoundException e) {
      //          oss = oss;
        //    }
            
            if ((samp.flags & 1) != 0  && (samp.flags & 8) != 0) {
                // compressed samples
                sampData = new byte[toRead];
                if (nbBits == 8) {
                    decompress8(_input, sampData, toRead, version == 0x215 ? true : false);
                }
                else {
                    decompress16(_input, sampData, toRead / 2, version == 0x215 ? true : false);
                }
            }
            else
            {
                // raw samples
                sampData = new byte[toRead];
                _input.read(sampData, 0, toRead);
            }
   //         try {
     //           oss.close();
       //     }
     //       catch(IOException io) {      
      //      }

            if (nbChans == 2) {
                byte tmp[] = new byte[ toRead ];
                if (nbBits == 8) {
                    byte dst[] = tmp;
                    int off = 0;
                    for (int ii = 0; ii < samp.len; ii++) {
                        byte s1 = sampData[ii];
                        byte s2 = sampData[ii + samp.len ];
                        dst[off++] = s1;
                        dst[off++] = s2;
                    }
                    for (int ii = 0; ii < samp.len * 2; ii++)
                        sampData[ii] = tmp[ii];
                    //memcpy(sampData, tmp, samp->length * 2);
                }
                else {
                    byte src[] = sampData;
                    byte dst[] = tmp;
                    int off = 0;
                    for (int ii = 0; ii < samp.len*2; ii+= 2) {
                        byte s1 = src[ii];
                        byte s2 = src[ii+1];
                        byte s3 = src[ii + samp.len*2 ];
                        byte s4 = src[ii+1 + samp.len*2 ];
                        dst[off++] = s1;
                        dst[off++] = s2;
                        dst[off++] = s3;
                        dst[off++] = s4;
                    }
                    //memcpy(sampData, tmp, samp->length * 2 * 2);
                    for (int ii = 0; ii < samp.len * 2 * 2; ii++)
                        sampData[ii] = tmp[ii];
                }
            }
            
            if ((samp.flags & 16) != 0) {
                if ((samp.flags & 64) != 0)
                    samp.audioData.make(sampData, nbBits, nbChans, samp.loopStart, samp.loopEnd, AudioData.LOOP_PINGPONG);
                else
                    samp.audioData.make(sampData, nbBits, nbChans, samp.loopStart, samp.loopEnd, AudioData.LOOP_FORWARD);
            }
            else {
                samp.audioData.make(sampData, nbBits, nbChans);
            }
            
            _input.seek(seekBAK);
            
            int tell = _input.tell();
                 
            samp.vibSpeed = _input.readUByte();
            samp.vibDepth = _input.readUByte();
            samp.vibRate = _input.readUByte();
            samp.vibType = _input.readUByte();
            
            _input.seek(oldSeek);
        }

        // Read patterns

        patterns_ = new Pattern[nbPatterns_];
        for (int i = 0; i < nbPatterns_; i++) {
            Pattern pattern = new Pattern();
            patterns_[i] = pattern;
            
            int patOffset;
            patOffset = _input.readInt();
            if (patOffset == 0) {
                pattern.nbRows = 64;
                continue;
            }

            int oldSeek = _input.tell();
            _input.seek(patOffset);

            int size;
            size = _input.readUShort();

            int nbRows;
            nbRows = _input.readUShort();
            pattern.nbRows = nbRows;

            _input.forward(4);

            int numRow = 0;
            int maskVar[] = new int[64];
            Note lastNotes[] = new Note[64];

            for (int iNote = 0; iNote < 64; iNote++)
                lastNotes[iNote] = new Note();
            
            while(numRow < nbRows) {
                int bytt;
                bytt = _input.readUByte();
                if (bytt == 0) {
                    numRow++;
                    continue;
                }
                
                int numChan = (bytt-1) & 63;
                Note lastNote = lastNotes[numChan];

                if (pattern.columns[numChan] == null) {
                    pattern.columns[numChan] = new Column();
                    pattern.columns[numChan].notes = new Note[nbRows];
                    for (int ii = 0; ii < nbRows; ii++) {
                        pattern.columns[numChan].notes[ii] = new Note();
                    }
                }
                
                Note notes[] = pattern.columns[numChan].notes;
                Note note = notes[numRow];
//                Note note = new Note();
  //              notes[numRow] = note;

                if ((bytt & 128) != 0)
                    maskVar[numChan] = _input.readUByte();

                if ((maskVar[numChan] & 1) != 0) {
                    lastNote.note = _input.readUByte();
                    note.note = lastNote.note;
                }

                if ((maskVar[numChan] & 2) != 0) {
                    lastNote.numInstru = _input.readUByte();
                    lastNote.numInstru--;
                    note.numInstru = lastNote.numInstru;
                }

                if ((maskVar[numChan] & 4) != 0) {
                    int volpan;
                    volpan = _input.readUByte();
                    float vol = -10.0f, pan = -10.0f;
                    if (volpan >=0 && volpan <= 64)
                        vol = volpan / 64.0f;
                    else if (volpan >=128 && volpan <= 192)
                        pan = (volpan - 160) / 32.0f;

                    note.vol = lastNote.vol = vol;
                    note.pan = lastNote.pan = pan;
                    note.extraCommand = lastNote.extraCommand = volpan;
                }

                if ((maskVar[numChan] & 8) != 0) {
                    lastNote.command = _input.readUByte();
                    lastNote.commandVal = _input.readUByte();
                    note.command = lastNote.command;
                    note.commandVal = lastNote.commandVal;
                }

                if ((maskVar[numChan] & 16) != 0)
                    note.note = lastNote.note;

                if ((maskVar[numChan] & 32) != 0)
                    note.numInstru = lastNote.numInstru;

                if ((maskVar[numChan] & 64) != 0) {
                    note.vol = lastNote.vol;
                    note.pan = lastNote.pan;
                    note.extraCommand = lastNote.extraCommand;
                }

                if ((maskVar[numChan] & 128) != 0) {
                    note.command = lastNote.command;
                    note.commandVal = lastNote.commandVal;
                }
            }

            _input.seek(oldSeek);
        } 
    
        for (int i = 0; i < 128; i++)
            voices_[i] = new Voice();
        
        for (int i = 0; i < nbVCs_; i++) {
            voices_[i].numVoice_ = i;
            voices_[i].id_ = -1;
        }

        for (int i = 0; i < 64; i++)
            chanRemap_[i] = -1;

        if ((hdrFlags_& 8) != 0)
            bLinearSlide_ = true;
        else
            bLinearSlide_ = false;
        
        if ((hdrFlags_& 16) != 0)
            bOldEffects_ = true;
        else
            bOldEffects_ = false;
        
//        ((hdrFlags_& 8) != 0) ? bLinearSlide_ = true : bLinearSlide_ = false;
//        ((hdrFlags_& 16) != 0) ? bOldEffects_ = true : bOldEffects_ = false;

        speed_ = initialSpeed_;
        tempo_ = initialTempo_;
        localVol_ = 1.0f;

        chansList_ = new ChannelsList();

        return OZMod.proceedError(OZMod.ERR.NOERR);
    }

    void updateSoundWithEnvelope()
    {
        int nbr = 0;
        int nbr2 = 0;
        for (int i = 0; i < nbVCs_; i++) {
//        for (int i = 0; i < 1; i++) {
            
            Voice voice = voices_[i];

            if (voice.samplePlaying_ != null)
                nbr2++;

            if (voice.status_ != 0)
                nbr++;

            if (voice.actuInstru_ == null)
                continue;

            if (tick_ < voice.noteDelay_)
                continue;
            voice.noteDelay_ = 0;

            int env;

            // VOL
            env = voice.actuInstru_.volEnv.calc(  voice.envVolPoint_,
                                                  voice.envVolPos_,
                                                  voice.bKeyOff_,
                                                  voice.bFadeOut_,
                                                  voice.bGotLoop_,
                                                  voice.bEnvFinished_);
            if (env != -9999)
                voice.envVol_ = env / 64.0f;
            else
                voice.envVol_ = 1.0f;

            // PAN
            env = voice.actuInstru_.panEnv.calc(  voice.envPanPoint_,
                                                  voice.envPanPos_,
                                                  voice.bKeyOff_,
                                                  null,
                                                  null,
                                                  null);
            if (env != -9999)
                voice.envPan_ = env / 32.0f;
            else
                voice.envPan_ = 0.0f;

            Envelope pEnv = voice.actuInstru_.pitchEnv;
            if ((pEnv.flags & 0x80) != 0) {
                // FILTER
                env = voice.actuInstru_.pitchEnv.calc(voice.envPitchPoint_,
                                                      voice.envPitchPos_,
                                                      voice.bKeyOff_,
                                                      null,
                                                      null,
                                                      null);
                if (env != -9999) {
                    env = (env + 32);
                    if (voice.cutOff_ != -1)
                        env = (voice.cutOff_ * env) >> 6;
                    else
                        env <<= 1;
                    voice.envPitch_ = (float) env;
                }
                else
                    voice.envPitch_ = -1;
                voice.bGotFilterEnv_ = true;
            }
            else {
                // PITCH
                env = voice.actuInstru_.pitchEnv.calc(voice.envPitchPoint_,
                                                        voice.envPitchPos_,
                                                        voice.bKeyOff_,
                                                        null,
                                                        null,
                                                        null);
                if (env != -9999)
                    voice.envPitch_ = -env * 32.0f;
                else
                    voice.envPitch_ = -1;
                voice.bGotFilterEnv_ = false;
            }

        }

    //  printf("%ld  /  %ld \n", nbr, nbr2);
    }
    
    void init()
    {
    }

    /**
     * Starts to play the IT. The time latency between a note is read and then heard is approximatively of 100ms.
     * If the IT is not loopable and finish, you cannot restart it by invoking again this method.
     */
    public void play()
    {
        if (isAlive() == true || done_ == true)
            return;

        isRunning_ = true;
        start(); 
    }
    
    /**
     * Never call this method directly. Use play() instead.
     */
    public void run()
    {
        frequency_ = 44100;
        AudioFormat format = new AudioFormat(frequency_, 16, 2, true, true);

        try {
            line_ = AudioSystem.getSourceDataLine(format, OZMod.usedMixer_.getMixerInfo());
            line_.open(format, frequency_);
      }
        catch(LineUnavailableException e) {
        }
        
        int soundBufferLen = frequency_ * 4;
        pcm_ = new byte[soundBufferLen];
      
        long cumulTime = 0;
        long startTime = timer_.getElapsed();
        boolean lineStarted = false;
        
        while(isRunning_ == true) {
            float timerRate = 1000.0f / (tempo_ * 0.4f);
            int intTimerRate = (int) Math.floor(timerRate);

            long since = timer_.getDelta();
            cumulTime += since;
            
            if (cumulTime >= intTimerRate) {

                cumulTime -= intTimerRate;
                
                oneShot(intTimerRate);

                // wait 100ms before starting the audio line
                if (lineStarted == false) {
                    long waitFor = timer_.getElapsed() - startTime;
                    if (waitFor > 100) {
                        line_.start();
                        lineStarted = true;
                    }
                }
            }

        //   try {
               Thread.yield();
        //    }
        //    catch (InterruptedException e) {
          //  }
        }
        done_ = true;
    }
    
    void oneShot(int timer)
    {
        int i, j, k;
        
        if (tick_ == speed_)
            tick_ = 0;
        tick_++;

        if (tick_ == 1) {
            dispatchNotes();

            if (bOldEffects_ == false)
                for (i = 0; i < 64; i++)
                    voices_[i].updateSoundWithEffect2();

            for (i = 0; i < 64; i++)
                voices_[i].updateSoundWithEffect3();
        }
        else {
            for (i = 0; i < 64; i++) {
                voices_[i].updateSoundWithEffect();
                voices_[i].updateSoundWithEffect2();
                voices_[i].updateSoundWithEffect3();
            }
        }

        for (i = 0; i < nbVCs_; i++)
            voices_[i].doVibrato();

        updateSoundWithEnvelope();

        for (i = 0; i < nbVCs_; i++)
            voices_[i].updateFadeOut();

        for (i=0; i < nbVCs_; i++)
            voices_[i].soundUpdate();

        mixSample(timer);
    }
    
    void mixSample(int _time)
    {
        int nbsamp = frequency_ / (1000 / _time);
        Arrays.fill(pcm_,  (byte) 0);
        chansList_.mix(nbsamp, pcm_);
        line_.write(pcm_, 0, nbsamp*4);
    }

    int getPeriod(int note, int c5speed)
    {
        int n = note%12;
        int o = note/12;

        int h = (8363 * 16 * (g_ITperiod[n]) ) >> o;

        int ret = 1;
        if (c5speed != 0) ret  = h / c5speed;
        return ret;
    }

    void dispatchNotes()
    {
        int i, j, k;
        int lastH, lastQ, lastS, lastU;

        for (i = 0; i < nbVCs_; i++)
            voices_[i].tryToFree();

    again:;
        int iPattern = orders_[songPos_];
        if (iPattern == 254) {
            while(iPattern == 254) {
                songPos_++;
                iPattern = orders_[songPos_];
            }
        }
        else if (iPattern == 255) {
            if (loopable_) {
                chansList_.empty();
                globalVol_ = globalVolBAK_;
                songPos_ = 0;
                songRow_ = 0;
            }
            else {
                isRunning_ = false;
            }
            return;
        }

        Pattern pattern = patterns_[iPattern];
        
 //       System.out.println(songPos_);

    //  printf("%ld  \r", songRow_);
    //  printf("track pos %ld/%ld into pattern %ld (speed %ld, tempo %ld) in the pos %ld     \r", songRow_, 0, iPattern, speed_, tempo_, songPos_);

        for (i = 0; i < 64; i++) {
//      for (i = 0; i < 3; i++) {
    //        if (i != 100 && i != 3)
       //     if (i != 14)
         //       continue;

            
            int remap = chanRemap_[i];

            if (pattern.columns[i] == null || remap == -1)
                continue;

            Note not = pattern.columns[i].notes[songRow_];
            Voice voice = voices_[remap];
            int command = not.command;
            int commandVal = not.commandVal;
            int numInstru = not.numInstru;

            if (command == ('S'-0x40) ) {
                if (commandVal != 0) voice.lastS_ = commandVal;

                // NOTE DELAY
                if ( (voice.lastS_ >> 4) == 0xd) {
                    voice.noteDelay_ = (voice.lastS_ & 0xf) + 1;
                }
            }

            if (command == ('G'-0x40) && voice.samplePlaying_ != null) {
                voice.bUpdateNote_ = false;
                continue;
            }
            else
                voice.bUpdateNote_ = true;

            if (numInstru >= nbInstrus_)
                continue;

            if ( (not.note <= 0 || not.note > 119) )
                continue;

            int nna = voice.nna_;
            if (nna == IT_NNA_CUT) {
                continue;
            }

            if (voice.samplePlaying_ == null)
                continue;

//          float vol = voice.vol_ * voice.samplePlaying_.globalVol * voice.chanVol_ * voice.actuInstru_.globalVol * voice.childClass_.globalVol_ * voice.childClass_.localVol_ * voice.fadeVol_ * voice.envVol_;
//          if (vol == 0.0f)
//              continue;

            // search a free vc
//          int n = findFreeVC();
//          if (n == -1)
//              continue;

            /*
            ITVoice *nv = &voices_[n];
            memcpy(nv, voice, sizeof ITVoice);

            nv.id_ = i;
            nv.bGotRetrigger_ = false;
            nv.numVoice_ = n;
            nv.atRow_ = songRow_;
            nv.atPos_ = songPos_;
            nv.status_ = 1;

            if (nna == IT_NNA_CONT) {
                nv.status_ = 2;
            }
            else if (nna == IT_NNA_OFF) {
                nv.status_ = 3;
                nv.bKeyOff_ = true;
            }
            else if (nna == IT_NNA_FADE) {
                nv.status_ = 4;
                nv.bFadeOut_ = true;
            }
    */

            chanRemap_[i] = -1; //n;

            voice.id_ = i;
            voice.bGotRetrigger_ = false;
            voice.atRow_ = songRow_;
            voice.atPos_ = songPos_;


            /*
            chanRemap_[i] = n;
            voice.id_ = i;
            voice.bGotRetrigger_ = false;
            voice.atRow_ = songRow_;
            voice.atPos_ = songPos_;

            memcpy(&voices_[n], voice, sizeof ITVoice);
            voices_[n].numVoice_ = n;
            voices_[n].status_ = 1;
            voices_[n].fadeVol_ = 1.0f;
            voices_[n].envVol_ = 1.0f;      voices_[n].envPan_ = 0.0f;      voices_[n].envPitch_ = 0;
            voices_[n].bKeyOff_ = false;
            voices_[n].bFadeOut_ = false;
            voices_[n].bGotLoop_ = false;
            voices_[n].bEnvFinished_ = false;
            voices_[n].envVolPoint_ = 0;        voices_[n].envVolPos_ = 0;
            voices_[n].envPanPoint_ = 0;        voices_[n].envPanPos_ = 0;
            voices_[n].envPitchPoint_ = 0;      voices_[n].envPitchPos_ = 0;
    */
            if (nna == IT_NNA_CONT) {
                voice.status_ = 2;
            }
            else if (nna == IT_NNA_OFF) {
                voice.status_ = 3;
                voice.bKeyOff_[0] = true;
            }
            else if (nna == IT_NNA_FADE) {
                voice.status_ = 4;
                voice.bFadeOut_[0] = true;
            }

        }

        int newSpeed = 0;
        int newTempo = 0;
        boolean bGotPatternJump = false;
        boolean bGotPatternBreak = false;
        int whereToJump = 0, whereToBreak = 0;

        for (i = 0; i < 64; i++) {
//            for (i = 0; i < 3; i++) {

        //    if (i != 100 && i != 3)
     //           if (i != 14)
       //         continue;
            
            int remap = chanRemap_[i];
            Voice voice;

            if (pattern.columns[i] == null) {
                if (remap != -1) {
                    voice = voices_[remap];
                    voice.command_ = ITCOMMAND_NONE;
                    voice.bGotRetrigger_ = false;
                    voice.bGotVibrato_ = false;
                    voice.bGotVibratoU_ = false;
                    voice.bGotTremor_ = false;
                    voice.bGotArpeggio_ = false;
                    voice.vol_ = voice.volBAK_;
                    voice.period_ = voice.periodBAK_;
                }
                continue;
            }

            if (remap == -1) {
                remap = findFreeVC();
                if (remap == -1)
                    continue;
                chanRemap_[i] = remap;
                voice = voices_[remap];
                voice.status_ = 1;
                voice.bUpdateNote_ = true;
                voice.vol_ = 1.0f;
                if (chnsPan_[i] >=0 && chnsPan_[i] <= 64)
                    voice.pan_ = (chnsPan_[i] - 32) / 32.0f;
                if (chnsVol_[i] >=0 && chnsVol_[i] <= 64)
                    voice.chanVol_ = chnsVol_[i] / 64.0f;
                else
                    voice.chanVol_ = 1.0f;
                voice.envVol_ = 1.0f;      voice.envPan_ = 0.0f;      voice.envPitch_ = 0;
                voice.fadeVol_ = 1.0f;
                voice.bKeyOff_[0] = false;
                voice.bFadeOut_[0] = false;
                voice.bGotLoop_[0] = false;
                voice.bEnvFinished_[0] = false;
                voice.envVolPoint_[0] = 0;            voice.envVolPos_[0] = 0;
                voice.envPanPoint_[0] = 0;            voice.envPanPos_[0] = 0;
                voice.envPitchPoint_[0] = 0;          voice.envPitchPos_[0] = 0;
            }

            voice = voices_[remap];
            Note not = pattern.columns[i].notes[songRow_];

            int note = not.note;
            int command = not.command;
            int commandVal = not.commandVal;
            int numInstru = not.numInstru;

            if (command != ('H'-0x40) && voice.bGotVibrato_ == true) {
                voice.bGotVibrato_ = false;
                voice.period_ = voice.periodBAK_;
                voice.vibCounter_ = 0;
            }
            else if (command != ('U'-0x40) && voice.bGotVibratoU_ == true) {
                voice.bGotVibratoU_ = false;
                voice.period_ = voice.periodBAK_;
            }
            else if (command != ('I'-0x40) && voice.bGotTremor_ == true) {
                voice.bGotTremor_ = false;
                voice.vol_ = voice.volBAK_;
            }
            else if (command != ('J'-0x40) && voice.bGotArpeggio_ == true) {
                voice.bGotArpeggio_ = false;
                voice.period_ = voice.periodBAK_;
            }
            voice.bGotRetrigger_ = false;

            if (numInstru >= 0) {
                if (numInstru >= nbInstrus_)
                    voice.actuInstru_ = nullInstru_;
                else {
                    voice.actuInstru_ = instrus_[numInstru];
                    voice.nna_ = voice.actuInstru_.nna;
                }
                
                if (voice.samplePlaying_ != null)
                    voice.vol_ = voice.samplePlaying_.defaultVol;

                voice.fadeVol_ = 1.0f;
                voice.envVol_ = 1.0f;      voice.envPan_ = 0.0f;      voice.envPitch_ = 0;
                voice.bKeyOff_[0] = false;
                voice.bFadeOut_[0] = false;
                voice.bGotLoop_[0] = false;
                voice.bEnvFinished_[0] = false;
                voice.envVolPoint_[0] = 0;            voice.envVolPos_[0] = 0;
                voice.envPanPoint_[0] = 0;            voice.envPanPos_[0] = 0;
                voice.envPitchPoint_[0] = 0;          voice.envPitchPos_[0] = 0;
                voice.numInstru_ = numInstru;

                if (note == 0)
                    note = voice.lastNote_;
            }

            if (note > 0 && note <= 119 && voice.actuInstru_ != null && voice.actuInstru_ != nullInstru_) {
                voice.lastNote_ = note;

                Sample sample = null;
                int numSamp = voice.actuInstru_.noteTable[note].numSample - 1;
                if (numSamp >= 0 && numSamp < nbSamples_) {
                    sample = samples_[numSamp];
                    if (sample.len == 0)
                        sample = null;
                }
                voice.tremorCounter_ = 0;
                voice.vibCounter_ = 0;
                voice.vibCounter_s_ = 0;
                voice.vibCount_s_ = 0;

                if (numInstru >= 0 && sample != null) {
                    voice.vol_ = sample.defaultVol;
                    voice.volBAK_ = voice.vol_;
                    if ((voice.actuInstru_.ifc & 0x80) != 0)
                        voice.cutOff_ = voice.actuInstru_.ifc & 0x7f;
                    else voice.cutOff_ = -1;
                    if ((voice.actuInstru_.ifr & 0x80) != 0)
                        voice.resonance_ = voice.actuInstru_.ifr & 0x7f;
                    else voice.resonance_ = 0;
                    if (sample.bGotPan == true)
                        voice.pan_ = sample.defaultPan;
                }

                voice.note_ = note;

                voice.fadeVol_ = 1.0f;
                voice.envVol_ = 1.0f;      voice.envPan_ = 0.0f;      voice.envPitch_ = 0;
                voice.bKeyOff_[0] = false;
                voice.bFadeOut_[0] = false;
                voice.bGotLoop_[0] = false;
                voice.bEnvFinished_[0] = false;
                voice.envVolPoint_[0] = 0;        voice.envVolPos_[0] = 0;
                voice.envPanPoint_[0] = 0;        voice.envPanPos_[0] = 0;
                voice.envPitchPoint_[0] = 0;      voice.envPitchPos_[0] = 0;

                if ( (sample != null && sample != voice.samplePlaying_) || (voice.bUpdateNote_ == true) ) {
                    voice.samplePlaying_ = sample;
                    voice.bNeedToBePlayed_ = true;
                    voice.id_ = -1;
                    voice.sampleOffset_ = 0;
                }
                else if (sample == null && voice.samplePlaying_ != null) {
                    chansList_.removeChannel(voice.sndchan_);
                    voice.samplePlaying_ = null;
                }

                if (voice.bUpdateNote_ == true && sample != null) {
                    int period = getPeriod( voice.actuInstru_.noteTable[note].note, sample.c5Speed);
                    voice.period_ = (float) period;
                    voice.periodBAK_ = (float) period;
                    voice.dstPeriod_ = period;
                }
            }
            else if (note == 254) {
                if (voice.samplePlaying_ != null) {
                    //voices_.sndchan_.stop();
//                    chansList_.removeChannel(voices_[0].sndchan_);
                  chansList_.removeChannel(voice.sndchan_);
                    voice.samplePlaying_ = null;
                }
                voice.actuInstru_ = null;
            }
            else if (note == 255) {
                // NOTE OFF
                voice.bKeyOff_[0] = true;
            }

            if (not.vol != -10.0f) {
                // SET VOLUME
                voice.vol_ = not.vol;
                voice.volBAK_ = voice.vol_;
            }

            if (not.pan != -10.0f)
                voice.pan_ = not.pan; // SET PAN

            voice.command_ = ITCOMMAND_NONE;
            voice.noteCut_ = 0x7fffffff;

            switch(command) {
            case 'A' - 0x40:
                // SET SPEED
                newSpeed = commandVal;
                break;

            case 'B' - 0x40:
                // POSITION JUMP
                bGotPatternJump = true;
                whereToJump = commandVal;
                break;

            case 'C' - 0x40:
                // PATTERN BREAK;
                bGotPatternBreak = true;
                whereToBreak = commandVal;
                break;

            case 'D' - 0x40:
                // VOL SLIDING
                voice.evalue_DCommand(commandVal);
                break;

            case 'E' - 0x40:
                // PORTA DOWN
                if (commandVal != 0) voice.lastEFG_ = commandVal;
      //          if (voice.lastEFG_ >= 0xe0 && voice.lastEFG_ < 0xf0)
      //              voice.slidePeriod(voice.lastEFG_ - 0xe0);
      //          else if (voice.lastEFG_ >= 0xf0 )
       //             voice.slidePeriod( (voice.lastEFG_ - 0xf0) * 4);
        //        else voice.command_ = ITCOMMAND_PORTADOWN;
                break;

            case 'F' - 0x40:
                // PORTA UP
                if (commandVal != 0) voice.lastEFG_ = commandVal;
           //     if (voice.lastEFG_ >= 0xe0 && voice.lastEFG_ < 0xf0)
             //       voice.slidePeriod( -(voice.lastEFG_ - 0xe0) );
          //      else if (voice.lastEFG_ >= 0xf0 )
            //        voice.slidePeriod( -(voice.lastEFG_ - 0xf0) * 4);
            //    else voice.command_ = ITCOMMAND_PORTAUP;
                break;

            case 'G' - 0x40:
                // PORTA TO
                if (note > 0 && note <= 119 && voice.samplePlaying_ != null)
                    voice.dstPeriod_ = getPeriod( voice.actuInstru_.noteTable[note].note, voice.samplePlaying_.c5Speed);
                voice.command_ = ITCOMMAND_PORTATO;
                if (commandVal != 0) voice.lastEFG_ = commandVal;
                break;

            case 'H' - 0x40:
                // VIBRATO
                if ( ( (commandVal >> 4) != 0 ) && ( (commandVal & 0xf) != 0) )
                    voice.lastH_ = commandVal;
                lastH = voice.lastH_;
                voice.bGotVibrato_ = true;
                voice.vibSpeed_ = lastH >> 4;
                voice.vibDepth_ = lastH & 0x0f;
                voice.command_ = ITCOMMAND_VIBRATO;
                break;

            case 'I' - 0x40:
                // TREMOR
                voice.command_ = ITCOMMAND_TREMOR;
                voice.bGotTremor_ = true;
                if (commandVal != 0) voice.lastI_ = commandVal;
                break;

            case 'J' - 0x40:
                // ARPEGGIO
                voice.command_ = ITCOMMAND_ARPEGGIO;
                voice.bGotArpeggio_ = true;
                voice.arpeggioCounter_ = 0;
                if (commandVal != 0) voice.lastJ_ = commandVal;
                break;

            case 'K' - 0x40:
                //
                //voice.evalue_DCommand(commandVal);
                break;

            case 'L' - 0x40:
                //
                //voice.evalue_DCommand(commandVal);
                break;

            case 'M' - 0x40:
                // SET CHAN VOLUME
                voice.chanVol_ = commandVal / 64.0f;
                if (voice.chanVol_ > 1.f) voice.chanVol_ = 1.0f;
                break;

            case 'N' - 0x40:
                // CHAN VOL SLIDE
                //voice.evalue_NCommand(commandVal);
                break;

            case 'O' - 0x40:
                // SAMPLE OFFSET
                if (commandVal != 0) voice.lastO_ = commandVal;
                voice.sampleOffset_ = voice.lastO_;
                break;

            case 'Q' - 0x40:
                // RETRIGGER
                if (commandVal != 0) voice.lastQ_ = commandVal;
                lastQ = voice.lastQ_;
                voice.bGotRetrigger_ = true;
                break;

            case 'S' - 0x40:
                if (commandVal != 0) voice.lastS_ = commandVal;
                
                lastS = voice.lastS_;
                if (lastS == 0x70) {
                    // PAST CUT
                    for (int nc = 0; nc < nbVCs_; nc++) {
                        if (voices_[nc].id_ == i) {
                            if (voices_[nc].samplePlaying_ != null) {
                                //voices_[nc].samplePlaying_.gasound[ voices_[nc].numVoice_ ].stop();
                    //            voices_[nc].sndchan_.stop();
                      //          voices_[nc].samplePlaying_ = NULL;
                                chansList_.removeChannel(voices_[nc].sndchan_);
                                voices_[nc].samplePlaying_ = null;
                            }
                            if (voices_[nc].status_ != 1)
                                voices_[nc].status_ = 0;
                        }
                    }
                }
                else if (lastS == 0x71) {
                    // PAST OFF
                    for (int nc = 0; nc < nbVCs_; nc++)
                        if (voices_[nc].id_ == i) {
                            voices_[nc].bKeyOff_[0] = true;
                            if (voices_[nc].status_ != 1)
                                voices_[nc].status_ = 3;
                        }
                }
                else if (lastS == 0x72) {
                    // PAST FADE
                    for (int nc = 0; nc < nbVCs_; nc++)
                        if (voices_[nc].id_ == i) {
                            voices_[nc].bFadeOut_[0] = true;
                            if (voices_[nc].status_ != 1) voices_[nc].status_ = 4;
                        }
                }

                else if (lastS == 0x73) voice.nna_ = IT_NNA_CUT;
                else if (lastS == 0x74) voice.nna_ = IT_NNA_CONT;
                else if (lastS == 0x75) voice.nna_ = IT_NNA_OFF;
                else if (lastS == 0x76) voice.nna_ = IT_NNA_FADE;

                // SET PAN
                else if (lastS >= 0x80 && lastS <= 0x8f)
                    voice.pan_ = (lastS - 0x80) / 7.5f - 1.0f;

                else if (lastS >= 0xb0 && lastS <= 0xbf) {
                    // LOOP BACK
                    if (lastS == 0x0b0) {
                        voice.loopBackLoc_ = songRow_;
                        voice.nbLoopBack_ = 0;
                        voice.bGotLoopBack_ = false;
                    }
                    else if (lastS > 0x0b0) {
                        if (voice.nbLoopBack_ == 0 && voice.bGotLoopBack_ == false) {
                            voice.nbLoopBack_ = lastS - 0xb0;
                            voice.bGotLoopBack_ = true;
                        }
                        voice.nbLoopBack_--;
                        if (voice.nbLoopBack_ > -1)
                            songRow_ = voice.loopBackLoc_;
                    }
                }

                else if (lastS >= 0xc0 && lastS <= 0xcf) {
                    // NOTE CUT
                    voice.noteCut_ = lastS - 0xc0;
                }
                break;

            case 'T' - 0x40:
                // SET TEMPO
                if (commandVal >= 0x20)
                    newTempo = commandVal;
                break;

            case 'U' - 0x40:
                // VIBRATO
                if ( ( (commandVal >> 4) != 0 ) && ( (commandVal & 0xf) != 0) )
                    voice.lastU_ = commandVal;
                lastU = voice.lastU_;
                voice.bGotVibratoU_ = true;
                voice.vibSpeed_ = lastU >> 4;
                voice.vibDepth_ = (lastU & 0x0f) >> 2;
                voice.command_ = ITCOMMAND_VIBRATO;
                break;

            case 'V' - 0x40:
                // SET GLOBAL VOLUME
                globalVol_ = commandVal / 128.0f;
                if (globalVol_ > 1.f) globalVol_ = 1.0f;
                break;

            case 'W' - 0x40:
                // GLOBAL VOL SLIDE
                voice.evalue_WCommand(commandVal);
                break;

            case 'X' - 0x40:
                // SET PANNING
                voice.pan_ = (commandVal - 127.5f) / 127.5f;
                break;

            case 'Z' - 0x40:
                if (commandVal < 0x80) voice.cutOff_ = commandVal;
                else if (commandVal >= 0x80 && commandVal < 0x8f) voice.resonance_ = commandVal - 0x80;
                break;
            }

            // check for DCT
            if (voice.actuInstru_ != null) {
                int dct = voice.actuInstru_.dct;
                if (dct != IT_DCT_DISABLE) {
                    int dca = voice.actuInstru_.dca;
                    for (int nv = 0; nv < nbVCs_; nv++) {
                        if ( (voices_[nv].id_ == i) && (voices_[nv].actuInstru_ == voice.actuInstru_) ) {
                            Voice v = voices_[nv];
                            boolean bAction = false;
                            if ( (dct == IT_DCT_NOTE) && (v.note_ == voice.note_) )
                                bAction = true;
                            else if ( (dct == IT_DCT_SAMPLE) && (v.samplePlaying_ == voice.samplePlaying_) )
                                bAction = true;
                            else if (dct == IT_DCT_INSTRUMENT)
                                bAction = true;

                            if (bAction == true) {
                                if (dca == IT_DCA_CUT && v.samplePlaying_ != null) {
                                    //v.sndchan_.stop();
                                    chansList_.removeChannel(v.sndchan_);
                                    v.samplePlaying_ = null;
                                    if (v.status_ != 1)
                                        v.status_ = 0;
                                }
                                else if (dca == IT_DCA_OFF) {
                                    if (v.status_ != 1) v.status_ = 3;
                                    v.bKeyOff_[0] = true;
                                }
                                else if (dca == IT_DCA_FADE) {
                                    if (v.status_ != 1) v.status_ = 4;
                                    v.bFadeOut_[0] = true;
                                }
                            }
                        } // this voice is likely to interest us
                    } // next voice
                } // this instru has a valid DCT
            }
        }

        if (newSpeed != 0)
            speed_ = newSpeed;

        if (newTempo != 0 && tempo_ != newTempo) {
            tempo_ = newTempo;
            fltTimerRate_ = 1000.0f / (newTempo * 0.4f);
            intTimerRate_ = (int) ( fltTimerRate_) - 1;
        }

        songRow_++;
        if (songRow_ == pattern.nbRows || bGotPatternJump == true || bGotPatternBreak == true) {
            songRow_ = whereToBreak;
            if (bGotPatternJump == false) {
                songPos_++;
                if (songPos_  >= nbOrders_) {
                    if (loopable_) {
                        chansList_.empty();
                        globalVol_ = globalVolBAK_;
                        songPos_ = 0;
                        songRow_ = 0;
                    }
                    else {
                        isRunning_ = false;
                    }
                }
            }
            else
                songPos_ = whereToJump;
        }
    }

    /**
     * Stops the IT. Once a IT is stopped, it cannot be restarted.
     */
    public void done()
    {
        isRunning_ = false;
        try {
            join();
        }
        catch(InterruptedException e) {
        }
    }
    
    /**
     * Gets the current reading position of the song.
     * @return The current position.
     */
    public int getCurrentPos()
    {
        return songPos_;
    }
    
    /**
     * Gets the current reading row of the song.
     * @return The current row.
     */
    public int getCurrentRow()
    {
        return songRow_;
    }
    
    /**
     * Gets the internal buffer used to mix the samples together. It can be used for instance to analyze the wave, apply effect or whatever.
     * @return The internal mix buffer.
     */
    public byte[] getMixBuffer()
    {
        return pcm_;
    }

    /**
     * Tells if the IT is loopable or not.
     * @return true if loopable, false otherwhise.
     */
    public boolean isLoopable()
    {
        return loopable_;
    }

    /**
     * Sets the IT loopable or not. The method can be called at any time if the song is still playing.
     * @param _b true to loop the song, false otherwhise.
     */
    public void setLoopable(boolean _b)
    {
        loopable_ = _b;
    }
}
