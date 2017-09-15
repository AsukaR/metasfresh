package de.metas.ui.web.window.model.lookup;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.adempiere.ad.expression.api.IExpressionEvaluator.OnVariableNotFound;
import org.adempiere.ad.expression.api.IStringExpression;
import org.adempiere.ad.service.impl.LookupDAO.SQLNamePairIterator;
import org.adempiere.ad.trx.api.ITrx;
import org.adempiere.ad.validationRule.INamePairPredicate;
import org.compiere.util.CCache.CCacheStats;
import org.compiere.util.DB;
import org.slf4j.Logger;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import de.metas.i18n.ITranslatableString;
import de.metas.i18n.ImmutableTranslatableString;
import de.metas.logging.LogManager;
import de.metas.ui.web.window.WindowConstants;
import de.metas.ui.web.window.datatypes.LookupValue;
import de.metas.ui.web.window.datatypes.LookupValue.IntegerLookupValue;
import de.metas.ui.web.window.datatypes.LookupValue.StringLookupValue;
import de.metas.ui.web.window.datatypes.LookupValuesList;
import de.metas.ui.web.window.datatypes.WindowId;
import de.metas.ui.web.window.descriptor.LookupDescriptor;
import de.metas.ui.web.window.descriptor.sql.SqlLookupDescriptor;
import lombok.NonNull;

/*
 * #%L
 * metasfresh-webui-api
 * %%
 * Copyright (C) 2016 metas GmbH
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

public class GenericSqlLookupDataSourceFetcher implements LookupDataSourceFetcher
{
	public static final GenericSqlLookupDataSourceFetcher of(final LookupDescriptor lookupDescriptor)
	{
		return new GenericSqlLookupDataSourceFetcher(lookupDescriptor);
	}

	private static final Logger logger = LogManager.getLogger(GenericSqlLookupDataSourceFetcher.class);

	private final @NonNull String lookupTableName;
	private final @NonNull Optional<String> lookupTableNameAsOptional;
	private final boolean numericKey;
	private final int entityTypeIndex;

	private final IStringExpression sqlForFetchingExpression;
	private final IStringExpression sqlForFetchingDisplayNameByIdExpression;
	private final INamePairPredicate postQueryPredicate;

	private final boolean isTranslatable;

	private final Optional<WindowId> zoomIntoWindowId;

	private GenericSqlLookupDataSourceFetcher(final LookupDescriptor lookupDescriptor)
	{
		super();
		// NOTE: don't store a reference to our descriptor, just extract what we need!

		Preconditions.checkNotNull(lookupDescriptor);
		final SqlLookupDescriptor sqlLookupDescriptor = lookupDescriptor.cast(SqlLookupDescriptor.class);
		lookupTableNameAsOptional = sqlLookupDescriptor.getTableName();
		lookupTableName = lookupTableNameAsOptional.get();
		numericKey = sqlLookupDescriptor.isNumericKey();
		entityTypeIndex = sqlLookupDescriptor.getEntityTypeIndex();
		sqlForFetchingExpression = sqlLookupDescriptor.getSqlForFetchingExpression();
		sqlForFetchingDisplayNameByIdExpression = sqlLookupDescriptor.getSqlForFetchingDisplayNameByIdExpression();
		postQueryPredicate = sqlLookupDescriptor.getPostQueryPredicate();

		isTranslatable = sqlForFetchingDisplayNameByIdExpression.requiresParameter(LookupDataSourceContext.PARAM_AD_Language.getName());

		zoomIntoWindowId = lookupDescriptor.getZoomIntoWindowId();
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(this)
				.omitNullValues()
				.add("lookupTableName", lookupTableName)
				.add("sqlForFetchingExpression", sqlForFetchingExpression)
				.add("postQueryPredicate", postQueryPredicate)
				.toString();
	}

	@Override
	public String getCachePrefix()
	{
		// NOTE: it's very important to have the lookupTableName as cache name prefix because we want the cache invalidation to happen for this table
		return lookupTableName;
	}

	@Override
	public Optional<String> getLookupTableName()
	{
		return lookupTableNameAsOptional;
	}

	@Override
	public Optional<WindowId> getZoomIntoWindowId()
	{
		return zoomIntoWindowId;
	}

	@Override
	public boolean isCached()
	{
		return false;
	}

	@Override
	public List<CCacheStats> getCacheStats()
	{
		return ImmutableList.of();
	}

	@Override
	public final LookupDataSourceContext.Builder newContextForFetchingById(final Object id)
	{
		return LookupDataSourceContext.builder(lookupTableName)
				.putFilterByIdParameterName("?")
				.putFilterById(id)
				.setRequiredParameters(sqlForFetchingDisplayNameByIdExpression.getParameters());
	}

	@Override
	public LookupDataSourceContext.Builder newContextForFetchingList()
	{
		return LookupDataSourceContext.builder(lookupTableName)
				.putPostQueryPredicate(postQueryPredicate)
				.setRequiredParameters(sqlForFetchingExpression.getParameters());
	}

	@Override
	public final boolean isNumericKey()
	{
		return numericKey;
	}

	/**
	 *
	 * @param evalCtx
	 * @return lookup values list
	 * @see #getRetrieveEntriesParameters()
	 */
	@Override
	public LookupValuesList retrieveEntities(final LookupDataSourceContext evalCtx)
	{
		final String sqlForFetching = sqlForFetchingExpression.evaluate(evalCtx, OnVariableNotFound.Fail);
		final String adLanguage = isTranslatable ? evalCtx.getAD_Language() : null;

		try (final SQLNamePairIterator data = new SQLNamePairIterator(sqlForFetching, numericKey, entityTypeIndex))
		{
			Map<String, String> debugProperties = null;
			if (WindowConstants.isProtocolDebugging())
			{
				debugProperties = new LinkedHashMap<>();
				debugProperties.put("debug-sql", sqlForFetching);
				debugProperties.put("debug-params", evalCtx.toString());
			}

			final LookupValuesList values = data.fetchAll()
					.stream()
					.filter(evalCtx::acceptItem)
					.map(namePair -> LookupValue.fromNamePair(namePair, adLanguage))
					.collect(LookupValuesList.collect(debugProperties));

			logger.trace("Returning values={} (executed sql: {})", values, sqlForFetching);
			return values;
		}
	}

	@Override
	public final LookupValue retrieveLookupValueById(final LookupDataSourceContext evalCtx)
	{
		final Object id = evalCtx.getIdToFilter();
		if (id == null)
		{
			throw new IllegalStateException("No ID provided in " + evalCtx);
		}

		final String sqlDisplayName = sqlForFetchingDisplayNameByIdExpression.evaluate(evalCtx, OnVariableNotFound.Fail);
		final String displayName = DB.getSQLValueStringEx(ITrx.TRXNAME_ThreadInherited, sqlDisplayName, id);
		if (displayName == null)
		{
			return LOOKUPVALUE_NULL;
		}

		final ITranslatableString displayNameTrl;
		if (isTranslatable)
		{
			final String adLanguage = evalCtx.getAD_Language();
			displayNameTrl = ImmutableTranslatableString.singleLanguage(adLanguage, displayName);
		}
		else
		{
			displayNameTrl = ImmutableTranslatableString.anyLanguage(displayName);
		}

		//
		//
		if (id instanceof Integer)
		{
			final Integer idInt = (Integer)id;
			return IntegerLookupValue.of(idInt, displayNameTrl);
		}
		else
		{
			final String idString = id.toString();
			return StringLookupValue.of(idString, displayNameTrl);
		}
	}
}
