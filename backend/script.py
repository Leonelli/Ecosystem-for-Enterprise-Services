#!/usr/bin/python3
import paho.mqtt.client as mqtt
from paho.mqtt.properties import Properties
from paho.mqtt.packettypes import PacketTypes
from paho.mqtt.subscribeoptions import SubscribeOptions
from time import time, monotonic, sleep
import argparse
import os
import ssl

'''Argument parser'''
parser = argparse.ArgumentParser(formatter_class=argparse.RawTextHelpFormatter)
parser.add_argument('-n', '--client_number', dest='num', default = "1", type=str, 
                    help='Specify the ClientID number. E.g.,"1" for Client_1')
parser.add_argument('-v', '--version', dest='version', default = 3, type=int, 
                    help='Specify the MQTT version (defaults to 3. I.e., 3.1.1)')
parser.add_argument('-f', '--data_folder', dest='data_folder', default = "/home/data", type=str, 
                    help='Specify the data folder (containing "certificates" and "results") without trailing /')
parser.add_argument('-ip', '--ip', dest='ip', default = "10.20.30.15", type=str, 
                    help='Specify the IP address/hostname (defaults to "10.20.30.15")')
parser.add_argument('-p', '--port', dest='port', default = 8001, type=int, 
                    help='Specify the port (defaults to 8001 - anonymous)')
parser.add_argument('-q', '--qos', dest='qos', default = 1, type=int, 
                    help='Specify the QoS (defaults to 1)')
                    
parser.add_argument('--username', dest='username', type=str, 
                    help='Specify the client username')
parser.add_argument('--password', dest='password', type=str, 
                    help='Specify the client password')
                    
args = parser.parse_args()

def gen_connect_properties():
    connect_properties = Properties(PacketTypes.CONNECT)
    connect_properties.SessionExpiryInterval = 86400 #0x11 - 1 day
    return connect_properties
    
def on_connect(client, userdata, flags, reasonCode, properties):
    client.stop_time_connect = monotonic()
    client.on_connect_received = True
    
    '''print(f"{client._client_id.decode()} connection. Result: {mqtt.connack_string(reasonCode)} Code: {reasonCode.getName()}", flush=True)
    if (userdata):
        print("- Userdata "+ str(userdata), flush=True)
    if (flags):
        print("- Flags "+ str(flags), flush=True)
    if (properties):
        print("- Properties"+ str(properties), flush=True)'''
    
def on_connect_311(client, userdata, flags, reasonCode):
    client.stop_time_connect = monotonic()
    client.on_connect_received = True

    '''print(f"{client._client_id.decode()} connection. Result: {mqtt.connack_string(reasonCode)} Code: {reasonCode}", flush=True)
    if (userdata):
        print("- Userdata "+ str(userdata), flush=True)
    if (flags):
        print("- Flags "+ str(flags), flush=True)'''

def on_subscribe(client, userdata, mid, reasonCodes, properties = None):
    client.stop_time_subscribe = monotonic()
    client.on_subscribe_received = True

    '''print(f"{client._client_id.decode()} subscribed. Message ID: {mid}. Subscription codes:", flush=True)
    for rc in reasonCodes:
        print("--" + rc.getName(), flush=True)
    if (userdata):
        print("-Userdata "+ str(userdata), flush=True)
    if (properties):
        print("-Properties"+ str(properties), flush=True)'''
    
def on_subscribe_311(client, userdata, mid, granted_qos):
    client.stop_time_subscribe = monotonic()
    client.on_subscribe_received = True

    '''print(f"{client._client_id.decode()} subscribed. Message ID: {mid}", flush=True)
    for q in granted_qos:
        print("-Granted QoS: " + str(q), flush=True)
    if (userdata):
        print("-Userdata: "+ str(userdata), flush=True)'''

def on_message(client, userdata, msg):
    #Command to disconnect (clean session)
    global message_expected
    global message_counter
    global sleep_time
    
    global start_time_messages
    global last_message_time
    global first_message
    
    last_message_time = monotonic()
    
    if(msg.topic == "disconnect"):
        settings = msg.payload.decode('utf-8').split(";")
        sleep_time = int(settings[0])
        message_expected = int(settings[1])
        print(f"Expected to receive {message_expected} messages and sleep for {sleep_time}s", flush=True)
        
        client.disconnect()
        client.loop_stop()
    else: #On topic payload
        if(first_message):
            start_time_messages = monotonic() #Start measuring
            first_message = False
        else:
            message_counter += 1
            print(f"Received {message_counter} message(s)", flush=True)
        
    '''print(f"Message received from {client._client_id.decode()}. Topic {msg.topic}, QoS {msg.qos}", flush=True)
    #+print(f"Payload {msg.payload})
    if (userdata):
        print("-Userdata "+ str(userdata), flush=True)'''

