#!/usr.bin/python
#GROUND - ground lane
#DATA - GPIO 4 pin 7
#VCC - 3.3V pin 1


import sys
import Adafruit_DHT
from firebase import firebase
import json
firebase = firebase.FirebaseApplication('https://testmh3-abcf9.firebaseio.com/')

#result = firebase.get('/values', 'temp')

while True:
    humidity, temperature = Adafruit_DHT.read_retry(11,4)
    temperature = (temperature * 1.8) + 32
    q=str(temperature)
    z=str(humidity)
    #result = firebase.put('/values',"temp",q)
    #result = firebase.put('/values',"humidity",z)
    print 'Temp: {0:0.1f} C Humidity: {1:0.1f}%'.format(temperature,humidity)