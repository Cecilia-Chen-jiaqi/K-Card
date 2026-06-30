/**
 * 支付链路自动化诊断脚本
 */
async function main() {
  const report = [];
  const ok = (msg) => report.push(`[OK] ${msg}`);
  const fail = (msg) => report.push(`[FAIL] ${msg}`);
  const warn = (msg) => report.push(`[WARN] ${msg}`);

  // 1. 服务健康
  for (const [name, url] of [
    ['后端', 'http://localhost:8080/api/goods/list'],
    ['前端', 'http://localhost:3000'],
    ['ngrok面板', 'http://127.0.0.1:4040/api/tunnels'],
  ]) {
    try {
      const r = await fetch(url, { signal: AbortSignal.timeout(8000) });
      ok(`${name} 可访问 (${r.status})`);
    } catch (e) {
      fail(`${name} 不可访问: ${e.message}`);
    }
  }

  // 2. ngrok 隧道
  let ngrokUrl = '';
  try {
    const tunnels = await fetch('http://127.0.0.1:4040/api/tunnels').then((r) => r.json());
    const t = tunnels.tunnels?.find((x) => x.proto === 'https');
    ngrokUrl = t?.public_url || '';
    if (ngrokUrl) ok(`ngrok HTTPS: ${ngrokUrl}`);
    else fail('ngrok 未找到 HTTPS 隧道');
  } catch (e) {
    fail(`ngrok API 读取失败: ${e.message}`);
  }

  // 3. ngrok -> 后端 notify
  if (ngrokUrl) {
    try {
      const r = await fetch(`${ngrokUrl}/api/pay/notify`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'test=1',
        signal: AbortSignal.timeout(15000),
      });
      const text = await r.text();
      if (r.status === 200 || r.status === 401) ok(`ngrok 转发 notify 可达 (${r.status}) 响应: ${text.slice(0, 30)}`);
      else warn(`ngrok notify 响应异常: ${r.status} ${text.slice(0, 80)}`);
    } catch (e) {
      fail(`ngrok 无法转发到后端: ${e.message}`);
    }
  }

  // 4. 注册登录
  const u = 'paydiag' + Date.now();
  const reg = await fetch('http://localhost:8080/api/auth/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      username: u,
      password: '123456',
      phone: '138' + String(Date.now()).slice(-8),
      campus: '测试校区',
      nickname: 'diag',
    }),
  }).then((r) => r.json());
  if (reg.code !== 0) fail(`注册失败: ${reg.message}`);
  else ok(`测试用户注册: ${u}`);

  const login = await fetch('http://localhost:8080/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username: u, password: '123456' }),
  }).then((r) => r.json());
  const token = login.data?.token;
  if (!token) {
    fail('登录失败');
    console.log(report.join('\n'));
    return;
  }
  ok('登录成功');

  // 5. 创建订单
  const goods = await fetch('http://localhost:8080/api/goods/list').then((r) => r.json());
  const g = goods.data?.find((x) => (x.stock || 0) > 0 && x.status === 1) || goods.data?.[0];
  if (!g) {
    fail('无可用商品');
    console.log(report.join('\n'));
    return;
  }
  ok(`选用商品 id=${g.id} stock=${g.stock} price=${g.price}`);

  const created = await fetch('http://localhost:8080/api/orders/create', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + token },
    body: JSON.stringify({ goodsId: g.id, quantity: 1, payType: 1 }),
  }).then((r) => r.json());

  if (created.code !== 0) {
    fail(`创建订单失败: ${created.message}`);
    console.log(report.join('\n'));
    return;
  }
  const orderNo = created.data.orderNo;
  ok(`订单创建成功: ${orderNo}`);

  // 6. 生成支付表单
  const pay = await fetch('http://localhost:8080/api/pay/submit', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + token },
    body: JSON.stringify({ orderNo }),
  }).then((r) => r.json());

  if (pay.code !== 0) {
    fail(`支付表单生成失败: ${pay.message}`);
    console.log(report.join('\n'));
    return;
  }
  ok('支付表单生成成功');

  const html = pay.data || '';
  const method = html.match(/method="([^"]+)"/i)?.[1] || '?';
  const action = html.match(/action="([^"]+)"/i)?.[1]?.slice(0, 80) || '?';
  ok(`表单 method=${method} action=${action}...`);

  const decode = (s) => {
    try {
      return decodeURIComponent(s.replace(/\+/g, ' '));
    } catch {
      return s;
    }
  };
  const notifyMatch = html.match(/name="notify_url" value="([^"]+)"/i);
  const returnMatch = html.match(/name="return_url" value="([^"]+)"/i);
  const bizMatch = html.match(/name="biz_content" value="([^"]+)"/i);

  if (notifyMatch) {
    const notify = decode(notifyMatch[1]);
    if (notify.includes('localhost')) fail(`notify_url 仍为 localhost: ${notify}`);
    else if (notify.includes('ngrok')) ok(`notify_url 使用 ngrok: ${notify}`);
    else ok(`notify_url: ${notify}`);
  } else warn('表单中未找到 notify_url 字段');

  if (returnMatch) {
    ok(`return_url: ${decode(returnMatch[1])}`);
  }

  if (bizMatch) {
    ok(`biz_content: ${decode(bizMatch[1])}`);
  }

  // 7. 支付宝订单查询（验证 appId/密钥/网关）
  try {
    const sync = await fetch(`http://localhost:8080/api/pay/sync-status?orderNo=${orderNo}`, {
      headers: { Authorization: 'Bearer ' + token },
    }).then((r) => r.json());
    if (sync.code === 0) {
      ok(`支付宝查询接口正常 code=${sync.data?.alipayCode || 'n/a'} tradeStatus=${sync.data?.tradeStatus || 'WAIT_BUYER_PAY'}`);
    } else {
      fail(`支付宝查询失败: ${sync.message}`);
    }
  } catch (e) {
    fail(`sync-status 异常: ${e.message}`);
  }

  // 8. 尝试 POST 到沙箱网关（只看是否 504/502）
  if (html.includes('<form')) {
    try {
      const doc = new DOMParser().parseFromString(html, 'text/html');
      const form = doc.querySelector('form');
      const fd = new FormData();
      form.querySelectorAll('input').forEach((inp) => fd.append(inp.name, inp.value));
      const gw = await fetch(form.action, {
        method: 'POST',
        body: fd,
        redirect: 'manual',
        signal: AbortSignal.timeout(20000),
      });
      const loc = gw.headers.get('location') || '';
      const snippet = (await gw.text()).slice(0, 200);
      if (gw.status === 504 || gw.status === 502) {
        fail(`沙箱网关返回 ${gw.status} - 支付宝侧超时`);
      } else if (gw.status === 302 || gw.status === 301) {
        ok(`沙箱网关响应 ${gw.status} 重定向到收银台 (跳转正常)`);
        if (loc) ok(`Location: ${loc.slice(0, 100)}...`);
      } else if (gw.status === 200 && (snippet.includes('login') || snippet.includes('支付宝'))) {
        ok(`沙箱网关返回 200 收银台 HTML (支付页可打开)`);
      } else {
        warn(`沙箱网关响应 ${gw.status}: ${snippet.replace(/\s+/g, ' ').slice(0, 120)}`);
      }
    } catch (e) {
      fail(`请求沙箱网关失败: ${e.message}`);
    }
  }

  console.log('\n========== 支付诊断报告 ==========\n');
  console.log(report.join('\n'));
  console.log('\n========== 结论 ==========');
  const hasFail = report.some((l) => l.startsWith('[FAIL]'));
  if (!hasFail) {
    console.log('项目侧支付链路正常。若仍无法付款，问题在支付宝沙箱登录页（验证码/账号），非本项目代码。');
    console.log(`测试订单号: ${orderNo} — 可在浏览器打开结算页手动再试一次。`);
  } else {
    console.log('发现项目侧问题，见上方 [FAIL] 项。');
  }
}

// Node 18+ 无 DOMParser，用 cheerio 替代或简化第8步
if (typeof DOMParser === 'undefined') {
  global.DOMParser = class {
    parseFromString(html) {
      const inputs = [...html.matchAll(/<input[^>]*name="([^"]+)"[^>]*value="([^"]*)"/gi)];
      return {
        querySelector(sel) {
          if (sel !== 'form') return null;
          return {
            action: html.match(/action="([^"]+)"/)?.[1],
            querySelectorAll() {
              return inputs.map((m) => ({ name: m[1], value: m[2] }));
            },
          };
        },
      };
    }
  };
}

main().catch((e) => console.error(e));
