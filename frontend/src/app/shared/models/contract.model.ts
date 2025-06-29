export interface Contract {
  contractId?: string;
  contractName: string;
  createdDate?: Date;
  lastUpdatedDate?: Date;
  createdByUsername?: string;
  lastUpdatedByUsername?: string;
  assignedStaffUsername: string;
}

export class ContractModel implements Contract {
  contractId?: string;
  contractName = '';
  createdDate?: Date;
  lastUpdatedDate?: Date;
  createdByUsername?: string;
  lastUpdatedByUsername?: string;
  assignedStaffUsername = '';

  constructor(data?: Partial<Contract>) {
    if (data) {
      Object.assign(this, data);
      
      // Convert string dates to Date objects if they exist
      if (data.createdDate && typeof data.createdDate === 'string') {
        this.createdDate = new Date(data.createdDate);
      }
      
      if (data.lastUpdatedDate && typeof data.lastUpdatedDate === 'string') {
        this.lastUpdatedDate = new Date(data.lastUpdatedDate);
      }
    }
  }
}

export interface ContractCreateRequest {
  contractName: string;
  assignedStaffUsername: string;
}

export interface ContractUpdateRequest {
  contractName?: string;
  assignedStaffUsername?: string;
}

export interface ContractSearchParams {
  page?: number;
  size?: number;
  sortBy?: "contractName" | "assignedStaffUsername" | "createdDate" | "lastUpdatedDate";
  sortAsc?: boolean;
  query?: string;
  contractName?: string;
  assignedStaffUsername?: string;
  createdDateFrom?: string;
  createdDateTo?: string;
  updatedDateFrom?: string;
  updatedDateTo?: string;
}
