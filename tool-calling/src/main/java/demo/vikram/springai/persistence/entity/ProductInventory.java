package demo.vikram.springai.persistence.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Table;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class ProductInventory {

    @Id
    private Integer id;

    @JsonProperty("name")
    private String productName;

    @JsonProperty("category")
    private String productCategory;

    @JsonProperty("quantityInStock")
    private int quantityInStock;

    private Double unitPrice;
    private String productReleaseYear;
}
