import { Component, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';
import { ContractService } from './services/contract.service';
import { Contract, ContractCreateRequest, ContractUpdateRequest } from '../../shared/models/contract.model';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ToastService } from '../../core/services/toast.service';
import { AddContractFormComponent } from './components/add-contract-form/add-contract-form.component';
import { StaffDropdownComponent } from '../../shared/components/staff-dropdown/staff-dropdown.component';

@Component({
  selector: 'app-contract',
  imports: [CommonModule, FormsModule, AddContractFormComponent, StaffDropdownComponent],
  standalone: true,
  templateUrl: './contract.component.html'
})
export class ContractComponent implements OnInit {
  private contractService = inject(ContractService);
  private toastService = inject(ToastService);
  private router = inject(Router);

  contracts: Contract[] = [];
  loading = false;
  
  // Pagination
  currentPage = 1;
  pageSize = 10;
  totalItems = 0;
  totalPages = 0;
  currentSort = 'contractName';
  isCurrentSortAsc = true;
  
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

  // Modal states
  showCreateModal = false;
  showEditModal = false;
  showDeleteModal = false;
  showImportModal = false;
  selectedContract: Contract | null = null;

  // File upload
  selectedFile: File | null = null;

  // Add Math property for template usage
  Math = Math;

  ngOnInit(): void {
    
    this.loadContracts();
  }

  loadContracts(): void {
    this.loading = true;
    
    const params = {
      page: this.currentPage - 1,
      size: this.pageSize,
      sortBy: this.currentSort,
      sortAsc: this.isCurrentSortAsc
    };

    // Use search endpoint if we have search query or active filters
    if (this.searchQuery || this.hasActiveFilters()) {
      this.searchContracts();
      return;
    }

    this.contractService.search(params).subscribe({
      next: (response) => {
        this.handleContractsResponse(response);
      },
      error: (error) => {
        this.toastService.error('Failed to load contracts');
        this.loading = false;
        console.error('Error loading contracts:', error);
      }
    });
  }

  searchContracts(): void {
    this.loading = true;
    
    const params = {
      page: this.currentPage - 1,
      size: this.pageSize,
      sortBy: this.currentSort,
      sortAsc: this.isCurrentSortAsc,
      ...this.buildSearchParams()
    };

    this.contractService.search(params).subscribe({
      next: (response) => {
        this.handleContractsResponse(response);
      },
      error: (error) => {
        this.toastService.error('Failed to search contracts');
        this.loading = false;
        console.error('Error searching contracts:', error);
      }
    });
  }

