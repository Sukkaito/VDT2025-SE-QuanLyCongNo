import { Component, EventEmitter, Input, Output, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ContractCreateRequest, ContractUpdateRequest, Contract } from '../../../../shared/models/contract.model';
import { StaffDropdownComponent } from '../../../../shared/components/staff-dropdown/staff-dropdown.component';

@Component({
  selector: 'app-add-contract-form',
  imports: [CommonModule, FormsModule, StaffDropdownComponent],
  standalone: true,
  templateUrl: './add-contract-form.component.html'
})
export class AddContractFormComponent implements OnInit {
  @Input() showModal = false;
  @Input() isEditMode = false;
  @Input() loading = false;
  @Input() selectedContract: Contract | null = null;
  
  @Output() closeModal = new EventEmitter<void>();
  @Output() submitForm = new EventEmitter<ContractCreateRequest | ContractUpdateRequest>();

  contractForm: ContractCreateRequest = {
    contractName: '',
    assignedStaffUsername: ''
  };

  ngOnInit(): void {
    this.resetForm();
  }

  ngOnChanges(): void {
    if (this.isEditMode && this.selectedContract) {
      this.contractForm = {
        contractName: this.selectedContract.contractName,
        assignedStaffUsername: this.selectedContract.assignedStaffUsername
      };
    } else if (!this.isEditMode) {
      this.resetForm();
    }
  }

  resetForm(): void {
    this.contractForm = {
      contractName: '',
      assignedStaffUsername: ''
    };
  }

  onSubmit(): void {
    if (!this.contractForm.contractName || !this.contractForm.assignedStaffUsername) {
      return;
    }
    this.submitForm.emit(this.contractForm);
  }

  onClose(): void {
    this.resetForm();
    this.closeModal.emit();
  }

  onStaffSelected(staffUsername: string): void {
    this.contractForm.assignedStaffUsername = staffUsername;
  }
}
