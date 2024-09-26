import io
import socket
import logging
import speech_recognition as sr

from pydub import AudioSegment
from googletrans import Translator
from flask import Flask, request, jsonify
from threading import Thread


logging.basicConfig(level=logging.INFO)


def receive_audio_data():
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_socket.bind(('0.0.0.0', 12345))
    server_socket.listen(1)
    logging.info("Server is listening")

    while True:

        client_socket, addr = server_socket.accept()
        logging.info(f"Connection from {addr}")

        pcm_data = io.BytesIO()

        while True:
            data = client_socket.recv(1024)
            if not data:
                break
            pcm_data.write(data)

        client_socket.sendall(b"Audio data received. Processing...\n")

        pcm_data.seek(0)

        # Convert PCM to WAV using pydub and in-memory bytes buffer
        audio_segment = AudioSegment(
            data=pcm_data.read(),
            sample_width=2,  # Number of bytes per sample
            frame_rate=44100,  # Sample rate in Hz
            channels=1  # Number of audio channels (1 for mono, 2 for stereo)
        )

        wav_buffer = io.BytesIO()
        audio_segment.export(wav_buffer, format='wav')
        wav_buffer.seek(0)

        # Recognize speech
        recognizer = sr.Recognizer()
        with sr.AudioFile(wav_buffer) as audio_file:
            audio_data = recognizer.record(audio_file)

        try:
            text = recognizer.recognize_google(audio_data, language='fa-IR')
            logging.info(text)
            text = translate_to_en(text)
            response = f"Recognized text: {text}\n"
            logging.info(response)
        except sr.UnknownValueError:
            response = "Google Speech Recognition"
            + " could not understand the audio\n"
            logging.error(response)
        except sr.RequestError as e:
            response = "Could not request results from Google Speech"
            + f" Recognition service; {e}\n"
            logging.error(response)

        # Send the response back to the client
        client_socket.sendall(response.encode('utf-8'))

        client_socket.close()
        logging.info(f"Connection from {addr} closed")


def translate_to_en(text):
    translator = Translator()
    translated_text = translator.translate(text, src='fa', dest='en').text
    return translated_text


# Flask app for translation
app = Flask(__name__)
translator = Translator()


@app.route('/translate', methods=['POST'])
def translate_text():
    data = request.json
    text = data.get('text', '')
    target_lang = data.get('target_lang', 'fa')

    if not text:
        return jsonify({'error': 'No text provided'}), 400

    try:
        translated_text = translator.translate(text, dest=target_lang).text
        return jsonify({'translated_text': translated_text}), 200
    except Exception as e:
        return jsonify({'error': str(e)}), 500


if __name__ == "__main__":
    # Start the audio receiving server in a separate thread
    audio_thread = Thread(target=receive_audio_data)
    audio_thread.start()

    # Start the Flask app for translation
    app.run(host='0.0.0.0', port=5000)