def set_callbacks_and_parameters(client, port, username, password, folder, qos):
    # 8001 - No TLS
    unilateral_ports = [8002, 8003, 8006, 8007]
    mutual_ports = [8004, 8005, 8008, 8009]

    if port in unilateral_ports:
        client.tls_set(f"{folder}/certificates/ca.crt",
                       None,
                       None,
                       cert_reqs=ssl.CERT_NONE,
                       tls_version=ssl.PROTOCOL_TLS,
                       ciphers=None)
    elif port in mutual_ports:
        client.tls_set(f"{folder}/certificates/ca.crt",
                       f"{folder}/certificates/{client._client_id.decode()}.crt",
                       f"{folder}/certificates/{client._client_id.decode()}.key",
                       cert_reqs=ssl.CERT_NONE,
                       tls_version=ssl.PROTOCOL_TLS,
                       ciphers=None)                    
    if (username):
        if(password):
            client.username_pw_set(username,password)
        else:
            client.username_pw_set(username)

    if(client._protocol == 5):
        client.on_connect = on_connect
        client.on_subscribe = on_subscribe
        client.test_topics = [("disconnect", SubscribeOptions(qos)), ("payload", SubscribeOptions(qos))]
        #client.test_topic = ("#", SubscribeOptions(qos))
    else:
        client.on_connect = on_connect_311
        client.on_subscribe = on_subscribe_311
        client.test_topics = [("disconnect", qos), ("payload", qos)]
        #client.test_topic = ("#", qos)
    
    client.on_message = on_message
    client.on_connect_received = False
    client.on_subscribe_received = False

def init_client(version, cID, clean, ip, port, username, password, folder, qos):
    if version == 3:
        client = mqtt.Client(f"Client_{cID}", clean_session=clean)
        set_callbacks_and_parameters(client, port, username, password, folder, qos)
        client.connect(ip, port)
    else:
        client = mqtt.Client(f"Client_{cID}", protocol=mqtt.MQTTv5)
        set_callbacks_and_parameters(client, port, username, password, folder, qos)
        client.connect(ip, port, clean_start = clean, properties=gen_connect_properties())
    #client.start_time_connect  = monotonic()
    client.loop_start()
    return client

try:
    #Populate from ENV or cmd arguments
    cID = os.getenv('CLIENT_ID', args.num)
    version = int(os.getenv('VERSION', args.version))
    folder = os.getenv('DATA_FOLDER', args.data_folder)
    ip = os.getenv('IP', args.ip)
    port = int(os.getenv('PORT', args.port))
    username = os.getenv('USERNAME', args.username)
    password = os.getenv('PASSWORD', args.password)
    qos = int(os.getenv('QOS', args.qos))
    
    message_expected = 0
    message_counter = 0
    sleep_time = 10
    
    first_message = True
    start_time_messages = 0
    last_message_time = 0
    timeout_occurred = False
    
    # Create and connect a client using MQTT version 3.1.1 (default) or 5.0
    client = init_client(version, cID, False, ip, port, username, password, folder, qos)

    # Wait for the connection before proceding
    while(not client.on_connect_received):
        sleep(.1)

    #connect_time = client.stop_time_connect - client.start_time_connect

    # Subscribe to the three topics (# does not work) with <q> QoS:
    client.subscribe(client.test_topics)
    #client.subscribe(client.test_topic)

    # Wait for the subscription before proceding
    while(not client.on_subscribe_received):
        sleep(.1)

    # Disconnect upon setting expected messages; wait <STIME> seconds; reconnect with clean_session false and receive 1 message.
    while(message_expected == 0):
        sleep(.1)
    
    sleep(sleep_time)

    client = init_client(version, cID, False, ip, port, username, password, folder, qos)
    
    # Wait for messages or timeout (10 minutes from the last message)
    while(message_counter < message_expected):
        if(monotonic() > (last_message_time + 600)):
            timeout_occurred = True
            break
        sleep(.5)
    
    sleep(0.5*sleep_time)
    
    client.disconnect()
    client.loop_stop()
    
except KeyboardInterrupt:
    print("Keyboard-exit. Bye!", flush=True)
    
with open(f"{folder}/results/{cID}_{port}","w") as f:
    f.write(f"{message_counter} message(s) received (upon {message_expected}) in {last_message_time - start_time_messages : .2f}s\n")
    f.write(f"Timeout: {timeout_occurred}")
