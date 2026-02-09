"""Minimal YAML stub for Codex scripts (safe_load only)."""
from typing import Dict

class YAMLError(Exception):
    """Raised when the YAML payload cannot be parsed."""


def _flush(current_key, current_value, data):
    if current_key is None:
        return
    value = "\n".join(current_value).strip()
    data[current_key] = value


def safe_load(text: str) -> Dict[str, str]:
    data: Dict[str, str] = {}
    current_key = None
    current_value = []

    for raw_line in text.splitlines():
        line = raw_line.rstrip()
        if not line.strip():
            if current_key is not None:
                current_value.append("")
            continue
        if line.lstrip() != line and current_key is not None:
            current_value.append(line.strip())
            continue
        if ":" not in line:
            raise YAMLError(f"Unable to parse line: {line}")
        key, value = line.split(":", 1)
        key = key.strip()
        if not key:
            raise YAMLError(f"Missing key in line: {line}")
        _flush(current_key, current_value, data)
        current_key = key
        current_value = [value.strip()]

    _flush(current_key, current_value, data)
    return data
