package vn.viettel.quanlycongno.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import vn.viettel.quanlycongno.dto.ContractDto;
import vn.viettel.quanlycongno.entity.Contract;
import vn.viettel.quanlycongno.entity.Invoice;
import vn.viettel.quanlycongno.entity.Staff;
import vn.viettel.quanlycongno.entity.Customer;
import vn.viettel.quanlycongno.service.ContractService;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ContractControllerTest {

    @Mock
    private ContractService contractService;

    @InjectMocks
    private ContractController contractController;

    private ContractDto sampleContractDto;
    private Contract sampleContract;
    private List<ContractDto> contractDtoList;
    private Invoice invoice1, invoice2;
    private Staff staff1, staff2;
    private Customer customer;

    @BeforeEach
    void setUp() {
        // Clear security context
        SecurityContextHolder.clearContext();

        // Set up test data
        sampleContractDto = new ContractDto();
        sampleContractDto.setContractName("Test Contract");
        sampleContractDto.setAssignedStaffUsername("testStaff");

        staff1 = new Staff();
        staff1.setId("staff1");
        staff1.setUsername("staff1Username");

        staff2 = new Staff();
        staff2.setId("staff2");
        staff2.setUsername("staff2Username");

        customer = new Customer();
        customer.setCustomerId("customer1");
        customer.setCustomerName("Test Customer");

        sampleContract = new Contract();
        sampleContract.setContractId("testId");
        sampleContract.setContractName("Test Contract");
        sampleContract.setCreatedBy(staff1);
        sampleContract.setAssignedStaff(staff1);
        sampleContract.setLastUpdatedBy(staff1);

        // Create invoices with different staff members
        invoice1 = createTestInvoice("INV-001", staff1);
        invoice2 = createTestInvoice("INV-002", staff2);

        // Add invoices to contract
        sampleContract.setInvoices(Arrays.asList(invoice1, invoice2));

        contractDtoList = Arrays.asList(sampleContractDto);
    }

    private Invoice createTestInvoice(String symbol, Staff assignedStaff) {
        return new Invoice(
            symbol, // Invoice symbol
            "123456", // Invoice number
            new BigDecimal("1000.00"), // Original amount
            "USD", // Currency type
            new BigDecimal("23000"), // Exchange rate
            new BigDecimal("100.00"), // VAT
            new Date(), // Invoice date
            new Date(System.currentTimeMillis() + 2592000000L), // 30 days in future
            "Bank Transfer", // Payment method
            sampleContract, // Associated contract
            customer, // Associated customer
            "PRJ-001", // Project ID
            assignedStaff, // Staff in charge
            "Finance", // Department
            staff1, // Created by
            staff1 // Last updated by
        );
    }

    private void authenticateAsAdmin() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "admin",
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private void authenticateAsStaff() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "staff",
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_STAFF"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // ... existing tests ...

    @Test
    void shouldReturnAllContractsWhenAuthenticated() {
        // Arrange
        authenticateAsStaff();
        when(contractService.getAllContracts()).thenReturn(contractDtoList);

        // Act
        ResponseEntity<?> response = contractController.getAllContracts();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(contractDtoList, response.getBody());
        verify(contractService, times(1)).getAllContracts();
    }

    @Test
    void shouldNotReturnAllContractsWhenNotAuthenticated() {
        // This test would typically be covered by Spring Security's filter chain
        // and not directly testable in the controller unit test.
        // In a real Spring context test, an unauthenticated request would be rejected before reaching the controller.
        
        // However, we can verify that if somehow the service throws an exception (e.g., due to authentication issues),
        // the controller handles it properly
        when(contractService.getAllContracts()).thenThrow(new AccessDeniedException("Access denied"));

        // Act
        ResponseEntity<?> response = contractController.getAllContracts();

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(contractService, times(1)).getAllContracts();
    }

    @Test
    void shouldReturnOneContractWhenAuthenticated() {
        // Arrange
        authenticateAsStaff();
        when(contractService.getContractById("testId")).thenReturn(sampleContractDto);

        // Act
        ResponseEntity<?> response = contractController.getContractById("testId");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sampleContractDto, response.getBody());
        verify(contractService, times(1)).getContractById("testId");
    }

    @Test
    void shouldNotReturnOneContractWhenNotAuthenticated() {
        // Similar to the above, Spring Security would handle this at the filter level
        // Here we test how the controller handles errors from the service
        when(contractService.getContractById("testId")).thenThrow(new RuntimeException("Contract not found"));

        // Act
        ResponseEntity<?> response = contractController.getContractById("testId");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(contractService, times(1)).getContractById("testId");
    }

    @Test
    void shouldCreateContractWhenIsAdmin() {
        // Arrange
        authenticateAsAdmin();
        when(contractService.saveContract(any(ContractDto.class))).thenReturn(sampleContractDto);

        // Act
        ResponseEntity<?> response = contractController.createContract(sampleContractDto);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(sampleContractDto, response.getBody());
        verify(contractService, times(1)).saveContract(sampleContractDto);
    }

    @Test
    void shouldNotCreateContractWhenIsNotAdminOrNotAuthenticated() {
        // Arrange - test with a staff role trying to create contract (which should be rejected at service level)
        authenticateAsStaff();
        when(contractService.saveContract(any(ContractDto.class)))
                .thenThrow(new AccessDeniedException("Only admin can create contracts"));

        // Act
        ResponseEntity<?> response = contractController.createContract(sampleContractDto);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(contractService, times(1)).saveContract(sampleContractDto);
    }

    @Test
    void shouldUpdateContractWhenIsAuthorized() {
        // Arrange
        authenticateAsAdmin();
        when(contractService.updateContract(any(ContractDto.class))).thenReturn(sampleContractDto);

        // Act
        ResponseEntity<?> response = contractController.updateContract("testId", sampleContractDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sampleContractDto, response.getBody());
        assertEquals("testId", sampleContractDto.getContractId());
        verify(contractService, times(1)).updateContract(sampleContractDto);
    }

    @Test
    void shouldNotUpdateContractWhenIsNotAuthorized() {
        // Arrange
        authenticateAsStaff();
        when(contractService.updateContract(any(ContractDto.class)))
                .thenThrow(new RuntimeException("User is not authorized to update this contract"));

        // Act
        ResponseEntity<?> response = contractController.updateContract("testId", sampleContractDto);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("User is not authorized to update this contract", response.getBody());
        verify(contractService, times(1)).updateContract(sampleContractDto);
    }

    @Test
    void shouldDeleteWhenIsAdmin() {
        // Arrange
        authenticateAsAdmin();
        doNothing().when(contractService).deleteContract(anyString());

        // Act
        ResponseEntity<?> response = contractController.deleteContract("testId");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Contract deleted successfully", response.getBody());
        verify(contractService, times(1)).deleteContract("testId");
    }

    @Test
    void shouldNotDeleteWhenIsNotAdminOrNotAuthenticated() {
        // Arrange
        authenticateAsStaff();
        doThrow(new AccessDeniedException("Only admin can delete contracts"))
                .when(contractService).deleteContract(anyString());

        // Act
        ResponseEntity<?> response = contractController.deleteContract("testId");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(contractService, times(1)).deleteContract("testId");
    }

    @Test
    void shouldHandleExceptionWhenContractNotFoundDuringDelete() {
        // Arrange
        authenticateAsAdmin();
        doThrow(new RuntimeException("Contract not found with id: testId"))
                .when(contractService).deleteContract("testId");

        // Act
        ResponseEntity<?> response = contractController.deleteContract("testId");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Contract not found with id: testId", response.getBody());
        verify(contractService, times(1)).deleteContract("testId");
    }

    @Test
    void shouldHandleExceptionWhenContractNotFoundDuringRetrieval() {
        // Arrange
        authenticateAsStaff();
        when(contractService.getContractById("nonExistentId"))
                .thenThrow(new RuntimeException("Contract not found with id: nonExistentId"));

        // Act
        ResponseEntity<?> response = contractController.getContractById("nonExistentId");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Contract not found with id: nonExistentId", response.getBody());
        verify(contractService, times(1)).getContractById("nonExistentId");
    }

    @Test
    void shouldHandleExceptionDuringContractCreation() {
        // Arrange
        authenticateAsAdmin();
        when(contractService.saveContract(any(ContractDto.class)))
                .thenThrow(new RuntimeException("Error creating contract: missing required fields"));

        // Act
        ResponseEntity<?> response = contractController.createContract(new ContractDto());

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Error creating contract: missing required fields", response.getBody());
        verify(contractService, times(1)).saveContract(any(ContractDto.class));
    }
    
    @Test
    void contractShouldHaveMultipleInvoicesWithDifferentStaff() {
        // Verify that our sample contract has multiple invoices
        assertEquals(2, sampleContract.getInvoices().size());
        
        // Verify that the invoices have different staff members
        Staff staff1 = sampleContract.getInvoices().get(0).getStaff();
        Staff staff2 = sampleContract.getInvoices().get(1).getStaff();
        
        assertNotEquals(staff1.getId(), staff2.getId());
        assertEquals("staff1", staff1.getId());
        assertEquals("staff2", staff2.getId());
    }
}
