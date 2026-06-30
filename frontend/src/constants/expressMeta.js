export const EXPRESS_COMPANIES = [
  '顺丰速运', '圆通速递', '中通快递', '韵达快递', '申通快递',
  '京东物流', '邮政EMS', '极兔速递', '德邦快递', '校园面交', '其他',
];

export const loadExpressCompanies = async (axios) => {
  try {
    const res = await axios.get('/api/meta/express');
    if (res.data.code === 0 && Array.isArray(res.data.data) && res.data.data.length) {
      return res.data.data;
    }
  } catch (_) { /* fallback */ }
  return EXPRESS_COMPANIES;
};

export const formatLogistics = (order) => {
  if (!order) return '';
  const parts = [];
  if (order.expressCompany) parts.push(order.expressCompany);
  if (order.trackingNo) parts.push(`单号 ${order.trackingNo}`);
  if (order.logisticsNote) parts.push(order.logisticsNote);
  return parts.join(' · ') || '暂无物流信息';
};

export const formatShipTime = (shippedAt) => {
  if (!shippedAt) return '';
  const d = new Date(shippedAt);
  if (Number.isNaN(d.getTime())) return shippedAt;
  return d.toLocaleString('zh-CN', { hour12: false });
};
