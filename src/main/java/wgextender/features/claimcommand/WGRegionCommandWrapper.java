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

package wgextender.features.claimcommand;

import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.commands.region.RegionCommands;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wgextender.Config;
import wgextender.utils.CommandUtils;
import wgextender.utils.WEUtils;
import wgextender.utils.WGRegionUtils;

import java.math.BigInteger;

public class WGRegionCommandWrapper extends Command {

	public static void inject(Config config) throws IllegalAccessException {
		WGRegionCommandWrapper wrapper = new WGRegionCommandWrapper(config, CommandUtils.getCommands().get("region"));
		CommandUtils.replaceComamnd(wrapper.originalcommand, wrapper);
	}

	public static void uninject() throws IllegalAccessException {
		WGRegionCommandWrapper wrapper = (WGRegionCommandWrapper) CommandUtils.getCommands().get("region");
		CommandUtils.replaceComamnd(wrapper, wrapper.originalcommand);
	}

	protected final Config config;
	protected final Command originalcommand;
	protected static final RegionCommands regionCommands = new RegionCommands(WorldGuard.getInstance());
	private final ClaimCalculator claimCalculator = new ClaimCalculator();

	protected WGRegionCommandWrapper(Config config, Command originalcommand) {
		super(originalcommand.getName(), originalcommand.getDescription(), originalcommand.getUsage(), originalcommand.getAliases());
		this.config = config;
		this.originalcommand = originalcommand;
	}

	@Override
	public boolean execute(@NotNull CommandSender sender, @NotNull String label, String[] args) {
		if ((sender instanceof Player player) && (args.length >= 2) && args[0].equalsIgnoreCase("claim")) {
			String regionname = args[1];
			if (config.claimExpandSelectionVertical) {
				boolean result = WEUtils.expandVert((Player) sender);
				if (result) {
					player.sendMessage(ChatColor.GRAY + "Регион автоматически расширен по вертикали.");
				}
			}
			if (!player.hasPermission("worldguard.region.unlimited")) {
				ClaimCalculator.ClaimInfo info = claimCalculator.getClaimInfo(config, player);
				BigInteger size = info.getSize();
				if (config.claimBlockLimitsEnabled) {
					BigInteger maxsize = info.getMaxsize();
					if (size.compareTo(maxsize) > 0) {
						player.sendMessage(ChatColor.RED + "Вы не можете создать такой большой регион.");
						player.sendMessage(ChatColor.RED + "Ваш лимит: " + maxsize + ", Ваш размер: " + size);
						return true;
					}
				}
				int minsize = config.claimMinSize;
				if (minsize > 0) {
					if (size.compareTo(BigInteger.valueOf(minsize)) < 0) {
						player.sendMessage(ChatColor.RED + "Вы не можете создать такой маленький регион.");
						player.sendMessage(ChatColor.RED + "Минимальный размер: " + minsize + ", Ваш размер: " + size);
						return true;
					}
				}
				int minwidth = config.claimMinWidth;
				if (minwidth > 0) {
					BigInteger width = info.getMinWidthSize();
					if (width.compareTo(BigInteger.valueOf(minwidth)) < 0) {
						player.sendMessage(ChatColor.RED + "Вы не можете создать такой узкий регион.");
						player.sendMessage(ChatColor.RED + "Миниммальная ширина: " + minwidth + ", Ваша ширина: " + width);
						return true;
					}
				}
			}
			boolean hasRegion = AutoFlags.hasRegion(player.getWorld(), regionname);
			try {
				CommandContext ccontext = new CommandContext(String.format("claim %s", regionname));
				regionCommands.claim(ccontext, WGRegionUtils.wrapPlayer(player));
				if (!hasRegion && config.claimAutoFlagsEnabled) {
					AutoFlags.setFlagsForRegion(WGRegionUtils.wrapPlayer(player), player.getWorld(), config, regionname);
				}
			} catch (CommandException ex) {
				sender.sendMessage(ChatColor.RED + ex.getMessage());
			}
			return true;
		} else {
			return originalcommand.execute(sender, label, args);
		}
	}

}
