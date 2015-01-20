package spot.src.org.sunspotworld.demo.communication;

import com.sun.spot.resources.Resources;
import com.sun.spot.resources.transducers.ITriColorLED;
import com.sun.spot.resources.transducers.LEDColor;
import com.sun.spot.util.Utils;

public class LEDSignals {

	private ITriColorLED[] led = new ITriColorLED[8];
	private int lastindex = 0;
	
	public LEDSignals(){
		for(int i=0; i<led.length; i++)
        	led[i] = (ITriColorLED)Resources.lookup(ITriColorLED.class, "LED" + (i+1));
	}
	

	/**
	   * LED signal for receiving the address.	   * 
	   *
	   * @return Yellow light on last LED.
	   * 
	   * @author SpotIdentifier.
	   */
	public void LEDSignalForReceivingAdresses(){
		
		led[7].setColor(LEDColor.YELLOW);
			led[7].setOn();
			Utils.sleep(300);
			led[7].setOff();
								
	}//Ende LEDSignalForREceivingAdresses()
	
	

	/**
	   * Sets all leds off.
	   * 
	   *
	   */
	public void offAll(){
		
		for(int i=0; i<8; i++){
			led[i].setOff();
		}
								
	}//Ende LEDSignalForREceivingAdresses()
	
	
	/**
	   * LED signal for spot communication (receive).
	   *
	   * @return Green light on LEDs depending on the number of spots.
	   * @return Yellow light on LED depending on index of the sending spots.
	   * 
	   * @param spot_count number of spots.
	   * @param index index of the sending spot.
	   * 
	   * @author SpotCommunication.
	   */
	public void LEDSignalCommunicationReceive(int spot_count, int index){
		
		for(int i=0; i<spot_count; i++){
			if(i!=index){
				led[i].setColor(LEDColor.GREEN);
				led[i].setOn();
			}else{
				led[i].setColor(LEDColor.YELLOW);
				led[i].setOn();
			}
		}
								
	}//Ende LEDSignalForREceivingAdresses()
	
	

	/**
	   * LED signal for spot communication (send).
	   *
	   * @return Green light on LEDs depending on the number of spots.
	   * @return White light on LED depending on index of the receiving spots.
	   * 
	   * @param spot_count number of spots.
	   * @param index index of the receiving spot.
	   * 
	   * @author SpotCommunication.
	   */
	public void LEDSignalCommunicationSend(int spot_count, int index){
		
		for(int i=0; i<spot_count; i++){
			if(i!=index){
				led[i].setColor(LEDColor.GREEN);
				led[i].setOn();
			}else{
				led[i].setColor(LEDColor.WHITE);
				led[i].setOn();
			}
		}
								
	}//Ende LEDSignalForREceivingAdresses()

	/**
	   * LED signal for host found.
	   *
	   * @return Green light on all LEDs.
	   * 
	   * @author SpotIdentifier.
	   */
	public void LEDSignalForHostFound(){
		
		for(int i=0; i<led.length; i++){
			led[i].setColor(LEDColor.GREEN);
			led[i].setOn();
			Utils.sleep(300);
			led[i].setOff();
		}
						
	}//Ende LEDSignalForHostFound()
	
	/**
	   * Set a defined LED on. 
	   *
	   * @param	index Index of the LED (0..7)
	   * @param	color Color of the LED (LEDColor)
	   *
	   */
	public void on(int index, LEDColor color){
		led[index].setColor(color);
		led[index].setOn();
		lastindex = index;
	}//Ende on()
	
	/**
	   * Set a defined LED off. 
	   *
	   * @param	index Index of the LED (0..7)
	   * 
	   */
	public void off(int index){
		led[index].setOff();
	}//Ende off()
	

	/**
	   * Sets off the last used LED. 
	   *
	   */
	public void offLastIndex(){
		led[lastindex].setOff();
	}//Ende off()
	
	/**
	   * LED signal for Exception.
	   *
	   * @return Red light on all LEDs.
	   * 
	   * @author SensorSampler.
	   */
	public void LEDSignalForException(){
		for(int i=0; i<led.length; i++){
			led[i].setColor(LEDColor.RED);
			led[i].setOn();
		}
	}
	
	/**
	   * Sets one blink on an LED with a defined color.
	   *
	   * @param index Index of the LED (0..7)
	   * @param	color Color of the LED (LEDColor)
	   * 
	   */
	public void blink(int index, LEDColor color){
		led[index].setColor(color);
		led[index].setOn();
		Utils.sleep(300);
		led[index].setOff();
	}
	
	
}
