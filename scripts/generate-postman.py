#!/usr/bin/env python3
"""Génère la collection autonome. Le JWT expiré est signé avec la clé locale de démonstration."""
import base64, hashlib, hmac, json, os
from pathlib import Path
root=Path(__file__).resolve().parent.parent
items=[]
def request(name,method,path,status,body=None,token=None,extra=(),pre=()):
    headers=[]
    if token: headers.append({'key':'Authorization','value':'Bearer '+token})
    req={'method':method,'header':headers,'url':'{{baseUrl}}'+path}
    if body is not None:
        headers.append({'key':'Content-Type','value':'application/json'})
        req['body']={'mode':'raw','raw':json.dumps(body,ensure_ascii=False),'options':{'raw':{'language':'json'}}}
    test=[f'pm.test("HTTP {status}", function () {{ pm.response.to.have.status({status}); }});']+list(extra)
    events=[{'listen':'test','script':{'type':'text/javascript','exec':test}}]
    if pre: events.insert(0,{'listen':'prerequest','script':{'type':'text/javascript','exec':list(pre)}})
    items.append({'name':name,'event':events,'request':req})
def login(role,email):
    request('Connexion '+role,'POST','/auth/login',200,{'email':email,'password':'Demo2026!'},extra=[f'pm.collectionVariables.set("{role}Token", pm.response.json().token);',f'pm.collectionVariables.set("{role}Id", pm.response.json().user.id);'])
login('admin','admin@sentinel.ma'); login('tech','technicien@sentinel.ma')
for number in (1,2):
    request(f'Inscription employé {number} (rôle forcé EMPLOYEE)','POST','/auth/register',201,{'fullName':f'Employé test {number}','email':'{{email'+str(number)+'}}','password':'Password123!','role':'ADMIN'},extra=[f'pm.collectionVariables.set("emp{number}Token", pm.response.json().token);',f'pm.collectionVariables.set("emp{number}Id", pm.response.json().user.id);','pm.test("Inscription sans élévation de privilège", () => pm.expect(pm.response.json().role).to.eql("EMPLOYEE"));'],pre=[f'pm.collectionVariables.set("email{number}", "postman{number}." + Date.now() + "@test.ma");'])
for number,owner in [(1,1),(2,1),(3,2)]:
    request(f'Déclarer incident {number} (employé {owner})','POST','/api/incidents',201,{'title':f'Incident Postman {number}','description':'Incident créé dans le scénario de validation du devoir.','category':'PHISHING','priority':'HIGH'},'{{emp'+str(owner)+'Token}}',extra=[f'pm.collectionVariables.set("incident{number}", pm.response.json().id);'])
