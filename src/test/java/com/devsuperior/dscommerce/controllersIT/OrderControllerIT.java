package com.devsuperior.dscommerce.controllersIT;

import com.devsuperior.dscommerce.dto.ProductDTO;
import com.devsuperior.dscommerce.entities.Product;
import com.devsuperior.dscommerce.tests.ProductFactory;
import com.devsuperior.dscommerce.utils.TokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class OrderControllerIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TokenUtil tokenUtil;

    private String bearerTokenAdmin;
    private String bearerTokenClient;
    private String invalidToken;
    private Long existsId, notExistingId;

    @BeforeEach
    void setUp() throws Exception {
        String productName = "MacBook";
        Product product = ProductFactory.createProduct();
        ProductDTO productDTO = new ProductDTO(product);

        notExistingId  = 999L;
        existsId = 1L;
    }

    @Test
    public void findByIdShouldReturnOrderWhenLoggedAsAdmin() throws Exception {
        bearerTokenAdmin = tokenUtil.obtainAccessToken(mockMvc, "alex@gmail.com","123456");

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .get("/orders/{id}", existsId)
                .header("Authorization", "Bearer " + bearerTokenAdmin)
                .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.id").value(1));
        resultActions.andExpect(jsonPath("$.total").value(1431.0));
        resultActions.andExpect(jsonPath("$.items.[0].name").value("The Lord of the Rings"));
        resultActions.andExpect(jsonPath("$.payment.moment").value("2022-07-25T15:00:00Z"));
    }

    @Test
    public void findByIdShouldReturnOrderWhenLoggedAsClientAndOrderIsYours() throws Exception {
        bearerTokenClient = tokenUtil.obtainAccessToken(mockMvc, "maria@gmail.com","123456");

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .get("/orders/{id}", 1)
                .header("Authorization", "Bearer " + bearerTokenClient)
                .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.id").value(1));
        resultActions.andExpect(jsonPath("$.client.name").value("Maria Brown"));
        resultActions.andExpect(jsonPath("$.items[0].name").value("The Lord of the Rings"));
    }

    @Test
    public void findByIdShouldReturnForbiddenWhenLoggedAsClientAndOrderIsNotBelongsUser() throws Exception {
        bearerTokenClient = tokenUtil.obtainAccessToken(mockMvc, "maria@gmail.com","123456");

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .get("/orders/{id}", 2)
                .header("Authorization", "Bearer " + bearerTokenClient)
                .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isForbidden());
    }

    @Test
    public void findByIdShouldReturnResourceNotFoundWhenLoggedAsAdminAndOrderDoNotExists() throws Exception {
        bearerTokenAdmin = tokenUtil.obtainAccessToken(mockMvc, "alex@gmail.com","123456");

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .get("/orders/{id}", notExistingId)
                .header("Authorization", "Bearer " + bearerTokenAdmin)
                .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isNotFound());
    }

    @Test
    public void findByIdShouldReturnResourceNotFoundWhenLoggedAsClientAndOrderDoNotExists() throws Exception {
        bearerTokenClient = tokenUtil.obtainAccessToken(mockMvc, "maria@gmail.com","123456");

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .get("/orders/{id}", notExistingId)
                .header("Authorization", "Bearer " + bearerTokenClient)
                .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isNotFound());
    }

    @Test
    public void findByIdShouldReturnUnauthorizedWhenNotLogged() throws Exception {

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .get("/orders/{id}", notExistingId)
                .header("Authorization", "Bearer " + bearerTokenClient)
                .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isUnauthorized());
    }
}
