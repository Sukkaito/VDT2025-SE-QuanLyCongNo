import { Component, OnInit, OnDestroy, OnChanges, SimpleChanges, Input, Output, EventEmitter, ViewChild, ElementRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, takeUntil, debounceTime, distinctUntilChanged, switchMap, of } from 'rxjs';
import { ContractService } from '../../../features/contract/services/contract.service';
import { Contract } from '../../models/contract.model';

@Component({
  selector: 'app-contract-dropdown',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './contract-dropdown.component.html'
})
export class ContractDropdownComponent implements OnInit, OnDestroy, OnChanges {
  private contractService = inject(ContractService);

  @Input() selectedContract: string = '';
  @Input() placeholder: string = 'Select contract...';
  @Input() required: boolean = false;
  @Input() disabled: boolean = false;
  @Input() resetSignal: any = null;
  @Output() contractSelected = new EventEmitter<string>();

  @ViewChild('searchInput', { static: false }) searchInput!: ElementRef<HTMLInputElement>;

  contractList: Contract[] = [];
  filteredContracts: Contract[] = [];
  searchQuery = '';
  isDropdownOpen = false;
  loading = false;
  selectedContractName = '';

  private destroy$ = new Subject<void>();
  private searchSubject = new Subject<string>();

  ngOnInit(): void {
    this.initializeSearch();
    this.loadInitialContracts();
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
            return this.contractService.search({ search: query, page: 0, size: 20 });
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
            this.filteredContracts = response.data.content;
          } else if (this.searchQuery.length < 3) {
            this.filteredContracts = this.contractList.slice(0, 10);
          }
        },
        error: (error) => {
          this.loading = false;
          console.error('Error searching contracts:', error);
          this.filteredContracts = [];
        }
      });
  }

  private loadInitialContracts(): void {
    this.loading = true;
    this.contractService.search({ search: '', page: 0, size: 10 })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          this.loading = false;
          if (response?.data?.content) {
            this.contractList = response.data.content;
            this.filteredContracts = [...this.contractList];
            this.setSelectedContractName();
          }
        },
        error: (error) => {
          this.loading = false;
          console.error('Error loading contracts:', error);
        }
      });
  }

  private setSelectedContractName(): void {
    if (this.selectedContract) {
      const contract = this.contractList.find(c => c.contractId === this.selectedContract);
      if (contract) {
        this.selectedContractName = contract.contractName;
      } else {
        this.contractService.getById(this.selectedContract).subscribe({
          next: (response) => {
            if (response.status === 200 && response.data) {
              this.selectedContractName = response.data.contractName;
            } else {
              this.selectedContractName = this.selectedContract || '';
            }
          }
        });
      }
    }
  }

  private resetComponent(): void {
    this.selectedContract = '';
    this.selectedContractName = '';
    this.searchQuery = '';
    this.isDropdownOpen = false;
    this.filteredContracts = this.contractList.slice(0, 10);
    this.contractSelected.emit('');
  }

  onSearchInput(): void {
    this.searchSubject.next(this.searchQuery);
    
    if (this.searchQuery.length < 3) {
      this.loading = true;
      this.contractService.search({ search: '', page: 0, size: 10 })
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (response) => {
            this.loading = false;
            if (response?.data?.content) {
              this.filteredContracts = response.data.content.slice(0, 10);
            }
          },
          error: (error) => {
            this.loading = false;
            console.error('Error loading contracts:', error);
            this.filteredContracts = [];
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
      this.filteredContracts = this.contractList.slice(0, 10);
    }
  }

  selectContract(contract: Contract): void {
    this.selectedContract = contract.contractId || '';
    this.selectedContractName = contract.contractName;
    this.contractSelected.emit(contract.contractId || '');
    this.closeDropdown();
  }

  clearSelection(): void {
    this.selectedContract = '';
    this.selectedContractName = '';
    this.contractSelected.emit('');
    this.closeDropdown();
  }

  private closeDropdown(): void {
    this.isDropdownOpen = false;
    this.searchQuery = '';
    this.filteredContracts = this.contractList.slice(0, 10);
  }

  onClickOutside(event: Event): void {
    const target = event.target as HTMLElement;
    if (!target.closest('.contract-dropdown-container')) {
      this.closeDropdown();
    }
  }
}
