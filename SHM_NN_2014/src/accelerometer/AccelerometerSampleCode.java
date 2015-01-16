/*
 * Copyright (c) 2007-2010 Sun Microsystems, Inc.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to 
 * deal in the Software without restriction, including without limitation the 
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or 
 * sell copies of the Software, and to permit persons to whom the Software is 
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
 * DEALINGS IN THE SOFTWARE.
 **/

package accelerometer;

import com.sun.spot.resources.Resources;
import com.sun.spot.resources.transducers.LEDColor;
import com.sun.spot.resources.transducers.IAccelerometer3D;
import com.sun.spot.resources.transducers.ITriColorLEDArray;
import com.sun.spot.util.Utils;

import java.io.IOException;

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

/**
 * Sample code snippet to show how to use the accelerometer
 * on the Sun SPOT General Purpose Sensor Board.
 *
 * Simple use of the accelerometer to measure tilt on the Sun SPOT.
 *
 * @author Ron Goldman<br>
 * date: August 14, 2006
 * date: March 12, 2007 - modified to use new IAccelerometer3D interface
 * date: May 26, 2010 - modified to use new Resource & Sensor APIs
 */
public class AccelerometerSampleCode extends MIDlet {

    private IAccelerometer3D accel = (IAccelerometer3D) Resources.lookup(IAccelerometer3D.class);
    private ITriColorLEDArray leds = (ITriColorLEDArray)Resources.lookup(ITriColorLEDArray.class);

    // The value for any particular SPOT may vary by as much as 10%. For more accurate results
    // each SPOT can be calibrated to determine the zero offset and conversion factor for each axis.

    /**
     * Simple accelerometer demo to measure the tilt of the SPOT.
     * Tilt is displayed by lighting LEDs like a bubble in a level.
     */
    public void demoBubbleLevel() {
        leds.setOff();		       // turn off all LEDs
        leds.setColor(LEDColor.BLUE);  // set them to be blue when lit
        
        int oldOffset = 3;
        while (true) {
            try {
                int tiltX = (int)Math.toDegrees(accel.getTiltX()); // returns [-90, +90]
                int offset = -tiltX / 15;                // convert angle to range [3, -3] - bubble goes to higher side
                if (offset < -3) offset = -3;
                if (offset > 3 ) offset =  3;
                if (oldOffset != offset) {
                    leds.getLED(3 + oldOffset).setOff(); // clear display
                    leds.getLED(4 + oldOffset).setOff();
                    leds.getLED(3 + offset).setOn();     // use 2 LEDs to display "bubble""
                    leds.getLED(4 + offset).setOn();
		    oldOffset = offset;
                }
                Utils.sleep(50);                         // update 20 times per second
            } catch (IOException ex) {
                System.out.println("Error reading accelerometer: " + ex);
            }
        }
    }

    /*
     * If we wanted to implement our own version of getTiltX() that returns the
     * tilt value in degrees instead of radians, then here's one way to do so
     * based on the code in the LIS3L02AQAccelerometer class:
     *
     *   public int getTiltXDegrees() {
     *       double x = getAccelX();        // get current acceleration along each axis
     *       double y = getAccelY();
     *       double z = getAccelZ();
     *       double a = Math.sqrt(x*x + y*y + z*z);     // acceleration magnitude
     *       double tilt = x / a;                       // normalize
     *       double tiltRadians = MathUtils.asin(tilt); // tilt angle in radians
     *       return (int)(Math.toDegrees(tiltRadians) + 0.5);   // rounded result in degrees
     *   }
     *
     */
    
    
    /**
     * MIDlet call to start our application.
     */
    protected void startApp() throws MIDletStateChangeException {
        // Listen for downloads/commands over USB connection
        new com.sun.spot.service.BootloaderListenerService().getInstance().start();
        demoBubbleLevel();
    }

    protected void pauseApp() {
        // This will never be called by the Squawk VM
    }

    /**
     * Called if the MIDlet is terminated by the system.
     *
     * @param unconditional If true the MIDlet must cleanup and release all resources.
     */
    protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
       leds.setOff();
    }
}
