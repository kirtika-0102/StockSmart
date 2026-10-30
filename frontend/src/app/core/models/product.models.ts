export interface ProductRecord {
  id: number;
  name: string;
  sku: string;
  barcode: string | null;
  description: string | null;
  unitPrice: number | null;
  categoryId: number | null;
  categoryName: string | null;
}

export interface ProductRequest {
  name: string;
  sku: string;
  barcode: string | null;
  description: string | null;
  unitPrice: number | null;
  categoryId: number | null;
}
