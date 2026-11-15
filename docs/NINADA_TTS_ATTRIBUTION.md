# Ninada Sanskrit Neural TTS attribution

UtsavSetu's optional on-device Sanskrit neural recitation uses the Sanskrit male and female ONNX specialists from **Ninada** by Hemanth HM.

- Model repository: https://huggingface.co/gnumanth/ninada
- Runtime and source: https://github.com/hemanth/ninada
- Model release integrated: v5.1.2
- Model weights and training corpus: CC BY 4.0
- Runtime source code: MIT
- Sanskrit training corpus attribution: AI4Bharat Rasa (CC BY 4.0)

The model files are downloaded on demand from the Hugging Face repository into app-private storage. After download, speech synthesis runs on-device. UtsavSetu does not upload mantra text to a speech-generation server.

The first playback for each selected voice requires an internet connection to download its model. Subsequent synthesis can run offline, provided the model has been downloaded successfully. Male and female models are separate downloads.

The neural model is an experimental integration until validated on physical Android devices and reviewed by Sanskrit speakers. The existing Android TextToSpeech engine remains the fallback if model download or neural inference fails.

## Required attribution

Ninada (निनाद), by Hemanth HM. Sanskrit speech model trained using AI4Bharat Rasa. Model weights and training corpus released under Creative Commons Attribution 4.0 International (CC BY 4.0). Runtime source code released under MIT.

CC BY 4.0: https://creativecommons.org/licenses/by/4.0/
