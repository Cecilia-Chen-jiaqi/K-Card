const fs = require('fs');

async function main() {
  const u = 't' + Date.now();
  await fetch('http://localhost:8080/api/auth/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      username: u,
      password: '123456',
      phone: '138' + String(Date.now()).slice(-8),
      campus: 'x',
      nickname: 'x',
    }),
  });
  const login = await fetch('http://localhost:8080/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username: u, password: '123456' }),
  }).then((r) => r.json());
  const token = login.data.token;
  const goods = await fetch('http://localhost:8080/api/goods/list').then((r) => r.json());
  const g = goods.data.find((x) => x.stock > 0) || goods.data[0];
  const order = await fetch('http://localhost:8080/api/orders/create', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + token },
    body: JSON.stringify({ goodsId: g.id, quantity: 1, payType: 1 }),
  }).then((r) => r.json());
  const pay = await fetch('http://localhost:8080/api/pay/submit', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + token },
    body: JSON.stringify({ orderNo: order.data.orderNo }),
  }).then((r) => r.json());
  const html = pay.data;
  const out = 'c:/Users/Public/CodeGeeXProjects/scripts/last-pay-form.html';
  fs.writeFileSync(out, html);
  console.log('saved', out);
  console.log('notify_url field', html.includes('notify_url'));
  console.log('ngrok', html.includes('ngrok'));
  console.log('httpbin', html.includes('httpbin'));
  const names = [];
  const re = /name="([^"]+)"/g;
  let m;
  while ((m = re.exec(html))) names.push(m[1]);
  console.log('fields', names.join(', '));
}

main();
