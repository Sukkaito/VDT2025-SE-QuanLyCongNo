import { Component, OnInit, inject, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CustomerService } from '../../services/customer.service';
import { ContractService } from '../../../contract/services/contract.service';
import { InvoiceService } from '../../../invoice/services/invoice.service';
import { Customer, CustomerUpdateRequest } from '../../../../shared/models/customer.model';
import { Contract } from '../../../../shared/models/contract.model';
import { Invoice, CreateInvoiceRequest, UpdateInvoiceRequest } from '../../../../shared/models/invoice.model';
import { ToastService } from '../../../../core/services/toast.service';
import { AddInvoiceFormComponent } from '../../../invoice/components/add-invoice-form/add-invoice-form.component';
import { StaffDropdownComponent } from '../../../../shared/components/staff-dropdown/staff-dropdown.component';

@Component({
  selector: 'app-customer-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, AddInvoiceFormComponent, StaffDropdownComponent],
  templateUrl: './customer-detail.component.html'
})
export class CustomerDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private customerService = inject(CustomerService);
  private contractService = inject(ContractService);
  private invoiceService = inject(InvoiceService);
  private toastService = inject(ToastService);

  @ViewChild(AddInvoiceFormComponent) addInvoiceForm!: AddInvoiceFormComponent;

  customer: Customer | null = null;
  contracts: Contract[] = [];
  invoices: Invoice[] = [];
  loading = false;
  customerId: string | null = null;

  // Edit mode
  isEditing = false;
  editForm: CustomerUpdateRequest = {
    customerName: '',
    taxCode: '',
    abbreviationName: '',
    assignedStaffUsername: ''
  };

  // Pagination for contracts
  contractsPage = 1;
  contractsPageSize = 5;
  contractsTotalItems = 0;
  contractsTotalPages = 0;

  // Pagination for invoices
  invoicesPage = 1;
  invoicesPageSize = 5;
  invoicesTotalItems = 0;
  invoicesTotalPages = 0;

  // Active tab
  activeTab: 'overview' | 'contracts' | 'invoices' = 'overview';

  // Staff assignment modal
  showStaffAssignmentModal = false;
  newStaffUsername = '';

  // Invoice creation modal
  showCreateInvoiceModal = false;
  prefilledInvoiceData: Partial<CreateInvoiceRequest> = {};

  // Add Math property for template usage
  Math = Math;

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.customerId = params['id'];
      if (this.customerId) {
        this.loadCustomerDetail();
      }
    });
  }

  loadCustomerDetail(): void {
    if (!this.customerId) return;

    this.loading = true;
    this.customerService.getById(this.customerId).subscribe({
      next: (response) => {
        this.loading = false;
        
        if (response.status !== 200 || !response.data) {
          this.toastService.error(response.message || 'Failed to load customer details');
          this.router.navigate(['/customers']);
          return;
        }

        this.customer = response.data;
        this.initializeEditForm();
        
        // Load related data
        this.loadContracts();
        this.loadInvoices();
      },
      error: (error) => {
        this.loading = false;
        this.toastService.error('Failed to load customer details');
        console.error('Error loading customer:', error);
        this.router.navigate(['/customers']);
      }
    });
  }

  loadContracts(): void {
    if (!this.customerId) return;

    const params = {
      page: this.contractsPage - 1,
      size: this.contractsPageSize,
      customerId: this.customerId
    };

    this.customerService.getContractByCustomerId(this.customerId, params).subscribe({
      next: (response) => {
        if (response.status === 200 && response.data) {
          this.contracts = response.data.content || [];
          this.contractsTotalItems = response.data.totalElements || 0;
          this.contractsTotalPages = response.data.totalPages || 0;
        }
      },
      error: (error) => {
        console.error('Error loading contracts:', error);
      }
    });
  }

  loadInvoices(): void {
    if (!this.customerId) return;

    const params = {
      page: this.invoicesPage - 1,
      size: this.invoicesPageSize,
      customerId: this.customerId
    };

    this.invoiceService.search(params).subscribe({
      next: (response) => {
        if (response.status === 200 && response.data) {
          this.invoices = response.data.content || [];
          this.invoicesTotalItems = response.data.totalElements || 0;
          this.invoicesTotalPages = response.data.totalPages || 0;
        }
      },
      error: (error) => {
        console.error('Error loading invoices:', error);
      }
    });
  }

  initializeEditForm(): void {
    if (this.customer) {
      this.editForm = {
        customerName: this.customer.customerName,
        taxCode: this.customer.taxCode,
        abbreviationName: this.customer.abbreviationName || '',
        assignedStaffUsername: this.customer.assignedStaffUsername || ''
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
    if (!this.customerId || !this.editForm.customerName || !this.editForm.taxCode) {
      this.toastService.error('Please fill in all required fields');
      return;
    }

    this.loading = true;
    this.customerService.update(this.customerId, this.editForm).subscribe({
      next: (response) => {
        this.loading = false;
        
        if (response.status !== 200 || !response.data) {
          this.toastService.error(response.message || 'Failed to update customer');
          return;
        }

        this.customer = response.data;
        this.isEditing = false;
        this.toastService.success('Customer updated successfully');
      },
      error: (error) => {
        this.loading = false;
        this.toastService.error('Failed to update customer');
        console.error('Error updating customer:', error);
      }
    });
  }

  assignStaffFromHistory(staffUsername: string): void {
    if (!this.customerId || staffUsername === this.customer?.assignedStaffUsername) {
      return;
    }

    if (confirm(`Are you sure you want to assign ${staffUsername} to this customer?`)) {
      this.assignStaff(staffUsername);
    }
  }

  openStaffAssignmentModal(): void {
    this.newStaffUsername = '';
    this.showStaffAssignmentModal = true;
  }

  closeStaffAssignmentModal(): void {
    this.showStaffAssignmentModal = false;
    this.newStaffUsername = '';
  }

  onStaffSelected(staffUsername: string): void {
    this.newStaffUsername = staffUsername;
  }

  onEditStaffSelected(staffUsername: string): void {
    this.editForm.assignedStaffUsername = staffUsername;
  }

  assignNewStaff(): void {
    if (!this.newStaffUsername.trim()) {
      this.toastService.error('Please select a staff member');
      return;
    }

    if (this.newStaffUsername === this.customer?.assignedStaffUsername) {
      this.toastService.error('This staff is already assigned to this customer');
      return;
    }

    this.assignStaff(this.newStaffUsername);
  }

  private assignStaff(staffUsername: string): void {
    if (!this.customerId) return;

    this.loading = true;
    
    // Use the inherited update method to assign staff
    const updateData = {
      assignedStaffUsername: staffUsername
    };

    this.customerService.update(this.customerId, updateData).subscribe({
      next: (response) => {
        this.loading = false;
        
        if (response.status !== 200 || !response.data) {
          this.toastService.error(response.message || 'Failed to assign staff');
          return;
        }

        this.customer = response.data;
        this.initializeEditForm();
        this.closeStaffAssignmentModal();
        this.toastService.success(`Staff ${staffUsername} assigned successfully`);
      },
      error: (error) => {
        this.loading = false;
        this.toastService.error('Failed to assign staff');
        console.error('Error assigning staff:', error);
      }
    });
  }

  isCurrentlyAssigned(staffUsername: string): boolean {
    return staffUsername === this.customer?.assignedStaffUsername;
  }

  setActiveTab(tab: 'overview' | 'contracts' | 'invoices'): void {
    this.activeTab = tab;
    
    if (tab === 'contracts' && this.contracts.length === 0) {
      this.loadContracts();
    } else if (tab === 'invoices' && this.invoices.length === 0) {
      this.loadInvoices();
    }
  }

  onContractsPageChange(page: number): void {
    if (page < 1 || page > this.contractsTotalPages) return;
    this.contractsPage = page;
    this.loadContracts();
  }

  onInvoicesPageChange(page: number): void {
    if (page < 1 || page > this.invoicesTotalPages) return;
    this.invoicesPage = page;
    this.loadInvoices();
  }

  getContractsPaginationArray(): { type: "page" | "ellipsis"; value: number }[] {
    const items: { type: "page" | "ellipsis"; value: number }[] = [];
    const maxVisiblePages = 5;
    const current = this.contractsPage;
    const total = this.contractsTotalPages;

    if (total <= maxVisiblePages) {
      for (let i = 1; i <= total; i++) {
        items.push({ type: "page", value: i });
      }
    } else {
      items.push({ type: "page", value: 1 });

      if (current <= 3) {
        for (let i = 2; i <= Math.min(4, total - 1); i++) {
          items.push({ type: "page", value: i });
        }
        if (total > 4) {
          items.push({ type: "ellipsis", value: 0 });
        }
      } else if (current >= total - 2) {
        if (total > 4) {
          items.push({ type: "ellipsis", value: 0 });
        }
        for (let i = Math.max(total - 3, 2); i <= total - 1; i++) {
          items.push({ type: "page", value: i });
        }
      } else {
        items.push({ type: "ellipsis", value: 0 });
        for (let i = current - 1; i <= current + 1; i++) {
          items.push({ type: "page", value: i });
        }
        items.push({ type: "ellipsis", value: 0 });
      }

      if (total > 1) {
        items.push({ type: "page", value: total });
      }
    }

    return items;
  }

  getInvoicesPaginationArray(): { type: "page" | "ellipsis"; value: number }[] {
    const items: { type: "page" | "ellipsis"; value: number }[] = [];
    const maxVisiblePages = 5;
    const current = this.invoicesPage;
    const total = this.invoicesTotalPages;

    if (total <= maxVisiblePages) {
      for (let i = 1; i <= total; i++) {
        items.push({ type: "page", value: i });
      }
    } else {
      items.push({ type: "page", value: 1 });

      if (current <= 3) {
        for (let i = 2; i <= Math.min(4, total - 1); i++) {
          items.push({ type: "page", value: i });
        }
        if (total > 4) {
          items.push({ type: "ellipsis", value: 0 });
        }
      } else if (current >= total - 2) {
        if (total > 4) {
          items.push({ type: "ellipsis", value: 0 });
        }
        for (let i = Math.max(total - 3, 2); i <= total - 1; i++) {
          items.push({ type: "page", value: i });
        }
      } else {
        items.push({ type: "ellipsis", value: 0 });
        for (let i = current - 1; i <= current + 1; i++) {
          items.push({ type: "page", value: i });
        }
        items.push({ type: "ellipsis", value: 0 });
      }

      if (total > 1) {
        items.push({ type: "page", value: total });
      }
    }

    return items;
  }

  navigateToContract(contract: Contract): void {
    if (contract.contractId) {
      this.router.navigate(['/contracts', contract.contractId]);
    }
  }

  navigateToInvoice(invoice: Invoice): void {
    if (invoice.invoiceId) {
      this.router.navigate(['/invoices', invoice.invoiceId]);
    }
  }

  formatCurrency(amount: number | undefined, currency: string = "USD"): string {
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

  getInvoiceStatusClass(invoice: Invoice): string {
    if (this.isOverdue(invoice.dueDate)) {
      return 'bg-red-100 text-red-800';
    }
    return 'bg-green-100 text-green-800';
  }

  getInvoiceStatusText(invoice: Invoice): string {
    if (this.isOverdue(invoice.dueDate)) {
      return 'Overdue';
    }
    return 'Current';
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

  goBack(): void {
    this.router.navigate(['/customers']);
  }

  openCreateInvoiceModal(contract?: Contract): void {
    const today = new Date();
    const nextWeek = new Date();
    nextWeek.setDate(today.getDate() + 7);

    this.prefilledInvoiceData = {
      customerId: this.customerId || '',
      contractId: contract?.contractId || '',
      invoiceDate: today,
      dueDate: nextWeek,
      staffUsername: contract?.assignedStaffUsername || this.customer?.assignedStaffUsername || '',
      department: '', // You might want to derive this from contract or customer data
      currencyType: 'USD',
      exchangeRate: 1,
      vat: 0,
      paymentMethod: 'Cash'
    };

    this.showCreateInvoiceModal = true;
  }

  closeCreateInvoiceModal(): void {
    this.showCreateInvoiceModal = false;
    this.prefilledInvoiceData = {};
  }

  onInvoiceFormSubmit(formData: CreateInvoiceRequest | UpdateInvoiceRequest): void {
    this.loading = true;
    this.invoiceService.create(formData).subscribe({
      next: (response) => {
        this.loading = false;
        
        if (response.status !== 200 && response.status !== 201) {
          this.toastService.error(response.message || 'Failed to create invoice');
          this.addInvoiceForm.onSubmissionFailed();
          return;
        }

        this.toastService.success('Invoice created successfully');
        this.loadInvoices(); // Refresh invoices list
        this.closeCreateInvoiceModal();
      },
      error: (error) => {
        this.loading = false;
        this.toastService.error('Failed to create invoice');
        this.addInvoiceForm.onSubmissionFailed();
        console.error('Error creating invoice:', error);
      }
    });
  }
}
