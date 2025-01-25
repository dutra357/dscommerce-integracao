package com.devsuperior.dscommerce.controllersIT;

import com.devsuperior.dscommerce.dto.ProductDTO;
import com.devsuperior.dscommerce.entities.Order;
import com.devsuperior.dscommerce.entities.Product;
import com.devsuperior.dscommerce.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dscommerce.tests.OrderFactory;
import com.devsuperior.dscommerce.tests.ProductFactory;
import com.devsuperior.dscommerce.tests.UserFactory;
import com.devsuperior.dscommerce.utils.TokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional //Declara que os testes devem ser 'transaction'. rollback no BD.
public class ProductControllerIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private TokenUtil tokenUtil;

    private String productName, bearerTokenAdmin, bearerTokenClient, invalidToken;
    private Product product;
    private ProductDTO productDTO;
    private Long dependentId, existsId;

    @BeforeEach
    void setUp() throws Exception {
        productName = "MacBook";
        product = ProductFactory.createProduct();
        productDTO = new ProductDTO(product);

        dependentId = 3L;
        existsId = 1L;
    }

    @Test
    public void findAllShouldReturnPageWhenNameParamIsNotEmpty() throws Exception {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .get("/products?size=12&page=0&sort=name&name={productName}", productName)
                .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.content[0].id").value(3));
    }

    @Test
    public void findAllShouldReturnPageWhenNameParamIsEmpty() throws Exception {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .get("/products")
                .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.content[0].id").value(1L));
        resultActions.andExpect(jsonPath("$.content[0].name").value("The Lord of the Rings"));
    }

    @Test
    public void insertShouldReturnProductDtoWhenLoggedAsAdmin() throws Exception {
        bearerTokenAdmin = tokenUtil.obtainAccessToken(mockMvc, "alex@gmail.com","123456");

        String productJson = objectMapper.writeValueAsString(productDTO);

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .post("/products")
                        .header("Authorization", "Bearer " + bearerTokenAdmin)
                        .content(productJson)
                        .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isCreated());
    }

    @Test
    public void insertShouldThrowUnProcessableEntityWhenNameIsInvalid() throws Exception {
        bearerTokenAdmin = tokenUtil.obtainAccessToken(mockMvc, "alex@gmail.com","123456");

        product.setName("");
        productDTO = new ProductDTO(product);
        String productJson = objectMapper.writeValueAsString(productDTO);

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .post("/products")
                .header("Authorization", "Bearer " + bearerTokenAdmin)
                .content(productJson)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void insertShouldThrowUnProcessableEntityWhenDescriptionIsInvalid() throws Exception {
        bearerTokenAdmin = tokenUtil.obtainAccessToken(mockMvc, "alex@gmail.com","123456");

        product.setDescription("as");
        productDTO = new ProductDTO(product);
        String productJson = objectMapper.writeValueAsString(productDTO);

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .post("/products")
                .header("Authorization", "Bearer " + bearerTokenAdmin)
                .content(productJson)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void insertShouldThrowUnProcessableEntityWhenPriceIsZero() throws Exception {
        bearerTokenAdmin = tokenUtil.obtainAccessToken(mockMvc, "alex@gmail.com","123456");

        product.setPrice(0.0);
        productDTO = new ProductDTO(product);
        String productJson = objectMapper.writeValueAsString(productDTO);

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .post("/products")
                .header("Authorization", "Bearer " + bearerTokenAdmin)
                .content(productJson)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void insertShouldThrowUnProcessableEntityWhenPriceIsNegative() throws Exception {
        bearerTokenAdmin = tokenUtil.obtainAccessToken(mockMvc, "alex@gmail.com","123456");

        product.setPrice(-10.0);
        productDTO = new ProductDTO(product);
        String productJson = objectMapper.writeValueAsString(productDTO);

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .post("/products")
                .header("Authorization", "Bearer " + bearerTokenAdmin)
                .content(productJson)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void insertShouldThrowUnProcessableEntityWhenProductDoNotHaveCategory() throws Exception {
        bearerTokenAdmin = tokenUtil.obtainAccessToken(mockMvc, "alex@gmail.com","123456");

        product.getCategories().clear();
        productDTO = new ProductDTO(product);
        String productJson = objectMapper.writeValueAsString(productDTO);

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .post("/products")
                .header("Authorization", "Bearer " + bearerTokenAdmin)
                .content(productJson)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void insertShouldReturnForbiddenWhenLoggedAsClient() throws Exception {
        bearerTokenClient = tokenUtil.obtainAccessToken(mockMvc, "maria@gmail.com","123456");

        String productJson = objectMapper.writeValueAsString(productDTO);

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .post("/products")
                .header("Authorization", "Bearer " + bearerTokenClient)
                .content(productJson)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isForbidden());
    }

    @Test
    public void insertShouldReturnInvalidTokenWhenTokenIsInvalid() throws Exception {
        String productJson = objectMapper.writeValueAsString(productDTO);

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .post("/products")
                .header("Authorization", "Bearer " + invalidToken)
                .content(productJson)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    public void deleteShouldReturnNoContentWhenLoggedAsAdmin() throws Exception {
        bearerTokenAdmin = tokenUtil.obtainAccessToken(mockMvc, "alex@gmail.com","123456");

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .delete("/products/{id}", existsId)
                .header("Authorization", "Bearer " + bearerTokenAdmin)
                .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isNoContent());
    }

    @Test
    public void deleteShouldReturnNotFoundExceptionWhenLoggedAsAdminAndProductDoNotExists() throws Exception {
        bearerTokenAdmin = tokenUtil.obtainAccessToken(mockMvc, "alex@gmail.com","123456");

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .delete("/products/{id}", 9999)
                .header("Authorization", "Bearer " + bearerTokenAdmin)
                .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isNotFound());
    }

    @Test
    @Transactional(propagation = Propagation.SUPPORTS)
    public void deleteShouldThrowBadRequestWhenLoggedAsAdminAndProductIsDependent() throws Exception {
        bearerTokenAdmin = tokenUtil.obtainAccessToken(mockMvc, "alex@gmail.com","123456");

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .delete("/products/{id}", existsId)
                .header("Authorization", "Bearer " + bearerTokenAdmin)
                .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isBadRequest());
    }

    @Test
    public void deleteShouldReturnForbiddenWhenLoggedAsClient() throws Exception {
        bearerTokenClient = tokenUtil.obtainAccessToken(mockMvc, "maria@gmail.com","123456");

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .delete("/products/{id}", existsId)
                .header("Authorization", "Bearer " + bearerTokenClient)
                .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isForbidden());
    }

    @Test
    public void deleteShouldReturnForbiddenWhenNotLogged() throws Exception {

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .delete("/products/{id}", existsId)
                .header("Authorization", "Bearer " + bearerTokenAdmin)
                .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isUnauthorized());
    }

}
