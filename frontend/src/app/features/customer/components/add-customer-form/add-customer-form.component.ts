import { Component, EventEmitter, Input, Output, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CustomerCreateRequest, CustomerUpdateRequest, Customer } from '../../../../shared/models/customer.model';
import { StaffDropdownComponent } from '../../../../shared/components/staff-dropdown/staff-dropdown.component';

@Component({
  selector: 'app-add-customer-form',
  imports: [CommonModule, FormsModule, StaffDropdownComponent],
  standalone: true,
  templateUrl: './add-customer-form.component.html'
})
export class AddCustomerFormComponent implements OnChanges {
  @Input() showModal = false;
  @Input() isEditMode = false;
  @Input() loading = false;
  @Input() selectedCustomer: Customer | null = null;
  
  @Output() closeModal = new EventEmitter<void>();
  @Output() submitForm = new EventEmitter<CustomerCreateRequest | CustomerUpdateRequest>();

  customerForm: CustomerCreateRequest = {
    customerName: '',
    taxCode: '',
    abbreviationName: '',
    assignedStaffUsername: ''
  };

  ngOnChanges(): void {
    if (this.showModal) {
      this.initializeForm();
    }
  }

  private initializeForm(): void {
    if (this.isEditMode && this.selectedCustomer) {
      this.customerForm = {
        customerName: this.selectedCustomer.customerName,
        taxCode: this.selectedCustomer.taxCode,
        abbreviationName: this.selectedCustomer.abbreviationName || '',
        assignedStaffUsername: this.selectedCustomer.assignedStaffUsername || ''
      };
    } else {
      this.customerForm = {
        customerName: '',
        taxCode: '',
        abbreviationName: '',
        assignedStaffUsername: ''
      };
    }
  }

  onSubmit(): void {
    if (this.validateForm()) {
      // Clean up form data - remove empty assignedStaffUsername if not provided
      const formData = { ...this.customerForm };
      if (!formData.assignedStaffUsername) {
        delete formData.assignedStaffUsername;
      }
      
      this.submitForm.emit(formData);
    }
  }

  onCancel(): void {
    this.closeModal.emit();
  }

  private validateForm(): boolean {
    return !!(
      this.customerForm.customerName &&
      this.customerForm.taxCode
    );
  }

  onStaffSelected(staffUsername: string): void {
    this.customerForm.assignedStaffUsername = staffUsername;
  }
}
