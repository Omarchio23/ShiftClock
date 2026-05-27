import os

langs = {
    'values': {'next_alarm_date': 'Next alarm: %1$s'},
    'values-fr': {'next_alarm_date': 'Prochaine alarme : %1$s'},
    'values-de': {'next_alarm_date': 'Nächster Wecker: %1$s'},
    'values-it': {'next_alarm_date': 'Prossima sveglia: %1$s'},
    'values-pt': {'next_alarm_date': 'Próximo alarme: %1$s'},
    'values-es': {'next_alarm_date': 'Próxima alarma: %1$s'}
}

for folder, strings in langs.items():
    filepath = f"app/src/main/res/{folder}/strings.xml"
    if not os.path.exists(filepath):
        continue
    
    with open(filepath, 'r') as f:
        content = f.read()
    
    if 'next_alarm_date' in content:
        continue
        
    content = content.replace('</resources>', '')
    for k, v in strings.items():
        content += f'    <string name="{k}">{v}</string>\n'
    content += '</resources>\n'
    
    with open(filepath, 'w') as f:
        f.write(content)
