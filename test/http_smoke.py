"""Integration checks against a running local server; Python 3 standard library only."""
import json
import sys
from urllib.error import HTTPError
from urllib.parse import urlencode
from urllib.request import Request, urlopen

BASE = sys.argv[1] if len(sys.argv) > 1 else 'http://127.0.0.1:8080'
passed = 0

def call(path, fields=None, headers=None):
    data = None if fields is None else urlencode(fields, doseq=True).encode()
    request = Request(BASE + path, data=data, headers=headers or {})
    try:
        response = urlopen(request, timeout=5)
    except HTTPError as error:
        response = error
    with response:
        raw = response.read().decode('utf-8')
        return response.status, json.loads(raw) if 'application/json' in response.headers.get('Content-Type', '') else raw

def check(name, condition):
    global passed
    assert condition, name
    passed += 1
    print('PASS:', name)

sample = dict(recipient='Ada', message='Enjoy "tea" & <books>!', discount='student', item=['book:1:1', 'candle:0:0'])
status, page = call('/')
check('frontend is served by Java', status == 200 and 'GiftBox Studio' in page)
status, quote = call('/api/quote', sample)
check('decorated subtotal and student total', status == 200 and quote['subtotal'] == 2600 and quote['total'] == 2340)
check('JSON safely preserves message text', quote['message'] == sample['message'])
status, quote = call('/api/quote', dict(sample, discount='threshold', item=sample['item'] + ['tea:0:0', 'tea:0:0']))
check('threshold discount at 5000', status == 200 and quote['total'] == 4500)
status, order = call('/api/orders', sample)
check('order creation with empty event history', status == 201 and order['status'] == 'CREATED' and order['customer'] == [])
status, ignored = call('/api/status', dict(id=order['id'], status='READY'))
check('cannot skip packing', status == 400)
status, order = call('/api/status', dict(id=order['id'], status='PACKING'))
check('both observers receive packing', status == 200 and len(order['customer']) == len(order['packing']) == 1)
status, order = call('/api/status', dict(id=order['id'], status='READY'))
check('both observers receive ready', status == 200 and len(order['customer']) == len(order['packing']) == 2)
status, ignored = call('/api/status', dict(id=order['id'], status='READY'))
check('duplicate status is rejected', status == 400)
for label, fields in [('empty box', dict(sample, item=[])), ('blank recipient', dict(sample, recipient=' ')),
                      ('unknown product', dict(sample, item=['missing:0:0'])), ('invalid flag', dict(sample, item=['book:yes:0'])),
                      ('unknown discount', dict(sample, discount='missing')), ('box limit', dict(sample, item=['book:0:0'] * 13))]:
    check(label, call('/api/quote', fields)[0] == 400)
check('missing order', call('/api/status', dict(id='missing', status='PACKING'))[0] == 404)
check('GET cannot create orders', call('/api/orders')[0] == 405)
check('cross-origin request is rejected', call('/api/orders', sample, {'Origin': 'https://example.com'})[0] == 403)
check('unknown file is not served', call('/README.md')[0] == 404)
check('oversized request is rejected', call('/api/quote', dict(sample, message='x' * 17000))[0] == 413)
print(f'HTTP tests: {passed} passed, 0 failed.')
