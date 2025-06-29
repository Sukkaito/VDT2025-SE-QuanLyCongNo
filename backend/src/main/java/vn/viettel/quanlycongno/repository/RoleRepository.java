package vn.viettel.quanlycongno.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import vn.viettel.quanlycongno.constant.RoleEnum;
import vn.viettel.quanlycongno.entity.Role;

import java.util.Optional;

@Repository
public interface RoleRepository extends CrudRepository<Role, Long> {
    Optional<Role> findByRoleName(RoleEnum roleName);
}
