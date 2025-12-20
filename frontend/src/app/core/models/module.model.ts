export interface Module {
  id: number;
  moduleKey?: string;
  name: string;
  description?: string;
  type?: string;
  archived: boolean;
}

export interface CompanyModuleMapper {
  id: number;
  moduleMapperKey: string;
  company: { id: number; name: string };
  module: Module;
  active: boolean;
  archived: boolean;
  createdAt?: string;
}

