import socket

server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server_socket.bind(('0.0.0.0', 12345))
server_socket.listen(1)

print("Server is listening")

while True:
    client_socket, addr = server_socket.accept()
    print(f"Connection from {addr}")

    with open('received_audio.pcm', 'wb') as f:
        while True:
            data = client_socket.recv(1024)
            if not data:
                break
            f.write(data)

    client_socket.close()
    print(f"Connection from {addr} closed")
