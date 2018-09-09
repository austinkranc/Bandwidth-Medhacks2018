#http://codelectron.com/rotary-encoder-with-raspberry-pi/
#sudo python rotary.py
#DT - GPIO 17 pin 11 
#CLK - GPIO 18 (clk) pin 12
#SWITCH ---------------
#VCC - 5V pin 2
#GRND - ground lane

from RPi import GPIO
from time import sleep
from firebase import firebase
import json
firebase = firebase.FirebaseApplication('https://testmh3-abcf9.firebaseio.com/')

#result = firebase.get('/values', 'width')

clk = 18
dt = 17
GPIO.setmode(GPIO.BCM)
GPIO.setup(clk, GPIO.IN, pull_up_down=GPIO.PUD_UP)
GPIO.setup(dt, GPIO.IN, pull_up_down=GPIO.PUD_UP)
counter = 7
clkLastState = GPIO.input(clk)
try:
        while True:
                clkState = GPIO.input(clk)
                if clkState != clkLastState:
                        dtState = GPIO.input(dt)
                        if dtState != clkState:
                                if counter > 0:
                                    counter -= 0.05
                        else:
                                counter += 0.05
                        
                        q=str(counter)
                        
                        result = firebase.put('/values',"width",q)        
                        print counter
                clkLastState = clkState
finally:
        GPIO.cleanup()