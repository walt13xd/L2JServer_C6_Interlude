/*
 * This file is part of the L2JServer project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2jserver.gameserver.network.serverpackets;

import java.util.Calendar;
import java.util.logging.Logger;

import org.l2jserver.gameserver.datatables.sql.ClanTable;
import org.l2jserver.gameserver.model.actor.instance.PlayerInstance;
import org.l2jserver.gameserver.model.clan.Clan;
import org.l2jserver.gameserver.model.entity.siege.Fort;

/**
 * Shows the Siege Info<br>
 * <br>
 * packet type id 0xc9<br>
 * format: cdddSSdSdd<br>
 * <br>
 * c = c9<br>
 * d = FortID<br>
 * d = Show Owner Controls (0x00 default || >=0x02(mask?) owner)<br>
 * d = Owner ClanID<br>
 * S = Owner ClanName<br>
 * S = Owner Clan LeaderName<br>
 * d = Owner AllyID<br>
 * S = Owner AllyName<br>
 * d = current time (seconds)<br>
 * d = Siege time (seconds) (0 for selectable)<br>
 * d = (UNKNOW) Siege Time Select Related?
 * @author programmos
 */
public class FortressSiegeInfo extends GameServerPacket
{
	private static final Logger LOGGER = Logger.getLogger(FortressSiegeInfo.class.getName());
	private final Fort _fort;
	
	public FortressSiegeInfo(Fort fort)
	{
		_fort = fort;
	}
	
	@Override
	protected final void writeImpl()
	{
		final PlayerInstance player = getClient().getPlayer();
		if (player == null)
		{
			return;
		}
		
		writeC(0xc9);
		writeD(_fort.getFortId());
		writeD((_fort.getOwnerId() == player.getClanId()) && player.isClanLeader() ? 0x01 : 0x00);
		writeD(_fort.getOwnerId());
		if (_fort.getOwnerId() > 0)
		{
			final Clan owner = ClanTable.getInstance().getClan(_fort.getOwnerId());
			if (owner != null)
			{
				writeS(owner.getName()); // Clan Name
				writeS(owner.getLeaderName()); // Clan Leader Name
				writeD(owner.getAllyId()); // Ally ID
				writeS(owner.getAllyName()); // Ally Name
			}
			else
			{
				LOGGER.warning("Null owner for fort: " + _fort.getName());
			}
		}
		else
		{
			writeS("NPC"); // Clan Name
			writeS(""); // Clan Leader Name
			writeD(0); // Ally ID
			writeS(""); // Ally Name
		}
		
		writeD((int) (Calendar.getInstance().getTimeInMillis() / 1000));
		writeD((int) (_fort.getSiege().getSiegeDate().getTimeInMillis() / 1000));
		writeD(0x00); // number of choices?
	}
}
