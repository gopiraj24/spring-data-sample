package com.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Vernon on 3/6/2017.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class MethodLevelTests {

    @Autowired
    BookRepository bookRepository;;

    @Test
    public void findByTitleContainsDB() throws Exception {
        String queryString = "Spring";
        assertThat(bookRepository.findByTitleContains(queryString))
                .extracting("title", String.class)
                .allSatisfy(title -> assertThat(title).contains(queryString))
                .containsOnly("Spring Microservices", "Pro Spring Boot");
    }

    @Test
    public void  findByPublishedDateAfterDB(){

        LocalDate queryDate = LocalDate.of(2016, 6, 1);
        assertThat(bookRepository.findByPublishedDateAfter(queryDate))
                .extracting("publishedDate", LocalDate.class)
                .allSatisfy(publishedDate -> assertThat(publishedDate).isAfter(queryDate));
    }

    @Test
    public void findByTitleContainsAndPriceCurrencyAndPriceAmountBetweenDB(){
        String queryString = "Spring";
        BigDecimal low =  new BigDecimal(20.00), high = new BigDecimal(80.00);
        List<Book> books = bookRepository.findByTitleContainsAndPriceCurrencyAndPriceAmountBetween(queryString, Money.Currency.USD, low, high);
        assertThat(books).extracting("title", String.class).allSatisfy(title -> assertThat(title).contains(queryString));
        assertThat(books).extracting("price.currency", Money.Currency.class).contains(Money.Currency.USD);
        assertThat(books).extracting("price.amount", BigDecimal.class).allSatisfy(amount -> assertThat(amount).isBetween(low, high));
    }

    @Test
    public void findByTitleContainsAndPublishedDateAfterDB(){

        String queryString = "Spring";
        LocalDate queryDate = LocalDate.of(2016, 6, 1);
        List<Book> books = bookRepository.findByTitleContainsAndPublishedDateAfter(queryString, queryDate);
        assertThat(books).extracting("title", String.class).allSatisfy(title -> assertThat(title).contains(queryString));
        assertThat(books).extracting("publishedDate", LocalDate.class).allSatisfy(publishedDate -> assertThat(publishedDate).isAfter(queryDate));
    }

    @Test
    public void updateBookDB(){

        Iterable<Book> books = bookRepository.findAll();
        assertTrue(books.spliterator().getExactSizeIfKnown() > 0);
        Book book1 = books.iterator().next();
        book1.setDescription("...");
        bookRepository.save(book1);
        assertTrue("...".equals(bookRepository.findOne(book1.getId()).getDescription()));
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
}
