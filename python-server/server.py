from flask import Flask, request, jsonify
from chatterbot import ChatBot
from chatterbot.trainers import ChatterBotCorpusTrainer

app = Flask(__name__)

# Create chatbot
chatbot = ChatBot('VirtualAssistant')
trainer = ChatterBotCorpusTrainer(chatbot)
trainer.train("chatterbot.corpus.english")


@app.route('/get_response', methods=['GET'])
def get_response():
    user_input = request.args.get('input')
    if user_input:
        bot_response = chatbot.get_response(user_input)
        return jsonify({'response': str(bot_response)})
    else:
        return jsonify({'response': 'No input provided'}), 400


if __name__ == '__main__':
    app.run(debug=True)
