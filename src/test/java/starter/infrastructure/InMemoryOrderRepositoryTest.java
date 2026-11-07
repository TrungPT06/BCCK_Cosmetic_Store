package starter.infrastructure;

import org.junit.jupiter.api.DisplayName;
import starter.dathang.usecase.OrderRepository;

@DisplayName("Kiểm thử hợp đồng cho InMemoryOrderRepository")
class InMemoryOrderRepositoryTest extends OrderRepositoryContractTest {
    @Override
    protected OrderRepository createRepository() {
        return new InMemoryOrderRepository();
    }
}
