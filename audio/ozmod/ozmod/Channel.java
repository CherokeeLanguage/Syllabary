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

class Channel {

    static final int RAMP_NONE = 0;
    static final int RAMP_IN = 1;
    static final int RAMP_OUT = 2;

    Channel()
    {
        audio = new AudioData();
    }
    
    void setPan(float _p)
    {
        if (_p < -1.0f)
            _p = -1.0f;
        else if (_p > 1.0f)
            _p = 1.0f;

        if (_p < 0) {
            leftFactor = 1.0f;
            rightFactor = 1.0f + _p;
        }
        else {
            leftFactor = 1.0f - _p;
            rightFactor = 1.0f;
        }
    }
    
    AudioData audio;

    int frequency;
    float pos;
    float step;
    float leftFactor = 1.0f;
    float rightFactor = 1.0f;
    float vol;
    int ramp = RAMP_NONE;
    float rampVolume;
    float rampStep;
}
