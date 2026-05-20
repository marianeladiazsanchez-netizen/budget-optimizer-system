import os
import json
import google.generativeai as genai
from app.core.config import GeminiConfig
from app.core.exceptions import BudgetServiceError


class GeminiProvider:
    def __init__(self):
        self.config = GeminiConfig()
        if not self.config.api_key:
            raise BudgetServiceError("GEMINI_API_KEY not configured")
        genai.configure(api_key=self.config.api_key)
        self.model = genai.GenerativeModel(self.config.model_name)

    def call_model(self, prompt: str) -> dict:
        try:
            response = self.model.generate_content(prompt)
            result = response.text
            
            # Try to parse as JSON if the response contains JSON
            if result.strip().startswith("{"):
                try:
                    return json.loads(result)
                except json.JSONDecodeError:
                    pass
            
            return {
                "summary": result,
                "recommended_budget": 0.0,
                "risk_level": "moderado",
                "details": {"raw_response": result},
                "anomalies": []
            }
        except Exception as e:
            raise BudgetServiceError(f"Error calling Gemini API: {str(e)}")
