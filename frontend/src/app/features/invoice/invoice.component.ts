import { Component, OnInit, inject, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { InvoiceService } from './services/invoice.service';
import { Invoice, CreateInvoiceRequest, UpdateInvoiceRequest } from '../../shared/models/invoice.model';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ToastService } from '../../core/services/toast.service';
import { AddInvoiceFormComponent } from './components/add-invoice-form/add-invoice-form.component';
import { CustomerDropdownComponent } from '../../shared/components/customer-dropdown/customer-dropdown.component';
import { ContractDropdownComponent } from '../../shared/components/contract-dropdown/contract-dropdown.component';
import { StaffDropdownComponent } from '../../shared/components/staff-dropdown/staff-dropdown.component';

@Component({
  selector: 'app-invoice',
  imports: [
    CommonModule, 
    FormsModule, 
    AddInvoiceFormComponent,
    CustomerDropdownComponent,
    ContractDropdownComponent,
    StaffDropdownComponent
  ],
  standalone: true,
  templateUrl: './invoice.component.html'
})
export class InvoiceComponent implements OnInit {
  private invoiceService = inject(InvoiceService);
  private toastService = inject(ToastService);
  private router = inject(Router);

  @ViewChild(AddInvoiceFormComponent) addInvoiceForm!: AddInvoiceFormComponent;

  invoices: Invoice[] = [];
  loading = false;
  
  // Pagination
  currentPage = 1;
  pageSize = 10;
  totalItems = 0;
  totalPages = 0;
  currentSort = 'invoiceDate';
  isCurrentSortAsc = false;
  
  // Search
  searchQuery = '';
  
  // Filters
  showFilters = false;
  filters = {
    currencyType: '',
    paymentMethod: '',
    minAmount: '',
    maxAmount: '',
    status: '',
    invoiceDateStart: '',
    invoiceDateEnd: '',
    dueDateStart: '',
    dueDateEnd: '',
    department: '',
    staffUsername: '',
    contractId: '',
    customerId: '',
    createdByUsername: ''
  };

  // Add reset triggers for dropdown components
  staffDropdownResetTrigger = 0;
  contractDropdownResetTrigger = 0;
  customerDropdownResetTrigger = 0;

  // Modal states
  showCreateModal = false;
  showEditModal = false;
  showDeleteModal = false;
  showImportModal = false;
  selectedInvoice: Invoice | null = null;
  prefilledInvoiceData: Partial<CreateInvoiceRequest> = {};

  // File upload
  selectedFile: File | null = null;

  // Add Math property for template usage
  Math = Math;

  ngOnInit(): void {
    this.loadInvoices();
  }

  loadInvoices(): void {
    this.loading = true;
    
    const params = {
      page: this.currentPage - 1,
      size: this.pageSize,
      sortBy: this.currentSort,
      sortAsc: this.isCurrentSortAsc
    };

    // Use search endpoint if we have search query or active filters
    if (this.searchQuery || this.hasActiveFilters()) {
      this.searchInvoices();
      return;
    }

    this.invoiceService.search(params).subscribe({
      next: (response) => {
        this.handleInvoicesResponse(response);
      },
      error: (error) => {
        this.toastService.error('Failed to load invoices');
        this.loading = false;
        console.error('Error loading invoices:', error);
      }
    });
  }

  searchInvoices(): void {
    this.loading = true;
    
    const params = {
      page: this.currentPage - 1,
      size: this.pageSize,
      sortBy: this.currentSort,
      sortAsc: this.isCurrentSortAsc,
      ...this.buildSearchParams()
    };

    this.invoiceService.search(params).subscribe({
      next: (response) => {
        this.handleInvoicesResponse(response);
      },
      error: (error) => {
        this.toastService.error('Failed to search invoices');
        this.loading = false;
        console.error('Error searching invoices:', error);
      }
    });
  }

