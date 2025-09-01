package com.ddiring.backend_user.api;

import java.util.Collections;
import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product", url = "${product.base-url}", fallback = ProductClient.Fallback.class)
public interface ProductClient {

    @GetMapping("/product/{userSeq}")
    List<ProductDto> getUserProducts(@PathVariable("userSeq") String userSeq);

    class Fallback implements ProductClient {
        @Override
        public List<ProductDto> getUserProducts(String userSeq) {
            return Collections.emptyList();
        }
    }
}
