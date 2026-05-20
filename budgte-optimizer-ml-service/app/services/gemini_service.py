from app.providers.gemini_provider import GeminiProvider

class GeminiService:
    def __init__(self):
        self.provider = GeminiProvider()

    def call(self, prompt: str) -> dict:
        return self.provider.call_model(prompt)
