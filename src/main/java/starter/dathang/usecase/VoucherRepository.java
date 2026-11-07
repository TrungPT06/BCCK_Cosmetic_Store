package starter.dathang.usecase;

import starter.domain.Voucher;

import java.util.Optional;

/**
 * Port truy vấn và cập nhật Voucher giảm giá.
 */
public interface VoucherRepository {
    Optional<Voucher> findByCode(String code);

    void save(Voucher voucher);
}
