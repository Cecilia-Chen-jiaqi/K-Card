#!/usr/bin/env python3
import json, time, uuid, urllib.request

BASE = "http://localhost:8080"
TS = str(int(time.time()))
PNG = bytes([137,80,78,71,13,10,26,10,0,0,0,13,73,72,68,82,0,0,0,1,0,0,0,1,8,2,0,0,0,144,119,83,222,0,0,0,12,73,68,65,84,8,215,99,248,207,192,0,0,3,1,1,0,24,221,141,180,0,0,0,0,73,69,78,68,174,66,96,130])

def req(method, path, body=None, token=None, multipart=None):
    headers={}; data=None
    if multipart:
        b=uuid.uuid4().hex; fn,fb,ct=multipart
        data=(f'--{b}\r\nContent-Disposition: form-data; name="file"; filename="{fn}"\r\nContent-Type: {ct}\r\n\r\n').encode()+fb+f'\r\n--{b}--\r\n'.encode()
        headers['Content-Type']=f'multipart/form-data; boundary={b}'
    elif body is not None:
        data=json.dumps(body).encode(); headers['Content-Type']='application/json'
    if token: headers['Authorization']=f'Bearer {token}'
    r=urllib.request.Request(BASE+path,data=data,headers=headers,method=method)
    with urllib.request.urlopen(r,timeout=60) as resp: return json.loads(resp.read())

seller=f's{TS}'; buyer=f'b{TS}'
req('POST','/api/auth/register',{'username':seller,'password':'123456','phone':f'138{TS[-8:]}','campus':'T'})
req('POST','/api/auth/register',{'username':buyer,'password':'123456','phone':f'139{TS[-8:]}','campus':'T'})
st=req('POST','/api/auth/login',{'username':seller,'password':'123456'})['data']['token']
bt=req('POST','/api/auth/login',{'username':buyer,'password':'123456'})['data']['token']
img=req('POST','/api/upload/image',multipart=('t.png',PNG,'image/png'),token=st)['data']
gid=req('POST','/api/goods/create',{'title':'Q','description':'x','price':0.01,'stock':1,'groupName':'SEVENTEEN','idolName':'M','quality':'无暇','tradeType':'仅出售','deliveryMode':1,'coverImage':img},token=st)['data']['id']
ono=req('POST','/api/orders/create',{'goodsId':gid,'quantity':1,'payType':1},token=bt)['data']['orderNo']
qr=req('GET',f'/api/pay/qrcode?orderNo={ono}',token=bt)
print(json.dumps(qr, ensure_ascii=False, indent=2))
print('PASS' if qr.get('code')==0 and qr.get('data',{}).get('qrCode','').startswith('https://') else 'FAIL')
