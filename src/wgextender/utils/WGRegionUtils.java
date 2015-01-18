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

package wgextender.utils;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.protection.flags.StateFlag;

public class WGRegionUtils {

	private static WGRegionUtilsInterface utils;
	static {
		initUtils();
	}

	private static void initUtils() {
		try {
			if (Class.forName("com.sk89q.worldguard.protection.ApplicableRegionSet").isInterface()) {
				utils = new NewWGRegionUtils();
				newWG = true;
				return;
			}
		} catch (ClassNotFoundException e) {
		}
		utils = new OldWGRegionUtils();
	}

	public static boolean canBypassProtection(Player p) {
		if (p.hasPermission("worldguard.*")) {
			return true;
		}
		if (p.hasPermission("worldguard.region.*")) {
			return true;
		}
		if (p.hasPermission("worldguard.region.bypass.*")) {
			return true;
		}
		if (p.hasPermission("worldguard.region.bypass." + p.getWorld().getName())) {
			return true;
		}
		return false;
	}

	private static boolean newWG = false;

	public static boolean isNewWG() {
		return newWG;
	}

	public static boolean isInWGRegion(Location l) {
		return utils.isInWGRegion(l);
	}

	public static boolean isInTheSameRegion(Location l1, Location l2) {
		return utils.isInTheSameRegion(l1, l2);
	}

	public static boolean canBuild(Player player, Location l) {
		return utils.canBuild(player, l);
	}

	public static boolean isFlagAllows(Player player, Block block, StateFlag flag) {
		return utils.isFlagAllows(player, block, flag);
	}

	public static boolean isFlagAllows(Player player, Entity entity, StateFlag flag) {
		return utils.isFlagAllows(player, entity, flag);
	}

}