  private handleContractsResponse(response: any): void {
    this.loading = false;
    
    if (response.status !== 200) {
      this.toastService.error(response.message || 'Failed to load contracts');
      this.contracts = [];
      this.totalItems = 0;
      this.totalPages = 0;
      return;
    }

    let data = response.data;
    if (!data) {
      this.toastService.error('No data found');
      this.contracts = [];
      this.totalItems = 0;
      this.totalPages = 0;
      return;
    }

    this.contracts = data.content || [];
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
      return; // Invalid page number
    }
    this.currentPage = page;
    this.loadContracts();
  }

  onSearch(): void {
    this.currentPage = 1;
    this.searchContracts();
  }

  onSearchQueryChange(): void {
    if (!this.searchQuery && !this.hasActiveFilters()) {
      this.loadContracts();
    } else if (this.searchQuery.length >= 3) {
      this.currentPage = 1;
      this.searchContracts();
    }
  }

  toggleFilters(): void {
    this.showFilters = !this.showFilters;
  }

  onFiltersChange(): void {
    // Optional: Auto-apply filters on change
    this.applyFilters();
  }

  applyFilters(): void {
    this.currentPage = 1;
    this.searchContracts();
  }

  clearFilters(): void {
    this.filters = {
      assignedStaff: '',
      createdDateFrom: '',
      createdDateTo: '',
      updatedDateFrom: '',
      updatedDateTo: ''
    };
    this.staffDropdownResetTrigger++; // Trigger reset in dropdown
    this.currentPage = 1;
    this.loadContracts();
  }

  clearFilter(filterType: string): void {
    switch (filterType) {
      case 'assignedStaff':
        this.filters.assignedStaff = '';
        this.staffDropdownResetTrigger++; // Trigger reset in dropdown
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
    this.searchContracts();
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
    this.selectedContract = null;
    this.showCreateModal = true;
  }

  openEditModal(contract: Contract): void {
    this.selectedContract = contract;
    this.showEditModal = true;
  }

  openDeleteModal(contract: Contract): void {
    this.selectedContract = contract;
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
    this.selectedContract = null;
    this.selectedFile = null;
  }

  onFormSubmit(formData: ContractCreateRequest | ContractUpdateRequest): void {
    if (this.showCreateModal) {
      this.createContract(formData as ContractCreateRequest);
    } else if (this.showEditModal) {
      this.updateContract(formData as ContractUpdateRequest);
    }
  }

  createContract(contractForm: ContractCreateRequest): void {
    if (!contractForm.contractName || !contractForm.assignedStaffUsername) {
      return;
    }

    this.loading = true;
    this.contractService.create(contractForm).subscribe({
      next: (response) => {
        this.loading = false;
        
        if (response.status !== 200 && response.status !== 201) {
          this.toastService.error(response.message || 'Failed to create contract');
          return;
        }

        this.toastService.success('Contract created successfully');
        this.loadContracts();
        this.closeModals();
      },
      error: (error) => {
        this.toastService.error('Failed to create contract');
        this.loading = false;
        console.error('Error creating contract:', error);
      }
    });
  }

  updateContract(contractForm: ContractUpdateRequest): void {
    if (!this.selectedContract?.contractId || !contractForm.contractName || !contractForm.assignedStaffUsername) {
      return;
    }

    this.loading = true;
    const updateData: ContractUpdateRequest = {
      contractName: contractForm.contractName,
      assignedStaffUsername: contractForm.assignedStaffUsername
    };

    this.contractService.update(this.selectedContract.contractId, updateData).subscribe({
      next: (response) => {
        this.loading = false;
        
        if (response.status !== 200) {
          this.toastService.error(response.message || 'Failed to update contract');
          return;
        }

        this.toastService.success('Contract updated successfully');
        this.loadContracts();
        this.closeModals();
      },
      error: (error) => {
        this.toastService.error('Failed to update contract');
        this.loading = false;
        console.error('Error updating contract:', error);
      }
    });
  }

  deleteContract(): void {
    if (!this.selectedContract?.contractId) {
      return;
    }

    this.loading = true;
    this.contractService.delete(this.selectedContract.contractId).subscribe({
      next: (response) => {
        this.loading = false;
        
        if (response.status !== 200) {
          this.toastService.error(response.message || 'Failed to delete contract');
          return;
        }

        this.toastService.success('Contract deleted successfully');
        this.loadContracts();
        this.closeModals();
      },
      error: (error) => {
        this.toastService.error('Failed to delete contract');
        this.loading = false;
        console.error('Error deleting contract:', error);
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

  importContracts(): void {
    if (!this.selectedFile) {
      this.toastService.error('Please select a CSV file');
      return;
    }

    this.loading = true;
    this.contractService.import(this.selectedFile).subscribe({
      next: (response) => {
        this.loading = false;
        
        if (response.status !== 200) {
          this.toastService.error(response.message || 'Failed to import contracts');
          return;
        }

        this.toastService.success('Contracts imported successfully');
        this.loadContracts();
        this.closeModals();
      },
      error: (error) => {
        this.toastService.error('Failed to import contracts');
        this.loading = false;
        console.error('Error importing contracts:', error);
      }
    });
  }

  exportContracts(): void {
    this.loading = true;
    
    const params = {
      page: this.currentPage - 1,
      size: this.pageSize,
      sortBy: this.currentSort,
      sortAsc: this.isCurrentSortAsc,
      ...this.buildSearchParams()
    };

    this.contractService.export(params).subscribe({
      next: (blob) => {
        this.loading = false;
        
        // Create download link
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `contracts_${new Date().toISOString().split('T')[0]}.csv`;
        link.click();
        window.URL.revokeObjectURL(url);
        
        this.toastService.success('Contracts exported successfully');
      },
      error: (error) => {
        this.toastService.error('Failed to export contracts');
        this.loading = false;
        console.error('Error exporting contracts:', error);
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
        this.currentSort = 'contractName';
        this.isCurrentSortAsc = true;
      }
    } else {
      // Different column - start with ASC
      this.currentSort = column;
      this.isCurrentSortAsc = true;
    }

    // Reset to first page and reload
    this.currentPage = 1;
    this.loadContracts();
  }

  getSortState(column: string): 'asc' | 'desc' | 'none' {
    if (this.currentSort !== column) {
      return 'none';
    }
    return this.isCurrentSortAsc ? 'asc' : 'desc';
  }

  getPaginationArray(): number[] {
    const pages: number[] = [];
    for (let i = 1; i <= this.totalPages; i++) {
      pages.push(i);
    }
    return pages;
  }

  getShortPaginationArray(): { type: 'page' | 'ellipsis', value: number }[] {
    const items: { type: 'page' | 'ellipsis', value: number }[] = [];
    const maxVisiblePages = 5; // Maximum number of page buttons to show
    const current = this.currentPage;
    const total = this.totalPages;

    if (total <= maxVisiblePages) {
      // Show all pages if total is small
      for (let i = 1; i <= total; i++) {
        items.push({ type: 'page', value: i });
      }
    } else {
      // Always show first page
      items.push({ type: 'page', value: 1 });

      if (current <= 3) {
        // Current page is near the beginning
        for (let i = 2; i <= Math.min(4, total - 1); i++) {
          items.push({ type: 'page', value: i });
        }
        if (total > 4) {
          items.push({ type: 'ellipsis', value: 0 });
        }
      } else if (current >= total - 2) {
        // Current page is near the end
        if (total > 4) {
          items.push({ type: 'ellipsis', value: 0 });
        }
        for (let i = Math.max(total - 3, 2); i <= total - 1; i++) {
          items.push({ type: 'page', value: i });
        }
      } else {
        // Current page is in the middle
        items.push({ type: 'ellipsis', value: 0 });
        for (let i = current - 1; i <= current + 1; i++) {
          items.push({ type: 'page', value: i });
        }
        items.push({ type: 'ellipsis', value: 0 });
      }

      // Always show last page
      if (total > 1) {
        items.push({ type: 'page', value: total });
      }
    }

    return items;
  }

  // Add trackBy function for better performance
  trackByContractId(index: number, contract: Contract): string | undefined {
    return contract.contractId;
  }

  viewContractDetail(contract: Contract): void {
    if (contract.contractId) {
      this.router.navigate(['/contracts', contract.contractId]);
    }
  }

  onStaffSelected(staffUsername: string): void {
    this.filters.assignedStaff = staffUsername;
    this.onFiltersChange();
  }
}
