#!/usr/bin/python2

from threading import Thread
import RPi.GPIO as GPIO
import time
import urllib, urllib2
import base64
import sys
import sys
import pygame
import pygame.camera
import StringIO
from PIL import Image

house_id = 3
keep_alive_time = 15 # in seconds
stop = False

url = "http://argus-adrianodennanni.c9.io/"

GPIO.setmode(GPIO.BCM)
GPIO.setwarnings(False)


window1 = 2
window2 = 3
door = 4

GPIO.setup(window1, GPIO.IN, GPIO.PUD_UP)
GPIO.setup(window2, GPIO.IN, GPIO.PUD_UP)
GPIO.setup(door, GPIO.IN, GPIO.PUD_UP)



#main

current_window1 = GPIO.input(window1)
current_window2 = GPIO.input(window2)
current_door = GPIO.input(door)

#Init webcam
has_cam = False
cam = 0

pygame.init()
pygame.camera.init()
cams = pygame.camera.list_cameras()
if len(cams) != 0:
	cam = pygame.camera.Camera(cams[0], (480,360))
	cam.start()
	has_cam = True
	print "Camera on"

else:
	print "Camera not found"

def send_image():
	global url
	global has_cam
	global cam
	if not has_cam:
		return
	surface = cam.get_image() 
	imgstr = pygame.image.tostring(surface, 'RGB', False)
	image = Image.fromstring('RGB', surface.get_size(), imgstr)
	buffer = StringIO.StringIO()
	image.save(buffer, "JPEG", quality=100)
	encoded_image = base64.encodestring(buffer.getvalue())
	
	raw_params = { 'image': encoded_image, 'house_id': house_id}
	params = urllib.urlencode(raw_params)
	page = url + 'send_snap'
	request = urllib2.Request(page, params)
	request.add_header("Content-type", "application/x-www-form-urlencoded; charset=UTF-8")
	print 'Sending image to ' + page
	try: 
		
		response = urllib2.urlopen(request).read()
		print response
	except urllib2.HTTPError as e:
		error_message = e.read()
		print error_message

		
#envia pro servidor e printa a resposta
def send_up_sensor(idsensor,estadosensor):
	global url
	url_sensor = url + "sensor_update"
	values = {'open':estadosensor ,
          'sensor_id' : idsensor ,'house_id' : house_id
        }
    
    	url_values = urllib.urlencode(values)
	url_full = url_sensor + '?' + url_values
	print 'Sending ' + url_full
	response = urllib.urlopen(url_full).read()
	print response
	
def send_identification(panic):
	global url
	url_ident = url + "identification"
	values = {
          'panic' : 1 if panic else 0 ,'house_id' : house_id
        }
	url_full = url_ident + '?' + urllib.urlencode(values)
	print 'Sending ' + url_full
	response = urllib.urlopen(url_full).read()
	print response

def send_keep_alive():
	global url
	url_alive = url + "keep_alive"
	values = {'house_id' : house_id  }
	url_full = url_alive + '?' + urllib.urlencode(values)
	print 'Sending ' + url_full
	response = urllib.urlopen(url_full).read()
	print response

#Funcao de leitura dos sensores, envia json se algo mudar
def my_callback(event):

	global current_window1
	global current_window2
	global current_door
	global window1
	global window2
	global door
	

	if GPIO.input(window1) != current_window1:
		current_window1 = GPIO.input(window1) #atualiza
		if GPIO.input(window1) == True:
			print("Abriram a janela1")
			send_up_sensor(2,1)
		else :
			print("Fecharam a janela1")
			send_up_sensor(2,0)
		


	if GPIO.input(window2) != current_window2:
		current_window2 = GPIO.input(window2) #atualiza
		if GPIO.input(window2) == True:
			print("Abriram a janela2")
			send_up_sensor(3,1)
		else :
			print("Fecharam a janela2")
			send_up_sensor(3,0)			
		

	if GPIO.input(door) != current_door:
		current_door = GPIO.input(door)	#atualiza
		if GPIO.input(door) == True:
			print("Abriram a porta")
			send_up_sensor(1,1)
			temp_thread2 = Thread(target=send_image)
			temp_thread2.start()
			#send_image()
		else :
			print("Fecharam a porta")
			send_up_sensor(1,0)
		 
try:
	#GPIO.add_event_detect(window1, GPIO.RISING, callback=my_callback) 
	#GPIO.add_event_detect(window2, GPIO.RISING, callback=my_callback) 
	GPIO.add_event_detect(door, GPIO.RISING, callback=my_callback)
except:
	#GPIO.add_event_detect(window1, GPIO.RISING, callback=my_callback) 
	#GPIO.add_event_detect(window2, GPIO.RISING, callback=my_callback) 
	GPIO.add_event_detect(door, GPIO.RISING, callback=my_callback)
	


#codigo do frances
def check_password(user_input, panic=False):
	file = 'password' + ('_panic' if panic else '')
	try:
		f = open(file, 'r')
		password = f.read()
		if user_input == password :
			return 1 if not panic else 2
		elif not panic:
			return check_password(user_input, True)
		else:
			return 0
	except IOError:
		f = open(file, 'w')
		f.write('1234' if not panic else '4321')
		return check_password(user_input, panic)



#thread of keep alive 
def keep_alive():
	global stop
	while not stop:	
		send_keep_alive()
		time.sleep(keep_alive_time)
try:

	
	keep_alive_thread = Thread(target=keep_alive)
	keep_alive_thread.start()
	
	#loop of password
	while not stop:
		try:
			keyboard_input = raw_input("Password loop..\n\n")
			value = check_password(keyboard_input)
			if value > 0:
				send_identification(value == 2)	
		except:
			stop = True
	
finally:
	GPIO.cleanup()
	print("Fabyfaby")