request('SEC 1 — API sans JWT','GET','/api/profile',401)
request('SEC 2 — JWT invalide','GET','/api/profile',401,token='invalid.jwt.token')
request('SEC 3 — JWT signé mais expiré','GET','/api/profile',401,token='{{expiredJwt}}')
request('SEC 4 — Employé sur route ADMIN','GET','/api/admin/users',403,token='{{emp1Token}}')
request('SEC 5 — Lecture incident autre propriétaire','GET','/api/incidents/{{incident3}}',403,token='{{emp1Token}}')
request('SEC 6 — Technicien sur incident non affecté','PUT','/api/incidents/{{incident3}}/status',403,{'status':'IN_PROGRESS'},'{{techToken}}')
request('SEC 8 — Email déjà existant','POST','/auth/register',409,{'fullName':'Doublon','email':'{{email1}}','password':'Password123!'})
request('SEC 9 — Incident invalide','POST','/api/incidents',400,{'title':'','description':'','priority':None},'{{emp1Token}}')
request('Admin — Tous les incidents','GET','/api/incidents',200,token='{{adminToken}}')
request('Admin — Tous les utilisateurs','GET','/api/admin/users',200,token='{{adminToken}}')
request('Admin — Statistiques','GET','/api/admin/statistics',200,token='{{adminToken}}')
request('Admin — Affectation','PUT','/api/incidents/{{incident1}}/assign/{{techId}}',200,token='{{adminToken}}',extra=['pm.test("Statut ASSIGNED", () => pm.expect(pm.response.json().status).to.eql("ASSIGNED"));'])
request('Technicien — Incidents affectés','GET','/api/incidents/assigned',200,token='{{techToken}}')
request('Technicien — Démarrage traitement','PUT','/api/incidents/{{incident1}}/status',200,{'status':'IN_PROGRESS'},'{{techToken}}')
request('Technicien — Note de traitement','POST','/api/incidents/{{incident1}}/comments',201,{'content':'Le poste a été isolé du réseau. Analyse antivirus effectuée.'},'{{techToken}}')
request('Technicien — Résolution','PUT','/api/incidents/{{incident1}}/status',200,{'status':'RESOLVED'},'{{techToken}}')
request('Employé — Vérification statut et historique','GET','/api/incidents/{{incident1}}',200,token='{{emp1Token}}',extra=['pm.test("Incident résolu avec historique et note", function () { const i=pm.response.json(); pm.expect(i.status).to.eql("RESOLVED"); pm.expect(i.history.length).to.eql(5); pm.expect(i.comments.length).to.eql(1); });'])
request('Admin — Clôture','PUT','/api/incidents/{{incident1}}/status',200,{'status':'CLOSED'},'{{adminToken}}')
request('Sécurité — Incident clôturé immuable','PUT','/api/incidents/{{incident1}}/status',409,{'status':'IN_PROGRESS'},'{{techToken}}')
request('Admin — Désactiver employé 2','PUT','/api/admin/users/{{emp2Id}}/disable',200,token='{{adminToken}}')
request('SEC 7 — Compte désactivé à la connexion','POST','/auth/login',401,{'email':'{{email2}}','password':'Password123!'})
request('Sécurité — JWT du compte désactivé','GET','/api/profile',401,token='{{emp2Token}}')
request('Admin — Réactiver employé 2','PUT','/api/admin/users/{{emp2Id}}/enable',200,token='{{adminToken}}')
request('Refresh — Connexion employé 1','POST','/auth/login',200,{'email':'{{email1}}','password':'Password123!'},extra=['pm.collectionVariables.set("refreshToken",pm.response.json().refreshToken);','pm.collectionVariables.set("emp1Token",pm.response.json().token);'])
request('Refresh — Rotation du jeton','POST','/auth/refresh',200,{'refreshToken':'{{refreshToken}}'},extra=['pm.collectionVariables.set("oldRefreshToken",pm.collectionVariables.get("refreshToken"));','pm.collectionVariables.set("refreshToken",pm.response.json().refreshToken);','pm.collectionVariables.set("emp1Token",pm.response.json().token);'])
request('Refresh — Ancien jeton refusé','POST','/auth/refresh',401,{'refreshToken':'{{oldRefreshToken}}'})
request('Déconnexion','POST','/api/logout',200,token='{{emp1Token}}')
request('Sécurité — JWT révoqué après déconnexion','GET','/api/profile',401,token='{{emp1Token}}')
request('Sécurité — Refresh révoqué après déconnexion','POST','/auth/refresh',401,{'refreshToken':'{{refreshToken}}'})
def enc(v):return base64.urlsafe_b64encode(json.dumps(v,separators=(',',':')).encode()).rstrip(b'=')
u=enc({'alg':'HS256'})+b'.'+enc({'sub':'admin@sentinel.ma','iat':1,'exp':2,'role':'ADMIN','uid':1,'version':0})
secret=os.getenv('JWT_SECRET','sentinel-local-development-secret-at-least-32-bytes-change-in-production')
expired=(u+b'.'+base64.urlsafe_b64encode(hmac.new(secret.encode(),u,hashlib.sha256).digest()).rstrip(b'=')).decode()
collection={'info':{'name':'Sentinel — Devoir JWT : scénario et sécurité','schema':'https://schema.getpostman.com/json/collection/v2.1.0/collection.json','description':'Exécuter dans l’ordre avec les comptes démo activés. Données de test créées avec des emails uniques. Inclut les 9 tests de sécurité obligatoires et le cycle de traitement.'},'variable':[{'key':'baseUrl','value':'http://localhost:8081'},{'key':'expiredJwt','value':expired}],'item':items}
(root/'docs/Sentinel.postman_collection.json').write_text(json.dumps(collection,indent=2,ensure_ascii=False))
print(f'Collection générée : {len(items)} requêtes')
