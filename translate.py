import os

langs = {
    'values': {
        'settings_title': 'Settings',
        'settings_alarms_category': 'Alarms',
        'settings_global_vibrate_title': 'Global Vibration',
        'settings_global_vibrate_desc': 'Vibrate when an alarm rings',
        'settings_gradual_volume_title': 'Gradual Volume',
        'settings_gradual_volume_desc': 'Alarm volume will start low and gradually increase',
        'vibrate_label': 'Vibrate when ringing',
        'about': 'About'
    },
    'values-fr': {
        'settings_title': 'Paramètres',
        'settings_alarms_category': 'Alarmes',
        'settings_global_vibrate_title': 'Vibration Globale',
        'settings_global_vibrate_desc': 'Vibrer lorsqu\'une alarme sonne',
        'settings_gradual_volume_title': 'Volume Progressif',
        'settings_gradual_volume_desc': 'Le volume de l\'alarme augmentera progressivement',
        'vibrate_label': 'Vibrer lors de la sonnerie',
        'about': 'À propos'
    },
    'values-de': {
        'settings_title': 'Einstellungen',
        'settings_alarms_category': 'Wecker',
        'settings_global_vibrate_title': 'Globale Vibration',
        'settings_global_vibrate_desc': 'Vibrieren, wenn ein Wecker klingelt',
        'settings_gradual_volume_title': 'Ansteigende Lautstärke',
        'settings_gradual_volume_desc': 'Die Weckerlautstärke beginnt leise und steigt allmählich an',
        'vibrate_label': 'Beim Klingeln vibrieren',
        'about': 'Über'
    },
    'values-it': {
        'settings_title': 'Impostazioni',
        'settings_alarms_category': 'Sveglie',
        'settings_global_vibrate_title': 'Vibrazione Globale',
        'settings_global_vibrate_desc': 'Vibra quando suona una sveglia',
        'settings_gradual_volume_title': 'Volume Graduale',
        'settings_gradual_volume_desc': 'Il volume della sveglia aumenterà gradualmente',
        'vibrate_label': 'Vibra quando suona',
        'about': 'Informazioni'
    },
    'values-pt': {
        'settings_title': 'Configurações',
        'settings_alarms_category': 'Alarmes',
        'settings_global_vibrate_title': 'Vibração Global',
        'settings_global_vibrate_desc': 'Vibrar quando um alarme tocar',
        'settings_gradual_volume_title': 'Volume Gradual',
        'settings_gradual_volume_desc': 'O volume do alarme aumentará gradualmente',
        'vibrate_label': 'Vibrar ao tocar',
        'about': 'Sobre'
    }
}

for folder, strings in langs.items():
    filepath = f"app/src/main/res/{folder}/strings.xml"
    if not os.path.exists(filepath):
        print(f"Skipping {filepath}")
        continue
    
    with open(filepath, 'r') as f:
        content = f.read()
    
    if 'settings_title' in content:
        continue
        
    content = content.replace('</resources>', '')
    
    for k, v in strings.items():
        content += f'    <string name="{k}">{v}</string>\n'
    
    content += '</resources>\n'
    
    with open(filepath, 'w') as f:
        f.write(content)
    print(f"Updated {filepath}")
