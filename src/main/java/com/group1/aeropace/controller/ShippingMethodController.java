package com.group1.aeropace.controller;

import com.group1.aeropace.dto.shippingmethod.request.ShippingMethodRequest;
import com.group1.aeropace.dto.shippingmethod.request.ShippingMethodUpdateRequest;
import com.group1.aeropace.dto.shippingmethod.response.ShippingMethodResponse;
import com.group1.aeropace.service.ShippingMethodService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/shipping-methods")
public class ShippingMethodController {
    @Autowired
    private ShippingMethodService shippingMethodService;

    @PostMapping
    public ShippingMethodResponse createShippingMethod(@RequestBody @Valid ShippingMethodRequest request){
        return shippingMethodService.createShippingMethod(request);
    }

    @GetMapping
    public List<ShippingMethodResponse> getAllShippingMethod(){
        return shippingMethodService.getAllShippingMethod();
    }
    @PutMapping("/{id}")
    public ShippingMethodResponse updateShippingMethod(@PathVariable Long id,@RequestBody @Valid ShippingMethodUpdateRequest request){
        return shippingMethodService.updateShippingMethod(id,request);
    }
    @DeleteMapping("/{id}")
    public String deleteShippingMethod(@PathVariable Long id){
        shippingMethodService.deleteShippingMethod(id);
        return "Delete shipping method successfully.";
    }
}
