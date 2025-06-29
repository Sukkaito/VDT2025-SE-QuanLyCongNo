import { Component, OnInit, OnDestroy, OnChanges, SimpleChanges, Input, Output, EventEmitter, ViewChild, ElementRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, takeUntil, debounceTime, distinctUntilChanged, switchMap, of } from 'rxjs';
import { CustomerService } from '../../../features/customer/services/customer.service';
import { Customer } from '../../models/customer.model';

@Component({
  selector: 'app-customer-dropdown',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './customer-dropdown.component.html'
})
export class CustomerDropdownComponent implements OnInit, OnDestroy, OnChanges {
  private customerService = inject(CustomerService);

  @Input() selectedCustomer: string = '';
  @Input() placeholder: string = 'Select customer...';
  @Input() required: boolean = false;
  @Input() disabled: boolean = false;
  @Input() resetSignal: any = null;
  @Output() customerSelected = new EventEmitter<string>();

  @ViewChild('searchInput', { static: false }) searchInput!: ElementRef<HTMLInputElement>;

  customerList: Customer[] = [];
  filteredCustomers: Customer[] = [];
  searchQuery = '';
  isDropdownOpen = false;
  loading = false;
  selectedCustomerName = '';

  private destroy$ = new Subject<void>();
  private searchSubject = new Subject<string>();

  ngOnInit(): void {
    this.initializeSearch();
    this.loadInitialCustomers();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['resetSignal'] && !changes['resetSignal'].firstChange) {
      this.resetComponent();
    }
  }

  private initializeSearch(): void {
    this.searchSubject
      .pipe(
        takeUntil(this.destroy$),
        debounceTime(300),
        distinctUntilChanged(),
        switchMap(query => {
          if (query.length >= 3) {
            this.loading = true;
            return this.customerService.search({ search: query, page: 0, size: 20 });
          } else {
            this.loading = false;
            return of(null);
          }
        })
      )
      .subscribe({
        next: (response) => {
          this.loading = false;
          if (response?.data?.content) {
            this.filteredCustomers = response.data.content;
          } else if (this.searchQuery.length < 3) {
            this.filteredCustomers = this.customerList.slice(0, 10);
          }
        },
        error: (error) => {
          this.loading = false;
          console.error('Error searching customers:', error);
          this.filteredCustomers = [];
        }
      });
  }

  private loadInitialCustomers(): void {
    this.loading = true;
    this.customerService.search({ search: '', page: 0, size: 10 })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          this.loading = false;
          if (response?.data?.content) {
            this.customerList = response.data.content;
            this.filteredCustomers = [...this.customerList];
            this.setSelectedCustomerName();
          }
        },
        error: (error) => {
          this.loading = false;
          console.error('Error loading customers:', error);
        }
      });
  }

  private setSelectedCustomerName(): void {
    if (this.selectedCustomer) {
      const customer = this.customerList.find(c => c.customerId === this.selectedCustomer);
      
      if (customer) {
        this.selectedCustomerName = customer.customerName;
      } else {
        this.customerService.getById(this.selectedCustomer).subscribe({
          next: (response) => {
            if (response.status === 200 && response.data) {
              this.selectedCustomerName = response.data.customerName;
            } else {
              this.selectedCustomerName = this.selectedCustomer || '';
            }
          }
        })
      }
    }
  }

  private resetComponent(): void {
    this.selectedCustomer = '';
    this.selectedCustomerName = '';
    this.searchQuery = '';
    this.isDropdownOpen = false;
    this.filteredCustomers = this.customerList.slice(0, 10);
    this.customerSelected.emit('');
  }

  onSearchInput(): void {
    this.searchSubject.next(this.searchQuery);
    
    if (this.searchQuery.length < 3) {
      this.loading = true;
      this.customerService.search({ search: '', page: 0, size: 10 })
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (response) => {
            this.loading = false;
            if (response?.data?.content) {
              this.filteredCustomers = response.data.content.slice(0, 10);
            }
          },
          error: (error) => {
            this.loading = false;
            console.error('Error loading customers:', error);
            this.filteredCustomers = [];
          }
        });
    }
  }

  toggleDropdown(): void {
    if (this.disabled) return;
    
    this.isDropdownOpen = !this.isDropdownOpen;
    
    if (this.isDropdownOpen) {
      setTimeout(() => {
        this.searchInput?.nativeElement?.focus();
      }, 100);
    } else {
      this.searchQuery = '';
      this.filteredCustomers = this.customerList.slice(0, 10);
    }
  }

  selectCustomer(customer: Customer): void {
    this.selectedCustomer = customer.customerId || '';
    this.selectedCustomerName = customer.customerName;
    this.customerSelected.emit(customer.customerId || '');
    this.closeDropdown();
  }

  clearSelection(): void {
    this.selectedCustomer = '';
    this.selectedCustomerName = '';
    this.customerSelected.emit('');
    this.closeDropdown();
  }

  private closeDropdown(): void {
    this.isDropdownOpen = false;
    this.searchQuery = '';
    this.filteredCustomers = this.customerList.slice(0, 10);
  }

  onClickOutside(event: Event): void {
    const target = event.target as HTMLElement;
    if (!target.closest('.customer-dropdown-container')) {
      this.closeDropdown();
    }
  }
}
