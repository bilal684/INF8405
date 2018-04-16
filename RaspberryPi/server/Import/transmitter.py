#!/usr/bin/python3
import socket
import logging
import threading
import queue
import time


class TransmitterThread(threading.Thread):

	def __init__(self, serialPort, conList, sonarQueue, logger): 
		threading.Thread.__init__(self)
		self.serial = serialPort
		self.conList = conList
		self.queue = sonarQueue
		self.logger = logger
		self.stop_event = threading.Event()

	def run(self):
		while not self.stopRequest():			
			if self.conList:
				if len(self.conList) > 1:
					for conn in range(0, len(self.conList)-2):
						self.conList.remove(conn)
				connection = self.conList[0]
				try:
					recvCommand = connection.recv(1024).decode()
					if not recvCommand:
						self.serial.write('x'.encode())
						continue
					if not self.queue.empty():
						distance = self.queue.get()
						com = recvCommand.lower()
						if com != 'z' and com != 's' and com != 'c':
							self.serial.write('x'.encode())
							self.logger.debug('x'.encode())
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
		
