export interface Customer {
  // Read-only properties (set by backend)
  customerId?: string;
  createdDate?: Date;
  lastUpdateDate?: Date;
  createdByUsername?: string;
  lastUpdatedByUsername?: string;
  usedToBeHandledByStaffUsernames?: string[];

  // Required properties
  customerName: string;
  taxCode: string;

  // Optional properties
  abbreviationName?: string;
  assignedStaffUsername?: string;
}

export class CustomerModel implements Customer {
  customerId?: string;
  customerName = '';
  taxCode = '';
  abbreviationName?: string;
  assignedStaffUsername?: string;
  createdDate?: Date;
  lastUpdateDate?: Date;
  createdByUsername?: string;
  lastUpdatedByUsername?: string;
  usedToBeHandledByStaffUsernames?: string[] = [];

  constructor(data?: Partial<Customer>) {
    if (data) {
      Object.assign(this, data);
      
      // Convert string dates to Date objects if they exist
      if (data.createdDate && typeof data.createdDate === 'string') {
        this.createdDate = new Date(data.createdDate);
      }
      
      if (data.lastUpdateDate && typeof data.lastUpdateDate === 'string') {
        this.lastUpdateDate = new Date(data.lastUpdateDate);
      }

      // Ensure usedToBeHandledByStaffUsernames is always an array
      if (!this.usedToBeHandledByStaffUsernames) {
        this.usedToBeHandledByStaffUsernames = [];
      }
    }
  }
}

export interface CustomerCreateRequest {
  customerName: string;
  taxCode: string;
  abbreviationName?: string;
  assignedStaffUsername?: string;
}

export interface CustomerUpdateRequest {
  customerName?: string;
  taxCode?: string;
  abbreviationName?: string;
  assignedStaffUsername?: string;
}

export interface CustomerSearchParams {
  page?: number;
  size?: number;
  sortBy?: "customerName" | "taxCode" | "abbreviationName" | "createdDate" | "assignedStaffUsername" | "lastUpdateDate";
  sortAsc?: boolean;
  query?: string;
  customerName?: string;
  taxCode?: string;
  assignedStaffUsername?: string;
  createdDateFrom?: string;
  createdDateTo?: string;
  updatedDateFrom?: string;
  updatedDateTo?: string;
}
