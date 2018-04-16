#!/usr/bin/python3
import RPi.GPIO as GPIO
import time
import threading


class BuzzerThread(threading.Thread):
	
	#GPIO Mode (BOARD / BCM)
	GPIO.setmode(GPIO.BOARD) # Numbers GPIOs by physical location
	 
	#set GPIO Pins
	GPIO_BUZZER = 10
	
	SPEED = 1 
	
	#set GPIO direction (IN / OUT)
	GPIO.setup(GPIO_BUZZER, GPIO.OUT)
	
	
		
	# List of tone-names with frequency
	TONES = {"c6":1047,
		"b5":988,
		"a5":880,
		"g5":784,
		"f5":698,
		"e5":659,
		"eb5":622,
		"d5":587,
		"c5":523,
		"b4":494,
		"a4":440,
		"ab4":415,
		"g4":392,
		"f4":349,
		"e4":330,
		"d4":294,
		"c4":262}

	# Song is a list of tones with name and 1/duration. 16 means 1/16
	SONG =	[
		["e5",16],["eb5",16],
		["e5",16],["eb5",16],["e5",16],["b4",16],["d5",16],["c5",16],
		["a4",8],["p",16],["c4",16],["e4",16],["a4",16],
		["b4",8],["p",16],["e4",16],["ab4",16],["b4",16],
		["c5",8],["p",16],["e4",16],["e5",16],["eb5",16],
		["e5",16],["eb5",16],["e5",16],["b4",16],["d5",16],["c5",16],
		["a4",8],["p",16],["c4",16],["e4",16],["a4",16],
		["b4",8],["p",16],["e4",16],["c5",16],["b4",16],["a4",4]
		]

	def __init__(self, buzzerQueue, logger): 
			threading.Thread.__init__(self)
			self.buzzerQueue = buzzerQueue
			self.logger = logger
			self.stop_event = threading.Event()


	def playTone(self, p, tone):
			# calculate duration based on speed and tone-length
		duration = (1./(tone[1]*0.25*SPEED))

		if tone[0] == "p": # p => pause
			time.sleep(duration)
		else: # let's rock
			frequency = TONES[tone[0]]
			p.ChangeFrequency(frequency)
			p.start(0.5)
			time.sleep(duration)
			p.stop()

	def run(self, maxValue):
		while not self.stopRequest():
			if not self.queue.empty():
				distance = self.queue.get()
				p = GPIO.PWM(GPIO_BUZZER, 440)
				p.start(0.5)
				if maxValue >= len(SONG):
					maxValue = len(SNG) - 1
				elif maxValue < 5:
					maxValue = 5
				for t in range(0, maxValue):				
					self.playTone(p, SONG[t])
				self.logger.debug("Play buzzer value : ", maxValue)
		GPIO.output(GPIO_BUZZER, GPIO.HIGH)
		GPIO.cleanup()			# Release resource

		
	def stop(self):
		self.stop_event.set()
		
	def stopRequest(self):
		return self.stop_event.is_set()