  private handleInvoicesResponse(response: any): void {
    this.loading = false;
    
    if (response.status !== 200) {
      this.toastService.error(response.message || 'Failed to load invoices');
      this.invoices = [];
      this.totalItems = 0;
      this.totalPages = 0;
      return;
    }

    let data = response.data;
    if (!data) {
      this.toastService.error('No data found');
      this.invoices = [];
      this.totalItems = 0;
      this.totalPages = 0;
      return;
    }

    this.invoices = data.content || [];
    this.totalItems = data.totalElements;
    this.totalPages = data.totalPages;
  }

  private buildSearchParams(): any {
    const params: any = {};
    
    // Add search query
    if (this.searchQuery) {
      params.query = this.searchQuery;
    }
    
    // Add filters with correct parameter names
    if (this.filters.currencyType) params.currencyType = this.filters.currencyType;
    if (this.filters.minAmount) params.minAmount = this.filters.minAmount;
    if (this.filters.maxAmount) params.maxAmount = this.filters.maxAmount;
    if (this.filters.invoiceDateStart) params.invoiceDateStart = this.filters.invoiceDateStart;
    if (this.filters.invoiceDateEnd) params.invoiceDateEnd = this.filters.invoiceDateEnd;
    if (this.filters.dueDateStart) params.dueDateStart = this.filters.dueDateStart;
    if (this.filters.dueDateEnd) params.dueDateEnd = this.filters.dueDateEnd;
    if (this.filters.department) params.department = this.filters.department;
    if (this.filters.staffUsername) params.staffUsername = this.filters.staffUsername;
    if (this.filters.contractId) params.contractId = this.filters.contractId;
    if (this.filters.customerId) params.customerId = this.filters.customerId;
    if (this.filters.createdByUsername) params.createdByUsername = this.filters.createdByUsername;
    
    return params;
  }

  onPageChange(page: number): void {
    if (page < 1 || page > this.totalPages) {
      return;
    }
    this.currentPage = page;
    this.loadInvoices();
  }

  onSearch(): void {
    this.currentPage = 1;
    this.searchInvoices();
  }

  onSearchQueryChange(): void {
    if (!this.searchQuery && !this.hasActiveFilters()) {
      this.loadInvoices();
    } else if (this.searchQuery.length >= 3) {
      this.currentPage = 1;
      this.searchInvoices();
    }
  }

  toggleFilters(): void {
    this.showFilters = !this.showFilters;
  }

  onFiltersChange(): void {
    this.applyFilters();
  }

  applyFilters(): void {
    this.currentPage = 1;
    this.searchInvoices();
  }

  clearFilters(): void {
    this.filters = {
      currencyType: '',
      paymentMethod: '',
      minAmount: '',
      maxAmount: '',
      status: '',
      invoiceDateStart: '',
      invoiceDateEnd: '',
      dueDateStart: '',
      dueDateEnd: '',
      department: '',
      staffUsername: '',
      contractId: '',
      customerId: '',
      createdByUsername: ''
    };
    // Trigger reset in all dropdown components
    this.staffDropdownResetTrigger++;
    this.contractDropdownResetTrigger++;
    this.customerDropdownResetTrigger++;
    this.currentPage = 1;
    this.loadInvoices();
  }

  clearFilter(filterType: string): void {
    switch (filterType) {
      case 'currencyType':
        this.filters.currencyType = '';
        break;
      case 'paymentMethod':
        this.filters.paymentMethod = '';
        break;
      case 'status':
        this.filters.status = '';
        break;
      case 'department':
        this.filters.department = '';
        break;
      case 'staffUsername':
        this.filters.staffUsername = '';
        this.staffDropdownResetTrigger++; // Trigger reset for staff dropdown
        break;
      case 'contractId':
        this.filters.contractId = '';
        this.contractDropdownResetTrigger++; // Trigger reset for contract dropdown
        break;
      case 'customerId':
        this.filters.customerId = '';
        this.customerDropdownResetTrigger++; // Trigger reset for customer dropdown
        break;
      case 'createdByUsername':
        this.filters.createdByUsername = '';
        break;
    }
    this.currentPage = 1;
    this.searchInvoices();
  }

