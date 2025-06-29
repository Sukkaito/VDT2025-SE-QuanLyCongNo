import { Component, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';
import { CustomerService } from './services/customer.service';
import { Customer, CustomerCreateRequest, CustomerUpdateRequest } from '../../shared/models/customer.model';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ToastService } from '../../core/services/toast.service';
import { AddCustomerFormComponent } from './components/add-customer-form/add-customer-form.component';
import { StaffDropdownComponent } from '../../shared/components/staff-dropdown/staff-dropdown.component';

@Component({
  selector: 'app-customer',
  imports: [CommonModule, FormsModule, AddCustomerFormComponent, StaffDropdownComponent],
  standalone: true,
  templateUrl: './customer.component.html'
})
export class CustomerComponent implements OnInit {
  private customerService = inject(CustomerService);
  private toastService = inject(ToastService);
  private router = inject(Router);

  customers: Customer[] = [];
  loading = false;
  
  // Pagination
  currentPage = 1;
  pageSize = 10;
  totalItems = 0;
  totalPages = 0;
  currentSort: string | null = 'customerName'; // Default sort by customerName
  isCurrentSortAsc = true; // Default ASC
  
  // Search
  searchQuery = '';
  
  // Filters
  showFilters = false;
  filters = {
    assignedStaff: '',
    createdDateFrom: '',
    createdDateTo: '',
    updatedDateFrom: '',
    updatedDateTo: ''
  };

  // Add reset trigger for staff dropdown
  staffDropdownResetTrigger = 0;

  // Modal states - remove showCreateModal and showEditModal, keep others
  showCustomerFormModal = false;
  isEditMode = false;
  showDeleteModal = false;
  showImportModal = false;
  selectedCustomer: Customer | null = null;
  
  // File upload
  selectedFile: File | null = null;

  // Add Math property for template usage
  Math = Math;

  ngOnInit(): void {
    this.loadCustomers();
  }

  loadCustomers(): void {
    this.loading = true;
    
    const params = {
      page: this.currentPage - 1,
      size: this.pageSize,
      sortBy: this.currentSort || 'customerName',
      sortAsc: this.isCurrentSortAsc
    };

    // Add sorting parameters only if sorting is active
    if (this.currentSort) {
      params["sortBy"] = this.currentSort;
      params['sortAsc'] = this.isCurrentSortAsc;
    }

    // Use search endpoint if we have search query or active filters
    if (this.searchQuery || this.hasActiveFilters()) {
      this.searchCustomers();
      return;
    }

    this.customerService.search(params).subscribe({
      next: (response) => {
        this.handleCustomersResponse(response);
      },
      error: (error) => {
        this.toastService.error('Failed to load customers');
        this.loading = false;
        console.error('Error loading customers:', error);
      }
    });
  }

  searchCustomers(): void {
    this.loading = true;
    
    const params = {
      page: this.currentPage - 1,
      size: this.pageSize,
      ...this.buildSearchParams()
    };

    // Add sorting parameters only if sorting is active
    if (this.currentSort) {
      params['sortBy'] = this.currentSort;
      params['sortAsc'] = this.isCurrentSortAsc;
    }

    this.customerService.search(params).subscribe({
      next: (response) => {
        this.handleCustomersResponse(response);
      },
      error: (error) => {
        this.toastService.error('Failed to search customers');
        this.loading = false;
        console.error('Error searching customers:', error);
      }
    });
  }

  private handleCustomersResponse(response: any): void {
    this.loading = false;
    
    if (response.status !== 200) {
      this.toastService.error(response.message || 'Failed to load customers');
      this.customers = [];
      this.totalItems = 0;
      this.totalPages = 0;
      return;
    }

    let data = response.data;
    if (!data) {
      this.toastService.error('No data found');
      this.customers = [];
      this.totalItems = 0;
      this.totalPages = 0;
      return;
    }

    this.customers = data.content || [];
    this.totalItems = data.totalElements;
    this.totalPages = data.totalPages;
  }

