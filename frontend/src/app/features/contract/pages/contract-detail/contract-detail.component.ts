import { Component, OnInit, inject } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { ContractService } from "../../services/contract.service";
import { InvoiceService } from "../../../invoice/services/invoice.service";
import { Contract, ContractUpdateRequest } from "../../../../shared/models/contract.model";
import { Invoice, UpdateInvoiceRequest } from "../../../../shared/models/invoice.model";
import { ToastService } from "../../../../core/services/toast.service";
import { StaffDropdownComponent } from "../../../../shared/components/staff-dropdown/staff-dropdown.component";

@Component({
  selector: "app-contract-detail",
  standalone: true,
  imports: [CommonModule, FormsModule, StaffDropdownComponent],
  templateUrl: "./contract-detail.component.html",
})
export class ContractDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private contractService = inject(ContractService);
  private invoiceService = inject(InvoiceService);
  private toastService = inject(ToastService);

  contract: Contract | null = null;
  invoices: Invoice[] = [];
  loading = false;
  contractId: string = "";

  // Contract editing
  isEditing = false;
  contractEditForm: any = {};

  // Invoice pagination
  invoiceCurrentPage = 1;
  invoicePageSize = 10;
  invoiceTotalItems = 0;
  invoiceTotalPages = 0;

  // Invoice editing
  showInvoiceEditModal = false;
  selectedInvoice: Invoice | null = null;
  invoiceForm: any = {};

  // Add Math property for template usage
  Math = Math;

  ngOnInit(): void {
    this.contractId = this.route.snapshot.paramMap.get("id") || "";
    if (this.contractId) {
      this.loadContractDetails();
      this.loadInvoices();
    }
  }

  loadContractDetails(): void {
    this.loading = true;
    this.contractService.getById(this.contractId).subscribe({
      next: (response) => {
        if (response.status === 200 && response.data) {
          this.contract = response.data;
          this.initializeContractEditForm();
        } else {
          this.toastService.error(
            response.message || "Failed to load contract details"
          );
          this.router.navigate(["/contracts"]);
        }
        this.loading = false;
      },
      error: (error) => {
        this.toastService.error("Failed to load contract details");
        this.loading = false;
        console.error("Error loading contract:", error);
        this.router.navigate(["/contracts"]);
      },
    });
  }

  loadInvoices(): void {
    this.loading = true;
    
    const params = {
      page: this.invoiceCurrentPage - 1,
      size: this.invoicePageSize,
      contractId: this.contractId
    };

    this.invoiceService.search(params).subscribe({
      next: (response) => {
        this.loading = false;
        
        if (response.status === 200 && response.data) {
          this.invoices = response.data.content || [];
          this.invoiceTotalItems = response.data.totalElements;
          this.invoiceTotalPages = response.data.totalPages;
        } else {
          this.invoices = [];
          this.invoiceTotalItems = 0;
          this.invoiceTotalPages = 0;
        }
      },
      error: (error) => {
        this.loading = false;
        console.error('Error loading invoices:', error);
        this.invoices = [];
        this.invoiceTotalItems = 0;
        this.invoiceTotalPages = 0;
      }
    });
  }

  initializeContractEditForm(): void {
    if (this.contract) {
      this.contractEditForm = {
        contractName: this.contract.contractName,
        assignedStaffUsername: this.contract.assignedStaffUsername
      };
    }
  }

  toggleContractEdit(): void {
    if (this.isEditing) {
      this.isEditing = false;
      this.initializeContractEditForm();
    } else {
      this.isEditing = true;
    }
  }

  saveContractChanges(): void {
    if (!this.contractId || !this.contractEditForm.contractName) {
      this.toastService.error('Please fill in all required fields');
      return;
    }

    this.loading = true;
    
    const updateData: ContractUpdateRequest = {
      contractName: this.contractEditForm.contractName,
      assignedStaffUsername: this.contractEditForm.assignedStaffUsername
    };

    this.contractService.update(this.contractId, updateData).subscribe({
      next: (response) => {
        this.loading = false;
        
        if (response.status === 200 && response.data) {
          this.contract = response.data;
          this.isEditing = false;
          this.toastService.success('Contract updated successfully');
        } else {
          this.toastService.error(response.message || 'Failed to update contract');
        }
      },
      error: (error) => {
        this.loading = false;
        this.toastService.error('Failed to update contract');
        console.error('Error updating contract:', error);
      }
    });
  }

  onInvoicePageChange(page: number): void {
    if (page < 1 || page > this.invoiceTotalPages) {
      return;
    }
    this.invoiceCurrentPage = page;
    this.loadInvoices();
  }

  goBack(): void {
    this.router.navigate(["/contracts"]);
  }

  navigateToInvoiceDetail(invoice: Invoice): void {
    if (invoice.invoiceId) {
      this.router.navigate(['/invoices', invoice.invoiceId]);
    }
  }

  formatCurrency(amount: number | undefined, currency = "USD"): string {
    if (amount === undefined || amount === null) return "N/A";
    
    // For VND currency, format without decimals
    if (currency === "VND") {
      return new Intl.NumberFormat("vi-VN", {
        style: "currency",
        currency: "VND",
        minimumFractionDigits: 0,
        maximumFractionDigits: 0,
      }).format(amount);
    }
    
    return new Intl.NumberFormat("en-US", {
      style: "currency",
      currency: currency,
    }).format(amount);
  }

  formatVNDCurrency(amount: number | undefined): string {
    if (amount === undefined || amount === null) return "N/A";
    
    return new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND",
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(amount);
  }

  getPreVATAmount(invoice: Invoice): number {
    if (!invoice?.totalAmountWithVat || !invoice?.vat) return 0;
    return invoice.totalAmountWithVat / (1 + invoice.vat / 100);
  }

  getTotalAmount(invoice: Invoice): number {
    return invoice?.totalAmountWithVat || 0;
  }

  formatDate(date: Date | undefined): string {
    if (!date) return "N/A";
    return new Date(date).toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
    });
  }

  isOverdue(dueDate: Date | undefined): boolean {
    if (!dueDate) return false;
    const today = new Date();
    const due = new Date(dueDate);
    return due < today;
  }

  getPaymentMethodClass(paymentMethod: string): string {
    switch (paymentMethod?.toLowerCase()) {
      case "cash":
        return "bg-green-100 text-green-800";
      case "credit":
      case "credit card":
        return "bg-blue-100 text-blue-800";
      case "bank transfer":
      case "wire transfer":
        return "bg-purple-100 text-purple-800";
      case "check":
      case "cheque":
        return "bg-yellow-100 text-yellow-800";
      default:
        return "bg-gray-100 text-gray-800";
    }
  }

  truncateId(id: string | undefined): string {
    if (!id) return 'N/A';
    return id.length > 8 ? id.substring(0, 8) + '...' : id;
  }

  getInvoicePaginationArray(): { type: 'page' | 'ellipsis', value: number }[] {
    const items: { type: 'page' | 'ellipsis', value: number }[] = [];
    const maxVisiblePages = 5;
    const current = this.invoiceCurrentPage;
    const total = this.invoiceTotalPages;

    if (total <= maxVisiblePages) {
      for (let i = 1; i <= total; i++) {
        items.push({ type: 'page', value: i });
      }
    } else {
      items.push({ type: 'page', value: 1 });

      if (current <= 3) {
        for (let i = 2; i <= Math.min(4, total - 1); i++) {
          items.push({ type: 'page', value: i });
        }
        if (total > 4) {
          items.push({ type: 'ellipsis', value: 0 });
        }
      } else if (current >= total - 2) {
        if (total > 4) {
          items.push({ type: 'ellipsis', value: 0 });
        }
        for (let i = Math.max(total - 3, 2); i <= total - 1; i++) {
          items.push({ type: 'page', value: i });
        }
      } else {
        items.push({ type: 'ellipsis', value: 0 });
        for (let i = current - 1; i <= current + 1; i++) {
          items.push({ type: 'page', value: i });
        }
        items.push({ type: 'ellipsis', value: 0 });
      }

      if (total > 1) {
        items.push({ type: 'page', value: total });
      }
    }

    return items;
  }

  onStaffSelected(staffUsername: string): void {
    this.contractEditForm.assignedStaffUsername = staffUsername;
  }

  // Invoice editing methods (keeping existing functionality)
  openInvoiceEditModal(invoice: Invoice): void {
    this.selectedInvoice = invoice;
    this.invoiceForm = {
      invoiceSymbol: invoice.invoiceSymbol,
      invoiceNumber: invoice.invoiceNumber,
      originalAmount: invoice.originalAmount,
      currencyType: invoice.currencyType,
      exchangeRate: invoice.exchangeRate,
      vat: invoice.vat,
      invoiceDate: this.formatDateForInput(invoice.invoiceDate),
      dueDate: this.formatDateForInput(invoice.dueDate),
      paymentMethod: invoice.paymentMethod,
      customerId: invoice.customerId,
      projectId: invoice.projectId,
      staffUsername: invoice.staffUsername,
      department: invoice.department,
      notes: invoice.notes || ''
    };
    this.showInvoiceEditModal = true;
  }

  closeInvoiceEditModal(): void {
    this.showInvoiceEditModal = false;
    this.selectedInvoice = null;
    this.invoiceForm = {};
  }

  updateInvoice(): void {
    if (!this.selectedInvoice?.invoiceId) {
      return;
    }

    this.loading = true;
    
    const updateData: UpdateInvoiceRequest = {
      invoiceId: this.selectedInvoice.invoiceId,
      invoiceSymbol: this.invoiceForm.invoiceSymbol,
      invoiceNumber: this.invoiceForm.invoiceNumber,
      originalAmount: this.invoiceForm.originalAmount,
      currencyType: this.invoiceForm.currencyType,
      exchangeRate: this.invoiceForm.exchangeRate,
      vat: this.invoiceForm.vat,
      invoiceDate: new Date(this.invoiceForm.invoiceDate),
      dueDate: new Date(this.invoiceForm.dueDate),
      paymentMethod: this.invoiceForm.paymentMethod,
      contractId: this.contractId,
      customerId: this.invoiceForm.customerId,
      projectId: this.invoiceForm.projectId,
      staffUsername: this.invoiceForm.staffUsername,
      department: this.invoiceForm.department,
      notes: this.invoiceForm.notes || undefined
    };

    this.invoiceService.update(this.selectedInvoice.invoiceId, updateData).subscribe({
      next: (response) => {
        this.loading = false;
        
        if (response.status === 200) {
          this.toastService.success('Invoice updated successfully');
          this.loadInvoices();
          this.closeInvoiceEditModal();
        } else {
          this.toastService.error(response.message || 'Failed to update invoice');
        }
      },
      error: (error) => {
        this.loading = false;
        this.toastService.error('Failed to update invoice');
        console.error('Error updating invoice:', error);
      }
    });
  }

  private formatDateForInput(date: Date | undefined): string {
    if (!date) return '';
    const d = new Date(date);
    return d.toISOString().split('T')[0];
  }
}