  clearAmountFilter(): void {
    this.filters.minAmount = '';
    this.filters.maxAmount = '';
    this.currentPage = 1;
    this.searchInvoices();
  }

  clearDateFilter(type: 'invoice' | 'due'): void {
    if (type === 'invoice') {
      this.filters.invoiceDateStart = '';
      this.filters.invoiceDateEnd = '';
    } else {
      this.filters.dueDateStart = '';
      this.filters.dueDateEnd = '';
    }
    this.currentPage = 1;
    this.searchInvoices();
  }

  getDateRangeText(type: 'invoice' | 'due'): string {
    const fromField = type === 'invoice' ? 'invoiceDateStart' : 'dueDateStart';
    const toField = type === 'invoice' ? 'invoiceDateEnd' : 'dueDateEnd';
    
    const from = this.filters[fromField];
    const to = this.filters[toField];
    
    if (from && to) {
      return `${from} to ${to}`;
    } else if (from) {
      return `from ${from}`;
    } else if (to) {
      return `until ${to}`;
    }
    return '';
  }

  hasActiveFilters(): boolean {
    return Object.values(this.filters).some(value => value !== '');
  }

  openEditModal(invoice: Invoice): void {
    this.selectedInvoice = invoice;
    this.showEditModal = true;
  }

  openCreateModal(): void {
    this.selectedInvoice = null;
    this.prefilledInvoiceData = {};
    this.showCreateModal = true;
  }

  openDeleteModal(invoice: Invoice): void {
    this.selectedInvoice = invoice;
    this.showDeleteModal = true;
  }

  openImportModal(): void {
    this.selectedFile = null;
    this.showImportModal = true;
  }

  closeModals(): void {
    this.showCreateModal = false;
    this.showEditModal = false;
    this.showDeleteModal = false;
    this.showImportModal = false;
    this.selectedInvoice = null;
    this.selectedFile = null;
    this.prefilledInvoiceData = {};
  }

  onFormSubmit(formData: CreateInvoiceRequest | UpdateInvoiceRequest): void {
    if (this.showCreateModal) {
      this.createInvoice(formData as CreateInvoiceRequest);
    } else if (this.showEditModal) {
      this.updateInvoice(formData as UpdateInvoiceRequest);
    }
  }

  createInvoice(invoiceData: CreateInvoiceRequest): void {
    this.loading = true;
    this.invoiceService.create(invoiceData).subscribe({
      next: (response) => {
        this.loading = false;
        
        if (response.status !== 200 && response.status !== 201) {
          this.toastService.error(response.message || 'Failed to create invoice');
          this.addInvoiceForm.onSubmissionFailed();
          return;
        }

        this.toastService.success('Invoice created successfully');
        this.addInvoiceForm.onSubmissionSuccess();
        this.loadInvoices();
        this.closeModals();
      },
      error: (error) => {
        this.toastService.error('Failed to create invoice');
        this.loading = false;
        this.addInvoiceForm.onSubmissionFailed();
        console.error('Error creating invoice:', error);
      }
    });
  }

  updateInvoice(invoiceData: UpdateInvoiceRequest): void {
    if (!this.selectedInvoice?.invoiceId) {
      this.toastService.error('No invoice selected for update');
      return;
    }

    this.loading = true;
    this.invoiceService.update(this.selectedInvoice.invoiceId, invoiceData).subscribe({
      next: (response) => {
        this.loading = false;
        
        if (response.status !== 200) {
          this.toastService.error(response.message || 'Failed to update invoice');
          this.addInvoiceForm.onSubmissionFailed();
          return;
        }

        this.toastService.success('Invoice updated successfully');
        this.loadInvoices();
        this.closeModals();
      },
      error: (error) => {
        this.toastService.error('Failed to update invoice');
        this.loading = false;
        this.addInvoiceForm.onSubmissionFailed();
        console.error('Error updating invoice:', error);
      }
    });
  }

