export interface StaffStatDTO {
  readonly staffId: number;
  readonly staffName: string;
  readonly contractCount: number;
  readonly invoiceCount: number;
  readonly contractPercentage: number;
  readonly invoicePercentage: number;
}

export interface MonthlyStatDTO {
  month: number;
  year: number;
  monthName: string;
  contractCount: number;
  invoiceCount: number;
  total: number;
}
