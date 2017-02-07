package com.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.Arrays;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;


@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class SpringDataSampleApplicationTests {

    public static final MediaType APPLICATION_JSON_UTF8 = new MediaType("application", "hal+json", Charset.forName("utf8"));

	private MockMvc mockMvc;

	@Autowired
	private WebApplicationContext webApplicationContext;

	private HttpMessageConverter mappingJackson2HttpMessageConverter;

	@Autowired
	BookRepository bookRepository;

	@Before
	public void setup() throws Exception {
		this.mockMvc = webAppContextSetup(webApplicationContext).build();
	}

	@Test
	public void findAllBooks() {
		assertThat(bookRepository.findAll())
				.hasSize(2)
				.extracting("title")
				.containsOnly("Spring Microservices", "Pro Spring Boot");
	}

    @Test
    public void findSingleBookRest() throws Exception {
        mockMvc.perform(get("/books/2"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.title", is("Pro Spring Boot")))
                .andExpect(jsonPath("$.description", is("A no-nonsense guide containing case studies and best practise for Spring Boot")));
    }

    @Test
    public void findAllBooksRest() throws Exception {
        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json;charset=UTF-8"))
                .andExpect(jsonPath("$._embedded.books", hasSize(2)))
                .andExpect(jsonPath("$._embedded.books[0].title", is("Spring Microservices")))
                .andExpect(jsonPath("$._embedded.books[1].title", is("Pro Spring Boot")));
    }

	@Test
	public void updateBook(){

		Iterable<Book> books = bookRepository.findAll();
		assertTrue(books.spliterator().getExactSizeIfKnown() > 0);
		Book book1 = books.iterator().next();
		book1.setDescription("...");
		bookRepository.save(book1);
		assertTrue(bookRepository.findOne(book1.getId()).getDescription()  == "...");
	}

//    @Test
    public void createBook(){

        Book book = new Book("aba", ".....", LocalDate.of(1954, 4, 21), new Money(Money.Currency.USD, new BigDecimal(34.95)));
        assertNull(book.getId());
        book =  bookRepository.save(book);
        assertNotNull(book.getId());
        assertTrue(book.getId() > 0);
    }

//    @Test
    public void createBookRest() throws Exception {
        String bookJson = json(new Book("aba", ".....", LocalDate.of(1954, 4, 21), new Money(Money.Currency.USD, new BigDecimal(34.95))));

        this.mockMvc.perform(post("/books")
                .contentType(APPLICATION_JSON_UTF8)
                .content(bookJson))
                .andExpect(status().isCreated());
    }

	protected String json(Object o) throws IOException {
		MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
		this.mappingJackson2HttpMessageConverter.write(
				o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
		return mockHttpOutputMessage.getBodyAsString();
	}
}
