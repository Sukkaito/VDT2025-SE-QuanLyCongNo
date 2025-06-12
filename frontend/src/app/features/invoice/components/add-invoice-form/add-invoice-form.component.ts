import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CreateInvoiceRequest, UpdateInvoiceRequest, Invoice } from '../../../../shared/models/invoice.model';
import { StaffDropdownComponent } from '../../../../shared/components/staff-dropdown/staff-dropdown.component';
import { CustomerDropdownComponent } from '../../../../shared/components/customer-dropdown/customer-dropdown.component';
import { ContractDropdownComponent } from '../../../../shared/components/contract-dropdown/contract-dropdown.component';

@Component({
  selector: 'app-add-invoice-form',
  imports: [CommonModule, FormsModule, StaffDropdownComponent, CustomerDropdownComponent, ContractDropdownComponent],
  standalone: true,
  templateUrl: './add-invoice-form.component.html'
})
export class AddInvoiceFormComponent {
  @Input() showModal = false;
  @Input() isEditMode = false;
  @Input() loading = false;
  @Input() selectedInvoice: Invoice | null = null;
  @Input() prefilledData: Partial<CreateInvoiceRequest> = {};
  
  @Output() closeModal = new EventEmitter<void>();
  @Output() submitForm = new EventEmitter<CreateInvoiceRequest | UpdateInvoiceRequest>();

  private shouldResetForm = true;

  invoiceForm: CreateInvoiceRequest = {
    invoiceSymbol: '',
    invoiceNumber: '',
    originalAmount: 0,
    currencyType: 'USD',
    exchangeRate: 1,
    vat: 0,
    invoiceDate: new Date(),
    dueDate: new Date(),
    paymentMethod: 'Cash',
    contractId: '',
    customerId: '',
    projectId: '',
    staffUsername: '',
    department: '',
    notes: ''
  };

  ngOnChanges(): void {
    if (this.showModal) {
      this.initializeForm();
    }
  }

  private initializeForm(): void {
    // Don't reset form if we're reopening after a failed submission
    if (!this.shouldResetForm && !this.isEditMode) {
      this.shouldResetForm = true; // Reset flag for next time
      return;
    }

    if (this.isEditMode && this.selectedInvoice) {
      this.invoiceForm = {
        invoiceSymbol: this.selectedInvoice.invoiceSymbol,
        invoiceNumber: this.selectedInvoice.invoiceNumber,
        originalAmount: this.selectedInvoice.originalAmount,
        currencyType: this.selectedInvoice.currencyType,
        exchangeRate: this.selectedInvoice.exchangeRate,
        vat: this.selectedInvoice.vat,
        invoiceDate: this.selectedInvoice.invoiceDate,
        dueDate: this.selectedInvoice.dueDate,
        paymentMethod: this.selectedInvoice.paymentMethod,
        contractId: this.selectedInvoice.contractId,
        customerId: this.selectedInvoice.customerId,
        projectId: this.selectedInvoice.projectId,
        staffUsername: this.selectedInvoice.staffUsername,
        department: this.selectedInvoice.department,
        notes: this.selectedInvoice.notes || ''
      };
    } else {
      const today = new Date();
      const nextWeek = new Date();
      nextWeek.setDate(today.getDate() + 7);

      this.invoiceForm = {
        invoiceSymbol: this.prefilledData.invoiceSymbol || '',
        invoiceNumber: this.prefilledData.invoiceNumber || '',
        originalAmount: this.prefilledData.originalAmount || 0,
        currencyType: this.prefilledData.currencyType || 'USD',
        exchangeRate: this.prefilledData.exchangeRate || 1,
        vat: this.prefilledData.vat || 0,
        invoiceDate: this.prefilledData.invoiceDate || today,
        dueDate: this.prefilledData.dueDate || nextWeek,
        paymentMethod: this.prefilledData.paymentMethod || 'Cash',
        contractId: this.prefilledData.contractId || '',
        customerId: this.prefilledData.customerId || '',
        projectId: this.prefilledData.projectId || '',
        staffUsername: this.prefilledData.staffUsername || '',
        department: this.prefilledData.department || '',
        notes: this.prefilledData.notes || ''
      };
    }
  }

  onSubmit(): void {
    if (this.validateForm()) {
      this.shouldResetForm = false; // Allow reset on next modal open since we're submitting
      this.submitForm.emit(this.invoiceForm);
    }
  }

  onCancel(): void {
    this.shouldResetForm = true; // User explicitly canceled, so reset form
    this.closeModal.emit();
  }

  // Add method to handle failed submissions
  onSubmissionFailed(): void {
    this.shouldResetForm = false; // Don't reset form data on next modal open
  }

  onSubmissionSuccess(): void {
    this.shouldResetForm = true; // Reset form data for next modal open
    this.closeModal.emit();
  }

  private validateForm(): boolean {
    return !!(
      this.invoiceForm.invoiceSymbol &&
      this.invoiceForm.invoiceNumber &&
      this.invoiceForm.originalAmount &&
      this.invoiceForm.currencyType &&
      this.invoiceForm.exchangeRate &&
      this.invoiceForm.vat !== undefined &&
      this.invoiceForm.invoiceDate &&
      this.invoiceForm.dueDate &&
      this.invoiceForm.paymentMethod &&
      this.invoiceForm.contractId &&
      this.invoiceForm.customerId &&
      this.invoiceForm.projectId &&
      this.invoiceForm.staffUsername &&
      this.invoiceForm.department
    );
  }

  onStaffSelected(staffUsername: string): void {
    this.invoiceForm.staffUsername = staffUsername;
  }

  onCustomerSelected(customerId: string): void {
    this.invoiceForm.customerId = customerId;
  }

  onContractSelected(contractId: string): void {
    this.invoiceForm.contractId = contractId;
  }
}
