export interface Invoice {
  // Read-only properties (set by backend)
  invoiceId?: string;
  convertedAmountPreVat?: number;
  totalAmountWithVat?: number;
  createdDate?: Date;
  lastUpdateDate?: Date;
  createdByUsername?: string;
  lastUpdatedByUsername?: string;

  // Required properties
  invoiceSymbol: string;
  invoiceNumber: string;
  originalAmount: number;
  currencyType: string;
  exchangeRate: number;
  vat: number;
  invoiceDate: Date;
  dueDate: Date;
  paymentMethod: string;
  contractId: string;
  customerId: string;
  projectId: string;
  staffUsername: string;
  department: string;

  // Optional property
  notes?: string;
}

export interface CreateInvoiceRequest {
  invoiceSymbol: string;
  invoiceNumber: string;
  originalAmount: number;
  currencyType: string;
  exchangeRate: number;
  vat: number;
  invoiceDate: Date;
  dueDate: Date;
  paymentMethod: string;
  contractId: string;
  customerId: string;
  projectId: string;
  staffUsername: string;
  department: string;
  notes?: string;
}

export interface UpdateInvoiceRequest {
  invoiceId?: string;
  invoiceSymbol?: string;
  invoiceNumber?: string;
  originalAmount?: number;
  currencyType?: string;
  exchangeRate?: number;
  vat?: number;
  invoiceDate?: Date;
  dueDate?: Date;
  paymentMethod?: string;
  contractId?: string;
  customerId?: string;
  projectId?: string;
  staffUsername?: string;
  department?: string;
  notes?: string;
}

export interface InvoiceSearchParams {
  page?: number;
  size?: number;
  sortBy?: "invoiceSymbol" | "invoiceNumber" | "originalAmount" | "totalAmountWithVat" | "currencyType" | "invoiceDate" | "dueDate" | "paymentMethod" | "department" | "staffUsername" | "createdDate" | "lastUpdateDate";
  sortAsc?: boolean;
  query?: string;
  staffUsername?: string;
  contractId?: string;
  customerId?: string;
  createdByUsername?: string;
  invoiceDateStart?: string;
  invoiceDateEnd?: string;
  dueDateStart?: string;
  dueDateEnd?: string;
  minAmount?: string;
  maxAmount?: string;
  currencyType?: string;
  department?: string;
}
