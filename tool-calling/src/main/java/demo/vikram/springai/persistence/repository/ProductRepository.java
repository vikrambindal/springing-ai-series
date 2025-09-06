package demo.vikram.springai.persistence.repository;

import demo.vikram.springai.persistence.entity.ProductInventory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends CrudRepository<ProductInventory, Integer> {

    List<ProductInventory> findAllByProductCategory(String category);
}
