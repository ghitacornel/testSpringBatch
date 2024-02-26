package jpa.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.transaction.ChainedTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.persistence.EntityManagerFactory;

@Configuration
class TransactionManagementConfiguration {

    @Bean
    JpaTransactionManager inputTransactionManager(@Qualifier("inputEntityManager") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean
    JpaTransactionManager outputTransactionManager(@Qualifier("outputEntityManager") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Primary
    @Bean
    PlatformTransactionManager transactionManager(@Qualifier("inputTransactionManager") JpaTransactionManager jpaTransactionManager1, @Qualifier("outputTransactionManager") JpaTransactionManager jpaTransactionManager2) {
        return new ChainedTransactionManager(jpaTransactionManager1, jpaTransactionManager2);
    }

}
