const fs = require('fs');

async function main() {
  const html = fs.readFileSync('c:/Users/Public/CodeGeeXProjects/scripts/last-pay-form.html', 'utf8');
  const action = html.match(/action="([^"]+)"/)[1];
  const biz = html.match(/name="biz_content" value="([^"]+)"/)[1]
    .replace(/&quot;/g, '"')
    .replace(/&amp;/g, '&');

  const body = new URLSearchParams({ biz_content: biz });
  console.log('POST', action.slice(0, 100) + '...');
  const start = Date.now();
  try {
    const r = await fetch(action, {
      method: 'POST',
      body,
      redirect: 'manual',
      signal: AbortSignal.timeout(60000),
    });
    const ms = Date.now() - start;
    const text = await r.text();
    console.log('status', r.status, 'time_ms', ms);
    console.log('location', r.headers.get('location')?.slice(0, 120) || '(none)');
    console.log('body_snippet', text.replace(/\s+/g, ' ').slice(0, 300));
    fs.writeFileSync('c:/Users/Public/CodeGeeXProjects/scripts/gateway-response.html', text);
  } catch (e) {
    console.log('error after', Date.now() - start, 'ms:', e.message);
  }
}

main();
