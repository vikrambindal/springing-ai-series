package demo.vikram.springai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import demo.vikram.springai.persistence.entity.ProductInventory;
import demo.vikram.springai.persistence.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductFunctionBasedService {

    private final ProductRepository productRepository;

    public record ProductSearch(@ToolParam(description = "Category for which the products must be searched") String category) {}

    @Bean
    @Description("Based on the given category, this provides list of available products belonging to that category")
    public Function<ProductSearch, String> productSearchByCategory(final ProductRepository productRepository) {
        return productSearch -> {
            log.info("Searching for products by category {}", productSearch.category());
            List<ProductInventory> productInventoryList = productRepository.findAllByProductCategory(productSearch.category());
            String productList = null;
            try {
                productList = new ObjectMapper().writeValueAsString(productInventoryList);
            } catch (JsonProcessingException e) {
                log.error("Error occurred processing json", e);
            }
            return productList;
        };
    }

    @Bean
    @Description("Provides list of all the products available")
    public Supplier<String> productListSupplier(final ProductRepository productRepository) {
        return () -> {
            log.info("Getting list of products...");
            List<ProductInventory> productInventoryList = (List<ProductInventory>) productRepository.findAll();
            try {
                return new ObjectMapper().writeValueAsString(productInventoryList);
            } catch (JsonProcessingException e) {
                log.error("Error occurred processing json", e);
            }
            return null;
        };
    }

}
