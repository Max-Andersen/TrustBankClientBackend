package com.trb_client.backend

import com.trb_client.backend.data.HeaderServerInterceptor
import com.trb_client.backend.domain.*
import com.trb_client.backend.kafka.produce.TransactionInitProducer
import com.trb_client.backend.services.AccountOperationService
import com.trb_client.backend.services.LoanOperationService
import com.trb_client.backend.services.MobileAppService
import com.trb_client.backend.services.UserOperationService
import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.ServerInterceptors
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.web.reactive.function.client.WebClient
import javax.annotation.PostConstruct

@Configuration
@EnableWebSecurity
@EntityScan(basePackages = ["com.trb_client.backend.data.models"])
//@PropertySource("classpath:application.properties")
class SecurityConfig: ApplicationContextAware {

    @Value("\${core_url_spring}")
    private lateinit var core_url_spring: String

    @Value("\${users_url_spring}")
    private lateinit var users_url_spring: String

    @Value("\${loan_url_spring}")
    private lateinit var loan_url_spring: String

    @Value("\${prefs_url_spring}")
    private lateinit var prefs_url_spring: String


    private lateinit var context: ApplicationContext

//    @Bean
//    fun dataSource(): DataSource {
//        val dataSource = DriverManagerDataSource()
//        dataSource.setDriverClassName("org.postgresql.Driver")
//        dataSource.url = "jdbc:postgresql://localhost:5432/postgres"
//        dataSource.username = "qwe"
//        dataSource.password = "qwe"
//        return dataSource
//    }
//
//    @Bean
//    fun entityManagerFactory(dataSource: DataSource): LocalContainerEntityManagerFactoryBean {
//        val emf = LocalContainerEntityManagerFactoryBean()
//        emf.dataSource = dataSource
//        emf.setPackagesToScan("com.trb_client.backend.data")
//        emf.persistenceUnitName = "YourPersistenceUnitName"
//        emf.jpaVendorAdapter = HibernateJpaVendorAdapter()
//        emf.setPersistenceProviderClass(HibernatePersistenceProvider::class.java)
//
//        val properties = Properties()
//        properties["hibernate.dialect"] = "org.hibernate.dialect.PostgreSQLDialect"
//        properties["hibernate.hbm2ddl.auto"] = "update"
//        properties["hibernate.show_sql"] = "true"
//
//        emf.setJpaProperties(properties)
//        return emf
//    }

//    @Bean
//    fun entityManager(entityManagerFactory: EntityManagerFactory): EntityManager {
//        return entityManagerFactory.createEntityManager()
//    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        context = applicationContext
    }

    @Bean
    fun webClientBuilder(): WebClient.Builder {
        return WebClient.builder()
    }

    @Bean("coreWebClient")
    fun webCoreClient(builder: WebClient.Builder): WebClient {
        return builder.baseUrl(core_url_spring).build()
    }

    @Bean("loanWebClient")
    fun webLoanClient(builder: WebClient.Builder): WebClient {
        return builder.baseUrl(loan_url_spring).build()
    }

    @Bean("usersWebClient")
    fun webUsersClient(builder: WebClient.Builder): WebClient {
        return builder.baseUrl(users_url_spring).build()
    }
    @Bean("prefsWebClient")
    fun webPrefsClient(builder: WebClient.Builder): WebClient {
        return builder.baseUrl(prefs_url_spring).build()
    }

    @Bean
    fun coreRequest(): CoreRepository {
        val webClient = context.getBean("coreWebClient", WebClient::class.java)
        val producer = context.getBean(TransactionInitProducer::class.java)
        return CoreRepository(webClient, producer)
    }

    @Bean
    fun userRequest(): UserRepository {
        val webClient = context.getBean("usersWebClient", WebClient::class.java)
        return UserRepository(webClient)
    }

    @Bean
    fun loanRequest(): LoanRepository {
        val webClient = context.getBean("loanWebClient", WebClient::class.java)
        return LoanRepository(webClient)
    }

    @Bean
    fun themeRequest(): ThemeRepository {
        val webClient = context.getBean("prefsWebClient", WebClient::class.java)
        return ThemeRepository(webClient)
    }

    @Bean
    fun hiddenRequest(): HiddenAccountRepository {
        val webClient = context.getBean("prefsWebClient", WebClient::class.java)
        return HiddenAccountRepository(webClient)
    }


    @Bean
    fun server(): AccountOperationsServer {
        val accountOperationService = context.getBean(AccountOperationService::class.java)
        val userOperationService = context.getBean(UserOperationService::class.java)
        val loanOperationService = context.getBean(LoanOperationService::class.java)
        val mobileAccountService = context.getBean(MobileAppService::class.java)
        return AccountOperationsServer(
            50051,
            accountOperationService,
            userOperationService,
            loanOperationService,
            mobileAccountService
        )
    }

    @Bean
    fun grpcServer(): String {
        val server = context.getBean(AccountOperationsServer::class.java)// AccountOperationsServer(50051)
        server.start()
        server.blockUntilShutdown()
        return ""
    }

