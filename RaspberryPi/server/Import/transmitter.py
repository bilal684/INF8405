#!/usr/bin/python3
import socket
import logging
import threading
import queue
import time


class TransmitterThread(threading.Thread):

	def __init__(self, serialPort, conList, DistanceList, logger): 
		threading.Thread.__init__(self)
		self.serial = serialPort
		self.conList = conList
		self.DistanceList = DistanceList
		self.logger = logger
		self.stop_event = threading.Event()
		self.STOP_DISTANCE = 8.0

	def run(self):
		while not self.stopRequest():			
			if self.conList:
				connection = self.conList[len(self.conList)-1]
				try:
					recvCommand = connection.recv(1024).decode()
					if not recvCommand:
						self.serial.write('x'.encode())
						continue
					if self.DistanceList:
						distance = self.DistanceList[0]
						com = recvCommand.lower()
						if distance <= self.STOP_DISTANCE and com != 'z' and com != 's' and com != 'c' and com != 'x':
							self.serial.write('x'.encode())
							self.logger.debug('x')
							continue
					self.serial.write(recvCommand.encode())					
					self.logger.debug(recvCommand)
				except socket.error as e:
					if not self.queue.empty():
						distance = self.queue.get()
					self.logger.info(str(e) + ": connection is interrupted ")
					self.conList.remove(conn)
					conn.close()
					self.logger.info("Server is listening ...[CTRL] + [C] to quit")
			else:
				if not self.queue.empty():
					distance = self.queue.get()
				time.sleep(1)
		
	def stop(self):
		self.stop_event.set()
		
	def stopRequest(self):
		return self.stop_event.is_set()
		
