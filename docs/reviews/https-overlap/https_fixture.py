"""Loopback-only real TLS fixture. No production credentials or remote services."""
import json, ssl, threading, time
from http.server import ThreadingHTTPServer, BaseHTTPRequestHandler
from pathlib import Path
root=Path('/private/tmp/wobbrik-https')
lock=threading.Lock();counts={};active={}
def log(**event):
    with lock:
        with (root/'server.jsonl').open('a') as out: out.write(json.dumps(dict(t_ns=time.monotonic_ns(),**event))+'\n')
snapshot=dict(rules_version='wobbrik-v1',revision=1,coins=1250,xp=0,attempt=None,server_time='2026-11-05T00:00:00Z',first_online_at='2026-11-05T00:00:00Z',grace_until='2026-11-05T20:00:00Z',level=1,rescues=0,achievements=[],unlocks=[],land=[],part_progress=[])
def listing(i): return dict(id=f'l-{i}',building_id=f'b-{i}',revision=1,building_revision=1,price=350,fee=18,status='active')
def building(i): return dict(id=f'b-{i}',revision=1,floors=12,residents=48,base_value=500,max_price=600,parts={c:f'base_1_{c}' for c in ['foundation','body','roof','windows','door']},traded=False,accepted_geometry=dict(geometry=dict(version='city-geometry-v1',centers_x_ticks=[0]*12),attempt_id='attempt',sequence=1,operation_id='op',rules_version='wobbrik-v1',engine_version='wobbrik-structural-v1'))
class Handler(BaseHTTPRequestHandler):
    protocol_version='HTTP/1.1'
    def log_message(self,*args): pass
    def do_GET(self):
        mode=(root/'mode').read_text().strip();generation=(root/'mode').stat().st_mtime_ns;path=self.path.split('/game/',1)[-1]
        with lock:
            counts[(mode,generation)]=counts.get((mode,generation),0)+1;n=counts[(mode,generation)]
            key=(mode,generation,path);active[key]=active.get(key,0)+1;overlap=active[key]>1
        log(mode=mode,n=n,path=path,event='received',overlap=overlap,tls=self.connection.version())
        hold=n <= (2 if mode=='market' else 1)
        data=snapshot if path=='sync' else (dict(listing=listing(int(path.split('l-')[-1])),building=building(int(path.split('l-')[-1]))) if path.startswith('market/l-') else dict(listings=[listing(i) for i in range(1,13)]))
        # Exact category fixture comes from Kotlin's current production catalog.
        if 'building' in data:
            categories=json.loads((root/'categories.json').read_text());data['building']['parts']={c:f'base_1_{c}' for c in categories}
        body=json.dumps(data,separators=(',',':')).encode()
        try:
            if hold and mode=='body':
                self.send_response(200);self.send_header('Content-Length',str(len(body)));self.end_headers()
                self.wfile.write(body[:1]);self.wfile.flush();log(mode=mode,n=n,path=path,event='body_prefix')
            (root/f'{mode}-received-{n}').write_text('ready')
            if hold:
                until=time.monotonic()+12
                while not (root/f'{mode}-release').exists() and time.monotonic()<until: time.sleep(.005)
            if not (hold and mode=='body'):
                self.send_response(200);self.send_header('Content-Length',str(len(body)));self.end_headers()
            self.wfile.write(body[1:] if hold and mode=='body' else body);self.wfile.flush()
            log(mode=mode,n=n,path=path,event='response_written',bytes=len(body))
        except (BrokenPipeError,ConnectionResetError,ssl.SSLError) as e:
            log(mode=mode,n=n,path=path,event='write_failed',error=type(e).__name__)
        finally:
            with lock: active[key]-=1
server=ThreadingHTTPServer(('127.0.0.1',0),Handler)
context=ssl.SSLContext(ssl.PROTOCOL_TLS_SERVER);context.load_cert_chain(root/'cert.pem',root/'key.pem')
server.socket=context.wrap_socket(server.socket,server_side=True)
(root/'origin').write_text(f'https://localhost:{server.server_port}')
server.serve_forever()