//    @Bean
//    fun themeRepository(): ThemeRepository{
//        val entityManager = context.getBean(EntityManager::class.java)
//        val jpaRepositoryFactory = JpaRepositoryFactory(entityManager)
//        return jpaRepositoryFactory.getRepository(ThemeRepository::class.java)
//    }
//    @Bean
//    fun hidedAccountsRepository(): HidedAccountRepository{
//        val entityManager = context.getBean(EntityManager::class.java)
//        val jpaRepositoryFactory = JpaRepositoryFactory(entityManager)
//        return jpaRepositoryFactory.getRepository(HidedAccountRepository::class.java)
//    }

}

class AccountOperationsServer(
    private val port: Int,
    private val accountOperationService: AccountOperationService,
    private val userOperationService: UserOperationService,
    private val loanOperationService: LoanOperationService,
    private val mobileAccountService: MobileAppService
) {
    //    private val accountOperationService: AccountOperationService
    //    private val userOperationService: UserOperationService
    //    private val loanOperationService: LoanOperationService
    //    private val mobileAccountRepository: MobileAppService
//    val context: ApplicationContext

    init {
//        context = AnnotationConfigApplicationContext(SecurityConfig::class.java)
//        val coreRepository = context.getBean(CoreRepository::class.java)
//        val userRepository = context.getBean(UserRepository::class.java)
//        val loanRepository = context.getBean(LoanRepository::class.java)
//
//        val hidedAccountRepository = context.getBean(HidedAccountRepository::class.java)
//        val themeRepository = context.getBean(ThemeRepository::class.java)
//        accountOperationService =
//            AccountOperationService(coreRepository, userRepository, hidedAccountRepository)
//        userOperationService = UserOperationService(userRepository)
//        loanOperationService = LoanOperationService(loanRepository)
//        mobileAccountRepository = MobileAppService(themeRepository, hidedAccountRepository)

//        val db = context.getBean(ThemeRepository::class.java)
//        println(db.getUserThemeById(UUID.fromString("8b168a60-dc64-4cc7-bf57-45040d0d8f59"))?.isThemeDark)
//        println(db.save(UUID.fromString("8b168a60-dc64-4cc7-bf57-45040d0d8f59"))?.isThemeDark)
//        println(db.getUserThemeById(UUID.fromString("8b168a60-dc64-4cc7-bf57-45040d0d8f59"))?.isThemeDark)

//        println(System.getenv("core_url"))
//        println(System.getenv("loan_url"))
//        println(System.getenv("users_url"))


        println()
    }


    private val server: Server = ServerBuilder
        .forPort(port)
        .addService(ServerInterceptors.intercept(accountOperationService, HeaderServerInterceptor()))
        .addService(ServerInterceptors.intercept(userOperationService, HeaderServerInterceptor()))
        .addService(ServerInterceptors.intercept(loanOperationService, HeaderServerInterceptor()))
        .addService(ServerInterceptors.intercept(mobileAccountService, HeaderServerInterceptor()))
        .build()

    fun start() {
        server.start()
        Runtime.getRuntime().addShutdownHook(
            Thread {
                println("*** shutting down gRPC server since JVM is shutting down")
                this@AccountOperationsServer.stop()
                println("*** server shut down")
            }
        )
    }

    private fun stop() {
        server.shutdown()
    }

    fun blockUntilShutdown() {
        server.awaitTermination()
    }
}

//fun main() {
//    val port = 50051
//    val server = AccountOperationsServer(port)
//    server.start()
//    server.blockUntilShutdown()
//}