package org.adempiere.mm.attributes.api;

import org.adempiere.mm.attributes.AttributeId;

/*
 * #%L
 * de.metas.adempiere.adempiere.base
 * %%
 * Copyright (C) 2015 metas GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

import org.adempiere.util.ISingletonService;
import org.compiere.model.I_M_Attribute;
import org.compiere.model.I_M_AttributeInstance;
import org.compiere.model.I_M_AttributeSetInstance;
import org.compiere.model.I_M_AttributeValue;

import de.metas.product.ProductId;

/**
 * Service to create and update AttributeInstances and AttributeSetInstances.
 *
 * @author metas-dev <dev@metasfresh.com>
 *
 */
public interface IAttributeSetInstanceBL extends ISingletonService
{
	/** Call {@link #buildDescription(I_M_AttributeSetInstance, boolean)} with verbose = false. */
	String buildDescription(I_M_AttributeSetInstance asi);

	/**
	 * Build ASI Description
	 *
	 * e.g. - Product Values - Instance Values - SerNo = #123 - Lot = \u00ab123\u00bb - GuaranteeDate = 10/25/2003
	 *
	 * @param asi may be {@code null}; in that case, an empty string is returned
	 */
	String buildDescription(I_M_AttributeSetInstance asi, boolean verboseDescription);

	/**
	 * Builds and set {@link I_M_AttributeSetInstance#COLUMNNAME_Description}.
	 *
	 * @param asi may be {@code null}; in that case, nothing is done;
	 */
	void setDescription(I_M_AttributeSetInstance asi);

	/**
	 * Creates and saves a new "empty" ASI based on the given product's attribute set.
	 *
	 * @param product
	 * @return newly created and saved ASI; never return null
	 */
	I_M_AttributeSetInstance createASI(ProductId productId);

	/**
	 * Get an existing Attribute Set Instance, create a new one if none exists yet.
	 *
	 * In case a new ASI is created, it will be saved and also set to ASI aware ({@link IAttributeSetInstanceAware#setM_AttributeSetInstance(I_M_AttributeSetInstance)}).
	 *
	 * @param asiAware
	 * @return existing ASI/newly created ASI
	 */
	I_M_AttributeSetInstance getCreateASI(IAttributeSetInstanceAware asiAware);

	/**
	 * Get/Create {@link I_M_AttributeInstance} for given <code>asi</code>. If a new ai is created, if is also saved.
	 *
	 * @param asi
	 * @param attributeId
	 * @return attribute instance; never return null
	 */
	I_M_AttributeInstance getCreateAttributeInstance(I_M_AttributeSetInstance asi, AttributeId attributeId);

	/**
	 * Convenient way to quickly create/update and save an {@link I_M_AttributeInstance} for {@link I_M_AttributeValue}.
	 *
	 * @param asi
	 * @param attributeValue attribute value to set; must be not null
	 * @return created/updated attribute instance
	 */
	I_M_AttributeInstance getCreateAttributeInstance(I_M_AttributeSetInstance asi, I_M_AttributeValue attributeValue);

	/**
	 * If both the given <code>to</code> and <code>from</code> can be converted to {@link IAttributeSetInstanceAware}s and if <code>from</code>'s ASI-aware has an M_AttributeSetInstance,
	 * then that ASI is copied/cloned to the given <code>to</code> and saved.
	 * <p>
	 * Note that <code>to</code> itself is not saved. Also note that any existing ASI which might already be referenced by <code>to</code> is discarded/ignored.
	 *
	 * @param to
	 * @param from
	 * @see IAttributeSetInstanceAwareFactoryService#createOrNull(Object)
	 */
	void cloneASI(Object to, Object from);

	I_M_AttributeSetInstance createASIFromAttributeSet(IAttributeSet attributeSet);

	I_M_AttributeSetInstance createASIWithASFromProductAndInsertAttributeSet(ProductId productId, IAttributeSet attributeSet);

	/**
	 * set in {@link I_M_AttributeInstance} the correct value for given <code>asi</code> and given <code>attribute</code>
	 * <br>
	 * the ai is also saved.
	 *
	 * @param asi
	 * @param attribute
	 * @param value
	 * @return
	 */
	void setAttributeInstanceValue(I_M_AttributeSetInstance asi, I_M_Attribute attribute, Object value);

	void setAttributeInstanceValue(I_M_AttributeSetInstance asi, AttributeId attributeId, Object value);
}
