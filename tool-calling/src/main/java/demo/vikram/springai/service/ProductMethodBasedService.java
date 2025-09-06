package demo.vikram.springai.service;

import demo.vikram.springai.persistence.entity.ProductInventory;
import demo.vikram.springai.persistence.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProductMethodBasedService {

    private final ProductRepository productRepository;

    @Tool(description = "Retrieves list containing information about all available products")
    public List<ProductInventory> getProductList() {
        return (List<ProductInventory>) productRepository.findAll();
    }

    @Tool(description = "Retrieves product information for a specified category")
    public List<ProductInventory> getProductByCategory(@ToolParam(description = "Category for which product must be fetched") final String category) {

        log.info("Product category {}", category);
        return productRepository.findAllByProductCategory(category);
    }
}
