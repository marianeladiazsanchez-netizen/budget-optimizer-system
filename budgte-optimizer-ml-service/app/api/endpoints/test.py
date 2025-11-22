import google.generativeai as genai

genai.configure(api_key="YOUR_API_KEY")

# Listar modelos
for model in genai.list_models():
    if 'generateContent' in model.supported_generation_methods:
        print(f"Model: {model.name}")
        print(f"Display name: {model.display_name}")
        print(f"Max input tokens: {model.input_token_limit}\n")