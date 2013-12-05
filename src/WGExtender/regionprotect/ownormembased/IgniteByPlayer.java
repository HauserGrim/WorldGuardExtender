/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 */

package WGExtender.regionprotect.ownormembased;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;

import WGExtender.Config;
import WGExtender.WGExtender;
import WGExtender.utils.WGRegionUtils;

public class IgniteByPlayer implements Listener {

	private WGExtender main;
	private Config config;

	
	public IgniteByPlayer(WGExtender main, Config config) {
		this.main = main;
		this.config = config;
	}
	
	@EventHandler(priority=EventPriority.LOWEST,ignoreCancelled=true)
	public void onPlayerIgnitedBlock(BlockIgniteEvent e)
	{
		if (!config.blockigniteotherregionbyplayer) {return;}
		
		Player player = e.getPlayer();
		if (player != null)
		{
			if (!WGRegionUtils.isOwnerOrMember(main.wg, player, e.getBlock().getLocation()))
			{
				player.sendMessage(ChatColor.RED+"Вы не можете поджечь блок в чужом регионе");
				e.setCancelled(true);
			}
		}
	}
	
	
}