  private buildSearchParams(): any {
    const params: any = {};
    
    // Add search query
    if (this.searchQuery) {
      params.query = this.searchQuery;
    }
    
    // Add assigned staff filter
    if (this.filters.assignedStaff) {
      params.assignedStaffUsername = this.filters.assignedStaff;
    }
    
    // Add created date range
    if (this.filters.createdDateFrom) {
      params.createdDateFrom = this.filters.createdDateFrom;
    }
    if (this.filters.createdDateTo) {
      params.createdDateTo = this.filters.createdDateTo;
    }
    
    // Add updated date range
    if (this.filters.updatedDateFrom) {
      params.updatedDateFrom = this.filters.updatedDateFrom;
    }
    if (this.filters.updatedDateTo) {
      params.updatedDateTo = this.filters.updatedDateTo;
    }
    
    return params;
  }

  onPageChange(page: number): void {
    if (page < 1 || page > this.totalPages) {
      return;
    }
    this.currentPage = page;
    this.loadCustomers();
  }

  onSearch(): void {
    this.currentPage = 1;
    this.searchCustomers();
  }

  onSearchQueryChange(): void {
    if (!this.searchQuery && !this.hasActiveFilters()) {
      this.loadCustomers();
    } else if (this.searchQuery.length >= 3) {
      this.currentPage = 1;
      this.searchCustomers();
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
    this.searchCustomers();
  }

  clearFilters(): void {
    this.filters = {
      assignedStaff: '',
      createdDateFrom: '',
      createdDateTo: '',
      updatedDateFrom: '',
      updatedDateTo: ''
    };
    // Trigger reset in staff dropdown
    this.staffDropdownResetTrigger++;
    this.currentPage = 1;
    this.loadCustomers();
  }

  clearFilter(filterType: string): void {
    switch (filterType) {
      case 'assignedStaff':
        this.filters.assignedStaff = '';
        this.staffDropdownResetTrigger++; // Trigger reset for staff dropdown
        break;
      case 'createdDate':
        this.filters.createdDateFrom = '';
        this.filters.createdDateTo = '';
        break;
      case 'updatedDate':
        this.filters.updatedDateFrom = '';
        this.filters.updatedDateTo = '';
        break;
    }
    this.currentPage = 1;
    this.searchCustomers();
  }

  hasActiveFilters(): boolean {
    return !!(
      this.filters.assignedStaff ||
      this.filters.createdDateFrom ||
      this.filters.createdDateTo ||
      this.filters.updatedDateFrom ||
      this.filters.updatedDateTo
    );
  }

  getDateRangeText(type: 'created' | 'updated'): string {
    const fromField = type === 'created' ? 'createdDateFrom' : 'updatedDateFrom';
    const toField = type === 'created' ? 'createdDateTo' : 'updatedDateTo';
    
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

  openCreateModal(): void {
    this.selectedCustomer = null;
    this.isEditMode = false;
    this.showCustomerFormModal = true;
  }

  openEditModal(customer: Customer): void {
    this.selectedCustomer = customer;
    this.isEditMode = true;
    this.showCustomerFormModal = true;
  }

  openDeleteModal(customer: Customer): void {
    this.selectedCustomer = customer;
    this.showDeleteModal = true;
  }

  openImportModal(): void {
    this.selectedFile = null;
    this.showImportModal = true;
  }

  closeModals(): void {
    this.showCustomerFormModal = false;
    this.showDeleteModal = false;
    this.showImportModal = false;
    this.selectedCustomer = null;
    this.selectedFile = null;
    this.isEditMode = false;
  }

  onCustomerFormSubmit(formData: CustomerCreateRequest | CustomerUpdateRequest): void {
    if (this.isEditMode) {
      this.updateCustomer(formData as CustomerUpdateRequest);
    } else {
      this.createCustomer(formData as CustomerCreateRequest);
    }
  }

  createCustomer(formData: CustomerCreateRequest): void {
    this.loading = true;
    this.customerService.create(formData).subscribe({
      next: (response) => {
        this.loading = false;
        
        if (response.status !== 200 && response.status !== 201) {
          this.toastService.error(response.message || 'Failed to create customer');
          return;
        }

        this.toastService.success('Customer created successfully');
        this.loadCustomers();
        this.closeModals();
      },
      error: (error) => {
        this.toastService.error('Failed to create customer');
        this.loading = false;
        console.error('Error creating customer:', error);
      }
    });
  }

  updateCustomer(formData: CustomerUpdateRequest): void {
    if (!this.selectedCustomer?.customerId) {
      return;
    }

    this.loading = true;
    this.customerService.update(this.selectedCustomer.customerId, formData).subscribe({
      next: (response) => {
        this.loading = false;
        
        if (response.status !== 200) {
          this.toastService.error(response.message || 'Failed to update customer');
          return;
        }

        this.toastService.success('Customer updated successfully');
        this.loadCustomers();
        this.closeModals();
      },
      error: (error) => {
        this.toastService.error('Failed to update customer');
        this.loading = false;
        console.error('Error updating customer:', error);
      }
    });
  }

  deleteCustomer(): void {
    if (!this.selectedCustomer?.customerId) {
      return;
    }

    this.loading = true;
    this.customerService.delete(this.selectedCustomer.customerId).subscribe({
      next: (response) => {
        this.loading = false;
        
        if (response.status !== 200) {
          this.toastService.error(response.message || 'Failed to delete customer');
          return;
        }

        this.toastService.success('Customer deleted successfully');
        this.loadCustomers();
        this.closeModals();
      },
      error: (error) => {
        this.toastService.error('Failed to delete customer');
        this.loading = false;
        console.error('Error deleting customer:', error);
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

  importCustomers(): void {
    if (!this.selectedFile) {
      this.toastService.error('Please select a CSV file');
      return;
    }

    this.loading = true;
    this.customerService.import(this.selectedFile).subscribe({
      next: (response) => {
        this.loading = false;
        
        if (response.status !== 200) {
          this.toastService.error(response.message || 'Failed to import customers');
          return;
        }

        this.toastService.success('Customers imported successfully');
        this.loadCustomers();
        this.closeModals();
      },
      error: (error) => {
        this.toastService.error('Failed to import customers');
        this.loading = false;
        console.error('Error importing customers:', error);
      }
    });
  }

  exportCustomers(): void {
    this.loading = true;
    
    const params = {
      page: this.currentPage - 1,
      size: this.pageSize,
      sortBy: this.currentSort,
      sortAsc: this.isCurrentSortAsc,
      ...this.buildSearchParams()
    };

    this.customerService.export(params).subscribe({
      next: (blob) => {
        this.loading = false;
        
        // Create download link
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `customers_${new Date().toISOString().split('T')[0]}.csv`;
        link.click();
        window.URL.revokeObjectURL(url);
        
        this.toastService.success('Customers exported successfully');
      },
      error: (error) => {
        this.toastService.error('Failed to export customers');
        this.loading = false;
        console.error('Error exporting customers:', error);
      }
    });
  }

  onSort(column: string): void {
    if (this.currentSort === column) {
      // Same column - cycle through states: ASC -> DESC -> No Sort
      if (this.isCurrentSortAsc) {
        // Currently ASC, change to DESC
        this.isCurrentSortAsc = false;
      } else {
        // Currently DESC, remove sorting (go to default)
        this.currentSort = 'customerName';
        this.isCurrentSortAsc = true;
      }
    } else {
      // Different column - start with ASC
      this.currentSort = column;
      this.isCurrentSortAsc = true;
    }

    // Reset to first page and reload
    this.currentPage = 1;
    this.loadCustomers();
  }

  getSortState(column: string): 'asc' | 'desc' | 'none' {
    if (this.currentSort !== column) {
      return 'none';
    }
    return this.isCurrentSortAsc ? 'asc' : 'desc';
  }

  getShortPaginationArray(): { type: 'page' | 'ellipsis', value: number }[] {
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

  trackByCustomerId(index: number, customer: Customer): string | undefined {
    return customer.customerId;
  }

  viewCustomerDetail(customer: Customer): void {
    if (customer.customerId) {
      this.router.navigate(['/customers', customer.customerId]);
    }
  }

  onStaffSelected(staffUsername: string): void {
    this.filters.assignedStaff = staffUsername;
    this.onFiltersChange();
  }
}