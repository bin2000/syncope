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
package org.syncope.core.persistence.beans.role;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import org.syncope.core.persistence.beans.AbstractAttributable;
import org.syncope.core.persistence.beans.AbstractAttribute;
import org.syncope.core.persistence.beans.AbstractDerivedAttribute;
import org.syncope.core.persistence.beans.Entitlement;
import org.syncope.core.persistence.beans.membership.Membership;
import org.syncope.core.persistence.beans.user.SyncopeUser;

@Entity
@Table(uniqueConstraints =
@UniqueConstraint(columnNames = {"name", "parent_id"}))
public class SyncopeRole extends AbstractAttributable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    @ManyToOne(optional = true)
    private SyncopeRole parent;
    @OneToMany(cascade = CascadeType.MERGE, mappedBy = "syncopeRole")
    private List<Membership> memberships;
    @ManyToMany(fetch = FetchType.LAZY)
    private Set<Entitlement> entitlements;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner")
    private List<RoleAttribute> attributes;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner")
    private List<RoleDerivedAttribute> derivedAttributes;
    @Basic
    private Character inheritAttributes;
    @Basic
    private Character inheritDerivedAttributes;

    public SyncopeRole() {
        memberships = new ArrayList<Membership>();
        entitlements = new HashSet<Entitlement>();
        attributes = new ArrayList<RoleAttribute>();
        derivedAttributes = new ArrayList<RoleDerivedAttribute>();
        inheritAttributes = 'F';
        inheritDerivedAttributes = 'F';
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) throws IllegalArgumentException {
        this.name = name;
    }

    public SyncopeRole getParent() {
        return parent;
    }

    public void setParent(SyncopeRole parent) {
        this.parent = parent;
    }

    public boolean addEntitlement(Entitlement entitlement) {
        return entitlements.add(entitlement);
    }

    public boolean removeEntitlement(Entitlement entitlement) {
        return entitlements.remove(entitlement);
    }

    public Set<Entitlement> getEntitlements() {
        return entitlements;
    }

    public void setEntitlements(Set<Entitlement> entitlements) {
        this.entitlements = entitlements;
    }

    public boolean addMembership(Membership membership) {
        return memberships.add(membership);
    }

    public boolean removeMembership(Membership membership) {
        return memberships.remove(membership);
    }

    public List<Membership> getMemberships() {
        return memberships;
    }

    public void setMemberships(List<Membership> memberships) {
        this.memberships = memberships;
    }

    public Set<SyncopeUser> getUsers() {
        Set<SyncopeUser> result = new HashSet<SyncopeUser>();

        for (Membership membership : memberships) {
            result.add(membership.getSyncopeUser());
        }

        return result;
    }

    @Override
    public <T extends AbstractAttribute> boolean addAttribute(T attribute) {
        return attributes.add((RoleAttribute) attribute);
    }

    @Override
    public <T extends AbstractAttribute> boolean removeAttribute(T attribute) {
        return attributes.remove((RoleAttribute) attribute);
    }

    @Override
    public List<? extends AbstractAttribute> getAttributes() {
        return attributes;
    }

    @Override
    public void setAttributes(List<? extends AbstractAttribute> attributes) {
        this.attributes = (List<RoleAttribute>) attributes;
    }

    @Override
    public <T extends AbstractDerivedAttribute> boolean addDerivedAttribute(
            T derivedAttribute) {

        return derivedAttributes.add((RoleDerivedAttribute) derivedAttribute);
    }

    @Override
    public <T extends AbstractDerivedAttribute> boolean removeDerivedAttribute(
            T derivedAttribute) {

        return derivedAttributes.remove((RoleDerivedAttribute) derivedAttribute);
    }

    @Override
    public List<? extends AbstractDerivedAttribute> getDerivedAttributes() {
        return derivedAttributes;
    }

    @Override
    public void setDerivedAttributes(
            List<? extends AbstractDerivedAttribute> derivedAttributes) {

        this.derivedAttributes = (List<RoleDerivedAttribute>) derivedAttributes;
    }

    public boolean isInheritAttributes() {
        return inheritAttributes != null && inheritAttributes == 'T';
    }

    public void setInheritAttributes(boolean inheritAttributes) {
        this.inheritAttributes = inheritAttributes ? 'T' : 'F';
    }

    public boolean isInheritDerivedAttributes() {
        return inheritDerivedAttributes != null
                && inheritDerivedAttributes == 'T';
    }

    public void setInheritDerivedAttributes(boolean inheritDerivedAttributes) {
        this.inheritDerivedAttributes = inheritDerivedAttributes ? 'T' : 'F';
    }
}
