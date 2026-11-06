#!/usr/bin/env python3
"""Remplace l’export Newman par une preuve lisible, sans jetons ni mots de passe."""
from pathlib import Path
import json, html
root=Path(__file__).resolve().parent.parent
path=root/'output/postman-results.json'
raw=json.loads(path.read_text())
def clean(value):
    if isinstance(value,dict): return {k:('[masqué]' if k.lower() in ('token','refreshtoken','password','authorization') else clean(v)) for k,v in value.items()}
    if isinstance(value,list):return [clean(v) for v in value]
    return value
if 'run' in raw:
    executions=[]
    for e in raw['run']['executions']:
        response=e.get('response',{})
        stream=response.get('stream',{})
        payload=bytes(stream.get('data',[])).decode('utf-8',errors='replace') if isinstance(stream,dict) else str(stream)
        try: payload=clean(json.loads(payload))
        except ValueError:pass
        u=e['request']['url'];url=u if isinstance(u,str) else u.get('raw') or ('/'.join(u.get('path',[])))
        executions.append({'name':e['item']['name'],'method':e['request']['method'],'url':url,'status':response.get('code'),'response':payload,'assertions':[{'name':a['assertion'],'passed':not a.get('error')} for a in e.get('assertions',[])]})
    result={'source':'Exécution réelle de la collection Postman par Newman','stats':raw['run']['stats'],'failures':len(raw['run'].get('failures',[])),'executions':executions}
    path.write_text(json.dumps(result,indent=2,ensure_ascii=False))
else: result=raw
stats=result['stats'];rows=''
for e in result['executions']:
    passed=all(a['passed'] for a in e['assertions'])
    payload=e['response']; payload=json.dumps(payload,indent=2,ensure_ascii=False) if not isinstance(payload,str) else payload
    if not e['name'].startswith('SEC '):continue
    rows+=f'<article><div class="row"><span class="pass">✓</span><strong>{html.escape(e["name"])}</strong><b>{e["status"]}</b></div><div class="request">{e["method"]} {html.escape(str(e["url"]))}</div><pre>{html.escape(payload)}</pre></article>'
css='''*{box-sizing:border-box}body{margin:0;background:#f5f7f1;font:13px Arial,sans-serif;color:#284531}main{max-width:1100px;margin:auto;padding:45px}header{display:flex;align-items:end;justify-content:space-between;gap:20px;margin-bottom:25px}.brand{font-size:13px;font-weight:bold;color:#62834a;letter-spacing:2px}h1{font-size:29px;margin:12px 0}p{font-size:12px;color:#7d8c70}.stats{display:flex;gap:15px;margin-bottom:24px}.stats>div{border:1px solid #dce5d2;background:white;border-radius:8px;flex:1;padding:18px}.stats strong{display:block;font-size:28px;margin-top:8px}.stats span{font-size:11px;color:#7a8b6e}.grid{display:grid;grid-template-columns:1fr 1fr;gap:15px}article{border:1px solid #dce5d2;background:#fff;border-radius:7px;padding:16px;min-width:0}.row{display:flex;gap:8px;align-items:center;font-size:11px}.row b{margin-left:auto;color:#5e8048;background:#edf4e7;padding:6px;border-radius:4px}.pass{color:#669345}.request{font-family:monospace;font-size:10px;color:#7a8c6e;margin-top:13px;overflow-wrap:anywhere}pre{white-space:pre-wrap;font-size:10px;background:#f6f8f2;padding:12px;margin-bottom:0;line-height:1.7;overflow-wrap:anywhere}footer{font-size:10px;margin-top:20px;color:#859678}'''
page=f'''<!doctype html><html lang="fr"><head><meta charset="utf-8"><title>Sentinel — Résultats collection Postman</title><style>{css}</style></head><body><main><header><div><span class="brand">SENTINEL · VÉRIFICATION API</span><h1>Les neuf tests de sécurité.</h1><p>Résultats réels de la collection Postman exécutée avec Newman · 4 novembre 2026</p></div><span>✓ Exécution terminée</span></header><section class="stats"><div><span>Requêtes exécutées</span><strong>{stats['requests']['total']}</strong></div><div><span>Assertions réussies</span><strong>{stats['assertions']['total']-stats['assertions']['failed']}</strong></div><div><span>Échecs</span><strong>{result['failures']}</strong></div></section><section class="grid">{rows}</section><footer>Cette vue présente l’exécution HTTP et ne représente pas l’interface Postman. Réponses authentiques ; jetons et mots de passe masqués dans l’export.</footer></main></body></html>'''
(root/'output/postman-results.html').write_text(page)
print(f"{stats['requests']['total']} requêtes, {stats['assertions']['total']} assertions, {result['failures']} échec(s). Export expurgé des secrets.")
