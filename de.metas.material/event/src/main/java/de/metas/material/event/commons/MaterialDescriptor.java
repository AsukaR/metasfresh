package de.metas.material.event.commons;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/*
 * #%L
 * metasfresh-material-event
 * %%
 * Copyright (C) 2017 metas GmbH
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
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@EqualsAndHashCode(exclude = "quantity", // ignore quantity to avoid trouble comparing e.g. 10 with 10.0 with 1E+1
		callSuper = true)
@ToString(callSuper = true)
public class MaterialDescriptor extends ProductDescriptor
{
	@Getter
	int warehouseId;

	/**
	 * Optional, may be <= 0; if set, then the respective candidate allocated to the respective *customer*
	 * and is not available to other customers.
	 */
	@Getter
	int customerId;

	@Getter
	BigDecimal quantity;

	/**
	 * The projected date at which we expect this candidate's {@link #getQuantity()}.
	 */
	@Getter
	Date date;

	@Builder
	private MaterialDescriptor(
			final int warehouseId,
			final int customerId,
			final Date date,
			final ProductDescriptor productDescriptor,
			final BigDecimal quantity)
	{
		this(
				warehouseId,
				customerId,
				quantity,
				date,
				productDescriptor == null ? 0 : productDescriptor.getProductId(),
				productDescriptor == null ? -1 : productDescriptor.getAttributeSetInstanceId(),
				productDescriptor == null ? AttributesKey.ALL : productDescriptor.getStorageAttributesKey());
	}

	@JsonCreator
	public MaterialDescriptor(
			@JsonProperty("warehouseId") final int warehouseId,
			@JsonProperty("customerId") final int customerId,
			@JsonProperty("quantity") final BigDecimal quantity,
			@JsonProperty("date") final Date date,
			@JsonProperty("productId") final int productId,
			@JsonProperty("attributeSetInstanceId") final int attributeSetInstanceId,
			@JsonProperty("storageAttributesKey") final AttributesKey attributesKey)
	{
		super(productId, attributesKey, attributeSetInstanceId);

		this.warehouseId = warehouseId;
		this.customerId = customerId;
		this.quantity = quantity;

		this.date = date;

		asssertMaterialDescriptorComplete();
	}

	public MaterialDescriptor asssertMaterialDescriptorComplete()
	{
		Preconditions.checkArgument(warehouseId > 0, "warehouseId=%s needs to be >0", warehouseId);
		Preconditions.checkArgument(customerId >= 0, "customerId=%s needs to be >=0", customerId);
		Preconditions.checkNotNull(quantity, "quantity needs to be not-null");
		Preconditions.checkNotNull(date, "date needs to not-null");

		return this;
	}

	public MaterialDescriptor withQuantity(@NonNull final BigDecimal quantity)
	{
		final MaterialDescriptor result = MaterialDescriptor.builder()
				.quantity(quantity)
				.date(this.date)
				.productDescriptor(this)
				.warehouseId(this.warehouseId)
				.customerId(this.customerId)
				.build();
		return result.asssertMaterialDescriptorComplete();
	}

	public MaterialDescriptor withDate(@NonNull final Date date)
	{
		final MaterialDescriptor result = MaterialDescriptor.builder()
				.date(date)
				.productDescriptor(this)
				.warehouseId(this.warehouseId)
				.customerId(this.customerId)
				.quantity(this.quantity)
				.build();
		return result.asssertMaterialDescriptorComplete();
	}

	public MaterialDescriptor withProductDescriptor(final ProductDescriptor productDescriptor)
	{
		final MaterialDescriptor result = MaterialDescriptor.builder()
				.productDescriptor(productDescriptor)
				.date(this.date)
				.warehouseId(this.warehouseId)
				.customerId(this.customerId)
				.quantity(this.quantity)
				.build();
		return result.asssertMaterialDescriptorComplete();
	}

	public MaterialDescriptor withWarehouseId(final int warehouseId)
	{
		final MaterialDescriptor result = MaterialDescriptor.builder()
				.warehouseId(warehouseId)
				.date(this.date)
				.productDescriptor(this)
				.customerId(this.customerId)
				.quantity(this.quantity)
				.build();
		return result.asssertMaterialDescriptorComplete();
	}

	public MaterialDescriptor withCustomerId(final int customerId)
	{
		final MaterialDescriptor result = MaterialDescriptor.builder()
				.warehouseId(this.warehouseId)
				.date(this.date)
				.productDescriptor(this)
				.customerId(customerId)
				.quantity(this.quantity)
				.build();
		return result.asssertMaterialDescriptorComplete();
	}
}
