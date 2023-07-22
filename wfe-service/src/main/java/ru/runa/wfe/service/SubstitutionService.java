/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wfe.service;

import java.util.List;

import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.SubstitutionCriteria;
import ru.runa.wfe.ss.SubstitutionDoesNotExistException;
import ru.runa.wfe.user.User;

/**
 * Service for operations with {@link Substitution},
 * {@link SubstitutionCriteria}.
 * 
 * @since 3.0
 */
public interface SubstitutionService {

    /**
     * Creates new substitution rule for user.
     * 
     * @param user
     * @param substitution
     * @return
     */
    public Substitution createSubstitution(User user, Substitution substitution);

    /**
     * Gets all substitutions for user specified by id.
     * 
     * @param user
     * @param ownerId
     * @return
     */
    public List<Substitution> getSubstitutions(User user, Long ownerId);

    /**
     * Gets substitution rule by id.
     * 
     * @param user
     * @param substitutionId
     * @return
     */
    public Substitution getSubstitution(User user, Long substitutionId);

    /**
     * Updates substitution rule.
     * 
     * @param user
     * @param substitution
     */
    public void updateSubstitution(User user, Substitution substitution);

    /**
     * Deletes substitution rules by ids.
     * 
     * @param user
     * @param substitutionIds
     * @throws SubstitutionDoesNotExistException
     */
    public void deleteSubstitutions(User user, List<Long> substitutionIds) throws SubstitutionDoesNotExistException;

    /**
     * Creates new criteria for substitution rules.
     * 
     * @param user
     * @param substitutionCriteria
     */
    public <T extends SubstitutionCriteria> void createCriteria(User user, T substitutionCriteria);

    /**
     * Gets criteria by id.
     * 
     * @param user
     * @param criteriaId
     * @return
     */
    public SubstitutionCriteria getCriteria(User user, Long criteriaId);

    /**
     * Gets criteria by name.
     */
    public SubstitutionCriteria getCriteriaByName(User user, String name);

    /**
     * Gets all criterias.
     * 
     * @param user
     * @return
     */
    public List<SubstitutionCriteria> getAllCriterias(User user);

    /**
     * Updates criteria.
     * 
     * @param user
     * @param criteria
     */
    public void updateCriteria(User user, SubstitutionCriteria criteria);

    /**
     * Deletes criterias.
     * 
     * @param user
     * @param criterias
     */
    public void deleteCriterias(User user, List<SubstitutionCriteria> criterias);

    /**
     * Deletes criteria.
     * 
     * @param user
     * @param criteria
     */
    public void deleteCriteria(User user, SubstitutionCriteria criteria);

    /**
     * Gets all substitution rules which uses criteria.
     * 
     * @param user
     * @param criteria
     * @return
     */
    public List<Substitution> getSubstitutionsByCriteria(User user, SubstitutionCriteria criteria);

}
