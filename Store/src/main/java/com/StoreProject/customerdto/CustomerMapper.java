package com.StoreProject.customerdto;

import com.StoreProject.model.Customer;
import org.springframework.stereotype.Component;

@Component("customerRequestMapper")
public class CustomerMapper {

    public Customer toEntity(CustomerRequestDTO dto) {
        Customer customer = new Customer();
        customer.setName(dto.getName());
        customer.setEmail(dto.getEmail());
        customer.setPassword(dto.getPassword());
        customer.setPhone(dto.getPhone());
        customer.setEndereco(dto.getEndereco());
        customer.setCpf(dto.getCpf());
        customer.setCep(dto.getCep());
        customer.setCity(dto.getCidade());
        customer.setEstado(dto.getEstado());
        return customer;
    }

    public CustomerResponseDTO toResponseDTO(Customer customer) {
        CustomerResponseDTO dto = new CustomerResponseDTO();
        dto.setId(customer.getId());
        dto.setName(customer.getName());
        dto.setUsername(customer.getUsername());
        dto.setEmail(customer.getEmail());
        dto.setPhone(customer.getPhone());
        dto.setEndereco(customer.getEndereco());
        dto.setCpf(customer.getCpf());
        dto.setCep(customer.getCep());
        dto.setCidade(customer.getCity());
        dto.setEstado(customer.getEstado());
        dto.setCreatedIn(customer.getCriadoEm());
        dto.setAtivo(customer.getAtivo());
        return dto;
    }

    public void updateEntityFromDTO(Customer customer, CustomerUpdateDTO dto) {
        if (dto.getName() != null) customer.setName(dto.getName());
        if (dto.getUsername() != null) customer.setUsername(dto.getUsername());
        if (dto.getEmail() != null) customer.setEmail(dto.getEmail());
        if (dto.getPhone() != null) customer.setPhone(dto.getPhone());
        if (dto.getEndereco() != null) customer.setEndereco(dto.getEndereco());
        if (dto.getCpf() != null) customer.setCpf(dto.getCpf());
        if (dto.getCep() != null) customer.setCep(dto.getCep());
        if (dto.getCidade() != null) customer.setCity(dto.getCidade());
        if (dto.getEstado() != null) customer.setEstado(dto.getEstado());
    }
}