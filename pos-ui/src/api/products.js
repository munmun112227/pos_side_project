/**
 * 商品查詢與條碼刷讀 API 服務 (等同 Java 的 Service Client)
 */

export async function searchProducts(query) {
  const res = await fetch(`/api/products/search?query=${encodeURIComponent(query)}`);
  const data = await res.json();
  if (!data.success) {
    throw new Error(data.message || '搜尋商品失敗');
  }
  return data.data;
}

export async function scanProduct(barcode) {
  const res = await fetch(`/api/products/scan?barcode=${encodeURIComponent(barcode)}`);
  const data = await res.json();
  if (!data.success) {
    throw new Error(data.message || '查無此商品條碼/編號');
  }
  return data.data;
}
