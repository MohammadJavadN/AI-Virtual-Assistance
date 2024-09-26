import speech_recognition as sr


def stt(audio_file):
    # Initialize the recognizer
    recognizer = sr.Recognizer()

    # Load the audio file
    with sr.AudioFile(audio_file) as source:
        audio_data = recognizer.record(source)

    # Convert audio to text
    try:
        # Specify the language as Persian (Farsi)
        text = recognizer.recognize_google(audio_data, language='fa-IR')
        print("Audio converted to text: \n", text, "\n")
        return text
    except sr.UnknownValueError:
        print("Google Speech Recognition could not understand audio")
    except sr.RequestError as e:
        print(
            f"Could not request results from Google Speech Recognition service; {e}")
    return None


# Example usage
text = stt('received_audio.wav')
