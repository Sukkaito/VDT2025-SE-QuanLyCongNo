package vn.viettel.quanlycongno.configuration;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.viettel.quanlycongno.dto.ContractDto;
import vn.viettel.quanlycongno.entity.Contract;

@Configuration
public class ModelMapperConfig {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STANDARD)
                .setSkipNullEnabled(true);
        modelMapper.addMappings(
                new org.modelmapper.PropertyMap<Contract, ContractDto>() {
                    @Override
                    protected void configure() {
                        map().setAssignedStaffUsername(source.getAssignedStaff().getUsername());
                        map().setCreatedByUsername(source.getCreatedBy().getUsername());
                        map().setLastUpdatedByUsername(source.getLastUpdatedBy().getUsername());
                    }
                });

        modelMapper.addMappings(
                new org.modelmapper.PropertyMap<ContractDto, Contract>() {
                    @Override
                    protected void configure() {
                        skip(destination.getAssignedStaff());
                        skip(destination.getCreatedBy());
                        skip(destination.getLastUpdatedBy());
                    }
                });
        return modelMapper;
    }
}
