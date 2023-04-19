package jdbc.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.transaction.ChainedTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;

@Configuration
public class TransactionManagementConfiguration {

    @Bean
    JpaTransactionManager h2PTM(@Qualifier("h2EMFB") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean
    JpaTransactionManager hsqlPTM(@Qualifier("hsqlEMFB") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean
    PlatformTransactionManager chainTxManager(JpaTransactionManager h2PTM, JpaTransactionManager hsqlPTM) {
        ChainedTransactionManager txManager =
                new ChainedTransactionManager(
                        h2PTM, hsqlPTM
                );
        return txManager;
    }


}
