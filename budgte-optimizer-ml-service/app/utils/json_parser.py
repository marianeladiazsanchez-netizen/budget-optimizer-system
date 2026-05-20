import json
from pathlib import Path


def load_json(path: str) -> dict:
    file_path = Path(path)
    with file_path.open("r", encoding="utf-8") as file:
        return json.load(file)


def dump_json(path: str, data: dict) -> None:
    file_path = Path(path)
    with file_path.open("w", encoding="utf-8") as file:
        json.dump(data, file, indent=2, ensure_ascii=False)
