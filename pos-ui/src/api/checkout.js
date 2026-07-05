/**
 * 結帳與收銀流程 API 服務 (等同 Java 的 Service Client)
 */

export async function startNewTransaction(orderType = 'RETAIL') {
  const res = await fetch(`/api/checkout/new-transaction?orderType=${orderType}`, {
    method: 'POST'
  });
  const data = await res.json();
  if (!data.success) {
    throw new Error(data.message || '建立新交易失敗');
  }
  return data;
}

export async function calculateCart(payload) {
  const res = await fetch('/api/cart/calculate', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  });
  const data = await res.json();
  if (!data.success) {
    throw new Error(data.message || '計算購物車失敗');
  }
  return data.data;
}

export async function prepareCheckout(orderId, payload) {
  const res = await fetch(`/api/checkout/${orderId}/prepare`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  });
  const data = await res.json();
  if (!data.success) {
    throw new Error(data.message || '進入結帳失敗');
  }
  return data;
}

export async function submitPaymentStep(orderId, payload) {
  const res = await fetch(`/api/checkout/${orderId}/payment`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  });
  const data = await res.json();
  if (!data.success) {
    throw new Error(data.message || '付款失敗');
  }
  return data.data;
}

export async function voidPaymentStep(orderId, paymentId) {
  const res = await fetch(`/api/checkout/${orderId}/payment/${paymentId}`, {
    method: 'DELETE'
  });
  const data = await res.json();
  if (!data.success) {
    throw new Error(data.message || '作廢付款失敗');
  }
  return data.data;
}

export async function cancelTransactionStep(orderId) {
  const res = await fetch(`/api/checkout/${orderId}/cancel`, {
    method: 'POST'
  });
  const data = await res.json();
  if (!data.success) {
    throw new Error(data.message || '取消交易失敗');
  }
  return data;
}

export async function abandonCartStep(orderId, payload) {
  const res = await fetch(`/api/cart/abandon?orderId=${orderId}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  });
  const data = await res.json();
  if (!data.success) {
    throw new Error(data.message || '放棄交易失敗');
  }
  return data;
}
