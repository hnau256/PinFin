import json
import os
import re

# Регулярное выражение для UUID-имени файла
UUID_PATTERN = re.compile(r'^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$', re.IGNORECASE)

def format_amount(value):
    """Делит на 100 и убирает все лишние нули/точки"""
    try:
        num = float(value) / 100
        # Формат :g убирает лишнее (10.0 -> 10, 0.50 -> 0.5)
        return f"{num:g}"
    except (ValueError, TypeError):
        return value

def process_node(data):
    """Рекурсивный обход JSON-дерева"""
    if isinstance(data, dict):
        for k, v in data.items():
            if k == "amount":
                data[k] = format_amount(v)
            else:
                process_node(v)
    elif isinstance(data, list):
        for item in obj_list: # Ошибка в переменной, исправляем ниже
            pass

# Исправленная функция обхода
def transform(data):
    if isinstance(data, dict):
        return {k: (format_amount(v) if k == "amount" else transform(v)) for k, v in data.items()}
    elif isinstance(data, list):
        return [transform(item) for item in data]
    return data

def main():
    # 1. Ищем файл с именем UUID
    target_file = next((f for f in os.listdir('.') if UUID_PATTERN.match(f) and os.path.isfile(f)), None)

    if not target_file:
        print("Файл с именем UUID не найден.")
        return

    temp_file = target_file + ".tmp"

    try:
        with open(target_file, 'r', encoding='utf-8') as f_in, \
             open(temp_file, 'w', encoding='utf-8') as f_out:

            for line in f_in:
                line = line.strip()
                if not line:
                    continue

                try:
                    obj = json.loads(line)
                    processed_obj = transform(obj)

                    # КЛЮЧЕВОЙ МОМЕНТ: separators=(',', ':') убирает все пробелы
                    compact_json = json.dumps(
                        processed_obj,
                        ensure_ascii=False,
                        separators=(',', ':')
                    )

                    f_out.write(compact_json + '\n')
                except json.JSONDecodeError:
                    f_out.write(line + '\n')

        os.replace(temp_file, target_file)
        print(f"Файл {target_file} успешно сжат и обновлен.")

    except Exception as e:
        print(f"Ошибка: {e}")
        if os.path.exists(temp_file):
            os.remove(temp_file)

if __name__ == "__main__":
    main()