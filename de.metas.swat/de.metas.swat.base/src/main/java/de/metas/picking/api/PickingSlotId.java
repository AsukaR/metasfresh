package de.metas.picking.api;

import org.adempiere.util.Check;

import de.metas.lang.RepoIdAware;
import lombok.Value;

/*
 * #%L
 * de.metas.swat.base
 * %%
 * Copyright (C) 2018 metas GmbH
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

@Value
public class PickingSlotId implements RepoIdAware
{
	public static PickingSlotId ofRepoId(final int repoId)
	{
		return new PickingSlotId(repoId);
	}

	public static PickingSlotId ofRepoIdOrNull(final int repoId)
	{
		return repoId > 0 ? new PickingSlotId(repoId) : null;
	}

	public static int toRepoId(final PickingSlotId pickingSlotId)
	{
		return pickingSlotId != null ? pickingSlotId.getRepoId() : -1;
	}

	int repoId;

	private PickingSlotId(final int repoId)
	{
		this.repoId = Check.assumeGreaterThanZero(repoId, "repoId");
	}
}
