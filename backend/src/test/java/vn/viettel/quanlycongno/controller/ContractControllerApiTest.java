package vn.viettel.quanlycongno.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import vn.viettel.quanlycongno.dto.ContractDto;
import vn.viettel.quanlycongno.entity.Contract;
import vn.viettel.quanlycongno.entity.Staff;
import vn.viettel.quanlycongno.service.AuthenticationService;
import vn.viettel.quanlycongno.service.ContractService;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ContractController.class)
public class ContractControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    @Autowired
    private ContractService contractService;
    
    @MockitoBean
    @Autowired
    private AuthenticationService authenticationService;

    private ContractDto contractDto1;
    private ContractDto contractDto2;
    private List<ContractDto> contractDtoList;
    private Contract contract;
    private Staff staff;

    @BeforeEach
    void setUp() {
        // Setup test data
        contractDto1 = new ContractDto(
                "contract-1",
                "Test Contract 1",
                new Date(),
                new Date(),
                "admin",
                "admin",
                "staff1"
        );

        contractDto2 = new ContractDto(
                "contract-2",
                "Test Contract 2",
                new Date(),
                new Date(),
                "admin",
                "admin",
                "staff2"
        );

        contractDtoList = Arrays.asList(contractDto1, contractDto2);

        staff = new Staff();
        staff.setId("staff-1");
        staff.setUsername("staff1");

        contract = new Contract();
        contract.setContractId("contract-1");
        contract.setContractName("Test Contract 1");
        contract.setCreatedBy(staff);
        contract.setLastUpdatedBy(staff);
        contract.setAssignedStaff(staff);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllContracts_ShouldReturnAllContractsWithOkStatus_WhenAdmin() throws Exception {
        // Arrange
        when(contractService.getAllContracts()).thenReturn(contractDtoList);

        // Act & Assert
        mockMvc.perform(get("/api/contracts"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].contractId", is("contract-1")))
                .andExpect(jsonPath("$[0].contractName", is("Test Contract 1")))
                .andExpect(jsonPath("$[1].contractId", is("contract-2")))
                .andExpect(jsonPath("$[1].contractName", is("Test Contract 2")));

        verify(contractService, times(1)).getAllContracts();
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void getAllContracts_ShouldReturnAllContractsWithOkStatus_WhenStaff() throws Exception {
        // Arrange
        when(contractService.getAllContracts()).thenReturn(contractDtoList);

        // Act & Assert
        mockMvc.perform(get("/api/contracts"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)));

        verify(contractService, times(1)).getAllContracts();
    }

    @Test
    void getAllContracts_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/contracts"))
                .andExpect(status().isUnauthorized());

        verify(contractService, never()).getAllContracts();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getContractById_ShouldReturnContractWithOkStatus_WhenExists() throws Exception {
        // Arrange
        when(contractService.getContractById("contract-1")).thenReturn(contractDto1);

        // Act & Assert
        mockMvc.perform(get("/api/contracts/contract-1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.contractId", is("contract-1")))
                .andExpect(jsonPath("$.contractName", is("Test Contract 1")))
                .andExpect(jsonPath("$.assignedStaffUsername", is("staff1")));

        verify(contractService, times(1)).getContractById("contract-1");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getContractById_ShouldReturnNotFound_WhenContractDoesNotExist() throws Exception {
        // Arrange
        when(contractService.getContractById("non-existent-id"))
                .thenThrow(new RuntimeException("Contract not found with id: non-existent-id"));

        // Act & Assert
        mockMvc.perform(get("/api/contracts/non-existent-id"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Contract not found with id: non-existent-id"));

        verify(contractService, times(1)).getContractById("non-existent-id");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createContract_ShouldReturnCreatedContract_WhenAdmin() throws Exception {
        // Arrange
        ContractDto newContractDto = new ContractDto(null, "New Contract", null, null, null, null, "staff1");
        when(contractService.saveContract(any(ContractDto.class))).thenReturn(contractDto1);

        // Act & Assert
        mockMvc.perform(post("/api/contracts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newContractDto))
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.contractId", is("contract-1")))
                .andExpect(jsonPath("$.contractName", is("Test Contract 1")));

        verify(contractService, times(1)).saveContract(any(ContractDto.class));
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void createContract_ShouldReturnForbidden_WhenStaff() throws Exception {
        // Arrange
        ContractDto newContractDto = new ContractDto(null, "New Contract", null, null, null, null, "staff1");

        // Act & Assert
        mockMvc.perform(post("/api/contracts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newContractDto)))
                .andExpect(status().isForbidden());

        verify(contractService, never()).saveContract(any(ContractDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createContract_ShouldReturnBadRequest_WhenInvalidData() throws Exception {
        // Arrange
        ContractDto invalidContractDto = new ContractDto(null, "New Contract", null, null, null, null, "non-existent-staff");
        when(contractService.saveContract(any(ContractDto.class)))
                .thenThrow(new RuntimeException("Assigned staff not found"));

        // Act & Assert
        mockMvc.perform(post("/api/contracts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidContractDto))
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Assigned staff not found"));

        verify(contractService, times(1)).saveContract(any(ContractDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateContract_ShouldReturnUpdatedContract_WhenAdmin() throws Exception {
        // Arrange
        ContractDto updatedContractDto = new ContractDto(
                "contract-1",
                "Updated Contract Name",
                new Date(),
                new Date(),
                "admin",
                "admin",
                "staff1"
        );
        when(contractService.updateContract(any(ContractDto.class))).thenReturn(updatedContractDto);

        // Act & Assert
        mockMvc.perform(put("/api/contracts/contract-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedContractDto))
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contractId", is("contract-1")))
                .andExpect(jsonPath("$.contractName", is("Updated Contract Name")));

        verify(contractService, times(1)).updateContract(any(ContractDto.class));
    }

    @Test
    @WithMockUser(username = "staff1", roles = "STAFF")
    void updateContract_ShouldReturnUpdatedContract_WhenStaffIsAuthorized() throws Exception {
        // Arrange
        ContractDto updatedContractDto = new ContractDto(
                "contract-1",
                "Updated Contract Name",
                new Date(),
                new Date(),
                "admin",
                "staff1",
                "staff1"
        );
        when(authenticationService.isAuthorized("contract-1")).thenReturn(true);
        when(contractService.updateContract(any(ContractDto.class))).thenReturn(updatedContractDto);

        // Act & Assert
        mockMvc.perform(put("/api/contracts/contract-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedContractDto))
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contractId", is("contract-1")))
                .andExpect(jsonPath("$.contractName", is("Updated Contract Name")));

        verify(contractService, times(1)).updateContract(any(ContractDto.class));
    }

    @Test
    @WithMockUser(username = "anyStaffNotStaff1", roles = "STAFF")
    void updateContract_ShouldReturnForbidden_WhenStaffIsNotAuthorized() throws Exception {
        // Arrange
        ContractDto updatedContractDto = new ContractDto(
                "contract-1",
                "Updated Contract Name",
                new Date(),
                new Date(),
                "admin",
                "staff2",
                "staff1"
        );
        when(authenticationService.isAuthorized("contract-1")).thenReturn(false);

        // Act & Assert
        mockMvc.perform(put("/api/contracts/contract-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedContractDto)))
                .andExpect(status().isForbidden());

        verify(contractService, never()).updateContract(any(ContractDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteContract_ShouldReturnSuccessMessage_WhenAdmin() throws Exception {
        // Arrange
        doNothing().when(contractService).deleteContract(anyString());

        // Act & Assert
        mockMvc.perform(delete("/api/contracts/contract-1")
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Contract deleted successfully"));

        verify(contractService, times(1)).deleteContract("contract-1");
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void deleteContract_ShouldReturnForbidden_WhenStaff() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/contracts/contract-1"))
                .andExpect(status().isForbidden());

        verify(contractService, never()).deleteContract(anyString());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteContract_ShouldReturnNotFound_WhenContractDoesNotExist() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Contract not found with id: non-existent-id"))
                .when(contractService).deleteContract("non-existent-id");

        // Act & Assert
        mockMvc.perform(delete("/api/contracts/non-existent-id")
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Contract not found with id: non-existent-id"));

        verify(contractService, times(1)).deleteContract("non-existent-id");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllContracts_ShouldReturnInternalServerError_WhenServiceThrowsException() throws Exception {
        // Arrange
        when(contractService.getAllContracts()).thenThrow(new RuntimeException("Database connection error"));

        // Act & Assert
        mockMvc.perform(get("/api/contracts"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Database connection error"));

        verify(contractService, times(1)).getAllContracts();
    }
}
