export interface CategoryRecord {
  id: number;
  name: string;
  parentId: number | null;
  parentName: string | null;
}

export interface CategoryRequest {
  name: string;
  parentId: number | null;
}
