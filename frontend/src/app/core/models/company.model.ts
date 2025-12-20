export interface Company {
  id: number;
  companyKey: string;
  name: string;
  slug: string;
  address?: string;
  city?: string;
  country?: string;
  email?: string;
  phone?: string;
  description?: string;
  active: boolean;
  archived: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface CompanyRequest {
  name: string;
  slug?: string;
  address?: string;
  city?: string;
  country?: string;
  email?: string;
  phone?: string;
  description?: string;
  active?: boolean;
}

export interface CompanyResponse {
  id: number;
  companyKey: string;
  name: string;
  slug: string;
  address?: string;
  city?: string;
  country?: string;
  email?: string;
  phone?: string;
  description?: string;
  active: boolean;
  archived: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface AddUserToCompanyRequest {
  userId: number;
  roleId: number;
  moduleIds?: number[];
}

export interface AddModuleToCompanyRequest {
  moduleId: number;
}

