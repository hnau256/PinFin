import json
import os
import re

# Регулярное выражение для UUID
UUID_PATTERN = re.compile(r'^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$', re.IGNORECASE)

def format_amount(value):
    """Делит на 100 и возвращает строку в максимально коротком виде (x.x или x)"""
    try:
        num = float(value) / 100
        # Формат :g убирает лишние нули и точку, если число целое
        # Например: 10.0 -> "10", 10.50 -> "10.5", -50.96 -> "-50.96"
        return f"{num:g}"
    except (ValueError, TypeError):
        return value

def process_node(data):
    """Рекурсивно обходит объект и меняет все ключи 'amount'"""
    if isinstance(data, dict):
        for k, v in data.items():
            if k == "amount":
                data[k] = format_amount(v)
            else:
                process_node(v)
    elif isinstance(data, list):
        for item in data:
            process_node(item)

def main():
    # 1. Поиск файла-UUID
    target_file = next((f for f in os.listdir('.') if UUID_PATTERN.match(f) and os.path.isfile(f)), None)

    if not target_file:
        print("Файл с именем UUID не найден в текущей папке.")
        return

    print(f"Обработка файла: {target_file}")
    temp_file = target_file + ".tmp"

    try:
        with open(target_file, 'r', encoding='utf-8') as f_in, \
             open(temp_file, 'w', encoding='utf-8') as f_out:

            for line in f_in:
                line = line.strip()
                if not line:
                    continue

                try:
                    # Десериализация строки (одна строка = один JSON объект)
                    obj = json.loads(line)
                    process_node(obj)
                    # Сериализация обратно в одну строку без лишних пробелов
                    f_out.write(json.dumps(obj, ensure_ascii=False) + '\n')
                except json.JSONDecodeError:
                    # Если строка вдруг не JSON, сохраняем её как есть
                    f_out.write(line + '\n')

        # Замена оригинала результатом
        os.replace(temp_file, target_file)
        print("Успешно завершено.")

    except Exception as e:
        print(f"Ошибка: {e}")
        if os.path.exists(temp_file):
            os.remove(temp_file)

if __name__ == "__main__":
    main()