import { Component, OnInit, OnDestroy, OnChanges, SimpleChanges, Input, Output, EventEmitter, ViewChild, ElementRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, takeUntil, debounceTime, distinctUntilChanged, switchMap, of } from 'rxjs';
import { StaffService } from '../../services/staff.service';
import { Staff } from '../../models/staff.model';

@Component({
  selector: 'app-staff-dropdown',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './staff-dropdown.component.html'
})
export class StaffDropdownComponent implements OnInit, OnDestroy, OnChanges {
  private staffService = inject(StaffService);

  @Input() selectedStaff: string | undefined = '';
  @Input() placeholder = 'Select staff...';
  @Input() required = false;
  @Input() disabled = false;
  @Input() resetSignal: any = null;
  @Output() staffSelected = new EventEmitter<string>();

  @ViewChild('searchInput', { static: false }) searchInput!: ElementRef<HTMLInputElement>;

  staffList: Staff[] = [];
  filteredStaff: Staff[] = [];
  searchQuery = '';
  isDropdownOpen = false;
  loading = false;
  selectedStaffName = '';

  private destroy$ = new Subject<void>();
  private searchSubject = new Subject<string>();

  ngOnInit(): void {
    this.initializeSearch();
    this.loadInitialStaff();
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
            return this.staffService.search({ search: query, page: 0, size: 20 });
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
            this.filteredStaff = response.data.content;
          } else if (this.searchQuery.length < 3) {
            this.filteredStaff = this.staffList.slice(0, 10);
          }
        },
        error: (error) => {
          this.loading = false;
          console.error('Error searching staff:', error);
          this.filteredStaff = [];
        }
      });
  }

  private loadInitialStaff(): void {
    this.loading = true;
    this.staffService.search({ search: '', page: 0, size: 10 })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          this.loading = false;
          if (response?.data?.content) {
            this.staffList = response.data.content;
            this.filteredStaff = [...this.staffList];
            this.setSelectedStaffName();
          }
        },
        error: (error) => {
          this.loading = false;
          console.error('Error loading staff:', error);
        }
      });
  }

  private setSelectedStaffName(): void {
    if (this.selectedStaff) {
      const staff = this.staffList.find(s => s.username === this.selectedStaff);
      if (staff) {
        this.selectedStaffName = staff.username;
      } else {
        this.staffService.getById(this.selectedStaff).subscribe({
          next: (response) => {
            if (response.status === 200 && response.data) {
              this.selectedStaffName = response.data.username;
            } else {
              this.selectedStaffName = this.selectedStaff || '';
            }
          }
        })
      }
    }
  }

  onSearchInput(): void {
    this.searchSubject.next(this.searchQuery);
    
    if (this.searchQuery.length < 3) {
      // Use empty search query for showing initial results
      this.loading = true;
      this.staffService.search({ search: '', page: 0, size: 10 })
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (response) => {
            this.loading = false;
            if (response?.data?.content) {
              this.filteredStaff = response.data.content.slice(0, 10);
            }
          },
          error: (error) => {
            this.loading = false;
            console.error('Error loading staff:', error);
            this.filteredStaff = [];
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
      this.filteredStaff = this.staffList.slice(0, 10);
    }
  }

  selectStaff(staff: Staff): void {
    this.selectedStaff = staff.username;
    this.selectedStaffName = staff.username;
    this.staffSelected.emit(staff.username);
    this.closeDropdown();
  }

  clearSelection(): void {
    this.selectedStaff = '';
    this.selectedStaffName = '';
    this.staffSelected.emit('');
    this.closeDropdown();
  }

  private closeDropdown(): void {
    this.isDropdownOpen = false;
    this.searchQuery = '';
    // Reset to initial staff list with empty search
    this.filteredStaff = this.staffList.slice(0, 10);
  }

  private resetComponent(): void {
    this.selectedStaff = '';
    this.selectedStaffName = '';
    this.searchQuery = '';
    this.isDropdownOpen = false;
    this.filteredStaff = this.staffList.slice(0, 10);
    this.staffSelected.emit('');
  }

  onClickOutside(event: Event): void {
    const target = event.target as HTMLElement;
    if (!target.closest('.staff-dropdown-container')) {
      this.closeDropdown();
    }
  }
}
