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
    JpaTransactionManager h2PTM(@Qualifier("h2EMFB") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean
    JpaTransactionManager hsqlPTM(@Qualifier("hsqlEMFB") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean
    PlatformTransactionManager platformTransactionManager(@Qualifier("h2PTM") JpaTransactionManager jpaTransactionManager1, @Qualifier("hsqlPTM") JpaTransactionManager jpaTransactionManager2) {
        return new ChainedTransactionManager(jpaTransactionManager1, jpaTransactionManager2);
    }

}
