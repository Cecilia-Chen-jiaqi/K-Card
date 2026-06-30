async function main() {
  const u = 'paytest' + Date.now();
  await fetch('http://localhost:8080/api/auth/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      username: u,
      password: '123456',
      phone: '139' + String(Date.now()).slice(-8),
      campus: '测试',
      nickname: 't',
    }),
  });
  const login = await fetch('http://localhost:8080/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username: u, password: '123456' }),
  }).then((r) => r.json());
  const token = login.data.token;
  const goods = await fetch('http://localhost:8080/api/goods/list').then((r) => r.json());
  const goodsId = goods.data?.[0]?.id;
  if (!goodsId) {
    console.log('no goods');
    return;
  }
  const created = await fetch('http://localhost:8080/api/orders/create', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: 'Bearer ' + token,
    },
    body: JSON.stringify({ goodsId, quantity: 1, payType: 1 }),
  }).then((r) => r.json());
  const orderNo = created.data?.orderNo;
  console.log('orderNo', orderNo);
  if (!orderNo) return;
  const pay = await fetch('http://localhost:8080/api/pay/submit', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: 'Bearer ' + token,
    },
    body: JSON.stringify({ orderNo }),
  }).then((r) => r.json());
  console.log('pay code', pay.code);
  const body = pay.data || '';
  console.log('starts with', body.slice(0, 120));
  const methodMatch = body.match(/method="([^"]+)"/i);
  console.log('form method', methodMatch ? methodMatch[1] : 'none');
  console.log('has localhost notify', body.includes('localhost'));
  console.log('has httpbin notify', body.includes('httpbin.org'));
}

main().catch(console.error);
