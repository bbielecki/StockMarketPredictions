#!/usr/bin/python3

import logging
import socket

TCP_HOST = "127.0.0.1"
TCP_PORT = 10000
BUFFER_SIZE = 1024
QUEUE_SIZE = 128

logging.basicConfig(format='[%(asctime)s][%(levelname)s] %(name)s: %(message)s', level=logging.INFO)

# Serve model on TCP socket
s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.bind((TCP_HOST, TCP_PORT))
s.listen(QUEUE_SIZE)

logging.info('Model running on %s:%s', TCP_HOST, TCP_PORT)

while True:
    try:
        connection, address = s.accept()

        logging.info('%s:%s connected', *address)

        data = b''
        while True:
            buffer = connection.recv(BUFFER_SIZE)
            data += buffer
            if len(buffer) < BUFFER_SIZE:
                break

        model_input_line = data.decode()
        logging.info('%s:%s received %s', *address, model_input_line)
        model_output_line = str([0.5] * 5)
        connection.sendall(model_output_line.encode())
        logging.info('%s:%s sent %s', *address, model_output_line)
        connection.close()
    except Exception as e:
        logging.error(e)
