# Spring data jpa cursor paging

[![CircleCI](https://circleci.com/gh/HalfWeight/spring-data-jpa-cursor-paging/tree/master.svg?style=shield)](https://circleci.com/gh/HalfWeight/spring-data-jpa-cursor-paging/tree/master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.halfweight/spring-data-jpa-cursor-paging/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/io.github.halfweight/spring-data-jpa-cursor-paging/)
[![License](https://img.shields.io/badge/license-MIT-green)](https://choosealicense.com/licenses/mit/)

The spring-data-jpa-cursor-paging Java library allows to easily implement cursor pagination for Spring Boot projects.
This is a community-based project, not maintained by the Spring Framework Contributors (Pivotal)

# Cursor pagination

### Usage

1. Add the following dependency

   ```xml
   <groupId>io.github.halfweight</groupId>
   <artifactId>spring-data-jpa-cursor-paging</artifactId>
   <version>last-version</version>
   ```

2. Configure the repository base class to be used to create repository proxies for this particular configuration

   ```java
   @EnableJpaRepositories(repositoryBaseClass = CustomRepositoryImpl.class)
   ```

3. Create a jpa repository that extends the CustomRepository

   ```java
   public interface UserRepository extends CustomRepository<User, Long> {

   }
   ```
4. Use the created repository and call `findAllBy` to execute cursor pagination.
   
   The first parameter is a specification to filter for. 
   
   The second parameter is a `CursorPageRequest`. For the first query, you have to fill the _size_ and _sort_ fields.

   From the second query onwards make sure to provide the **continuationToken**

   ```java
    userRepository.findAllBy(null, CursorPageRequest.of(1, Sort.by(Sort.Order.desc("id"))))
   ```

   This query returns a `CursorPaginationSlice` that contains

   | field             | description                                                                          |
   |-------------------|--------------------------------------------------------------------------------------|
   | content           | list of found rows                                                                   |
   | hasNext           | if there are other elements                                                          |
   | continuationToken | token to use to execute the next query. Keep in mind that it changes for every query |
   | size              | size of content                                                                      |


   You can use every field in your entity to sort, but keep in mind that :
   - When you use the continuationToken you have to use always the same sort fields
   - Be sure to use always a unique field as a sorting field for your query as the last parameter (for example the column id)


   
### Customization

To convert values to strings and vice-versa the library uses the `ConvertUtils` from `commons-beanutils`. This is useful
to store information into the *continuationToken*.

If in your entity you are using a not mapped type you would use to sort, you can add a custom converter in this way:

   ```java
ConvertUtils.register(new AbstractConverter() {

    @Override
    protected <T> T convertToType(Class<T> type, Object value) throws Throwable {
        return (T) Instant.ofEpochMilli(Long.parseLong((String) value));
    }

    @Override
    protected String convertToString(Object value) throws Throwable {
        return Long.toString(((Instant)value).toEpochMilli());
    }

    @Override
    protected Class<?> getDefaultType() {
        return Instant.class;
    }
}, Instant.class);
   ```

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

Please make sure to update tests as appropriate.