  deleteInvoice(): void {
    if (!this.selectedInvoice?.invoiceId) {
      return;
    }

    this.loading = true;
    this.invoiceService.delete(this.selectedInvoice.invoiceId).subscribe({
      next: (response) => {
        this.loading = false;
        
        if (response.status !== 200) {
          this.toastService.error(response.message || 'Failed to delete invoice');
          return;
        }

        this.toastService.success('Invoice deleted successfully');
        this.loadInvoices();
        this.closeModals();
      },
      error: (error) => {
        this.toastService.error('Failed to delete invoice');
        this.loading = false;
        console.error('Error deleting invoice:', error);
      }
    });
  }

  exportInvoices(): void {
    this.loading = true;
    
    const params = {
      page: this.currentPage - 1,
      size: this.pageSize,
      sortBy: this.currentSort,
      sortAsc: this.isCurrentSortAsc,
      ...this.buildSearchParams()
    };

    this.invoiceService.export(params).subscribe({
      next: (blob) => {
        this.loading = false;
        
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `invoices_${new Date().toISOString().split('T')[0]}.csv`;
        link.click();
        window.URL.revokeObjectURL(url);
        
        this.toastService.success('Invoices exported successfully');
      },
      error: (error) => {
        this.toastService.error('Failed to export invoices');
        this.loading = false;
        console.error('Error exporting invoices:', error);
      }
    });
  }

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file && file.type === 'text/csv') {
      this.selectedFile = file;
    } else {
      this.toastService.error('Please select a valid CSV file');
      this.selectedFile = null;
    }
  }

  importInvoices(): void {
    if (!this.selectedFile) {
      this.toastService.error('Please select a CSV file');
      return;
    }

    this.loading = true;
    this.invoiceService.import(this.selectedFile).subscribe({
      next: (response) => {
        this.loading = false;
        
        if (response.status !== 200) {
          this.toastService.error(response.message || 'Failed to import invoices');
          return;
        }

        this.toastService.success('Invoices imported successfully');
        this.loadInvoices();
        this.closeModals();
      },
      error: (error) => {
        this.toastService.error('Failed to import invoices');
        this.loading = false;
        console.error('Error importing invoices:', error);
      }
    });
  }

  viewInvoiceDetail(invoice: Invoice): void {
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

  getStatusClass(invoice: Invoice): string {
    if (this.isOverdue(invoice.dueDate)) {
      return 'bg-red-100 text-red-800';
    }
    return 'bg-green-100 text-green-800';
  }

  getStatusText(invoice: Invoice): string {
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

  trackByInvoiceId(index: number, invoice: Invoice): string | undefined {
    return invoice.invoiceId;
  }

  getPaginationArray(): { type: 'page' | 'ellipsis', value: number }[] {
    const items: { type: 'page' | 'ellipsis', value: number }[] = [];
    const maxVisiblePages = 5;
    const current = this.currentPage;
    const total = this.totalPages;

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

  onSort(column: string): void {
    if (this.currentSort === column) {
      // Same column - cycle through states: ASC -> DESC -> No Sort
      if (this.isCurrentSortAsc) {
        // Currently ASC, change to DESC
        this.isCurrentSortAsc = false;
      } else {
        // Currently DESC, remove sorting (go to default)
        this.currentSort = 'invoiceDate';
        this.isCurrentSortAsc = false;
      }
    } else {
      // Different column - start with ASC
      this.currentSort = column;
      this.isCurrentSortAsc = true;
    }

    // Reset to first page and reload
    this.currentPage = 1;
    this.loadInvoices();
  }

  getSortState(column: string): 'asc' | 'desc' | 'none' {
    if (this.currentSort !== column) {
      return 'none';
    }
    return this.isCurrentSortAsc ? 'asc' : 'desc';
  }

  onStaffFilterSelected(staffUsername: string): void {
    this.filters.staffUsername = staffUsername;
    this.onFiltersChange();
  }

  onContractFilterSelected(contractId: string): void {
    this.filters.contractId = contractId;
    this.onFiltersChange();
  }

  onCustomerFilterSelected(customerId: string): void {
    this.filters.customerId = customerId;
    this.onFiltersChange();
  }
}
