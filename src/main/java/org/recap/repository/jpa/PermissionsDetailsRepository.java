package org.recap.repository.jpa;

import org.recap.model.jpa.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;


/**
 * Created by dharmendrag on 13/12/16.
 */
public interface PermissionsDetailsRepository extends JpaRepository<PermissionEntity,Integer> {

    PermissionEntity findByPermissionName(String permissionName);

}
