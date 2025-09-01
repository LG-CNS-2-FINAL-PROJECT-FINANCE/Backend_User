package com.ddiring.backend_user.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@FeignClient(name = "product", url = "${product.base-url}", fallback = ProductClient.Fallback.class)
public interface ProductClient {

    @GetMapping("/api/product")
    List<ProductDto> getAllProduct();

    class Fallback implements ProductClient {
        @Override
        public List<ProductDto> getAllProduct() {
            return java.util.Collections.emptyList();
        }
    }
}
