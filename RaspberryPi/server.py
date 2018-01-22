import serial
import sys
import socket
from time import sleep

ser = serial.Serial('/dev/serial/by-id/usb-FTDI_FT232R_USB_UART_A100Q8UX-if00-port0')

HOST = '192.168.50.33' #this is your localhost
PORT = 8888

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
#socket.socket: must use to create a socket.
#socket.AF_INET: Address Format, Internet = IP Addresses.
#socket.SOCK_STREAM: two-way, connection-based byte streams.
print 'socket created'

#Bind socket to Host and Port
try:
    s.bind((HOST, PORT))
except socket.error as err:
    print 'Bind Failed, Error Code: ' + str(err[0]) + ', Message: ' + err[1]
    sys.exit()

print 'Socket Bind Success!'


#listen(): This method sets up and start TCP listener.
s.listen(10)
print 'Socket is now listening'

conn, addr = s.accept()
print 'Connect with ' + addr[0] + ':' + str(addr[1])

while 1:
    
    recvCommand = conn.recv(64)
    
    ser.write(recvCommand)
    
    print recvCommand

s.close()