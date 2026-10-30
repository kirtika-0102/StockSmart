export interface SupplierRecord {
  id: number;
  name: string;
  contactName: string | null;
  email: string | null;
  phone: string | null;
  address: string | null;
}

export interface SupplierRequest {
  name: string;
  contactName: string | null;
  email: string | null;
  phone: string | null;
  address: string | null;
}

export interface SupplierProductRecord {
  id: number;
  supplierId: number;
  supplierName: string;
  productId: number;
  productName: string;
  productSku: string;
  unitCost: number | null;
}

export interface AddSupplierProductRequest {
  productId: number;
  unitCost: number | null;
}
