package com.devsuperior.dscommerce.controllersIT;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional //Declara que os testes devem ser 'transaction'. rollback no BD.
public class ProductControllerIT {


}
