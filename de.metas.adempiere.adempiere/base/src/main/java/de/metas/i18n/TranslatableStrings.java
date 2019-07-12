package de.metas.i18n;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

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

/**
 * Translatable Strings Facade.
 */
@UtilityClass
public class TranslatableStrings
{
	public static TranslatableStringBuilder builder()
	{
		return TranslatableStringBuilder.newInstance();
	}

	public static ITranslatableString join(final String joiningString, final Object... trls)
	{
		Check.assumeNotEmpty(trls, "trls is not empty");

		final List<ITranslatableString> trlsList = Stream.of(trls)
				.flatMap(TranslatableStrings::explodeCollections)
				.map(TranslatableStrings::toTranslatableStringOrNull)
				.filter(Predicates.notNull()) // skip nulls
				.collect(ImmutableList.toImmutableList());

		return joinList(joiningString, trlsList);
	}

	private static Stream<Object> explodeCollections(final Object obj)
	{
		if (obj == null)
		{
			return Stream.empty();
		}
		else if (obj instanceof Collection)
		{
			@SuppressWarnings("unchecked")
			final Collection<Object> coll = (Collection<Object>)obj;
			return coll.stream()
					.flatMap(TranslatableStrings::explodeCollections);
		}
		else
		{
			return Stream.of(obj);
		}
	}

	/**
	 * @return translatable string or null if the <code>trlObj</code> is null or empty string
	 */
	private static ITranslatableString toTranslatableStringOrNull(final Object trlObj)
	{
		if (trlObj == null)
		{
			return null;
		}
		else if (trlObj instanceof ITranslatableString)
		{
			return (ITranslatableString)trlObj;
		}
		else
		{
			final String trlStr = trlObj.toString();
			if (trlStr == null || trlStr.isEmpty())
			{
				return null;
			}
			else
			{
				return constant(trlStr);
			}
		}
	}

	public static ITranslatableString joinList(final String joiningString, @NonNull final List<ITranslatableString> trls)
	{
		if (trls.isEmpty())
		{
			return empty();
		}
		else if (trls.size() == 1)
		{
			return trls.get(0);
		}
		else
		{
			return new CompositeTranslatableString(trls, joiningString);
		}
	}

	public static Collector<ITranslatableString, ?, ITranslatableString> joining(final String joiningString)
	{
		final Supplier<List<ITranslatableString>> supplier = ArrayList::new;
		final BiConsumer<List<ITranslatableString>, ITranslatableString> accumulator = (accum, e) -> accum.add(e);
		final BinaryOperator<List<ITranslatableString>> combiner = (accum1, accum2) -> {
			accum1.addAll(accum2);
			return accum1;
		};
		final Function<List<ITranslatableString>, ITranslatableString> finisher = accum -> joinList(joiningString, accum);
		return Collector.of(supplier, accumulator, combiner, finisher);
	}

	public static ITranslatableString constant(final String value)
	{
		return ConstantTranslatableString.of(value);
	}

	public static ITranslatableString anyLanguage(final String value)
	{
		return ConstantTranslatableString.anyLanguage(value);
	}

	public static ITranslatableString singleLanguage(@Nullable final String adLanguage, @Nullable final String value)
	{
		if (Check.isEmpty(adLanguage, true))
		{
			return ConstantTranslatableString.of(value);
		}

		final String valueNorm = value == null ? "" : value;
		return ofMap(ImmutableMap.of(adLanguage, valueNorm), valueNorm);
	}

	public static ITranslatableString empty()
	{
		return ConstantTranslatableString.EMPTY;
	}

	public static boolean isEmpty(final ITranslatableString trl)
	{
		if (trl == null)
		{
			return true;
		}
		else if (trl == ConstantTranslatableString.EMPTY)
		{
			return true;
		}
		else if (trl instanceof ConstantTranslatableString)
		{
			return Check.isEmpty(trl.getDefaultValue(), false);
		}
		else
		{
			return false;
		}
	}

	public static boolean isBlank(@Nullable final ITranslatableString trl)
	{
		if (trl == null)
		{
			return true;
		}
		else if (trl == ConstantTranslatableString.EMPTY)
		{
			return true;
		}
		else if (trl instanceof ConstantTranslatableString)
		{
			return Check.isEmpty(trl.getDefaultValue(), true);
		}
		else
		{
			return false;
		}
	}

	public static ITranslatableString nullToEmpty(final ITranslatableString trl)
	{
		return trl != null ? trl : empty();
	}

	public static NumberTranslatableString number(final BigDecimal valueBD, final int displayType)
	{
		return NumberTranslatableString.of(valueBD, displayType);
	}

	public static NumberTranslatableString number(final int valueInt)
	{
		return NumberTranslatableString.of(valueInt);
	}

	public static DateTimeTranslatableString date(@NonNull final java.util.Date date)
	{
		return DateTimeTranslatableString.ofDate(date);
	}

	public static DateTimeTranslatableString date(@NonNull final LocalDate date)
	{
		return DateTimeTranslatableString.ofDate(date);
	}

	public static DateTimeTranslatableString date(@NonNull final Object obj, final int displayType)
	{
		return DateTimeTranslatableString.ofObject(obj, displayType);
	}

	public static DateTimeTranslatableString dateAndTime(@NonNull final LocalDateTime date)
	{
		return DateTimeTranslatableString.ofDateTime(date);
	}

	public static DateTimeTranslatableString dateAndTime(@NonNull final ZonedDateTime date)
	{
		return DateTimeTranslatableString.ofDateTime(date);
	}

	public static DateTimeTranslatableString dateAndTime(@NonNull final java.util.Date date)
	{
		return DateTimeTranslatableString.ofDateTime(date);
	}

	public static ITranslatableString ofMap(final Map<String, String> trlMap)
	{
		if (trlMap == null || trlMap.isEmpty())
		{
			return ConstantTranslatableString.EMPTY;
		}
		else
		{
			return new ImmutableTranslatableString(trlMap, ConstantTranslatableString.EMPTY.getDefaultValue());
		}
	}

	public static ITranslatableString ofMap(final Map<String, String> trlMap, final String defaultValue)
	{
		if (trlMap == null || trlMap.isEmpty())
		{
			return ConstantTranslatableString.of(defaultValue);
		}
		else
		{
			return new ImmutableTranslatableString(trlMap, defaultValue);
		}
	}

	public static ForwardingTranslatableString forwardingTo(@NonNull final Supplier<ITranslatableString> delegateSupplier)
	{
		return new ForwardingTranslatableString(delegateSupplier);
	}

	public static ITranslatableString copyOf(@NonNull final ITranslatableString trl)
	{
		return copyOfNullable(trl);
	}

	/**
	 * @return {@link ImmutableTranslatableString} or {@link #empty()} if <code>trl</code> was null
	 */
	public static ITranslatableString copyOfNullable(@Nullable final ITranslatableString trl)
	{
		if (trl == null)
		{
			return ConstantTranslatableString.EMPTY;
		}

		if (trl instanceof ConstantTranslatableString)
		{
			return trl;
		}
		if (trl instanceof ImmutableTranslatableString)
		{
			return trl;
		}

		final Set<String> adLanguages = trl.getAD_Languages();
		final Map<String, String> trlMap = new LinkedHashMap<>(adLanguages.size());
		for (final String adLanguage : adLanguages)
		{
			final String trlString = trl.translate(adLanguage);
			if (trlString == null)
			{
				continue;
			}

			trlMap.put(adLanguage, trlString);
		}

		return ofMap(trlMap, trl.getDefaultValue());
	}
}
