#!/usr/bin/python3
import RPi.GPIO as GPIO
import logging
import threading
import queue
import time



class SonarThread(threading.Thread):
	
	def __init__(self, sonarQueue, buzzerQueue, logger): 
		threading.Thread.__init__(self)
		self.sonarQueue = sonarQueue
		self.buzzerQueue = buzzerQueue
		self.logger = logger
		self.stop_event = threading.Event()
		self.WARN_DISTANCE = 15.0
		self.CRIT_DISTANCE = 10.0
		self.STOP_DISTANCE = 5.0
		self.setup()

	def run(self):
		while not self.stopRequest():
			distance = self.distance()
			formattedDistance = format(distance, '.1f')
			if distance < self.WARN_DISTANCE:
				self.logger("Warning Distance : " + str(formattedDistance))
				self.switchOffRgbLed()
				GPIO.output(GPIO_BLUE_LIGHT, True)
			elif distance < self.CRIT_DISTANCE:
				self.logger("Critical Distance : " + str(formattedDistance))
				self.switchOffRgbLed()
				GPIO.output(GPIO_GREEN_LIGHT, True)
			elif distance < self.STOP_DISTANCE:
				self.logger("Stop Distance : " + str(formattedDistance))
				self.sonarQueue.put(distance)
				self.buzzerQueue.put(distance)
				self.switchOffRgbLed()
				GPIO.output(GPIO_RED_LIGHT, True)
			time.sleep(1)
		self.destroy()
		
	def distance(self):
		# set Trigger to HIGH
		GPIO.output(self.GPIO_TRIGGER, True)

		# set Trigger after 0.01ms to LOW
		time.sleep(0.00001)
		GPIO.output(self.GPIO_TRIGGER, False)

		StartTime = time.time()
		StopTime = time.time()

		# save StartTime
		while GPIO.input(self.GPIO_ECHO) == 0:
			StartTime = time.time()

		# save time of arrival
		while GPIO.input(self.GPIO_ECHO) == 1:
			StopTime = time.time()

		# time difference between start and arrival
		TimeElapsed = StopTime - StartTime
		# multiply with the sonic speed (34300 cm/s)
		# and divide by 2, because there and back
		distance = (TimeElapsed * 34300) / 2

		return distance
	
	def switchOffRgbLed(self):
		GPIO.output(self.GPIO_BLUE_LIGHT, False)
		GPIO.output(self.GPIO_GREEN_LIGHT, False)
		GPIO.output(self.GPIO_RED_LIGHT, False)
		
	def setup(self):
		#GPIO Mode (BOARD / BCM)
		GPIO.setmode(GPIO.BCM)
		 
		#set GPIO Pins
		self.GPIO_TRIGGER = 23
		self.GPIO_ECHO = 24
		self.GPIO_RED_LIGHT = 16
		self.GPIO_GREEN_LIGHT = 20
		self.GPIO_BLUE_LIGHT = 21
		 
		#set GPIO direction (IN / OUT)
		GPIO.setup(self.GPIO_TRIGGER, GPIO.OUT)
		GPIO.setup(self.GPIO_ECHO, GPIO.IN)
		GPIO.setup(self.GPIO_RED_LIGHT, GPIO.OUT)
		GPIO.setup(self.GPIO_GREEN_LIGHT, GPIO.OUT)
		GPIO.setup(self.GPIO_BLUE_LIGHT, GPIO.OUT)
		
	def destroy(self):
		#GPIO.output(self.GPIO_TRIGGER, GPIO.HIGH)
		#GPIO.output(self.GPIO_ECHO, GPIO.HIGH)
		#GPIO.output(self.GPIO_RED_LIGHT, GPIO.HIGH)
		#GPIO.output(self.GPIO_GREEN_LIGHT, GPIO.HIGH)
		#GPIO.output(self.GPIO_BLUE_LIGHT, GPIO.HIGH)
		GPIO.cleanup()
		
	
	def stop(self):
		self.stop_event.set()
		
	def stopRequest(self):
		return self.stop_event.is_set()


