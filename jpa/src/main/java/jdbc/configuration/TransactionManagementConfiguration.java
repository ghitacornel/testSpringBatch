package jdbc.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.transaction.ChainedTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;

@Configuration
public class TransactionManagementConfiguration {

    @Bean
    JpaTransactionManager h2PTM(EntityManagerFactory h2EMFB) {
        return new JpaTransactionManager(h2EMFB);
    }

    @Bean
    JpaTransactionManager hsqlPTM(EntityManagerFactory hsqlEMFB) {
        return new JpaTransactionManager(hsqlEMFB);
    }

    @Bean
    PlatformTransactionManager chainTxManager(JpaTransactionManager h2PTM, JpaTransactionManager hsqlPTM) {
        return new ChainedTransactionManager(h2PTM, hsqlPTM);
    }

}
