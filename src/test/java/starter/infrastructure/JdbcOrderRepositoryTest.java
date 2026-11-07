package starter.infrastructure;

import org.junit.jupiter.api.DisplayName;
import starter.dathang.usecase.OrderRepository;

@DisplayName("Kiểm thử hợp đồng cho JdbcOrderRepository (H2 Database)")
class JdbcOrderRepositoryTest extends OrderRepositoryContractTest {
    @Override
    protected OrderRepository createRepository() {
        return new JdbcOrderRepository("jdbc:h2:mem:order_repo_contract_test;DB_CLOSE_DELAY=-1");
    }
}
