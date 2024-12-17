import network
import config
import time
import ssl

from simple import MQTTClient
from machine import Pin, I2C
from bmp280 import BMP280


# setup wifi
ssid = config.ssid
password = config.pwd

# connect to wifi
wlan = network.WLAN(network.STA_IF)
wlan.active(True)
wlan.connect(ssid, password)

connection_timeout = 10
while connection_timeout > 0:
    if wlan.status() == 3: # connected
        break
    connection_timeout -= 1
    print('Waiting for Wi-Fi connection...')
    time.sleep(1)

# check if connection successful
if wlan.status() != 3: 
    raise RuntimeError('[ERROR] Failed to establish a network connection')
else: 
    print('[INFO] CONNECTED!')
    network_info = wlan.ifconfig()
    print('[INFO] IP address:', network_info[0])
    
# config ssl connection w Transport Layer Security encryption (no cert)
context = ssl.SSLContext(ssl.PROTOCOL_TLS_CLIENT) # TLS_CLIENT = connect as client not server/broker
context.verify_mode = ssl.CERT_NONE # CERT_NONE = not verify server/broker cert - CERT_REQUIRED: verify

# mqtt client connect
client = MQTTClient(client_id=b'kissa', server=config.MQTT_BROKER, port=config.MQTT_PORT,
                    user=config.MQTT_USER, password=config.MQTT_PWD, ssl=context)

client.connect()
print("Connected to broker!")

# define I2C connection and BMP
i2c = machine.I2C(id=0, sda=Pin(20), scl=Pin(21)) # id=channel
bmp = BMP280(i2c)


def publish(mqtt_client, topic, value):
    mqtt_client.publish(topic, value)
    print("[INFO][PUB] Published {} to {} topic".format(value, topic))

while True:
    #For evaluation part: Taking timestamp and writing it to a txt file
    print("START TIME: " + str(time.time_ns() // 1000000) + "ms")
    with open("sensorValues_norm.txt", "a") as file:
        file.write(str(time.time_ns() // 1000000) + "\n")
        
    # publish as MQTT payload
    publish(client, 'kissa/temp', str(bmp.temperature))
    publish(client, 'kissa/pressure', str(bmp.pressure))

    # sleep for 3s
    time.sleep_ms(3000)