export interface Role {
  id: number;
  roleKey?: string;
  name: string;
  description?: string;
  role?: string;
  companyRole?: string;
}

export interface CompanyRoleMapper {
  id: number;
  companyRoleKey?: string;
  company: { id: number; name: string };
  role: Role;
  active: boolean;
  archived: boolean;
}

