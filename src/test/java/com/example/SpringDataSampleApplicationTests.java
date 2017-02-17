package com.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.jayway.jsonpath.JsonPath;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.repository.query.Param;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


import static org.hamcrest.Matchers.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.halLinks;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class SpringDataSampleApplicationTests {

    public static final MediaType APPLICATION_JSON_UTF8 = new MediaType("application", "hal+json", Charset.forName("utf8"));

	private MockMvc mockMvc;

	@Rule
	public JUnitRestDocumentation restDocument = new JUnitRestDocumentation("target/generated-snippets");

	@Autowired
	private WebApplicationContext webApplicationContext;

	private HttpMessageConverter mappingJackson2HttpMessageConverter;

	@Autowired
	BookRepository bookRepository;

	@Autowired
	private ObjectMapper objectMapper;

	@Before
	public void setup() throws Exception {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)//webAppContextSetup(webApplicationContext).build();
						.apply(documentationConfiguration(restDocument))
						.build();

//		this.document = document("{method-name}");
//		mockMvc = MockMvcBuilders.webAppContextSetup(wac)
//				.apply(documentationConfiguration(this.restDocumentation).uris().withScheme("https")).alwaysDo(this.document)
//				.addFilter(new JwtFilter(),"/*")
//				.build();
//		objectWriter = objectMapper.writer();
//
//		authToken = TestUtil.getAuthToken();
//		TestUtil.setAuthorities();
	}

    @Test
    public void findSingleBookRest() throws Exception {
        mockMvc.perform(get("/books/2"))		// MockMvcRequestBuilders.get
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.title", is("Pro Spring Boot")))
                .andExpect(jsonPath("$.description", is("A no-nonsense guide containing case studies and best practise for Spring Boot")));
    }

	@Test
	public void findSingleBookRest1() throws Exception {
		mockMvc.perform(RestDocumentationRequestBuilders.get("/books/{bookId}", 2))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
				.andExpect(jsonPath("$.title", is("Pro Spring Boot")))
				.andExpect(jsonPath("$.description", is("A no-nonsense guide containing case studies and best practise for Spring Boot")))
				.andDo(document("book-get", preprocessResponse(prettyPrint()),
						links(
								halLinks(),
								linkWithRel("self").description("This book's resource"),
								linkWithRel("book").description("The book's projection")),
						pathParameters(
								parameterWithName("bookId").description("Id of the book")),
						responseFields(
								fieldWithPath("title").description("The book title"),
								fieldWithPath("description").description("The book description"),
								fieldWithPath("publishedDate").description("The book published date"),
								fieldWithPath("price").description("The book price"),
								fieldWithPath("_links").description("<<resource-book-links,links>> to other resources"))));
	}


	@Test
	public void searchBookDB() throws Exception {
		assertThat(bookRepository.findByTitleContains("Spring"))
				.hasSize(2)
				.extracting("title")
				.containsOnly("Spring Microservices", "Pro Spring Boot");
	}

    @Test
	public void searchBookRest() throws Exception {

		mockMvc.perform(RestDocumentationRequestBuilders.get("/books/search/findByTitleContains?keyword={keyword}", "Spring"))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/hal+json;charset=UTF-8"))
				.andExpect(jsonPath("$._embedded.books", hasSize(2)))
				.andExpect(jsonPath("$._embedded.books[0].title", is("Spring Microservices")))
				.andExpect(jsonPath("$._embedded.books[1].title", is("Pro Spring Boot")))
				.andDo(document("book-search-findByTitleContains", preprocessResponse(prettyPrint()),
						links(
								halLinks(),
								linkWithRel("self").description("This book's resource")),
						responseFields(
								fieldWithPath("_embedded.books.[].publishedDate").description("The book's published date"),
								fieldWithPath("_embedded.books.[].price").description("The book's price"),
								fieldWithPath("_embedded.books.[].title").description("The book's title"),
								fieldWithPath("_embedded.books.[].description").description("The book's description"),
								fieldWithPath("_embedded.books.[]._links").description("The book's links to other resource"),
								fieldWithPath("_links").description("The book's links to this search resource"))));
	}

	@Test
	public void  findByPublishedDateAfterDB(){

		assertThat(bookRepository.findByPublishedDateAfter(LocalDate.of(2016, 6, 1))).hasSize(1);
	}

	@Test
	public void  findByPublishedDateAfterRest() throws Exception{

		mockMvc.perform(RestDocumentationRequestBuilders.get("/books/search/findByPublishedDateAfter?publishedDate={date}", "2016-06-01"))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/hal+json;charset=UTF-8"))
				.andExpect(jsonPath("$._embedded.books", hasSize(1)))
				.andExpect(jsonPath("$._embedded.books[0].publishedDate", greaterThan("2016-06-01")))
				.andDo(document("book-search-findByPublishedDateAfter", preprocessResponse(prettyPrint()),
						links(
								halLinks(),
								linkWithRel("self").description("This book's resource")),
						responseFields(
								fieldWithPath("_embedded.books.[].publishedDate").description("The book's published date"),
								fieldWithPath("_embedded.books.[].price").description("The book's price"),
								fieldWithPath("_embedded.books.[].title").description("The book's title"),
								fieldWithPath("_embedded.books.[].description").description("The book's description"),
								fieldWithPath("_embedded.books.[]._links").description("The book's links to other resource"),
								fieldWithPath("_links").description("The book's links to this search resource"))));
	}

	@Test
	public void findByTitleContainsAndPriceCurrencyAndPriceAmountBetweenDB(){
		assertThat(bookRepository.findByTitleContainsAndPriceCurrencyAndPriceAmountBetween("Spring", Money.Currency.USD, new BigDecimal(20.00), new BigDecimal(80.00)))
			.hasSize(2)
			.extracting("title")
			.containsOnly("Spring Microservices", "Pro Spring Boot");
	}

	@Test
	public void findByTitleContainsAndPriceCurrencyAndPriceAmountBetweenRest() throws Exception {
		mockMvc.perform(RestDocumentationRequestBuilders.get(
				"/books/search/findByTitleContainsAndPriceCurrencyAndPriceAmountBetween?keyword={keyword}&currency={currency}&low={low}&high={high}", "Spring", "USD", 20.00, 80.00))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/hal+json;charset=UTF-8"))
				.andExpect(jsonPath("$._embedded.books", hasSize(2)))
				.andExpect(jsonPath("$._embedded.books[0].title", containsString("Spring")))
				.andExpect(jsonPath("$._embedded.books[1].title", containsString("Spring")))
				.andExpect(jsonPath("$._embedded.books[0].price.currency", is("USD")))
				.andExpect(jsonPath("$._embedded.books[1].price.currency", is("USD")))
				.andExpect(jsonPath("$._embedded.books[0].price.amount", greaterThan(20.00)))
				.andExpect(jsonPath("$._embedded.books[0].price.amount", lessThan(80.00)))
				.andExpect(jsonPath("$._embedded.books[1].price.amount", greaterThan(20.00)))
				.andExpect(jsonPath("$._embedded.books[1].price.amount", lessThan(80.00)))
				.andDo(document("book-search-findByTitleContainsAndPriceCurrencyAndPriceAmountBetween", preprocessResponse(prettyPrint()),
						links(
								halLinks(),
								linkWithRel("self").description("This book's resource")),
						responseFields(
								fieldWithPath("_embedded.books.[].publishedDate").description("The book's published date"),
								fieldWithPath("_embedded.books.[].price").description("The book's price"),
								fieldWithPath("_embedded.books.[].title").description("The book's title"),
								fieldWithPath("_embedded.books.[].description").description("The book's description"),
								fieldWithPath("_embedded.books.[]._links").description("The book's links to other resource"),
								fieldWithPath("_links").description("The book's links to this search resource"))));
	}

	@Test
	public void findByTitleContainsAndPublishedDateAfterDB(){

		assertThat(bookRepository.findByTitleContainsAndPublishedDateAfter("Spring", LocalDate.of(2016, 6, 1)))
				.hasSize(1)
				.extracting("title")
				.containsSequence("Spring Microservices");
	}

	@Test
	public void findByTitleContainsAndPublishedDateAfterRest() throws Exception {
		mockMvc.perform(RestDocumentationRequestBuilders.get(
				"/books/search/findByTitleContainsAndPublishedDateAfter?keyword={keyword}&publishedDate={publishedDate}", "Spring", "2016-06-01"))
				.andExpect(status().isOk())
				.andExpect(content().contentType("application/hal+json;charset=UTF-8"))
				.andExpect(jsonPath("$._embedded.books", hasSize(1)))
				.andExpect(jsonPath("$._embedded.books[0].title", containsString("Spring")))
				.andExpect(jsonPath("$._embedded.books[0].publishedDate", greaterThan("2016-06-01")))
				.andDo(document("book-search-findByTitleContainsAndPublishedDateAfter", preprocessResponse(prettyPrint()),
						links(
								halLinks(),
								linkWithRel("self").description("This book's resource")),
						responseFields(
								fieldWithPath("_embedded.books.[].publishedDate").description("The book's published date"),
								fieldWithPath("_embedded.books.[].price").description("The book's price"),
								fieldWithPath("_embedded.books.[].title").description("The book's title"),
								fieldWithPath("_embedded.books.[].description").description("The book's description"),
								fieldWithPath("_embedded.books.[]._links").description("The book's links to other resource"),
								fieldWithPath("_links").description("The book's links to this search resource"))));
	}

	@Test
	public void updateBookDB(){

		Iterable<Book> books = bookRepository.findAll();
		assertTrue(books.spliterator().getExactSizeIfKnown() > 0);
		Book book1 = books.iterator().next();
		book1.setDescription("...");
		bookRepository.save(book1);
		assertTrue(bookRepository.findOne(book1.getId()).getDescription()  == "...");
	}

	@Test
	public void updateBookRest() throws Exception {

		int bookId = 1;
		MvcResult resul = mockMvc.perform(get("/books/{bookdId}", bookId))
				.andExpect(status().isOk())
				.andReturn();
		String contentString = resul.getResponse().getContentAsString();
		String tit = JsonPath.read(contentString, "$.title");
		String pd = JsonPath.read(contentString, "$.publishedDate");
		String currency = JsonPath.read(contentString, "$.price.currency");
		double amount = JsonPath.read(contentString, "$.price.amount");

		String newDescr = "...";
		Book book = new Book(tit, newDescr, LocalDate.parse(pd), new Money(Money.Currency.valueOf(currency), new BigDecimal(amount)));

		this.mockMvc.perform(patch("/books/{bookId}", bookId)
				.contentType(APPLICATION_JSON_UTF8)
				.content(objectMapper.writeValueAsString(book)))
				.andExpect(status().is2xxSuccessful());

	}

    @Test
    public void createBookDB(){

        Book book = new Book("Building Microservices",
				"Microservices are small, autonomous services that work together. Let’s break that definition down a bit and consider the characteristics that make microservices different.",
							LocalDate.of(2015, 2, 20), new Money(Money.Currency.USD, new BigDecimal(44.59)));
        assertNull(book.getId());
        book =  bookRepository.save(book);
        assertNotNull(book.getId());
        assertTrue(book.getId() > 0);
    }

//    @Test
    public void createBookRest() throws Exception {

        Book book = new Book("Microservice Architecture: Aligning Principles, Practices, and Culture",
				"This practical guide covers the entire microservices landscape, including the principles, technologies, and methodologies of this unique, modular style of system building.",
							LocalDate.of(2015, 8, 5), new Money(Money.Currency.USD, new BigDecimal(35.74)));
		this.mockMvc.perform(post("/books")
				.contentType(APPLICATION_JSON_UTF8)
				.content(objectMapper.writeValueAsString(book)))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", notNullValue()))
				.andDo(document("book-post", preprocessResponse(prettyPrint()),
						links(
								halLinks(),
								linkWithRel("self").description("This book's resource"),
								linkWithRel("book").description("The book's projection")),
						responseFields(
								fieldWithPath("title").description("The book title"),
								fieldWithPath("description").description("The book description"),
								fieldWithPath("publishedDate").description("The book published date"),
								fieldWithPath("price").description("The book price"),
								fieldWithPath("_links").description("<<resource-book-links,links>> to other resources")

						)
				)
		)
		;
    }

}
