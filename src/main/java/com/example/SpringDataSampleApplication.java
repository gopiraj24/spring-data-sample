package com.example;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@SpringBootApplication
public class SpringDataSampleApplication {

	@Bean
	CommandLineRunner initData(BookRepository bookRepository){
		return args -> {
			bookRepository.save(new Book("Spring Microservices", "Learn how to efficiently build and implement microservices in Spring,\n" +
					"and how to use Docker and Mesos to push the boundaries. Examine a number of real-world use cases and hands-on code examples.\n" +
					"Distribute your microservices in a completely new way", LocalDate.of(2016, 06, 28), new Money(new BigDecimal(45.83))));
			bookRepository.save(new Book("Pro Spring Boot", "A no-nonsense guide containing case studies and best practise for Spring Boot",
					LocalDate.of(2016, 05, 21 ), new Money(new BigDecimal(42.74))));
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(SpringDataSampleApplication.class, args);
	}
}

@Data
@Entity
@NoArgsConstructor
class Book {

	@Id
	@GeneratedValue
	private Long id;

	@NotBlank
	@Size(max=255)
	private String title;

	@NotBlank
	@Size(max=255)
	private String description;

	@NotNull
	private LocalDate publishedDate;

	@NotNull
	@Embedded
	private Money price;

	Book(String title, String description, LocalDate publishedDate, Money price) {
		this.title = title;
		this.description = description;
		this.publishedDate = publishedDate;
		this.price = price;
	}
}

@Embeddable
@Data
@NoArgsConstructor
class Money {

	enum Currency {CAD, EUR, USD }

	@DecimalMin(value="0",inclusive=false)
	@Digits(integer=1000000000,fraction=2)
	private BigDecimal amount;

	private Currency currency;

	Money(BigDecimal amount){
		this(Currency.USD, amount);
	}

	Money(Currency currency, BigDecimal amount){
		this.currency = currency;
		this.amount = amount;
	}
}

@RepositoryRestResource
interface  BookRepository extends CrudRepository<Book, Long> {

	List<Book> findByTitle(@Param("title") String title);
	List<Book> findByTitleContains(@Param("keyword") String keyword);
	List<Book> findByPublishedDateAfter(@Param("publishedDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate publishedDate);
	List<Book> findByTitleContainsAndPublishedDateAfter(@Param("keyword") String keyword,
														@Param("publishedDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate publishedDate);
	List<Book> findByTitleContainsAndPriceCurrencyAndPriceAmountBetween(@Param("keyword") String keyword,
																		@Param("currency") Money.Currency currency,
																		@Param("low") BigDecimal low,
																		@Param("high") BigDecimal high);
}