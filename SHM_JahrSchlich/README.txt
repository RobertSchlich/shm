Sun SPOT Send Data Demo - Version 1.0

Author: Ron Goldman 

--------
OVERVIEW
--------

This demo application sends periodic sensor readings measured on one
or more Sun SPOTs to an application on your laptop or PC that displays
the values. It consists of two parts:

1.     SendDataDemo-onSPOT:

       This application runs on a Sun SPOT periodically sampling
       the built-in light sensor and broadcasting those readings
       over the radio.

2(a)   SendDataDemo-onDesktop:

       This application runs on a host computer to which a basestation
       has been attached [NOTE: a basestation is not required if you'll
       be running the SendDataDemo-onSPOT portion of the code entirely 
       on emulated SPOTs]. It listens for the sensor sample broadcasts
       and prints them.  

2(b)   SendDataDemo-GUIonDesktop:

       This is another application to display the data on a host
       computer to which a basestation has been attached. It listens
       for the sensor sample broadcasts and graphically displays them.  


NOTE:
=====

If you do not have Sun SPOT devices, you can still experiment with this 
code using the emulator. Specific instructions are included below but 
please refer to the emulator tutorial first if you are unfamiliar with 
the overall process of deploying and running code on virtual SPOTs.

---------------------------------------
STEPS FOR BUILDING AND RUNNING THE DEMO
---------------------------------------

1. Compile and deploy code to Sun SPOTs.

   [Follow the instructions in 1(a) if you are experimenting with
    this demo on real SPOTs. Otherwise, follow the instructions in
    1(b) if you are using emulated SPOTs.]

   (a) Go into the SendDataDemo-onSPOT directory, connect a SPOT to 
       your host using a USB cable and execute:

       ant deploy

       This will compile the sensor sampling application and load it. 
       Disconnect the SPOT and reset it. After the SPOT finishes rebooting,
       it'll enter a loop where it reads the light sensor, broadcasts that 
       reading and goes to sleep for ten seconds before the next sample.
       The SPOT flashes the rightmost LED to visually indicate a sampling
       event. You should notice the SPOT going into deep sleep between
       consecutive samples if it isn't running any other threads that 
       need to use the CPU, radio or other resources.

   (b) Go into the SendDataDemo-onSPOT directory and execute:

       ant solarium

       This will start up the Solarium tool that includes the SPOT
       emulator. 
          i.  Create a virtual SPOT by choosing Emulator -> New Virtual
              SPOT.
         ii.  Right click on the virtual SPOT, choose "Deploy a MIDlet 
              bundle ..." from the menu. A file chooser window will pop up.
              Navigate to the SendDataDemo-onSPOT subdirectory and select
              the "build.xml" file. This will automatically cause the 
              application to be compiled and loaded into the virtual SPOT.
        iii.  Right click on the virtual SPOT, choose "Display application
              output" -> "in Internal Frame" to open a window where
              output from the application will be displayed.
         iv.  Right click on the virtual SPOT, choose "Display sensor
              panel" to open up a multi-tabbed panel that allows you
              to manipulate various sensor readings experienced by the
              virtual SPOT.
          v.  Right click again on the virtual SPOT, choose "Run 
              MIDlet ..." and select the "SensorSampler" application.

       The SPOT will enter a loop where it reads the light sensor, 
       broadcasts that reading and goes to sleep for ten seconds before 
       the next sample. These readings will be displayed in the window
       created in step (iii) above. The SPOT flashes the rightmost LED 
       to visually indicate a sampling event. You can change the light
       sensor values read by a virtual SPOT by manipulating the slider
       marked "Light Sensor" (under the "Enviro" tab) on its sensor 
       panel.

   Repeat this step on as many SPOTs as you want to collect samples on.

2. Run the host application

   [Follow the instructions in 2(a) if you are experimenting with
    this demo on real SPOTs. Otherwise, follow the instructions in
    2(b) if you are using emulated SPOTs.]

   (a) Go into the SendDataDemo-onDesktop directory, connect a basestation
       to your host using a USB cable and execute:

       ant host-run

   (b) Go into the SendDataDemo-onDesktop directory and execute:

   ant host-run -Dbasestation.shared=true -Dbasestation.not.required=true 

   This will start the host application that collects sample readings
   over the radio and displays them. Each sample reading includes
   the Id (an IEEE address) of the sending SPOT, the timestamp when the
   sample was collected and the sensor reading. 

3. Repeat step 2 only this time use the SendDataDemo-GUIonDesktop
   version of the host app that will display the data gathered
   graphically, with a separate window for each SPOT.

---------------
WHAT TO DO NEXT
---------------

   - Try to collect and display multiple sensor readings, e.g. modify
     the demo to collect/display temperature readings in addition to
     the light readings. 

     Let the demo run overnight and see if you can correlate changes
     in the light/temperature sensor readings readings with the onset
     of day/night. Increase the sleep period between samples to make 
     the battery last longer between consecutive charges. This only
     applies if you are using real (not emulated) SPOTs. For the
     graphical display you will need a way to modify the scale of the
     X (time) access.

