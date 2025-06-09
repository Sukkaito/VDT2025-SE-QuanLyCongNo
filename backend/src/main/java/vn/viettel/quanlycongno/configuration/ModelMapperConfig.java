package vn.viettel.quanlycongno.configuration;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.PropertyMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.viettel.quanlycongno.dto.ContractDto;
import vn.viettel.quanlycongno.dto.CustomerDto;
import vn.viettel.quanlycongno.dto.InvoiceDto;
import vn.viettel.quanlycongno.entity.Contract;
import vn.viettel.quanlycongno.entity.Customer;
import vn.viettel.quanlycongno.entity.Invoice;
import vn.viettel.quanlycongno.entity.Staff;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class ModelMapperConfig {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STANDARD)
                .setSkipNullEnabled(true);
        modelMapper.addMappings(
                new PropertyMap<Contract, ContractDto>() {
                    @Override
                    protected void configure() {
                        map().setAssignedStaffUsername(source.getAssignedStaff().getUsername());
                        map().setCreatedByUsername(source.getCreatedBy().getUsername());
                        map().setLastUpdatedByUsername(source.getLastUpdatedBy().getUsername());
                    }
                });

        modelMapper.addMappings(
                new PropertyMap<ContractDto, Contract>() {
                    @Override
                    protected void configure() {
                        skip(destination.getAssignedStaff());
                        skip(destination.getCreatedBy());
                        skip(destination.getLastUpdatedBy());
                    }
                });

        modelMapper.addMappings(
                new PropertyMap<Invoice, InvoiceDto>() {
                    @Override
                    protected void configure() {
                        map().setCreatedByUsername(source.getCreatedBy().getUsername());
                        map().setLastUpdatedByUsername(source.getLastUpdatedBy().getUsername());
                        map().setContractId(source.getContract().getContractId());
                    }
                });

        modelMapper.addMappings(
                new PropertyMap<InvoiceDto, Invoice>() {
                    @Override
                    protected void configure() {
                        skip(destination.getContract());
                        skip(destination.getCustomer());
                        skip(destination.getStaff());
                        skip(destination.getCreatedBy());
                        skip(destination.getLastUpdatedBy());

                        skip(destination.getConvertedAmountPreVat());
                        skip(destination.getTotalAmountWithVat());
                    }
                });

        modelMapper.addMappings(
                new PropertyMap<Customer, CustomerDto>() {
                    @Override
                    protected void configure() {
                        skip(destination.getUsedToBeHandledByStaffUsernames());
//                        map().setUsedToBeHandledByStaffUsernames(
//                                source.getUsedToBeHandledByStaffs().stream().map(Staff::getUsername).toList()
//                        );
                        map().setCreatedByUsername(source.getCreatedBy().getUsername());
                        map().setLastUpdatedByUsername(source.getLastUpdatedBy().getUsername());
                    }
                });

        modelMapper.addMappings(
                new PropertyMap<CustomerDto, Customer>() {
                    @Override
                    protected void configure() {
                        skip(destination.getAssignedStaff());
                        skip(destination.getCreatedBy());
                        skip(destination.getLastUpdatedBy());
                        skip(destination.getUsedToBeHandledByStaffs());
                    }
                });
        return modelMapper;
    }
}
