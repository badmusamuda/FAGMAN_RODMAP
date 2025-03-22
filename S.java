

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>


<dependency>
    <groupId>org.springframework.retry</groupId>
    <artifactId>spring-retry</artifactId>
</dependency>


<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>


<dependency>
    <groupId>com.sybase.jdbc4</groupId>
    <artifactId>jconn4</artifactId>
    <version>16.0</version>

</dependency>

<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-api</artifactId>
</dependency>

package com.example.db.sybase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;


@Configuration
@EnableTransactionManagement
@EnableRetry
public class SybaseDatabaseConfig {
    
    private static final Logger log = LoggerFactory.getLogger(SybaseDatabaseConfig.class);
    
    @Value("${sybase.url}")
    private String dbUrl;
    
    @Value("${sybase.username}")
    private String dbUsername;
    
    @Value("${sybase.password}")
    private String dbPassword;
    
    @Value("${sybase.driver:com.sybase.jdbc4.jdbc.SybDriver}")
    private String dbDriverClassName;
    
    @Value("${sybase.connection.pool.size:10}")
    private int connectionPoolSize;
    
    @Value("${sybase.connection.timeout:30}")
    private int connectionTimeout;
    
    @Bean
    public DataSource sybaseDataSource() {
        log.info("Initializing Sybase DataSource");
        

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(dbDriverClassName);
        dataSource.setUrl(dbUrl);
        dataSource.setUsername(dbUsername);
        dataSource.setPassword(dbPassword);
        

        try (Connection conn = dataSource.getConnection()) {
            log.info("Successfully connected to Sybase database");
        } catch (SQLException e) {
            log.error("Failed to connect to Sybase database: {}", e.getMessage());
            throw new RuntimeException("Database connection failed", e);
        }
        
        return dataSource;
    }
    
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.setQueryTimeout(connectionTimeout);
        return jdbcTemplate;
    }
}


@Service
public class SybaseDatabaseService {
    
    private static final Logger log = LoggerFactory.getLogger(SybaseDatabaseService.class);
    
    private final JdbcTemplate jdbcTemplate;
    
    @Autowired
    public SybaseDatabaseService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    

    @Retryable(
        value = {SQLException.class, DataAccessException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public <T> List<T> executeQuery(String sql, Object[] params, RowMapper<T> rowMapper) {
        try {
            log.debug("Executing query: {}", sql);
            return jdbcTemplate.query(sql, params, rowMapper);
        } catch (DataAccessException e) {
            log.error("Query execution failed: {}", e.getMessage());
            throw e;
        }
    }
    

    @Retryable(
        value = {SQLException.class, DataAccessException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public <T> T executeComplexQuery(String sql, Object[] params, ResultSetExtractor<T> extractor) {
        try {
            log.debug("Executing complex query: {}", sql);
            return jdbcTemplate.query(sql, params, extractor);
        } catch (DataAccessException e) {
            log.error("Complex query execution failed: {}", e.getMessage());
            throw e;
        }
    }
    
 
    @Retryable(
        value = {SQLException.class, DataAccessException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public int executeUpdate(String sql, Object[] params) {
        try {
            log.debug("Executing update: {}", sql);
            return jdbcTemplate.update(sql, params);
        } catch (DataAccessException e) {
            log.error("Update execution failed: {}", e.getMessage());
            throw e;
        }
    }
    

    @Retryable(
        value = {SQLException.class, DataAccessException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public int[] executeBatchUpdate(List<String> sqlStatements, List<Object[]> batchParams) {
        try {
            log.debug("Executing batch update with {} statements", sqlStatements.size());
            
            int[] results = new int[sqlStatements.size()];
            for (int i = 0; i < sqlStatements.size(); i++) {
                results[i] = jdbcTemplate.update(sqlStatements.get(i), batchParams.get(i));
            }
            return results;
        } catch (DataAccessException e) {
            log.error("Batch update execution failed: {}", e.getMessage());
            throw e;
        }
    }
    

    @Retryable(
        value = {SQLException.class, DataAccessException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Map<String, Object> executeStoredProcedure(String procedureName, Map<String, Object> params) {
        try {
            log.debug("Executing stored procedure: {}", procedureName);
   
            return Collections.emptyMap();
        } catch (DataAccessException e) {
            log.error("Stored procedure execution failed: {}", e.getMessage());
            throw e;
        }
    }
}
