package org.adempiere.ad.dao.impl;

import java.util.List;

import org.adempiere.ad.dao.model.I_T_Query_Selection;
import org.adempiere.ad.dao.model.I_T_Query_Selection_Metadata;
import org.adempiere.util.lang.IContextAware;
import org.compiere.util.DB;
import org.slf4j.Logger;

import de.metas.logging.LogManager;
import de.metas.util.Check;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

/*
 * #%L
 * de.metas.adempiere.adempiere.base
 * %%
 * Copyright (C) 2019 metas GmbH
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

@UtilityClass
public class QuerySelectionHelper
{

	public static final String SELECTION_LINE_ALIAS = "ZZ_Line";

	private static final Logger logger = LogManager.getLogger(QuerySelectionHelper.class);

	/**
	 * Uses the given query and uuid to insert records into the {@code T_Query_Selection} table.
	 */
	public int createUUIDSelection(
			@NonNull final TypedSqlQuery<?> query,
			@NonNull final String querySelectionUUID)
	{
		insertSelectionMetadata(query, querySelectionUUID);

		final String tableName = query.getTableName();
		final String keyColumnNameFQ = tableName + "." + query.getKeyColumnName();

		final String orderBy = query.getOrderBy();

		final StringBuilder sqlRowNumber = new StringBuilder("row_number() OVER (");
		if (!Check.isEmpty(orderBy, true))
		{
			sqlRowNumber.append("ORDER BY ").append(orderBy);
		}
		sqlRowNumber.append(")");

		final StringBuilder sqlInsertIntoBuilder = new StringBuilder()
				.append("INSERT INTO ")
				.append(I_T_Query_Selection.Table_Name)
				.append(" (")
				.append(I_T_Query_Selection.COLUMNNAME_UUID)
				.append(", ").append(I_T_Query_Selection.COLUMNNAME_Line)
				.append(", ").append(I_T_Query_Selection.COLUMNNAME_Record_ID)
				.append(")");

		final StringBuilder sqlSelectBuilder = new StringBuilder()
				.append(" SELECT ")
				.append(DB.TO_STRING(querySelectionUUID))
				.append(", ").append(sqlRowNumber)
				.append(", ").append(keyColumnNameFQ);

		final StringBuilder sqlFromBuilder = new StringBuilder(" FROM ").append(tableName);

		// be sure to only pass the "SELECT", not the "INSERT" sql to avoid invalid SQL when ORs are exploded to unions
		final String sqlSelect = query.buildSQL(sqlSelectBuilder, sqlFromBuilder, true/* useOrderByClause */);
		final List<Object> params = query.getParametersEffective();

		final String sql = sqlInsertIntoBuilder.append(sqlSelect).toString();

		final String trxName = query.getTrxName();

		final int rowsCount = DB.executeUpdateEx(sql,
				params == null ? null : params.toArray(),
				trxName);

		if (logger.isTraceEnabled())
		{
			logger.info("sql=" + sql + ", params=" + params + ", trxName=" + trxName + ", rowsCount=" + rowsCount);
		}

		return rowsCount;
	}

	private void insertSelectionMetadata(final TypedSqlQuery<?> query, final String querySelectionUUID)
	{
		final String sqlInsertIntoMetadata = new StringBuilder()
				.append("INSERT INTO ")
				.append(I_T_Query_Selection_Metadata.Table_Name)
				.append(" (")
				.append(I_T_Query_Selection_Metadata.COLUMNNAME_UUID)
				.append(", ").append(I_T_Query_Selection_Metadata.COLUMNNAME_Table)
				.append(", ").append(I_T_Query_Selection_Metadata.COLUMNNAME_Column)
				.append(") VALUE (")
				.append(DB.TO_STRING(querySelectionUUID))
				.append(", ").append(query.getTableName())
				.append(", ").append(query.getKeyColumnName())
				.append(")")
				.toString();
		DB.executeUpdateEx(sqlInsertIntoMetadata, query.getTrxName());
	}

	public <T, ET extends T> TypedSqlQuery<ET> createUUIDSelectionQuery(
			@NonNull final IContextAware ctx,
			@NonNull final Class<ET> clazz,
			@NonNull final String querySelectionUUID)
	{

		I_T_Query_Selection_Metadata metadata = new TypedSqlQuery<I_T_Query_Selection_Metadata>(
				ctx.getCtx(),
				I_T_Query_Selection_Metadata.class,
				I_T_Query_Selection_Metadata.COLUMNNAME_UUID + "=?",
				ctx.getTrxName())
						.setParameters(querySelectionUUID)
						.firstOnly();

		final String tableName = metadata.getTable();
		final String keyColumnNameFQ = tableName + "." + metadata.getColumn();

		//
		// Build the query used to retrieve models by querying the selection.
		// NOTE: we are using LEFT OUTER JOIN instead of INNER JOIN because
		// * methods like hasNext() are comparing the rowsFetched counter with rowsCount to detect if we reached the end of the selection (optimization).
		// * POBufferedIterator is using LIMIT/OFFSET clause for fetching the next page and eliminating rows from here would fuck the paging if one record was deleted in meantime.
		// So we decided to load everything here, and let the hasNext() method to deal with the case when the record is really missing.
		final String selectionSqlFrom = "(SELECT "
				+ I_T_Query_Selection.COLUMNNAME_UUID + " as ZZ_UUID"
				+ ", " + I_T_Query_Selection.COLUMNNAME_Record_ID + " as ZZ_Record_ID"
				+ ", " + I_T_Query_Selection.COLUMNNAME_Line + " as " + SELECTION_LINE_ALIAS
				+ " FROM " + I_T_Query_Selection.Table_Name
				+ ") s "
				+ "\n LEFT OUTER JOIN " + tableName + " ON (" + keyColumnNameFQ + "=s.ZZ_Record_ID)";

		final String selectionWhereClause = "s.ZZ_UUID=?";
		final String selectionOrderBy = "s." + SELECTION_LINE_ALIAS;

		final TypedSqlQuery<ET> querySelection = new TypedSqlQuery<>(
				ctx.getCtx(),
				clazz,
				tableName,
				selectionWhereClause,
				ctx.getTrxName())
						.setParameters(querySelectionUUID)
						.setSqlFrom(selectionSqlFrom)
						.setOrderBy(selectionOrderBy);

		return querySelection;
	}
}
