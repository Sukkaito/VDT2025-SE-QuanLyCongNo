import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InvoiceService } from '../../services/invoice.service';
import { ContractService } from '../../../contract/services/contract.service';
import { CustomerService } from '../../../customer/services/customer.service';
import { Invoice, UpdateInvoiceRequest } from '../../../../shared/models/invoice.model';
import { Contract } from '../../../../shared/models/contract.model';
import { Customer } from '../../../../shared/models/customer.model';
import { ToastService } from '../../../../core/services/toast.service';
import { StaffDropdownComponent } from '../../../../shared/components/staff-dropdown/staff-dropdown.component';
import { CustomerDropdownComponent } from '../../../../shared/components/customer-dropdown/customer-dropdown.component';
import { ContractDropdownComponent } from '../../../../shared/components/contract-dropdown/contract-dropdown.component';

@Component({
  selector: 'app-invoice-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, 
    StaffDropdownComponent, 
    CustomerDropdownComponent, 
    ContractDropdownComponent],
  templateUrl: './invoice-detail.component.html'
})
export class InvoiceDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private invoiceService = inject(InvoiceService);
  private contractService = inject(ContractService);
  private customerService = inject(CustomerService);
  private toastService = inject(ToastService);

  invoice: Invoice | null = null;
  contract: Contract | null = null;
  customer: Customer | null = null;
  loading = false;
  invoiceId = "";

  // Edit mode
  isEditing = false;
  editForm: any = {};

  // Add Math property for template usage
  Math = Math;

  ngOnInit(): void {
    this.invoiceId = this.route.snapshot.paramMap.get("id") || "";
    if (this.invoiceId) {
      this.loadInvoiceDetails();
    }
  }

  loadInvoiceDetails(): void {
    this.loading = true;
    this.invoiceService.getById(this.invoiceId).subscribe({
      next: (response) => {
        if (response.status === 200 && response.data) {
          this.invoice = response.data;
          this.initializeEditForm();
          this.loadRelatedData();
        } else {
          this.toastService.error(
            response.message || "Failed to load invoice details"
          );
          this.router.navigate(["/invoices"]);
        }
        this.loading = false;
      },
      error: (error) => {
        this.toastService.error("Failed to load invoice details");
        this.loading = false;
        console.error("Error loading invoice:", error);
        this.router.navigate(["/invoices"]);
      },
    });
  }

  loadRelatedData(): void {
    if (!this.invoice) return;

    // Load contract details
    if (this.invoice.contractId) {
      this.contractService.getById(this.invoice.contractId).subscribe({
        next: (response) => {
          if (response.status === 200 && response.data) {
            this.contract = response.data;
          }
        },
        error: (error) => {
          console.error("Error loading contract:", error);
        }
      });
    }

    // Load customer details
    if (this.invoice.customerId) {
      this.customerService.getById(this.invoice.customerId).subscribe({
        next: (response) => {
          if (response.status === 200 && response.data) {
            this.customer = response.data;
          }
        },
        error: (error) => {
          console.error("Error loading customer:", error);
        }
      });
    }
  }

  initializeEditForm(): void {
    if (this.invoice) {
      this.editForm = {
        invoiceSymbol: this.invoice.invoiceSymbol,
        invoiceNumber: this.invoice.invoiceNumber,
        originalAmount: this.invoice.originalAmount,
        currencyType: this.invoice.currencyType,
        exchangeRate: this.invoice.exchangeRate,
        vat: this.invoice.vat,
        invoiceDate: this.formatDateForInput(this.invoice.invoiceDate),
        dueDate: this.formatDateForInput(this.invoice.dueDate),
        paymentMethod: this.invoice.paymentMethod,
        contractId: this.invoice.contractId,
        customerId: this.invoice.customerId,
        projectId: this.invoice.projectId,
        staffUsername: this.invoice.staffUsername,
        department: this.invoice.department,
        notes: this.invoice.notes || ''
      };
    }
  }

  toggleEdit(): void {
    if (this.isEditing) {
      // Cancel edit
      this.isEditing = false;
      this.initializeEditForm();
    } else {
      // Start edit
      this.isEditing = true;
    }
  }

  saveChanges(): void {
    if (!this.invoiceId || !this.validateForm()) {
      this.toastService.error('Please fill in all required fields');
      return;
    }

    this.loading = true;
    
    const updateData: UpdateInvoiceRequest = {
      invoiceId: this.invoiceId,
      invoiceSymbol: this.editForm.invoiceSymbol,
      invoiceNumber: this.editForm.invoiceNumber,
      originalAmount: this.editForm.originalAmount,
      currencyType: this.editForm.currencyType,
      exchangeRate: this.editForm.exchangeRate,
      vat: this.editForm.vat,
      invoiceDate: new Date(this.editForm.invoiceDate),
      dueDate: new Date(this.editForm.dueDate),
      paymentMethod: this.editForm.paymentMethod,
      contractId: this.editForm.contractId,
      customerId: this.editForm.customerId,
      projectId: this.editForm.projectId,
      staffUsername: this.editForm.staffUsername,
      department: this.editForm.department,
      notes: this.editForm.notes || undefined
    };

    this.invoiceService.update(this.invoiceId, updateData).subscribe({
      next: (response) => {
        this.loading = false;
        
        if (response.status === 200 && response.data) {
          this.invoice = response.data;
          this.isEditing = false;
          this.toastService.success('Invoice updated successfully');
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

  private validateForm(): boolean {
    return !!(
      this.editForm.invoiceSymbol &&
      this.editForm.invoiceNumber &&
      this.editForm.originalAmount &&
      this.editForm.currencyType &&
      this.editForm.exchangeRate &&
      this.editForm.vat !== undefined &&
      this.editForm.invoiceDate &&
      this.editForm.dueDate &&
      this.editForm.paymentMethod &&
      this.editForm.contractId &&
      this.editForm.customerId &&
      this.editForm.projectId &&
      this.editForm.staffUsername &&
      this.editForm.department
    );
  }

  goBack(): void {
    this.router.navigate(["/invoices"]);
  }

  navigateToContract(): void {
    if (this.contract?.contractId) {
      this.router.navigate(['/contracts', this.contract.contractId]);
    }
  }

  navigateToCustomer(): void {
    if (this.customer?.customerId) {
      this.router.navigate(['/customers', this.customer.customerId]);
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

  getPreVATAmount(): number {
    if (!this.invoice?.totalAmountWithVat || !this.invoice?.vat) return 0;
    return this.invoice.totalAmountWithVat / (1 + this.invoice.vat / 100);
  }

  getTotalAmount(): number {
    return this.invoice?.totalAmountWithVat || 0;
  }

  formatDate(date: Date | undefined): string {
    if (!date) return "N/A";
    return new Date(date).toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
    });
  }

  private formatDateForInput(date: Date | undefined): string {
    if (!date) return '';
    const d = new Date(date);
    return d.toISOString().split('T')[0];
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

  getStatusClass(): string {
    if (!this.invoice?.dueDate) return 'bg-gray-100 text-gray-800';
    
    if (this.isOverdue(this.invoice.dueDate)) {
      return 'bg-red-100 text-red-800';
    }
    
    return 'bg-green-100 text-green-800';
  }

  getStatusText(): string {
    if (!this.invoice?.dueDate) return 'Unknown';
    
    if (this.isOverdue(this.invoice.dueDate)) {
      return 'Overdue';
    }
    
    return 'Current';
  }

  onStaffSelected(staffUsername: string): void {
    this.editForm.staffUsername = staffUsername;
  }

  onCustomerSelected(customerId: string): void {
    this.editForm.customerId = customerId;
  }

  onContractSelected(contractId: string): void {
    this.editForm.contractId = contractId;
  }
}
