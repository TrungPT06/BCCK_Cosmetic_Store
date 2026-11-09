package starter.infrastructure;

import starter.shared.port.OrderRepository;
import starter.domain.*;
import starter.domain.state.OrderStateFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Adapter cơ sở dữ liệu quan hệ (H2 Database) cho OrderRepository.
 * Minh chứng cho nguyên tắc "Leave Open Options": có thể tráo đổi giữa InMemory và RDBMS mà không sửa đổi Domain hay Use Case.
 */
public final class JdbcOrderRepository implements OrderRepository {
    private final String jdbcUrl;

    public JdbcOrderRepository(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
        initSchema();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, "sa", "");
    }

    private void initSchema() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS orders (
                    id VARCHAR(64) PRIMARY KEY,
                    customer_id VARCHAR(64) NOT NULL,
                    created_at TIMESTAMP NOT NULL,
                    status VARCHAR(32) NOT NULL,
                    total_amount BIGINT NOT NULL,
                    shipping_fee BIGINT NOT NULL,
                    flag VARCHAR(32) NOT NULL
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS order_lines (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    order_id VARCHAR(64) NOT NULL,
                    sku VARCHAR(64) NOT NULL,
                    quantity INT NOT NULL,
                    unit_price BIGINT NOT NULL,
                    is_gift BOOLEAN NOT NULL
                )
            """);
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khởi tạo schema cơ sở dữ liệu H2", e);
        }
    }

    @Override
    public void save(Order order) {
        if (order == null) return;

        String upsertOrderSql = """
            MERGE INTO orders (id, customer_id, created_at, status, total_amount, shipping_fee, flag)
            KEY(id) VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        String deleteLinesSql = "DELETE FROM order_lines WHERE order_id = ?";
        String insertLineSql = "INSERT INTO order_lines (order_id, sku, quantity, unit_price, is_gift) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(upsertOrderSql)) {
                    ps.setString(1, order.id());
                    ps.setString(2, order.customerId());
                    ps.setTimestamp(3, Timestamp.valueOf(order.createdAt()));
                    ps.setString(4, order.status());
                    ps.setLong(5, order.total().amount());
                    ps.setLong(6, order.shippingFee().amount());
                    ps.setString(7, order.flag());
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(deleteLinesSql)) {
                    ps.setString(1, order.id());
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(insertLineSql)) {
                    for (Order.OrderLine line : order.lines()) {
                        ps.setString(1, order.id());
                        ps.setString(2, line.sku().value());
                        ps.setInt(3, line.quantity());
                        ps.setLong(4, line.unitPrice().amount());
                        ps.setBoolean(5, line.gift());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lưu đơn hàng vào H2 Database: " + order.id(), e);
        }
    }

    @Override
    public Optional<Order> findById(String id) {
        if (id == null) return Optional.empty();

        String selectOrderSql = "SELECT id, customer_id, created_at, status, total_amount, shipping_fee, flag FROM orders WHERE id = ?";
        String selectLinesSql = "SELECT sku, quantity, unit_price, is_gift FROM order_lines WHERE order_id = ?";

        try (Connection conn = getConnection()) {
            Order order = null;
            try (PreparedStatement ps = conn.prepareStatement(selectOrderSql)) {
                ps.setString(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String orderId = rs.getString("id");
                        String customerId = rs.getString("customer_id");
                        LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
                        String status = rs.getString("status");
                        Money totalAmount = Money.of(rs.getLong("total_amount"));
                        Money shippingFee = Money.of(rs.getLong("shipping_fee"));
                        String flag = rs.getString("flag");

                        List<Order.OrderLine> lines = new ArrayList<>();
                        try (PreparedStatement psLines = conn.prepareStatement(selectLinesSql)) {
                            psLines.setString(1, orderId);
                            try (ResultSet rsLines = psLines.executeQuery()) {
                                while (rsLines.next()) {
                                    lines.add(new Order.OrderLine(
                                            Sku.of(rsLines.getString("sku")),
                                            rsLines.getInt("quantity"),
                                            Money.of(rsLines.getLong("unit_price")),
                                            rsLines.getBoolean("is_gift")
                                    ));
                                }
                            }
                        }

                        order = new Order(orderId, customerId, createdAt, OrderStateFactory.from(status),
                                totalAmount, shippingFee, flag, lines);
                    }
                }
            }
            return Optional.ofNullable(order);
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi truy vấn đơn hàng từ H2: " + id, e);
        }
    }
}
