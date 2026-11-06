package starter.usecase;

import starter.entity.Voucher;
import java.util.Optional;

/**
 * Cổng đầu ra (Output Port) truy xuất thông tin Voucher từ cơ sở dữ liệu.
 */
public interface VoucherRepository {
    Optional<Voucher> findByCode(String code);
    void save(Voucher voucher);
}
