//package vn.viettel.quanlycongno.service;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.modelmapper.ModelMapper;
//import vn.viettel.quanlycongno.dto.ContractDto;
//import vn.viettel.quanlycongno.entity.Contract;
//import vn.viettel.quanlycongno.entity.Staff;
//import vn.viettel.quanlycongno.repository.impl.ContractRepositoryImpl;
//import vn.viettel.quanlycongno.repository.StaffRepository;
//
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class ContractServiceTest {
//
//    @Mock
//    private ContractRepositoryImpl contractRepository;
//
//    @Mock
//    private StaffRepository staffRepository;
//
//    @Mock
//    private AuthenticationService authenticationService;
//
//    @Mock
//    private ModelMapper modelMapper;
//
//    @InjectMocks
//    private ContractService contractService;
//
//    private Staff assignedStaff;
//    private Staff creatorStaff;
//    private Staff updaterStaff;
//    private Contract contract;
//    private ContractDto contractDto;
//    private final String CONTRACT_ID = "contract-123";
//    private final String ASSIGNED_USERNAME = "assigneduser";
//    private final String CREATOR_USERNAME = "creatoruser";
//    private final String UPDATER_USERNAME = "updateruser";
//
//    @BeforeEach
//    void setUp() {
//        // Setup test data with different staff for each role
//        // Note: Assumed staff has been authorized with @PreAuthorize annotations in the controller
//        assignedStaff = new Staff();
//        assignedStaff.setUsername(ASSIGNED_USERNAME);
//
//        creatorStaff = new Staff();
//        creatorStaff.setUsername(CREATOR_USERNAME);
//
//        updaterStaff = new Staff();
//        updaterStaff.setUsername(UPDATER_USERNAME);
//
//        contract = new Contract();
//        contract.setContractId(CONTRACT_ID);
//        contract.setContractName("Test Contract");
//        contract.setCreatedBy(creatorStaff);
//        contract.setLastUpdatedBy(updaterStaff);
//        contract.setAssignedStaff(assignedStaff);
//        contract.setCreatedDate(new Date());
//        contract.setLastUpdateDate(new Date());
//
//        contractDto = new ContractDto();
//        contractDto.setContractId(CONTRACT_ID);
//        contractDto.setContractName("Test Contract");
//        contractDto.setAssignedStaffUsername(ASSIGNED_USERNAME);
//        contractDto.setCreatedByUsername(CREATOR_USERNAME);
//        contractDto.setLastUpdatedByUsername(UPDATER_USERNAME);
//
//        // Mock the model mapper to return the contractDto when mapping from contract
//        lenient().when(modelMapper.map(any(Contract.class), eq(ContractDto.class))).thenReturn(contractDto);
//        lenient().when(modelMapper.map(any(ContractDto.class), eq(Contract.class))).thenReturn(contract);
//    }
//
//    @Test
//    void getAllContracts_shouldReturnAllContracts() {
//        // Arrange
//        List<Contract> contracts = Arrays.asList(contract);
//        when(contractRepository.findAll()).thenReturn(contracts);
//
//        // Act
//        List<ContractDto> result = contractService.getAllContracts();
//
//        // Assert
//        assertEquals(1, result.size());
//        verify(contractRepository).findAll();
//    }
//
//    @Test
//    void getAllContracts_shouldReturnEmptyList_whenNoContractsExist() {
//        // Arrange
//        when(contractRepository.findAll()).thenReturn(Collections.emptyList());
//
//        // Act
//        List<ContractDto> result = contractService.getAllContracts();
//
//        // Assert
//        assertTrue(result.isEmpty());
//        verify(contractRepository).findAll();
//    }
//
//    @Test
//    void getContractById_shouldReturnContract_whenContractExists() {
//        // Arrange
//        when(contractRepository.findById(CONTRACT_ID)).thenReturn(Optional.of(contract));
//
//        // Act
//        ContractDto result = contractService.getContractById(CONTRACT_ID);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(CONTRACT_ID, result.getContractId());
//        assertEquals(ASSIGNED_USERNAME, result.getAssignedStaffUsername());
//        assertEquals(CREATOR_USERNAME, result.getCreatedByUsername());
//        assertEquals(UPDATER_USERNAME, result.getLastUpdatedByUsername());
//
//        verify(contractRepository).findById(CONTRACT_ID);
//    }
//
//    @Test
//    void getContractById_shouldThrowException_whenContractDoesNotExist() {
//        // Arrange
//        when(contractRepository.findById(CONTRACT_ID)).thenReturn(Optional.empty());
//
//        // Act & Assert
//        RuntimeException exception = assertThrows(RuntimeException.class,
//            () -> contractService.getContractById(CONTRACT_ID));
//
//        assertEquals("Contract not found with id: " + CONTRACT_ID, exception.getMessage());
//        verify(contractRepository).findById(CONTRACT_ID);
//    }
//
//    @Test
//    void saveContract_shouldSaveAndReturnContract() {
//        // Arrange
//        when(staffRepository.findByUsername(ASSIGNED_USERNAME)).thenReturn(Optional.of(assignedStaff));
//        when(authenticationService.getCurrentUsername()).thenReturn(ASSIGNED_USERNAME);
//        when(contractRepository.save(any(Contract.class))).thenAnswer(invocation -> {
//            Contract savedContract = invocation.getArgument(0);
//            savedContract.setContractId(CONTRACT_ID);
//            return savedContract;
//        });
//
//        // Act
//        ContractDto result = contractService.saveContract(contractDto);
//
//        // Assert
//        assertNotNull(result);
//        verify(staffRepository, times(2)).findByUsername(anyString());
//        verify(contractRepository).save(any(Contract.class));
//        verify(authenticationService).getCurrentUsername();
//    }
//
//    @Test
//    void saveContract_shouldThrowException_whenAssignedStaffNotFound() {
//        // Arrange
//        when(staffRepository.findByUsername(ASSIGNED_USERNAME)).thenReturn(Optional.empty());
//
//        // Act & Assert
//        RuntimeException exception = assertThrows(RuntimeException.class,
//            () -> contractService.saveContract(contractDto));
//
//        assertEquals("Assigned staff not found", exception.getMessage());
//        verify(staffRepository).findByUsername(ASSIGNED_USERNAME);
//        verify(contractRepository, never()).save(any(Contract.class));
//    }
//
//    @Test
//    void saveContract_shouldThrowException_whenCreatedByStaffNotFound() {
//        // Arrange
//        when(staffRepository.findByUsername(ASSIGNED_USERNAME)).thenReturn(Optional.of(assignedStaff));
//        when(staffRepository.findByUsername(CREATOR_USERNAME)).thenReturn(Optional.empty());
//        when(authenticationService.getCurrentUsername()).thenReturn(CREATOR_USERNAME);
//
//        // Act & Assert
//        RuntimeException exception = assertThrows(RuntimeException.class,
//            () -> contractService.saveContract(contractDto));
//
//        assertEquals("Created/updated by staff not found", exception.getMessage());
//        verify(staffRepository, times(2)).findByUsername(anyString());
//        verify(contractRepository, never()).save(any(Contract.class));
//    }
//
//    @Test
//    void updateContract_shouldUpdateAndReturnContract_whenContractExistsAndUserIsAuthorized() {
//        // Arrange
//        when(contractRepository.existsById(CONTRACT_ID)).thenReturn(true);
//        when(contractRepository.findById(CONTRACT_ID)).thenReturn(Optional.of(contract));
//        when(staffRepository.findByUsername(ASSIGNED_USERNAME)).thenReturn(Optional.of(assignedStaff));
//        when(authenticationService.getCurrentUsername()).thenReturn(ASSIGNED_USERNAME);
//        when(contractRepository.save(any(Contract.class))).thenReturn(contract);
//
//        // Act
//        ContractDto result = contractService.updateContract(contractDto);
//
//        // Assert
//        assertNotNull(result);
//        verify(contractRepository).existsById(CONTRACT_ID);
//        verify(contractRepository).findById(CONTRACT_ID);
//        verify(staffRepository, times(2)).findByUsername(anyString());
//        verify(contractRepository).save(any(Contract.class));
//    }
//
//    @Test
//    void updateContract_shouldThrowException_whenContractDoesNotExist() {
//        // Arrange
//        when(contractRepository.existsById(CONTRACT_ID)).thenReturn(false);
//
//        // Act & Assert
//        RuntimeException exception = assertThrows(RuntimeException.class,
//            () -> contractService.updateContract(contractDto));
//
//        assertEquals("Cannot update non-existent contract", exception.getMessage());
//        verify(contractRepository).existsById(CONTRACT_ID);
//        verify(authenticationService, never()).isAuthorizedContract(anyString());
//        verify(contractRepository, never()).save(any(Contract.class));
//    }
//
//    @Test
//    void updateContract_shouldThrowException_whenContractNotFoundById() {
//        // Arrange
//        when(contractRepository.existsById(CONTRACT_ID)).thenReturn(true);
//        when(contractRepository.findById(CONTRACT_ID)).thenReturn(Optional.empty());
//
//        // Act & Assert
//        RuntimeException exception = assertThrows(RuntimeException.class,
//            () -> contractService.updateContract(contractDto));
//
//        assertEquals("Contract not found with id: " + CONTRACT_ID, exception.getMessage());
//        verify(contractRepository).existsById(CONTRACT_ID);
//        verify(contractRepository).findById(CONTRACT_ID);
//        verify(contractRepository, never()).save(any(Contract.class));
//    }
//
//    @Test
//    void updateContract_shouldThrowException_whenAssignedStaffNotFound() {
//        // Arrange
//        contractDto.setAssignedStaffUsername("nonexistent");
//
//        when(contractRepository.existsById(CONTRACT_ID)).thenReturn(true);
//        when(contractRepository.findById(CONTRACT_ID)).thenReturn(Optional.of(contract));
//        when(staffRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
//
//        // Act & Assert
//        RuntimeException exception = assertThrows(RuntimeException.class,
//            () -> contractService.updateContract(contractDto));
//
//        assertEquals("Assigned staff not found", exception.getMessage());
//        verify(contractRepository).existsById(CONTRACT_ID);
//        verify(contractRepository).findById(CONTRACT_ID);
//        verify(staffRepository).findByUsername("nonexistent");
//        verify(contractRepository, never()).save(any(Contract.class));
//    }
//
//    @Test
//    void updateContract_shouldThrowException_whenLastUpdatedByStaffNotFound() {
//        // Arrange
//        when(contractRepository.existsById(CONTRACT_ID)).thenReturn(true);
//        when(contractRepository.findById(CONTRACT_ID)).thenReturn(Optional.of(contract));
//        when(staffRepository.findByUsername(ASSIGNED_USERNAME)).thenReturn(Optional.of(assignedStaff));
//        when(authenticationService.getCurrentUsername()).thenReturn("nonexistent");
//        when(staffRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
//
//        // Act & Assert
//        RuntimeException exception = assertThrows(RuntimeException.class,
//            () -> contractService.updateContract(contractDto));
//
//        assertEquals("Last updated by staff not found", exception.getMessage());
//        verify(contractRepository).existsById(CONTRACT_ID);
//        verify(contractRepository).findById(CONTRACT_ID);
//        verify(contractRepository, never()).save(any(Contract.class));
//    }
//
//    @Test
//    void updateContract_shouldOnlyUpdateProvidedFields() {
//        // Arrange
//        ContractDto partialDto = new ContractDto();
//        partialDto.setContractId(CONTRACT_ID);
//        partialDto.setAssignedStaffUsername(ASSIGNED_USERNAME);
//        partialDto.setContractName("Updated Contract Name");
//
//        // Create a new contract that will be returned after mapping
//        Contract updatedContract = new Contract();
//        updatedContract.setContractId(CONTRACT_ID);
//        updatedContract.setContractName("Updated Contract Name"); // This is the updated name
//        updatedContract.setAssignedStaff(assignedStaff);
//        // Keep other properties from original contract if they shouldn't change
//        updatedContract.setCreatedBy(creatorStaff);
//        updatedContract.setCreatedDate(contract.getCreatedDate());
//
//        // Mock the model mapper to return the updated contract when mapping from DTO
//        doAnswer(invocation -> {
//            Contract existingContract = invocation.getArgument(1);
//            existingContract.setContractName("Updated Contract Name");
//            existingContract.setAssignedStaff(assignedStaff);
//            return existingContract;
//        }).when(modelMapper).map(eq(partialDto), any(Contract.class));
//
//        doReturn(UPDATER_USERNAME).when(authenticationService).getCurrentUsername();
//        when(staffRepository.findByUsername(ASSIGNED_USERNAME)).thenReturn(Optional.of(assignedStaff));
//        when(staffRepository.findByUsername(UPDATER_USERNAME)).thenReturn(Optional.of(updaterStaff));
//        when(contractRepository.existsById(CONTRACT_ID)).thenReturn(true);
//        when(contractRepository.findById(CONTRACT_ID)).thenReturn(Optional.of(contract));
//        when(contractRepository.save(any(Contract.class))).thenReturn(contract);
//
//        // Act
//        contractService.updateContract(partialDto);
//
//        // Assert
//        ArgumentCaptor<Contract> contractCaptor = ArgumentCaptor.forClass(Contract.class);
//        verify(contractRepository).save(contractCaptor.capture());
//        Contract savedContract = contractCaptor.getValue();
//        assertEquals("Updated Contract Name", savedContract.getContractName());
//    }
//
//    @Test
//    void deleteContract_shouldDeleteContract_whenContractExists() {
//        // Arrange
//        when(contractRepository.existsById(CONTRACT_ID)).thenReturn(true);
//        doNothing().when(contractRepository).deleteById(CONTRACT_ID);
//
//        // Act
//        contractService.deleteContract(CONTRACT_ID);
//
//        // Assert
//        verify(contractRepository).existsById(CONTRACT_ID);
//        verify(contractRepository).deleteById(CONTRACT_ID);
//    }
//
//    @Test
//    void deleteContract_shouldThrowException_whenContractDoesNotExist() {
//        // Arrange
//        when(contractRepository.existsById(CONTRACT_ID)).thenReturn(false);
//
//        // Act & Assert
//        RuntimeException exception = assertThrows(RuntimeException.class,
//            () -> contractService.deleteContract(CONTRACT_ID));
//
//        assertEquals("Contract not found with id: " + CONTRACT_ID, exception.getMessage());
//        verify(contractRepository).existsById(CONTRACT_ID);
//        verify(contractRepository, never()).deleteById(anyString());
//    }
//}
