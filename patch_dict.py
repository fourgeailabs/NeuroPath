import re

file_path = "app/src/main/java/com/example/data/model/AppLanguage.kt"
with open(file_path, "r", encoding="utf-8") as f:
    content = f.read()

# We need to add the 4 translations to all languages. Let's do a simple mapping.
translations = {
    "en": {"welcome": "Welcome to NeuroPath", "select_language": "Please select your language", "language_label": "Language", "continue_btn": "Continue"},
    "es": {"welcome": "Bienvenido a NeuroPath", "select_language": "Por favor, seleccione su idioma", "language_label": "Idioma", "continue_btn": "Continuar"},
    "fr": {"welcome": "Bienvenue sur NeuroPath", "select_language": "Veuillez sélectionner votre langue", "language_label": "Langue", "continue_btn": "Continuer"},
    "de": {"welcome": "Willkommen bei NeuroPath", "select_language": "Bitte wählen Sie Ihre Sprache", "language_label": "Sprache", "continue_btn": "Weiter"},
    "zh": {"welcome": "欢迎来到 NeuroPath", "select_language": "请选择您的语言", "language_label": "语言", "continue_btn": "继续"},
    "ja": {"welcome": "NeuroPathへようこそ", "select_language": "言語を選択してください", "language_label": "言語", "continue_btn": "次へ"},
    "pt": {"welcome": "Bem-vindo ao NeuroPath", "select_language": "Selecione o seu idioma", "language_label": "Idioma", "continue_btn": "Continuar"},
    "hi": {"welcome": "NeuroPath में आपका स्वागत है", "select_language": "कृपया अपनी भाषा चुनें", "language_label": "भाषा", "continue_btn": "जारी रखें"},
    "ar": {"welcome": "مرحبًا بك في NeuroPath", "select_language": "يرجى تحديد لغتك", "language_label": "اللغة", "continue_btn": "متابعة"},
    "it": {"welcome": "Benvenuto in NeuroPath", "select_language": "Seleziona la tua lingua", "language_label": "Lingua", "continue_btn": "Continua"},
    "ru": {"welcome": "Добро пожаловать в NeuroPath", "select_language": "Пожалуйста, выберите ваш язык", "language_label": "Язык", "continue_btn": "Продолжить"},
    "ko": {"welcome": "NeuroPath에 오신 것을 환영합니다", "select_language": "언어를 선택해주세요", "language_label": "언어", "continue_btn": "계속"},
    "tr": {"welcome": "NeuroPath'e Hoşgeldiniz", "select_language": "Lütfen dilinizi seçin", "language_label": "Dil", "continue_btn": "Devam Et"},
    "vi": {"welcome": "Chào mừng đến với NeuroPath", "select_language": "Vui lòng chọn ngôn ngữ của bạn", "language_label": "Ngôn ngữ", "continue_btn": "Tiếp tục"},
    "pl": {"welcome": "Witamy w NeuroPath", "select_language": "Wybierz swój język", "language_label": "Język", "continue_btn": "Kontynuuj"},
    "nl": {"welcome": "Welkom bij NeuroPath", "select_language": "Selecteer alstublieft uw taal", "language_label": "Taal", "continue_btn": "Doorgaan"},
    "th": {"welcome": "ยินดีต้อนรับสู่ NeuroPath", "select_language": "โปรดเลือกภาษาของคุณ", "language_label": "ภาษา", "continue_btn": "ดำเนินการต่อ"},
    "id": {"welcome": "Selamat datang di NeuroPath", "select_language": "Silakan pilih bahasa Anda", "language_label": "Bahasa", "continue_btn": "Lanjutkan"},
    "sv": {"welcome": "Välkommen till NeuroPath", "select_language": "Vänligen välj ditt språk", "language_label": "Språk", "continue_btn": "Fortsätt"},
    "el": {"welcome": "Καλώς ήρθατε στο NeuroPath", "select_language": "Παρακαλώ επιλέξτε τη γλώσσα σας", "language_label": "Γλώσσα", "continue_btn": "Συνέχεια"}
}

def replacer(match):
    lang = match.group(1)
    map_body = match.group(2)
    
    # Check if lang is in translations
    if lang in translations:
        # Append new items
        new_items = []
        for k, v in translations[lang].items():
            if f'"{k}"' not in map_body:
                new_items.append(f'\n            "{k}" to "{v}",')
        
        # Insert them before the first existing key to avoid trailing comma issues, or after.
        # Actually it's easier to insert at the beginning of the map
        # map_body format is: \n            "app_title" to ...
        # Let's just put it at the very top of the map
        insertion = "".join(new_items)
        return f'"{lang}" to mapOf({insertion}{map_body}'
    return match.group(0)

# Pattern looks for "lang_code" to mapOf( \n ... )
pattern = re.compile(r'"([a-z]{2})" to mapOf\((.*?)(?=\n        \),|\n        \)$|\n    \))', re.DOTALL)
new_content = pattern.sub(replacer, content)

with open(file_path, "w", encoding="utf-8") as f:
    f.write(new_content)

