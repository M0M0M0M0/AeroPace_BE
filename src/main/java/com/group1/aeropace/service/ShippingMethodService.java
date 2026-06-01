package com.group1.aeropace.service;

import com.group1.aeropace.dto.shippingmethod.request.ShippingMethodRequest;
import com.group1.aeropace.dto.shippingmethod.request.ShippingMethodUpdateRequest;
import com.group1.aeropace.dto.shippingmethod.response.ShippingMethodResponse;
import com.group1.aeropace.entity.ShippingMethod;
import com.group1.aeropace.repository.ShippingMethodRepository;
import com.group1.aeropace.shared.exception.AppException;
import com.group1.aeropace.shared.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShippingMethodService {
    @Autowired
    private ShippingMethodRepository shippingMethodRepository;

    public List<ShippingMethodResponse> getAllShippingMethod() {
        return shippingMethodRepository.findAll()
                .stream()
                .map(s -> ShippingMethodResponse.builder()
                        .id(s.getId())
                        .name(s.getName())
                        .fee(s.getFee())
                        .status(s.getStatus().name())
                        .build())
                .collect(Collectors.toList());
    }
    public ShippingMethodResponse createShippingMethod(ShippingMethodRequest request){
        ShippingMethod newMethod = new ShippingMethod();
        newMethod.setName(request.getName());
        newMethod.setFee(request.getFee());
        ShippingMethod savedMethod = shippingMethodRepository.save(newMethod);

        ShippingMethodResponse response = new ShippingMethodResponse();
        response.setId(savedMethod.getId());
        response.setName(savedMethod.getName());
        response.setFee(savedMethod.getFee());

        return response;
    }
    public ShippingMethodResponse updateShippingMethod(Long id, ShippingMethodUpdateRequest request){
        ShippingMethod method = shippingMethodRepository.findById(id)
                .orElseThrow( () -> new AppException(ErrorCode.SHIPPING_METHOD_NOT_FOUND));
        method.setName(request.getName());
        method.setFee(request.getFee());
        method.setStatus(request.getStatus());
        ShippingMethod savedMethod = shippingMethodRepository.save(method);

        ShippingMethodResponse response = new ShippingMethodResponse();
        response.setId(savedMethod.getId());
        response.setName(savedMethod.getName());
        response.setFee(savedMethod.getFee());

        return response;
    }

    public void deleteShippingMethod(Long id){
        ShippingMethod method = shippingMethodRepository.findById(id)
                .orElseThrow( () -> new AppException(ErrorCode.SHIPPING_METHOD_NOT_FOUND));
        shippingMethodRepository.delete(method);
    }
}
