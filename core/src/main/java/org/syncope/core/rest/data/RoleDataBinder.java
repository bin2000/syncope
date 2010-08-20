/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package org.syncope.core.rest.data;

import java.util.Collections;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.syncope.client.mod.RoleMod;
import org.syncope.client.to.RoleTO;
import org.syncope.client.validation.SyncopeClientCompositeErrorException;
import org.syncope.client.validation.SyncopeClientException;
import org.syncope.core.persistence.beans.role.SyncopeRole;
import org.syncope.core.persistence.propagation.ResourceOperations;
import org.syncope.types.SyncopeClientExceptionType;

@Component
public class RoleDataBinder extends AbstractAttributableDataBinder {

    public SyncopeRole create(RoleTO roleTO)
            throws SyncopeClientCompositeErrorException {

        SyncopeRole role = new SyncopeRole();
        role.setInheritAttributes(roleTO.isInheritAttributes());
        role.setInheritDerivedAttributes(
                roleTO.isInheritDerivedAttributes());

        SyncopeClientCompositeErrorException scce =
                new SyncopeClientCompositeErrorException(
                HttpStatus.BAD_REQUEST);

        // name and parent
        SyncopeClientException invalidRoles =
                new SyncopeClientException(
                SyncopeClientExceptionType.InvalidRoles);
        if (roleTO.getName() == null) {
            log.error("No name specified for this role");

            invalidRoles.addElement("No name specified for this role");
        } else {
            role.setName(roleTO.getName());
        }
        Long parentRoleId = null;
        SyncopeRole parentRole = syncopeRoleDAO.find(roleTO.getParent());
        if (parentRole == null) {
            log.error("Could not find role with id " + roleTO.getParent());

            invalidRoles.addElement(String.valueOf(roleTO.getParent()));
            scce.addException(invalidRoles);
        } else {
            role.setParent(parentRole);
            parentRoleId = role.getParent().getId();
        }

        SyncopeRole otherRole = syncopeRoleDAO.find(
                roleTO.getName(), parentRoleId);
        if (otherRole != null) {
            log.error("Another role exists with the same name "
                    + "and the same parent role: " + otherRole);

            invalidRoles.addElement(roleTO.getName());
        }

        // attributes, derived attributes and resources
        role = (SyncopeRole) fill(role, roleTO,
                AttributableUtil.ROLE, scce);

        return role;
    }

    public ResourceOperations update(SyncopeRole role, RoleMod roleMod)
            throws SyncopeClientCompositeErrorException {

        SyncopeClientCompositeErrorException scce =
                new SyncopeClientCompositeErrorException(
                HttpStatus.BAD_REQUEST);

        // name
        SyncopeClientException invalidRoles = new SyncopeClientException(
                SyncopeClientExceptionType.InvalidRoles);
        if (roleMod.getName() != null) {
            SyncopeRole otherRole = syncopeRoleDAO.find(
                    roleMod.getName(), role.getParent().getId());

            if (otherRole != null) {
                log.error("Another role exists with the same name "
                        + "and the same parent role: " + otherRole);

                invalidRoles.addElement(roleMod.getName());
                scce.addException(invalidRoles);
            } else {
                role.setName(roleMod.getName());
            }
        }

        // inherited attributes
        if (roleMod.isChangeInheritAttributes()) {
            role.setInheritAttributes(
                    !role.isInheritAttributes());
        }

        // inherited derived attributes
        if (roleMod.isChangeInheritDerivedAttributes()) {
            role.setInheritDerivedAttributes(
                    !role.isInheritDerivedAttributes());
        }

        // attributes, derived attributes and resources
        return fill(role, roleMod, AttributableUtil.ROLE, scce);
    }

    public RoleTO getRoleTO(SyncopeRole role) {
        RoleTO roleTO = new RoleTO();
        roleTO.setId(role.getId());
        roleTO.setName(role.getName());
        roleTO.setInheritAttributes(role.isInheritAttributes());
        roleTO.setInheritDerivedAttributes(role.isInheritDerivedAttributes());
        if (role.getParent() != null) {
            roleTO.setParent(role.getParent().getId());
        }

        roleTO = (RoleTO) fillTO(roleTO, role.getAttributes(),
                role.getDerivedAttributes(), role.getTargetResources());

        if (role.isInheritAttributes() || role.isInheritDerivedAttributes()) {
            roleTO = (RoleTO) fillTO(roleTO,
                    role.isInheritAttributes()
                    ? syncopeRoleDAO.findInheritedAttributes(role)
                    : Collections.EMPTY_SET,
                    role.isInheritDerivedAttributes()
                    ? syncopeRoleDAO.findInheritedDerivedAttributes(role)
                    : Collections.EMPTY_SET,
                    Collections.EMPTY_SET);
        }

        return roleTO;
    }
}
